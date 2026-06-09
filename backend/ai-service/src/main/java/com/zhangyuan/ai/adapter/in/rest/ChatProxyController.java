package com.zhangyuan.ai.adapter.in.rest;

import com.zhangyuan.ai.application.service.ChatProxyService;
import com.zhangyuan.ai.domain.model.ChatRequest;
import com.zhangyuan.ai.domain.model.ChatResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1")
public class ChatProxyController {
    private static final Logger log = LoggerFactory.getLogger(ChatProxyController.class);
    private final ChatProxyService chatProxyService;

    public ChatProxyController(ChatProxyService chatProxyService) {
        this.chatProxyService = chatProxyService;
    }

    @PostMapping("/chat/completions")
    public ResponseEntity<?> chatCompletions(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", Map.of("message", "Missing API key", "type", "auth_error")));
        }

        String apiKey = authHeader.substring(7);
        try {
            ChatResponse response = chatProxyService.chat(apiKey, request);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", Map.of("message", e.getMessage(), "type", "auth_error")));
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
