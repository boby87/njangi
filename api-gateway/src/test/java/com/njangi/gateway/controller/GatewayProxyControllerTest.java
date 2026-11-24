package com.njangi.gateway.controller;

import com.njangi.gateway.config.RoutingConfig.NjangiServicesConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatewayProxyControllerTest {

    @Mock
    private RestTemplate loadBalancedRestTemplate;

    @Mock
    private RestTemplate directRestTemplate;

    private NjangiServicesConfig servicesConfig;
    private GatewayProxyController controller;

    @BeforeEach
    void setUp() {
        servicesConfig = new NjangiServicesConfig();
        servicesConfig.setIdentiteUrl("lb://ms-identite");
        servicesConfig.setGouvernanceUrl("lb://ms-gouvernance");
        servicesConfig.setFinancesUrl("lb://ms-finances");
        servicesConfig.setEngagementUrl("lb://ms-engagement");
        controller = new GatewayProxyController(loadBalancedRestTemplate, directRestTemplate, servicesConfig);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Mappe les 9 préfixes publics /api/v1/* vers les 4 microservices consolidés")
    void resolveTargetServiceUrl_mapsAllConsolidatedRoutes() {
        assertEquals("lb://ms-identite", controller.resolveTargetServiceUrl("auth"));
        assertEquals("lb://ms-identite", controller.resolveTargetServiceUrl("membres"));
        assertEquals("lb://ms-gouvernance", controller.resolveTargetServiceUrl("groupes"));
        assertEquals("lb://ms-gouvernance", controller.resolveTargetServiceUrl("reunions"));
        assertEquals("lb://ms-finances", controller.resolveTargetServiceUrl("cotisations"));
        assertEquals("lb://ms-finances", controller.resolveTargetServiceUrl("paiements"));
        assertEquals("lb://ms-finances", controller.resolveTargetServiceUrl("penalites"));
        assertEquals("lb://ms-engagement", controller.resolveTargetServiceUrl("notifications"));
        assertEquals("lb://ms-engagement", controller.resolveTargetServiceUrl("statistiques"));
        assertNull(controller.resolveTargetServiceUrl("inconnu"));
    }

    @Test
    @DisplayName("Retourne 404 pour un préfixe de service inconnu")
    void proxyRequest_unknownService_returns404() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/inconnu/test");
        ResponseEntity<byte[]> response = controller.proxyRequest("inconnu", null, HttpMethod.GET, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verifyNoInteractions(loadBalancedRestTemplate, directRestTemplate);
    }

    @Test
    @DisplayName("Utilise le RestTemplate LoadBalanced Eureka pour lb://, purge les headers spoofés et propage l'identité JWT")
    @SuppressWarnings("unchecked")
    void proxyRequest_loadBalanced_propagatesTrustedJwtHeaders() {
        Jwt jwt = new Jwt(
            "mock-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "HS256"),
            Map.of("sub", "user-uuid-123", "telephone", "+237699123456", "email", "jp@njangi.cm")
        );
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/groupes/abc");
        request.setQueryString("actif=true");
        request.addHeader("X-User-Id", "spoofed-attacker-id");
        request.addHeader("Authorization", "Bearer mock-token");

        when(loadBalancedRestTemplate.exchange(
            eq(URI.create("http://ms-gouvernance/api/v1/groupes/abc?actif=true")),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(byte[].class)
        )).thenReturn(ResponseEntity.ok("{\"success\":true}".getBytes()));

        ResponseEntity<byte[]> response = controller.proxyRequest("groupes", null, HttpMethod.GET, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        ArgumentCaptor<HttpEntity<byte[]>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(loadBalancedRestTemplate).exchange(
            eq(URI.create("http://ms-gouvernance/api/v1/groupes/abc?actif=true")),
            eq(HttpMethod.GET),
            captor.capture(),
            eq(byte[].class)
        );
        verifyNoInteractions(directRestTemplate);

        HttpEntity<byte[]> forwarded = captor.getValue();
        assertEquals("user-uuid-123", forwarded.getHeaders().getFirst("X-User-Id"));
        assertEquals("+237699123456", forwarded.getHeaders().getFirst("X-User-Phone"));
        assertEquals("jp@njangi.cm", forwarded.getHeaders().getFirst("X-User-Email"));
    }

    @Test
    @DisplayName("Utilise le RestTemplate direct lorsque l'URL configurée est http://")
    void proxyRequest_directHttpUrl_usesDirectRestTemplate() {
        servicesConfig.setFinancesUrl("http://localhost:8003");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/paiements/cash");

        when(directRestTemplate.exchange(
            eq(URI.create("http://localhost:8003/api/v1/paiements/cash")),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(byte[].class)
        )).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body("{\"success\":true}".getBytes()));

        ResponseEntity<byte[]> response = controller.proxyRequest("paiements", "{}".getBytes(), HttpMethod.POST, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verifyNoInteractions(loadBalancedRestTemplate);
    }
}
