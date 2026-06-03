package com.crime.app.service;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
//import org.springframework.kafka.core.KafkaTemplate;

import com.dto.request.ComplaintRequest;
import com.dto.response.ComplaintResponse;
import com.dto.response.DashboardStats;
import com.entity.Complaint;
import com.entity.Officer;
import com.entity.Suspect;
import com.entity.User;
import com.exception.ResourceNotFoundException;
import com.repository.ComplaintRepository;
import com.repository.ComplaintStatusHistoryRepository;
import com.repository.EvidenceRepository;
import com.repository.OfficerRepository;
import com.repository.SuspectRepository;
import com.repository.UserRepository;
import com.service.FileStorageService;
import com.service.NotificationService;
import com.service.impl.ComplaintServiceImpl;
import com.util.TrackingNumberGenerator;

@ExtendWith(MockitoExtension.class)
@DisplayName("ComplaintService Tests")
class ComplaintServiceTest {

    @Mock ComplaintRepository     complaintRepo;
    @Mock UserRepository          userRepo;
    @Mock OfficerRepository       officerRepo;
    @Mock EvidenceRepository      evidenceRepo;
    @Mock SuspectRepository       suspectRepo;
    @Mock ComplaintStatusHistoryRepository historyRepo;
    @Mock FileStorageService      fileService;
    @Mock NotificationService     notifyService;
    @Mock TrackingNumberGenerator trackingGen;
 //   @Mock KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks ComplaintServiceImpl complaintService;

