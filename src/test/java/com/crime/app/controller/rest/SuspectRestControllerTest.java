package com.crime.app.controller.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.controller.rest.SuspectRestController;
import com.dto.response.SuspectResponse;
import com.entity.Suspect;
import com.service.SuspectService;

@WebMvcTest(SuspectRestController.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("SuspectRestController Tests")
class SuspectRestControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean SuspectService suspectService;

    private SuspectResponse sampleSuspect;

    @BeforeEach
    void setUp() {
        sampleSuspect = SuspectResponse.builder()
                .id(1L).suspectCode("SUSP-000001")
                .fullName("John Doe").alias("JD")
                .status(Suspect.SuspectStatus.WANTED)
                .dangerLevel(Suspect.DangerLevel.HIGH)
                .totalCases(3).arrested(false)
                .build();
    }

    @Test
    @DisplayName("GET /api/suspects/{id} should return suspect")
    @WithMockUser(roles = "OFFICER")
    void getById_shouldReturn200() throws Exception {
        when(suspectService.getById(1L)).thenReturn(sampleSuspect);

        mockMvc.perform(get("/api/suspects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.suspectCode").value("SUSP-000001"))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/suspects/wanted should return wanted suspects")
    @WithMockUser(roles = "OFFICER")
    void getWanted_shouldReturn200() throws Exception {
        when(suspectService.getWanted()).thenReturn(List.of(sampleSuspect));

        mockMvc.perform(get("/api/suspects/wanted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].status").value("WANTED"));
    }

    @Test
    @DisplayName("GET /api/suspects/most-wanted should return MOST_WANTED/VERY_HIGH suspects")
    @WithMockUser(roles = "ADMIN")
    void getMostWanted_shouldReturn200() throws Exception {
        SuspectResponse mostWanted = SuspectResponse.builder()
                .id(2L).suspectCode("SUSP-000002").fullName("Most Wanted")
                .status(Suspect.SuspectStatus.WANTED)
                .dangerLevel(Suspect.DangerLevel.MOST_WANTED).build();

        when(suspectService.getMostWanted()).thenReturn(List.of(mostWanted));

        mockMvc.perform(get("/api/suspects/most-wanted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].dangerLevel").value("MOST_WANTED"));
    }

    @Test
    @DisplayName("GET /api/suspects/match should return matched suspects")
    @WithMockUser(roles = "OFFICER")
    void matchBySuspectDetails_shouldReturn200() throws Exception {
        when(suspectService.searchBySuspectDetails(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(List.of(sampleSuspect));

        mockMvc.perform(get("/api/suspects/match")
                        .param("phone", "9876543210")
                        .param("email", "fraud@fake.com")
                        .param("upiId", "fraud@upi")
                        .param("bankAccount", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].fullName").value("John Doe"));
    }

    @Test
    @DisplayName("PATCH /api/suspects/{id}/arrest should record arrest for OFFICER")
    @WithMockUser(roles = "OFFICER")
    void arrest_shouldReturn200_forOfficer() throws Exception {
        when(suspectService.arrest(1L, "Mumbai Airport")).thenReturn(sampleSuspect);

        mockMvc.perform(patch("/api/suspects/1/arrest")
                        .param("details", "Mumbai Airport")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Suspect arrested"));
    }

    @Test
    @DisplayName("DELETE /api/suspects/{id} should require ADMIN role")
    @WithMockUser(roles = "OFFICER")
    void delete_shouldReturn403_forOfficer() throws Exception {
        mockMvc.perform(delete("/api/suspects/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/suspects should return paged suspects")
    @WithMockUser(roles = "OFFICER")
    void getAll_shouldReturnPage() throws Exception {
        Page<SuspectResponse> page = new PageImpl<>(List.of(sampleSuspect));
        when(suspectService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/suspects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/suspects/search should search suspects")
    @WithMockUser(roles = "ANALYST")
    void search_shouldReturnResults() throws Exception {
        Page<SuspectResponse> page = new PageImpl<>(List.of(sampleSuspect));
        when(suspectService.search(eq("fraud"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/suspects/search").param("q", "fraud"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}