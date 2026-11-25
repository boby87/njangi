package com.njangi.identite.infrastructure.adapter.out.sso;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "njangi.sso")
public class SsoProperties {

    private boolean allowDevFallback = false;
    private GoogleProperties google = new GoogleProperties();

    public boolean isAllowDevFallback() {
        return allowDevFallback;
    }

    public void setAllowDevFallback(boolean allowDevFallback) {
        this.allowDevFallback = allowDevFallback;
    }

    public GoogleProperties getGoogle() {
        return google;
    }

    public void setGoogle(GoogleProperties google) {
        this.google = google;
    }

    public static class GoogleProperties {
        private String jwkSetUri = "https://www.googleapis.com/oauth2/v3/certs";
        private List<String> issuers = List.of("https://accounts.google.com", "accounts.google.com");
        private String clientId = "";
        private String webClientId = "";
        private String androidClientId = "";
        private List<String> allowedClientIds = new ArrayList<>();

        public String getJwkSetUri() {
            return jwkSetUri;
        }

        public void setJwkSetUri(String jwkSetUri) {
            this.jwkSetUri = jwkSetUri;
        }

        public List<String> getIssuers() {
            return issuers;
        }

        public void setIssuers(List<String> issuers) {
            this.issuers = issuers;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getWebClientId() {
            return webClientId;
        }

        public void setWebClientId(String webClientId) {
            this.webClientId = webClientId;
        }

        public String getAndroidClientId() {
            return androidClientId;
        }

        public void setAndroidClientId(String androidClientId) {
            this.androidClientId = androidClientId;
        }

        public List<String> getAllowedClientIds() {
            return allowedClientIds;
        }

        public void setAllowedClientIds(List<String> allowedClientIds) {
            this.allowedClientIds = allowedClientIds;
        }

        public List<String> resolveAllClientIds() {
            List<String> ids = new ArrayList<>();
            addIfPresent(ids, clientId);
            addIfPresent(ids, webClientId);
            addIfPresent(ids, androidClientId);
            if (allowedClientIds != null) {
                for (String id : allowedClientIds) {
                    addIfPresent(ids, id);
                }
            }
            return List.copyOf(ids);
        }

        public String resolvePrimaryWebClientId() {
            if (webClientId != null && !webClientId.isBlank()) {
                return webClientId.trim();
            }
            if (clientId != null && !clientId.isBlank()) {
                return clientId.trim();
            }
            List<String> all = resolveAllClientIds();
            return all.isEmpty() ? "" : all.getFirst();
        }

        private void addIfPresent(List<String> target, String value) {
            if (value != null && !value.isBlank()) {
                for (String part : value.split(",")) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty() && !target.contains(trimmed)) {
                        target.add(trimmed);
                    }
                }
            }
        }
    }
}