    private User     testUser;
    private Complaint testComplaint;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).fullName("Test User")
                .email("test@test.com").phone("9876543210")
                .role(User.Role.CITIZEN).status(User.UserStatus.ACTIVE).build();

        testComplaint = Complaint.builder()
                .id(1L).trackingNumber("CRM-2024-00000001")
                .title("Test Fraud Case").description("Test description for fraud case")
                .category(Complaint.CrimeCategory.FRAUD)
                .status(Complaint.ComplaintStatus.SUBMITTED)
                .priority(Complaint.Priority.MEDIUM)
                .incidentDateTime(LocalDateTime.now().minusDays(1))
                .incidentLocation("Mumbai").complainant(testUser)
                .evidences(new ArrayList<>()).statusHistories(new ArrayList<>())
                .linkedSuspects(new ArrayList<>())
                .createdAt(LocalDateTime.now()).build();
    }

    // ── GET BY ID ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return complaint when found by ID")
    void getById_shouldReturnComplaint_whenExists() {
        when(complaintRepo.findById(1L)).thenReturn(Optional.of(testComplaint));
        ComplaintResponse result = complaintService.getById(1L);
        assertNotNull(result);
        assertEquals("CRM-2024-00000001", result.getTrackingNumber());
        assertEquals("Test Fraud Case", result.getTitle());
        verify(complaintRepo, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when complaint not found")
    void getById_shouldThrowException_whenNotFound() {
        when(complaintRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> complaintService.getById(99L));
    }

    // ── GET BY TRACKING NUMBER ────────────────────────────────────────────────

    @Test
    @DisplayName("Should return complaint when found by tracking number")
    void getByTrackingNumber_shouldReturnComplaint() {
        when(complaintRepo.findByTrackingNumber("CRM-2024-00000001"))
                .thenReturn(Optional.of(testComplaint));
        ComplaintResponse result = complaintService.getByTrackingNumber("CRM-2024-00000001");
        assertNotNull(result);
        assertEquals(Complaint.CrimeCategory.FRAUD, result.getCategory());
    }

    @Test
    @DisplayName("Should throw exception for invalid tracking number")
    void getByTrackingNumber_shouldThrow_whenNotFound() {
        when(complaintRepo.findByTrackingNumber("INVALID")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> complaintService.getByTrackingNumber("INVALID"));
    }

    // ── SUBMIT ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should submit complaint and return response")
    void submit_shouldCreateComplaint_withCorrectPriority() {
        ComplaintRequest req = ComplaintRequest.builder()
                .title("Online Banking Fraud Test")
                .description("Fraudster stole money from my account via fake UPI link")
                .category(Complaint.CrimeCategory.FRAUD)
                .incidentDateTime(LocalDateTime.now().minusDays(1))
                .incidentLocation("Mumbai")
                .financialLoss(75000.0)
                .build();

        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(trackingGen.generate()).thenReturn("CRM-2024-00000002");
        when(complaintRepo.save(any(Complaint.class))).thenReturn(testComplaint);
        when(historyRepo.save(any())).thenReturn(null);
        when(officerRepo.findAvailable()).thenReturn(Collections.emptyList());
        when(suspectRepo.findBySuspectDetails(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        ComplaintResponse result = complaintService.submit(req, 1L);

        assertNotNull(result);
        verify(complaintRepo, times(1)).save(any(Complaint.class));
        verify(notifyService, times(1)).notifySubmitted(any(Complaint.class));
    }

    @Test
    @DisplayName("Should set CRITICAL priority when financial loss >= 100000")
    void submit_shouldSetCriticalPriority_whenHighFinancialLoss() {
        ComplaintRequest req = ComplaintRequest.builder()
                .title("Large Scale Fraud Test Title Here")
                .description("Very large financial fraud committed by unknown persons online")
                .category(Complaint.CrimeCategory.FRAUD)
                .incidentDateTime(LocalDateTime.now().minusDays(1))
                .incidentLocation("Delhi").financialLoss(150000.0).build();

        ArgumentCaptor<Complaint> captor = ArgumentCaptor.forClass(Complaint.class);
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(trackingGen.generate()).thenReturn("CRM-2024-00000003");
        when(complaintRepo.save(captor.capture())).thenReturn(testComplaint);
        when(historyRepo.save(any())).thenReturn(null);
        when(officerRepo.findAvailable()).thenReturn(Collections.emptyList());
        when(suspectRepo.findBySuspectDetails(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        complaintService.submit(req, 1L);

        assertEquals(Complaint.Priority.CRITICAL, captor.getValue().getPriority());
    }

    // ── UPDATE STATUS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should update status and set resolvedAt when RESOLVED")
    void updateStatus_shouldSetResolvedAt_whenStatusIsResolved() {
        when(complaintRepo.findById(1L)).thenReturn(Optional.of(testComplaint));
        when(complaintRepo.save(any())).thenReturn(testComplaint);
        when(historyRepo.save(any())).thenReturn(null);
        doNothing().when(notifyService).notifyStatusChange(any(), any(), any());

        complaintService.updateStatus(1L, Complaint.ComplaintStatus.RESOLVED, "Case solved", "admin");

        ArgumentCaptor<Complaint> captor = ArgumentCaptor.forClass(Complaint.class);
        verify(complaintRepo).save(captor.capture());
        assertEquals(Complaint.ComplaintStatus.RESOLVED, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getResolvedAt());
        assertEquals("Case solved", captor.getValue().getResolutionNotes());
    }

    // ── ASSIGN OFFICER ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Should assign officer and update caseload")
    void assignOfficer_shouldUpdateCaseLoad() {
        Officer officer = Officer.builder().id(1L).badgeNumber("OFF-001")
                .fullName("Inspector Ramesh").currentCaseLoad(5).maxCaseLoad(20)
                .status(Officer.OfficerStatus.ACTIVE).build();

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(testComplaint));
        when(officerRepo.findById(1L)).thenReturn(Optional.of(officer));
        when(complaintRepo.save(any())).thenReturn(testComplaint);
        when(officerRepo.save(any())).thenReturn(officer);
        when(historyRepo.save(any())).thenReturn(null);

        complaintService.assignOfficer(1L, 1L);

        ArgumentCaptor<Officer> officerCaptor = ArgumentCaptor.forClass(Officer.class);
        verify(officerRepo).save(officerCaptor.capture());
        assertEquals(6, officerCaptor.getValue().getCurrentCaseLoad());
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should delete complaint when it exists")
    void delete_shouldDeleteComplaint_whenExists() {
        when(complaintRepo.existsById(1L)).thenReturn(true);
        assertDoesNotThrow(() -> complaintService.delete(1L));
        verify(complaintRepo, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent complaint")
    void delete_shouldThrow_whenNotFound() {
        when(complaintRepo.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> complaintService.delete(99L));
    }

    // ── DASHBOARD STATS ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return dashboard stats correctly")
    void getDashboardStats_shouldReturnCorrectCounts() {
        when(complaintRepo.count()).thenReturn(50L);
        when(complaintRepo.countByStatus(Complaint.ComplaintStatus.SUBMITTED)).thenReturn(10L);
        when(complaintRepo.countByStatus(Complaint.ComplaintStatus.UNDER_REVIEW)).thenReturn(5L);
        when(complaintRepo.countByStatus(Complaint.ComplaintStatus.RESOLVED)).thenReturn(20L);
        when(complaintRepo.countByStatus(Complaint.ComplaintStatus.REJECTED)).thenReturn(3L);
        when(complaintRepo.countByStatus(Complaint.ComplaintStatus.UNDER_INVESTIGATION)).thenReturn(7L);
        when(complaintRepo.countToday()).thenReturn(5L);
        when(complaintRepo.countThisMonth()).thenReturn(30L);
        when(complaintRepo.sumFinancialLoss()).thenReturn(500000.0);
        when(complaintRepo.countGroupByCategory()).thenReturn(Collections.emptyList());
        when(complaintRepo.countGroupByStatus()).thenReturn(Collections.emptyList());
        when(complaintRepo.countByMonth()).thenReturn(Collections.emptyList());
        when(suspectRepo.count()).thenReturn(15L);
        when(suspectRepo.countByStatus(Suspect.SuspectStatus.WANTED)).thenReturn(8L);
        when(suspectRepo.countByStatus(Suspect.SuspectStatus.ARRESTED)).thenReturn(5L);
        when(suspectRepo.countByDangerLevel()).thenReturn(Collections.emptyList());
        when(officerRepo.count()).thenReturn(10L);
        when(officerRepo.countActive()).thenReturn(8L);

        DashboardStats stats = complaintService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(50L, stats.getTotalComplaints());
        assertEquals(15L, stats.getPendingComplaints());
        assertEquals(20L, stats.getResolvedComplaints());
        assertEquals(5L,  stats.getTodayComplaints());
        assertEquals(500000.0, stats.getTotalFinancialLoss());
    }

    // ── GET ALL (PAGINATION) ─────────────────────────────────────────────────

    @Test
    @DisplayName("Should return paginated complaints")
    void getAll_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Complaint> page = new PageImpl<>(List.of(testComplaint), pageable, 1);
        when(complaintRepo.findAll(pageable)).thenReturn(page);

        var result = complaintService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("CRM-2024-00000001", result.getContent().get(0).getTrackingNumber());
    }

    // ── CRITICAL CASES ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Should return critical open complaints sorted by createdAt")
    void getCritical_shouldReturnSortedCriticalComplaints() {
        Complaint c1 = Complaint.builder().id(1L).trackingNumber("CRM-001")
                .title("Critical Case 1").priority(Complaint.Priority.CRITICAL)
                .status(Complaint.ComplaintStatus.SUBMITTED).complainant(testUser)
                .createdAt(LocalDateTime.now().minusDays(3))
                .evidences(new ArrayList<>()).statusHistories(new ArrayList<>())
                .linkedSuspects(new ArrayList<>()).build();

        Complaint c2 = Complaint.builder().id(2L).trackingNumber("CRM-002")
                .title("Critical Case 2").priority(Complaint.Priority.CRITICAL)
                .status(Complaint.ComplaintStatus.SUBMITTED).complainant(testUser)
                .createdAt(LocalDateTime.now().minusDays(1))
                .evidences(new ArrayList<>()).statusHistories(new ArrayList<>())
                .linkedSuspects(new ArrayList<>()).build();

        when(complaintRepo.findCriticalOpen()).thenReturn(List.of(c2, c1));
        var result = complaintService.getCritical();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getCreatedAt().isBefore(result.get(1).getCreatedAt()));
    }
}