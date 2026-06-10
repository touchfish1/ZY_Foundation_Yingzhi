package com.zhangyuan.ai.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangyuan.ai.client.OrderServiceClient;
import com.zhangyuan.ai.client.UserServiceClient;
import com.zhangyuan.ai.common.RateLimiter;
import com.zhangyuan.ai.domain.model.ChatRequest;
import com.zhangyuan.ai.domain.model.ChatResponse;
import com.zhangyuan.ai.domain.service.ModelProvider;
import com.zhangyuan.common.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChatProxyServiceTest {

    private final ProviderRegistry providerRegistry = mock(ProviderRegistry.class);
    private final UserServiceClient userServiceClient = mock(UserServiceClient.class);
    private final OrderServiceClient orderServiceClient = mock(OrderServiceClient.class);
    private final ModelAccessService modelAccessService = mock(ModelAccessService.class);
    private final RateLimiter rateLimiter = mock(RateLimiter.class);
    private final PricingService pricingService = mock(PricingService.class);

    private ChatProxyService chatProxyService;

    @BeforeEach
    void setUp() {
        chatProxyService = new ChatProxyService(
                providerRegistry, userServiceClient, orderServiceClient,
                modelAccessService, rateLimiter, pricingService);
    }

    @Test
    void chat_successful_returnsChatResponse() {
        // Arrange
        String apiKey = "test-api-key";
        ChatRequest request = createChatRequest("gpt-4o");

        Map<String, Object> quotaData = Map.of(
                "userId", 1L,
                "quotaUsed", 0L,
                "quotaLimit", 0L,
                "planCode", "pro");
        when(userServiceClient.verifyApiKey(eq(apiKey))).thenReturn(ApiResponse.ok(quotaData));

        doNothing().when(modelAccessService).checkAccess(eq("pro"), eq("gpt-4o"));
        when(rateLimiter.tryAcquire(anyLong(), anyInt(), anyInt(), anyInt())).thenReturn(true);

        ModelProvider modelProvider = mock(ModelProvider.class);
        when(modelProvider.getName()).thenReturn("openai");
        when(providerRegistry.resolveProvider(eq("gpt-4o"))).thenReturn(modelProvider);
        when(providerRegistry.getConfig(eq("openai"))).thenReturn(Map.of("api-key", "test-key"));

        ChatResponse.Usage usage = new ChatResponse.Usage(100, 50);
        ChatResponse.Choice choice = new ChatResponse.Choice();
        choice.setIndex(0);
        ChatResponse.Message msg = new ChatResponse.Message();
        msg.setRole("assistant");
        msg.setContent("Hello!");
        choice.setMessage(msg);
        choice.setFinishReason("stop");
        ChatResponse expectedResponse = new ChatResponse("chatcmpl-123", "gpt-4o", List.of(choice), usage);
        when(modelProvider.chat(any(), any())).thenReturn(expectedResponse);

        when(pricingService.calculateCost(eq("gpt-4o"), eq(100), eq(50)))
                .thenReturn(new BigDecimal("0.00500000"));

        when(orderServiceClient.recordUsage(any())).thenReturn(ApiResponse.ok());
        doNothing().when(rateLimiter).release(anyLong());

        // Act
        ChatResponse result = chatProxyService.chat(apiKey, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("chatcmpl-123");
        assertThat(result.getModel()).isEqualTo("gpt-4o");
        assertThat(result.getUsage().getPromptTokens()).isEqualTo(100);
        assertThat(result.getUsage().getCompletionTokens()).isEqualTo(50);
        assertThat(result.getChoices()).hasSize(1);

        // Verify interactions
        verify(userServiceClient).verifyApiKey(eq(apiKey));
        verify(modelAccessService).checkAccess(eq("pro"), eq("gpt-4o"));
        verify(rateLimiter).tryAcquire(eq(1L), eq(1), eq(60), eq(5));
        verify(providerRegistry).resolveProvider(eq("gpt-4o"));
        verify(modelProvider).chat(any(), any());
        verify(pricingService).calculateCost(eq("gpt-4o"), eq(100), eq(50));
        verify(orderServiceClient).recordUsage(any());
        verify(rateLimiter).release(eq(1L));
    }

    @Test
    void chat_invalidApiKey_throwsSecurityException() {
        // Arrange
        String apiKey = "invalid-key";
        ChatRequest request = createChatRequest("gpt-4o");

        when(userServiceClient.verifyApiKey(eq(apiKey)))
                .thenReturn(new ApiResponse<>(1, "Invalid API key", null));

        // Act & Assert
        assertThatThrownBy(() -> chatProxyService.chat(apiKey, request))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Invalid API key");

        verify(userServiceClient).verifyApiKey(eq(apiKey));
        // No further calls expected
        verifyNoInteractions(modelAccessService, rateLimiter, providerRegistry, pricingService, orderServiceClient);
    }

    @Test
    void chat_accessDenied_throwsAccessDeniedException() {
        // Arrange
        String apiKey = "test-api-key";
        ChatRequest request = createChatRequest("gpt-4o");

        Map<String, Object> quotaData = Map.of(
                "userId", 1L,
                "quotaUsed", 0L,
                "quotaLimit", 100L,
                "planCode", "free");
        when(userServiceClient.verifyApiKey(eq(apiKey))).thenReturn(ApiResponse.ok(quotaData));

        doThrow(new AccessDeniedException("free plan does not include gpt-4o"))
                .when(modelAccessService).checkAccess(eq("free"), eq("gpt-4o"));

        // Act & Assert
        assertThatThrownBy(() -> chatProxyService.chat(apiKey, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("free plan does not include gpt-4o");

        verify(userServiceClient).verifyApiKey(eq(apiKey));
        verify(modelAccessService).checkAccess(eq("free"), eq("gpt-4o"));
        // Rate limiter should NOT have been called (checkAccess throws before rate limit check)
        verifyNoInteractions(rateLimiter);
    }

    @Test
    void chat_quotaExhausted_throwsIllegalStateException() {
        // Arrange
        String apiKey = "test-api-key";
        ChatRequest request = createChatRequest("gpt-4o");

        Map<String, Object> quotaData = Map.of(
                "userId", 1L,
                "quotaUsed", 100L,
                "quotaLimit", 100L,
                "planCode", "pro");
        when(userServiceClient.verifyApiKey(eq(apiKey))).thenReturn(ApiResponse.ok(quotaData));
        doNothing().when(modelAccessService).checkAccess(anyString(), anyString());

        // Act & Assert
        assertThatThrownBy(() -> chatProxyService.chat(apiKey, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Quota exhausted");

        verify(userServiceClient).verifyApiKey(eq(apiKey));
        verify(modelAccessService).checkAccess(eq("pro"), eq("gpt-4o"));
        verifyNoInteractions(rateLimiter);
    }

    @Test
    void chat_rateLimitExceeded_throwsIllegalStateException() {
        // Arrange
        String apiKey = "test-api-key";
        ChatRequest request = createChatRequest("gpt-4o");

        Map<String, Object> quotaData = Map.of(
                "userId", 1L,
                "quotaUsed", 0L,
                "quotaLimit", 0L,
                "planCode", "pro");
        when(userServiceClient.verifyApiKey(eq(apiKey))).thenReturn(ApiResponse.ok(quotaData));
        doNothing().when(modelAccessService).checkAccess(anyString(), anyString());
        when(rateLimiter.tryAcquire(anyLong(), anyInt(), anyInt(), anyInt())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> chatProxyService.chat(apiKey, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Rate limit exceeded");

        verify(userServiceClient).verifyApiKey(eq(apiKey));
        verify(modelAccessService).checkAccess(eq("pro"), eq("gpt-4o"));
        verify(rateLimiter).tryAcquire(eq(1L), eq(1), eq(60), eq(5));
        verifyNoMoreInteractions(rateLimiter);
    }

    @Test
    void chatStream_successful_returnsFlux() {
        // Arrange
        String apiKey = "test-api-key";
        ChatRequest request = createChatRequest("gpt-4o");
        request.setStream(true);

        Map<String, Object> quotaData = Map.of(
                "userId", 1L,
                "quotaUsed", 0L,
                "quotaLimit", 0L,
                "planCode", "pro");
        when(userServiceClient.verifyApiKey(eq(apiKey))).thenReturn(ApiResponse.ok(quotaData));
        doNothing().when(modelAccessService).checkAccess(eq("pro"), eq("gpt-4o"));
        when(rateLimiter.tryAcquire(anyLong(), anyInt(), anyInt(), anyInt())).thenReturn(true);

        ModelProvider modelProvider = mock(ModelProvider.class);
        when(modelProvider.getName()).thenReturn("openai");
        when(modelProvider.estimateTokens(anyString(), anyString())).thenReturn(25);
        when(providerRegistry.resolveProvider(eq("gpt-4o"))).thenReturn(modelProvider);
        when(providerRegistry.getConfig(eq("openai"))).thenReturn(Map.of("api-key", "test-key"));
        when(modelProvider.chatStream(any(), any()))
                .thenReturn(reactor.core.publisher.Flux.just("chunk1", "chunk2"));

        // Act
        var flux = chatProxyService.chatStream(apiKey, request);

        // Assert
        assertThat(flux).isNotNull();
        verify(userServiceClient).verifyApiKey(eq(apiKey));
        verify(modelAccessService).checkAccess(eq("pro"), eq("gpt-4o"));
        verify(rateLimiter).tryAcquire(eq(1L), eq(1), eq(60), eq(5));
        verify(providerRegistry).resolveProvider(eq("gpt-4o"));
        verify(modelProvider).chatStream(any(), any());
    }

    private static ChatRequest createChatRequest(String model) {
        ChatRequest request = new ChatRequest();
        request.setModel(model);
        ChatRequest.Message message = new ChatRequest.Message();
        message.setRole("user");
        message.setContent("Hello");
        request.setMessages(List.of(message));
        return request;
    }
}
