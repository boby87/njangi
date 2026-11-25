package com.njangi.identite.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Configuration publique SSO exposée aux clients Web et Mobile (sans aucun secret)")
public record SsoPublicConfigDto(
        @Schema(description = "Indique si Google Sign-In OIDC est configuré côté serveur")
        boolean googleEnabled,

        @Schema(description = "Google OAuth2 Client ID public (Web) utilisé par Google Identity Services")
        String googleClientId,

        @Schema(description = "Indique si le mode fallback de développement local est autorisé")
        boolean devFallbackEnabled
) {}
