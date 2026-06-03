package com.crime.app.controller.rest;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

import com.controller.rest.ComplaintRestController;
import com.dto.response.ComplaintResponse;
import com.dto.response.DashboardStats;
import com.entity.Complaint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.ComplaintService;
import com.service.UserService;

@WebMvcTest(ComplaintRestController.class)
@ExtendWith(MockitoExtension.class)
@DisplayName("ComplaintRestController Tests")
class ComplaintRestControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ComplaintService complaintService;
    @MockBean UserService userService;

    private ComplaintResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = ComplaintResponse.builder()
                .id(1L).trackingNumber("CRM-2024-00000001")
                .title("Test Fraud").description("Test fraud description")
                .category(Complaint.CrimeCategory.FRAUD)
                .status(Complaint.ComplaintStatus.SUBMITTED)
                .priority(Complaint.Priority.HIGH)
                .complainantName("Test User")
                .complainantEmail("test@test.com")
                .createdAt(LocalDateTime.now())
                .evidences(new ArrayList<>())
                .statusHistory(new ArrayList<>())
                .daysPending(1L).totalEvidenceFiles(0)
                .build();
    }

    @Test
    @DisplayName("GET /api/complaints/{id} should return complaint")
    @WithMockUser(username = "test@test.com", roles = "CITIZEN")
    void getById_shouldReturn200() throws Exception {
        when(complaintService.getById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/complaints/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trackingNumber").value("CRM-2024-00000001"))
                .andExpect(jsonPath("$.data.title").value("Test Fraud"));
    }

    @Test
    @DisplayName("GET /api/complaints/track/{num} should return complaint publicly")
    @WithMockUser
    void trackByNumber_shouldReturn200() throws Exception {
        when(complaintService.getByTrackingNumber("CRM-2024-00000001")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/complaints/track/CRM-2024-00000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trackingNumber").value("CRM-2024-00000001"));
    }

    @Test
    @DisplayName("GET /api/complaints should require OFFICER/ADMIN/ANALYST role")
    @WithMockUser(roles = "CITIZEN")
    void getAll_shouldReturn403_forCitizen() throws Exception {
        mockMvc.perform(get("/api/complaints"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/complaints should return page for OFFICER")
    @WithMockUser(roles = "OFFICER")
    void getAll_shouldReturn200_forOfficer() throws Exception {
        Page<ComplaintResponse> page = new PageImpl<>(List.of(sampleResponse));
        when(complaintService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/complaints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/complaints/{id}/status should update status for OFFICER")
    @WithMockUser(username = "officer@test.com", roles = "OFFICER")
    void updateStatus_shouldReturn200_forOfficer() throws Exception {
        when(complaintService.updateStatus(eq(1L), eq(Complaint.ComplaintStatus.RESOLVED),
                anyString(), anyString())).thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/complaints/1/status")
                        .param("status", "RESOLVED")
                        .param("remarks", "Case resolved")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/complaints/{id} should require ADMIN role")
    @WithMockUser(roles = "OFFICER")
    void delete_shouldReturn403_forOfficer() throws Exception {
        mockMvc.perform(delete("/api/complaints/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/complaints/{id} should return 200 for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void delete_shouldReturn200_forAdmin() throws Exception {
        doNothing().when(complaintService).delete(1L);

        mockMvc.perform(delete("/api/complaints/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/complaints/dashboard/stats should return stats")
    @WithMockUser(roles = "ADMIN")
    void getDashboardStats_shouldReturn200() throws Exception {
        DashboardStats stats = DashboardStats.builder()
                .totalComplaints(100L).pendingComplaints(30L)
                .resolvedComplaints(50L).wantedSuspects(10L).build();
        when(complaintService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/complaints/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalComplaints").value(100));
    }

    @Test
    @DisplayName("GET /api/complaints/critical should return critical complaints")
    @WithMockUser(roles = "OFFICER")
    void getCritical_shouldReturn200() throws Exception {
        when(complaintService.getCritical()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/complaints/critical"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/complaints/unassigned should return unassigned complaints")
    @WithMockUser(roles = "ADMIN")
    void getUnassigned_shouldReturn200() throws Exception {
        when(complaintService.getUnassigned()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/complaints/unassigned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].trackingNumber").value("CRM-2024-00000001"));
    }
}