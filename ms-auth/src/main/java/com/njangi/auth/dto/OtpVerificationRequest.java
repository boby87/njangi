package com.njangi.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpVerificationRequest(
    @NotBlank String telephone,
    @NotBlank String otpCode
) {}
