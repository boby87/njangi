package com.njangi.gateway.config;

import com.njangi.gateway.controller.GatewayProxyController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GatewayProxyController.class)
@Import({SecurityConfig.class, RoutingConfig.NjangiServicesConfig.class})
@TestPropertySource(properties = {
    "njangi.jwt.secret=NjangiTestSecretKeyForGatewaySecurityHmacSha256MustBe32Chars!",
    "njangi.services.identite-url=lb://ms-identite",
    "njangi.services.gouvernance-url=lb://ms-gouvernance",
    "njangi.services.finances-url=lb://ms-finances",
    "njangi.services.engagement-url=lb://ms-engagement"
})
class GatewaySecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    @Qualifier("loadBalancedRestTemplate")
    private RestTemplate loadBalancedRestTemplate;

    @MockitoBean
    @Qualifier("directRestTemplate")
    private RestTemplate directRestTemplate;

    @Test
    @DisplayName("Autorise les routes publiques d'authentification sans JWT")
    void publicAuthRoute_isAccessibleWithoutJwt() throws Exception {
        when(loadBalancedRestTemplate.exchange(
            any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)
        )).thenReturn(ResponseEntity.ok("{\"success\":true}".getBytes()));

        mockMvc.perform(post("/api/v1/auth/login").contentType("application/json").content("{}"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Rejette avec 401 les routes métier protégées sans JWT")
    void protectedRoutes_withoutJwt_return401() throws Exception {
        mockMvc.perform(get("/api/v1/groupes")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/membres")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/cotisations")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/statistiques/groupe/123")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Autorise les routes protégées avec un jeton JWT valide")
    void protectedRoute_withValidJwt_isAllowed() throws Exception {
        when(loadBalancedRestTemplate.exchange(
            any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)
        )).thenReturn(ResponseEntity.ok("{\"success\":true}".getBytes()));

        mockMvc.perform(get("/api/v1/groupes").with(jwt().jwt(j -> j.subject("user-1"))))
            .andExpect(status().isOk());
    }
}
