package com.njangi.statistiques.controller;

import com.njangi.statistiques.StatistiquesApplication;
import com.njangi.statistiques.config.SecurityConfig;
import com.njangi.statistiques.dto.BilanFinancierDto;
import com.njangi.statistiques.dto.StatistiqueGroupeDto;
import com.njangi.statistiques.service.StatistiqueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatistiqueController.class)
@ContextConfiguration(classes = StatistiquesApplication.class)
@Import(SecurityConfig.class)
class StatistiqueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatistiqueService statistiqueService;

    @Test
    @DisplayName("GET /api/v1/statistiques/groupe/{groupeId} -> 200 OK")
    void testFindByGroupe() throws Exception {
        UUID groupeId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        StatistiqueGroupeDto dto = new StatistiqueGroupeDto(
                UUID.randomUUID(), groupeId, sessionId,
                BigDecimal.valueOf(100000), BigDecimal.valueOf(80000), BigDecimal.valueOf(20000),
                BigDecimal.ZERO, BigDecimal.valueOf(50000), BigDecimal.valueOf(50000),
                10, 2, BigDecimal.valueOf(100), BigDecimal.valueOf(90), LocalDateTime.now()
        );

        when(statistiqueService.findByGroupe(groupeId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/statistiques/groupe/" + groupeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].groupeId").value(groupeId.toString()))
                .andExpect(jsonPath("$.data[0].totalCollecte").value(100000));
    }

    @Test
    @DisplayName("GET /api/v1/statistiques/groupe/{groupeId}/session/{sessionId} -> 200 OK")
    void testFindByGroupeAndSession() throws Exception {
        UUID groupeId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        StatistiqueGroupeDto dto = new StatistiqueGroupeDto(
                UUID.randomUUID(), groupeId, sessionId,
                BigDecimal.valueOf(150000), BigDecimal.valueOf(100000), BigDecimal.valueOf(50000),
                BigDecimal.ZERO, BigDecimal.valueOf(150000), BigDecimal.ZERO,
                8, 1, BigDecimal.valueOf(100), BigDecimal.valueOf(100), LocalDateTime.now()
        );

        when(statistiqueService.findByGroupeAndSession(groupeId, sessionId)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/statistiques/groupe/" + groupeId + "/session/" + sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(sessionId.toString()));
    }

    @Test
    @DisplayName("POST /api/v1/statistiques/groupe/{groupeId}/session/{sessionId}/calculer -> 200 OK")
    void testCalculer() throws Exception {
        UUID groupeId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        StatistiqueGroupeDto dto = new StatistiqueGroupeDto(
                UUID.randomUUID(), groupeId, sessionId,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                0, 0, BigDecimal.valueOf(100), BigDecimal.valueOf(100), LocalDateTime.now()
        );

        when(statistiqueService.calculer(groupeId, sessionId)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/statistiques/groupe/" + groupeId + "/session/" + sessionId + "/calculer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Statistiques recalculées avec succès"));
    }

    @Test
    @DisplayName("GET /api/v1/statistiques/groupe/{groupeId}/session/{sessionId}/bilan -> 200 OK")
    void testGenererBilan() throws Exception {
        UUID groupeId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        BilanFinancierDto bilan = new BilanFinancierDto(
                groupeId,
                sessionId,
                BigDecimal.valueOf(75000),
                BigDecimal.valueOf(250000),
                BigDecimal.valueOf(180000),
                BigDecimal.valueOf(5000),
                Map.of("CASH", BigDecimal.valueOf(125000), "MOBILE_MONEY", BigDecimal.valueOf(125000)),
                BigDecimal.valueOf(98.5),
                BigDecimal.valueOf(92.0),
                "EXCELLENTE",
                15,
                4,
                LocalDateTime.now()
        );

        when(statistiqueService.genererBilanFinancier(groupeId, sessionId)).thenReturn(bilan);

        mockMvc.perform(get("/api/v1/statistiques/groupe/" + groupeId + "/session/" + sessionId + "/bilan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.soldeGlobalXAF").value(75000))
                .andExpect(jsonPath("$.data.statutSanteFinanciere").value("EXCELLENTE"));
    }
}
