package com.service.impl;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
//import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dto.request.ComplaintRequest;
import com.dto.response.ComplaintResponse;
import com.dto.response.DashboardStats;
import com.dto.response.EvidenceResponse;
import com.dto.response.StatusHistoryResponse;
import com.entity.Complaint;
import com.entity.ComplaintStatusHistory;
import com.entity.Evidence;
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
import com.service.ComplaintService;
import com.service.FileStorageService;
import com.service.NotificationService;
import com.util.TrackingNumberGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
 
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ComplaintServiceImpl implements ComplaintService {
 
    private final ComplaintRepository complaintRepo;
    private final UserRepository userRepo;
    private final OfficerRepository officerRepo;
    private final EvidenceRepository evidenceRepo;
    private final SuspectRepository suspectRepo;
    private final ComplaintStatusHistoryRepository historyRepo;
    private final FileStorageService fileService;
    private final NotificationService notifyService;
    private final TrackingNumberGenerator trackingGen;
  //  private final KafkaTemplate<String, Object> kafkaTemplate;
 
    @Override
    @CacheEvict(value = {"complaints", "dashboard"}, allEntries = true)
    public ComplaintResponse submit(ComplaintRequest req, Long userId) {
        log.info("Submitting complaint for userId={}", userId);
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
 
        Complaint complaint = Complaint.builder()
                .trackingNumber(trackingGen.generate())
                .title(req.getTitle())
                .description(req.getDescription())
                .category(req.getCategory())
                .status(Complaint.ComplaintStatus.SUBMITTED)
                .priority(calcPriority(req.getFinancialLoss()))
                .incidentDateTime(req.getIncidentDateTime())
                .incidentLocation(req.getIncidentLocation())
                .suspectPhone(req.getSuspectPhone())
                .suspectEmail(req.getSuspectEmail())
                .suspectBankAccount(req.getSuspectBankAccount())
                .suspectUpiId(req.getSuspectUpiId())
                .suspectIpAddress(req.getSuspectIpAddress())
                .suspectWebsite(req.getSuspectWebsite())
                .financialLoss(req.getFinancialLoss())
                .bankName(req.getBankName())
                .transactionId(req.getTransactionId())
                .complainant(user)
                .build();
 
        Complaint saved = complaintRepo.save(complaint);
 
        // Save evidence files using Stream API
        if (req.getEvidenceFiles() != null) {
            List<Evidence> evidences = req.getEvidenceFiles().stream()
                    .filter(f -> f != null && !f.isEmpty())
                    .map(f -> fileService.store(f, saved, user.getEmail()))
                    .collect(Collectors.toList());
            evidenceRepo.saveAll(evidences);
        }
 
        saveHistory(saved, null, Complaint.ComplaintStatus.SUBMITTED, "Complaint submitted", user.getEmail());
        autoAssign(saved);
        autoMatchSuspects(saved);
        publishEvent("complaint-events", "SUBMITTED", saved);
        notifyService.notifySubmitted(saved);
 
        log.info("Complaint submitted: {}", saved.getTrackingNumber());
        return toResponse(saved);
    }
 
    @Override
    @Cacheable(value = "complaints", key = "#id")
    @Transactional(readOnly = true)
    public ComplaintResponse getById(Long id) {
        return toResponse(find(id));
    }
 
    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getByTrackingNumber(String num) {
        return toResponse(complaintRepo.findByTrackingNumber(num)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + num)));
    }
 
    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getAll(Pageable pageable) {
        return complaintRepo.findAll(pageable).map(this::toResponse);
    }
 
    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getByUser(Long userId, Pageable pageable) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return complaintRepo.findByComplainant(user, pageable).map(this::toResponse);
    }
 
    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> search(String query, Pageable pageable) {
        return complaintRepo.searchComplaints(query, pageable).map(this::toResponse);
    }
 
    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> filter(Complaint.ComplaintStatus status,
                                           Complaint.CrimeCategory category,
                                           Complaint.Priority priority,
                                           LocalDateTime from,
                                           LocalDateTime to,
                                           Pageable pageable) {
        return complaintRepo.findWithFilters(status, category, priority, from, to, pageable)
                .map(this::toResponse);
    }
 
    @Override
    @CacheEvict(value = {"complaints", "dashboard"}, allEntries = true)
    public ComplaintResponse updateStatus(Long id, Complaint.ComplaintStatus newStatus,
                                           String remarks, String changedBy) {
        Complaint c = find(id);
        Complaint.ComplaintStatus old = c.getStatus();
        c.setStatus(newStatus);
 
        if (newStatus == Complaint.ComplaintStatus.RESOLVED) {
            c.setResolvedAt(LocalDateTime.now());
            c.setResolutionNotes(remarks);
        }
        if (newStatus == Complaint.ComplaintStatus.REJECTED) {
            c.setRejectionReason(remarks);
        }
 
        Complaint saved = complaintRepo.save(c);
        saveHistory(saved, old, newStatus, remarks, changedBy);
        publishEvent("status-events", "STATUS_CHANGED", saved);
        notifyService.notifyStatusChange(saved, old, newStatus);
        return toResponse(saved);
    }
 
    @Override
    @CacheEvict(value = "complaints", allEntries = true)
    public ComplaintResponse assignOfficer(Long complaintId, Long officerId) {
        Complaint c = find(complaintId);
        Officer officer = officerRepo.findById(officerId)
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found"));
 
        if (c.getAssignedOfficer() != null) {
            Officer prev = c.getAssignedOfficer();
            prev.setCurrentCaseLoad(Math.max(0, prev.getCurrentCaseLoad() - 1));
            officerRepo.save(prev);
        }
 
        c.setAssignedOfficer(officer);
        c.setStatus(Complaint.ComplaintStatus.ASSIGNED);
        officer.setCurrentCaseLoad(officer.getCurrentCaseLoad() + 1);
        officerRepo.save(officer);
 
        Complaint saved = complaintRepo.save(c);
        saveHistory(saved, Complaint.ComplaintStatus.SUBMITTED,
                Complaint.ComplaintStatus.ASSIGNED,
                "Assigned to " + officer.getFullName(), "SYSTEM");
        return toResponse(saved);
    }
 
    @Override
    @CacheEvict(value = {"complaints", "dashboard"}, allEntries = true)
    public ComplaintResponse update(Long id, ComplaintRequest req) {
        Complaint c = find(id);
        c.setTitle(req.getTitle());
        c.setDescription(req.getDescription());
        c.setCategory(req.getCategory());
        c.setIncidentDateTime(req.getIncidentDateTime());
        c.setIncidentLocation(req.getIncidentLocation());
        c.setSuspectPhone(req.getSuspectPhone());
        c.setSuspectEmail(req.getSuspectEmail());
        c.setSuspectBankAccount(req.getSuspectBankAccount());
        c.setSuspectUpiId(req.getSuspectUpiId());
        c.setFinancialLoss(req.getFinancialLoss());
        c.setBankName(req.getBankName());
        return toResponse(complaintRepo.save(c));
    }
 
    @Override
    @CacheEvict(value = {"complaints", "dashboard"}, allEntries = true)
    public void delete(Long id) {
        if (!complaintRepo.existsById(id))
            throw new ResourceNotFoundException("Complaint not found");
        complaintRepo.deleteById(id);
    }
 
    @Override
    @Cacheable(value = "dashboard")
    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        // Stream API for maps
        Map<String, Long> byCategory = complaintRepo.countGroupByCategory().stream()
                .collect(Collectors.toMap(
                        r -> ((Complaint.CrimeCategory) r[0]).name(),
                        r -> (Long) r[1]));
 
        Map<String, Long> byStatus = complaintRepo.countGroupByStatus().stream()
                .collect(Collectors.toMap(
                        r -> ((Complaint.ComplaintStatus) r[0]).name(),
                        r -> (Long) r[1]));
 
        Map<String, Long> byMonth = complaintRepo.countByMonth().stream()
                .collect(Collectors.toMap(
                        r -> monthName((Integer) r[0]),
                        r -> (Long) r[1]));
 
        Map<String, Long> byDanger = suspectRepo.countByDangerLevel().stream()
                .collect(Collectors.toMap(
                        r -> ((Suspect.DangerLevel) r[0]).name(),
                        r -> (Long) r[1]));
 
        return DashboardStats.builder()
                .totalComplaints(complaintRepo.count())
                .pendingComplaints(
                        byStatus.getOrDefault("SUBMITTED", 0L) +
                        byStatus.getOrDefault("UNDER_REVIEW", 0L))
                .resolvedComplaints(byStatus.getOrDefault("RESOLVED", 0L))
                .rejectedComplaints(byStatus.getOrDefault("REJECTED", 0L))
                .underInvestigation(byStatus.getOrDefault("UNDER_INVESTIGATION", 0L))
                .totalSuspects(suspectRepo.count())
                .wantedSuspects(suspectRepo.countByStatus(Suspect.SuspectStatus.WANTED))
                .arrestedSuspects(suspectRepo.countByStatus(Suspect.SuspectStatus.ARRESTED))
                .totalOfficers(officerRepo.count())
                .activeOfficers(officerRepo.countActive())
                .todayComplaints(complaintRepo.countToday())
                .thisMonthComplaints(complaintRepo.countThisMonth())
                .totalFinancialLoss(complaintRepo.sumFinancialLoss())
                .complaintsByCategory(byCategory)
                .complaintsByStatus(byStatus)
                .complaintsByMonth(byMonth)
                .suspectsByDangerLevel(byDanger)
                .build();
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getUnassigned() {
        return complaintRepo.findUnassigned().stream()
                .sorted(Comparator.comparing(Complaint::getPriority).reversed())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getCritical() {
        return complaintRepo.findCriticalOpen().stream()
                .sorted(Comparator.comparing(Complaint::getCreatedAt))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
 
    @Override
    @CacheEvict(value = "complaints", key = "#complaintId")
    public ComplaintResponse uploadEvidence(Long complaintId, List<MultipartFile> files,
                                             String desc, String uploadedBy) {
        Complaint c = find(complaintId);
        List<Evidence> evidences = files.stream()
                .filter(f -> f != null && !f.isEmpty())
                .map(f -> fileService.store(f, c, uploadedBy))
                .peek(e -> e.setDescription(desc))
                .collect(Collectors.toList());
        evidenceRepo.saveAll(evidences);
        return toResponse(complaintRepo.findById(complaintId).orElseThrow());
    }
 
    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────
 
    private void autoAssign(Complaint c) {
        List<Officer> available = officerRepo.findAvailable();
        if (!available.isEmpty()) {
            Officer o = available.get(0);
            c.setAssignedOfficer(o);
            c.setStatus(Complaint.ComplaintStatus.ASSIGNED);
            o.setCurrentCaseLoad(o.getCurrentCaseLoad() + 1);
            officerRepo.save(o);
            complaintRepo.save(c);
        }
    }
 
    private void autoMatchSuspects(Complaint c) {
        List<Suspect> matched = suspectRepo.findBySuspectDetails(
                c.getSuspectPhone(), c.getSuspectEmail(),
                c.getSuspectUpiId(), c.getSuspectBankAccount());
        if (!matched.isEmpty()) {
            c.setLinkedSuspects(matched);
            matched.forEach(s -> s.setTotalCases(s.getTotalCases() + 1));
            complaintRepo.save(c);
        }
    }
 
    private void saveHistory(Complaint c, Complaint.ComplaintStatus from,
                             Complaint.ComplaintStatus to, String remarks, String by) {
        historyRepo.save(ComplaintStatusHistory.builder()
                .complaint(c).fromStatus(from).toStatus(to)
                .remarks(remarks).changedBy(by).build());
    }
 
    private void publishEvent(String topic, String eventType, Complaint c) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("complaintId", c.getId());
            event.put("trackingNumber", c.getTrackingNumber());
            event.put("status", c.getStatus().name());
            event.put("priority", c.getPriority().name());
            event.put("timestamp", LocalDateTime.now().toString());
          //  kafkaTemplate.send(topic, c.getTrackingNumber(), event);
        } catch (Exception e) {
            log.warn("Kafka publish failed: {}", e.getMessage());
        }
    }
 
    private Complaint.Priority calcPriority(Double loss) {
        if (loss == null)         return Complaint.Priority.MEDIUM;
        if (loss >= 100000)       return Complaint.Priority.CRITICAL;
        if (loss >= 50000)        return Complaint.Priority.HIGH;
        if (loss >= 10000)        return Complaint.Priority.MEDIUM;
        return Complaint.Priority.LOW;
    }
 
    private Complaint find(Long id) {
        return complaintRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + id));
    }
 
    private ComplaintResponse toResponse(Complaint c) {
        List<EvidenceResponse> evidences = c.getEvidences().stream()
                .map(e -> EvidenceResponse.builder()
                        .id(e.getId())
                        .originalFileName(e.getOriginalFileName())
                        .fileType(e.getFileType())
                        .fileSize(e.getFileSize())
                        .evidenceType(e.getEvidenceType())
                        .description(e.getDescription())
                        .uploadedBy(e.getUploadedBy())
                        .verified(e.isVerified())
                        .uploadedAt(e.getUploadedAt())
                        .downloadUrl("/api/evidence/download/" + e.getId())
                        .build())
                .sorted(Comparator.comparing(EvidenceResponse::getUploadedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
 
        List<StatusHistoryResponse> history = c.getStatusHistories().stream()
                .map(h -> StatusHistoryResponse.builder()
                        .fromStatus(h.getFromStatus())
                        .toStatus(h.getToStatus())
                        .remarks(h.getRemarks())
                        .changedBy(h.getChangedBy())
                        .changedAt(h.getChangedAt())
                        .build())
                .sorted(Comparator.comparing(StatusHistoryResponse::getChangedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
 
        long days = c.getCreatedAt() != null
                ? ChronoUnit.DAYS.between(c.getCreatedAt(), LocalDateTime.now()) : 0;
 
        return ComplaintResponse.builder()
                .id(c.getId())
                .trackingNumber(c.getTrackingNumber())
                .title(c.getTitle())
                .description(c.getDescription())
                .category(c.getCategory())
                .status(c.getStatus())
                .priority(c.getPriority())
                .incidentDateTime(c.getIncidentDateTime())
                .incidentLocation(c.getIncidentLocation())
                .suspectPhone(c.getSuspectPhone())
                .suspectEmail(c.getSuspectEmail())
                .suspectBankAccount(c.getSuspectBankAccount())
                .suspectUpiId(c.getSuspectUpiId())
                .suspectIpAddress(c.getSuspectIpAddress())
                .financialLoss(c.getFinancialLoss())
                .bankName(c.getBankName())
                .transactionId(c.getTransactionId())
                .complainantName(c.getComplainant() != null ? c.getComplainant().getFullName() : "")
                .complainantEmail(c.getComplainant() != null ? c.getComplainant().getEmail() : "")
                .complainantPhone(c.getComplainant() != null ? c.getComplainant().getPhone() : "")
                .assignedOfficerName(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getFullName() : null)
                .assignedOfficerBadge(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getBadgeNumber() : null)
                .evidences(evidences)
                .statusHistory(history)
                .resolutionNotes(c.getResolutionNotes())
                .resolvedAt(c.getResolvedAt())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .daysPending(days)
                .totalEvidenceFiles(evidences.size())
                .build();
    }
 
    private String monthName(int m) {
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        return (m >= 1 && m <= 12) ? months[m - 1] : "Unknown";
    }
}
 