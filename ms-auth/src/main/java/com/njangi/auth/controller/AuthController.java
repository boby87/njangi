package com.njangi.auth.controller;

import com.njangi.auth.dto.*;
import com.njangi.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/otp/demander")
    public ResponseEntity<Void> demanderOtp(@RequestParam String identifiant) {
        authService.demanderOtp(identifiant);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/otp/verifier")
    public ResponseEntity<Map<String, String>> verifierOtp(@Valid @RequestBody OtpVerifyRequest request) {
        String token = authService.verifierOtp(request);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/moi")
    public ResponseEntity<UtilisateurDto> moi(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(authService.moi(userId));
    }
}
