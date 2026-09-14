package com.njangi.gateway.controller;

import com.njangi.gateway.config.RoutingConfig.NjangiServicesConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;

@RestController
public class GatewayProxyController {

    private final RestTemplate restTemplate;
    private final NjangiServicesConfig servicesConfig;

    public GatewayProxyController(RestTemplate restTemplate, NjangiServicesConfig servicesConfig) {
        this.restTemplate = restTemplate;
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
        String targetBaseUrl = resolveTargetServiceUrl(service);
        if (targetBaseUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(("{\"success\":false,\"message\":\"Service '" + service + "' inconnu dans la passerelle Njangi\"}").getBytes());
        }

        // Construire l'URL cible en préservant le sous-chemin et la query string
        String fullUri = request.getRequestURI();
        String prefix = "/api/v1/" + service;
        String subPath = fullUri.length() > prefix.length() ? fullUri.substring(prefix.length()) : "";
        if (!subPath.startsWith("/")) {
            subPath = "/" + subPath;
        }

        String queryString = request.getQueryString();
        String targetUrl = targetBaseUrl + "/api/v1/" + service + subPath + (queryString != null ? "?" + queryString : "");

        // Copier les en-têtes de la requête
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!headerName.equalsIgnoreCase(HttpHeaders.HOST) && !headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                headers.addAll(headerName, Collections.list(request.getHeaders(headerName)));
            }
        }

        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(URI.create(targetUrl), method, entity, byte[].class);
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .headers(ex.getResponseHeaders())
                    .body(ex.getResponseBodyAsByteArray());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(("{\"success\":false,\"message\":\"Passerelle Gateway : échec de liaison avec " + service + " (" + ex.getMessage() + ")\"}").getBytes());
        }
    }

    private String resolveTargetServiceUrl(String service) {
        return switch (service.toLowerCase()) {
            case "auth" -> servicesConfig.getAuthUrl();
            case "membres" -> servicesConfig.getMembresUrl();
            case "groupes" -> servicesConfig.getGroupesUrl();
            case "reunions" -> servicesConfig.getReunionsUrl();
            case "cotisations" -> servicesConfig.getCotisationsUrl();
            case "paiements" -> servicesConfig.getPaiementsUrl();
            case "penalites" -> servicesConfig.getPenalitesUrl();
            case "notifications" -> servicesConfig.getNotificationsUrl();
            case "statistiques" -> servicesConfig.getStatistiquesUrl();
            default -> null;
        };
    }
}
