package com.njangi.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpVerifyRequest(
    @NotBlank String identifiant,
    @NotBlank String code
) {}
