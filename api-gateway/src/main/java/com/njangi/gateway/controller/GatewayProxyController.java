package com.njangi.gateway.controller;

import com.njangi.gateway.config.RoutingConfig.NjangiServicesConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

@RestController
public class GatewayProxyController {

    private static final String LB_PREFIX = "lb://";
    private static final Set<String> STRIPPED_INCOMING_HEADERS = Set.of(
        HttpHeaders.HOST.toLowerCase(),
        HttpHeaders.CONTENT_LENGTH.toLowerCase(),
        "x-user-id",
        "x-user-phone",
        "x-user-email"
    );

    private final RestTemplate loadBalancedRestTemplate;
    private final RestTemplate directRestTemplate;
    private final NjangiServicesConfig servicesConfig;

    public GatewayProxyController(
            @Qualifier("loadBalancedRestTemplate") RestTemplate loadBalancedRestTemplate,
            @Qualifier("directRestTemplate") RestTemplate directRestTemplate,
            NjangiServicesConfig servicesConfig
    ) {
        this.loadBalancedRestTemplate = loadBalancedRestTemplate;
        this.directRestTemplate = directRestTemplate;
        this.servicesConfig = servicesConfig;
    }

    @RequestMapping(value = "/api/v1/{service}/**", method = {
            RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
            RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS
    })
    public ResponseEntity<byte[]> proxyRequest(
            @PathVariable("service") String service,
            @RequestBody(required = false) byte[] body,
            HttpMethod method,
            HttpServletRequest request
    ) {
        String configuredBaseUrl = resolveTargetServiceUrl(service);
        if (configuredBaseUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"success\":false,\"message\":\"Service '" + service + "' inconnu dans la passerelle Njangi\",\"data\":null,\"errorCode\":\"SERVICE_NOT_FOUND\"}").getBytes());
        }

        boolean useLoadBalancer = configuredBaseUrl.startsWith(LB_PREFIX);
        String effectiveBaseUrl = useLoadBalancer
                ? "http://" + configuredBaseUrl.substring(LB_PREFIX.length())
                : configuredBaseUrl;

        String fullUri = request.getRequestURI();
        String prefix = "/api/v1/" + service;
        String subPath = fullUri.length() > prefix.length() ? fullUri.substring(prefix.length()) : "";
        if (!subPath.isEmpty() && !subPath.startsWith("/")) {
            subPath = "/" + subPath;
        }

        String queryString = request.getQueryString();
        String targetUrl = effectiveBaseUrl + "/api/v1/" + service + subPath + (queryString != null ? "?" + queryString : "");

        HttpHeaders headers = buildForwardedHeaders(request);
        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);
        RestTemplate client = useLoadBalancer ? loadBalancedRestTemplate : directRestTemplate;

        try {
            ResponseEntity<byte[]> response = client.exchange(URI.create(targetUrl), method, entity, byte[].class);
            return ResponseEntity.status(response.getStatusCode())
                    .headers(filterResponseHeaders(response.getHeaders()))
                    .body(response.getBody());
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .headers(filterResponseHeaders(ex.getResponseHeaders()))
                    .body(ex.getResponseBodyAsByteArray());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"success\":false,\"message\":\"Passerelle Gateway : échec de liaison avec " + service + " (" + ex.getMessage() + ")\",\"data\":null,\"errorCode\":\"BAD_GATEWAY\"}").getBytes());
        }
    }

    String resolveTargetServiceUrl(String service) {
        return switch (service.toLowerCase()) {
            case "auth", "membres" -> servicesConfig.getIdentiteUrl();
            case "groupes", "reunions" -> servicesConfig.getGouvernanceUrl();
            case "cotisations", "paiements", "penalites" -> servicesConfig.getFinancesUrl();
            case "notifications", "statistiques" -> servicesConfig.getEngagementUrl();
            default -> null;
        };
    }

    private HttpHeaders buildForwardedHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!STRIPPED_INCOMING_HEADERS.contains(headerName.toLowerCase())) {
                headers.addAll(headerName, Collections.list(request.getHeaders(headerName)));
            }
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            if (jwt.getSubject() != null) {
                headers.set("X-User-Id", jwt.getSubject());
            }
            String telephone = jwt.getClaimAsString("telephone");
            if (telephone != null && !telephone.isBlank()) {
                headers.set("X-User-Phone", telephone);
            }
            String email = jwt.getClaimAsString("email");
            if (email != null && !email.isBlank()) {
                headers.set("X-User-Email", email);
            }
        }
        return headers;
    }

    private HttpHeaders filterResponseHeaders(HttpHeaders source) {
        HttpHeaders filtered = new HttpHeaders();
        if (source != null) {
            source.forEach((k, v) -> {
                if (!k.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING) &&
                    !k.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH) &&
                    !k.equalsIgnoreCase(HttpHeaders.CONNECTION)) {
                    filtered.addAll(k, v);
                }
            });
        }
        return filtered;
    }
}
