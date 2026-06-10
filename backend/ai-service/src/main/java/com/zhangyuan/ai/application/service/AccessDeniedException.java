package com.zhangyuan.ai.application.service;

/**
 * Thrown when a user's plan does not allow access to the requested model.
 * Maps to HTTP 403 Forbidden in the controller.
 */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
