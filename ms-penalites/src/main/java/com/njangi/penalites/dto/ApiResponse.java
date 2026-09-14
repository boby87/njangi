package com.njangi.penalites.dto;

import java.time.Instant;

public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    String errorCode,
    Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Opération effectuée avec succès");
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, errorCode, Instant.now());
    }
}
