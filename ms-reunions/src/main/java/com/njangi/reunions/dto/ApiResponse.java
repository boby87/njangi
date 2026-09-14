package com.njangi.reunions.dto;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        String errorCode
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Opération réussie", data, null);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, errorCode);
    }
}
