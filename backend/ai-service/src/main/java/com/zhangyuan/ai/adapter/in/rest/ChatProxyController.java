package com.zhangyuan.ai.adapter.in.rest;

import com.zhangyuan.ai.application.service.AccessDeniedException;
import com.zhangyuan.ai.application.service.ChatProxyService;
import com.zhangyuan.ai.domain.model.ChatRequest;
import com.zhangyuan.ai.domain.model.ChatResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class ChatProxyController {
    private static final Logger log = LoggerFactory.getLogger(ChatProxyController.class);
    private static final long SSE_TIMEOUT = 10 * 60 * 1000L;

    private final ChatProxyService chatProxyService;

    public ChatProxyController(ChatProxyService chatProxyService) {
        this.chatProxyService = chatProxyService;
    }

    @PostMapping("/chat/completions")
    public Object chatCompletions(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", Map.of("message", "Missing API key", "type", "auth_error")));
        }

        String apiKey = authHeader.substring(7);

        if (request.isStream()) {
            return handleStreamChat(apiKey, request);
        }

        try {
            ChatResponse response = chatProxyService.chat(apiKey, request);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", Map.of("message", e.getMessage(), "type", "auth_error")));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", Map.of("message", e.getMessage(), "type", "access_denied")));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", Map.of("message", e.getMessage(), "type", "quota_exceeded")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", Map.of("message", e.getMessage(), "type", "invalid_request")));
        } catch (Exception e) {
            log.error("Chat completion error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", Map.of("message", "Internal server error", "type", "server_error")));
        }
    }

    private SseEmitter handleStreamChat(String apiKey, ChatRequest request) {
        Flux<String> stream;
        try {
            stream = chatProxyService.chatStream(apiKey, request);
        } catch (SecurityException | AccessDeniedException | IllegalStateException | IllegalArgumentException e) {
            return createErrorSseEmitter(e);
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        stream.subscribeOn(Schedulers.boundedElastic()).subscribe(
                data -> {
                    try {
                        emitter.send(SseEmitter.event().data(data));
                    } catch (IOException ex) {
                        emitter.complete();
                    }
                },
                error -> {
                    try {
                        emitter.send(SseEmitter.event().name("error")
                                .data(Map.of("message", error.getMessage(), "type", "stream_error")));
                    } catch (IOException ignored) {}
                    emitter.completeWithError(error);
                },
                () -> {
                    try {
                        emitter.send(SseEmitter.event().data("[DONE]"));
                    } catch (IOException ignored) {}
                    emitter.complete();
                }
        );

        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> emitter.complete());

        return emitter;
    }

    private SseEmitter createErrorSseEmitter(Exception e) {
        SseEmitter errorEmitter = new SseEmitter(0L);
        try {
            errorEmitter.send(SseEmitter.event().name("error")
                    .data(Map.of("message", e.getMessage(), "type", e.getClass().getSimpleName())));
        } catch (IOException ignored) {}
        errorEmitter.complete();
        return errorEmitter;
    }

    @GetMapping("/models")
    public ResponseEntity<?> listModels() {
        return ResponseEntity.ok(Map.of("data", java.util.List.of(
                Map.of("id", "gpt-4o", "object", "model"),
                Map.of("id", "gpt-4-turbo", "object", "model"),
                Map.of("id", "gpt-3.5-turbo", "object", "model"),
                Map.of("id", "claude-3-opus", "object", "model"),
                Map.of("id", "claude-3-sonnet", "object", "model"),
                Map.of("id", "claude-3-haiku", "object", "model")
        )));
    }
}
