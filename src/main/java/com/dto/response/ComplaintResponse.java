package com.dto.response;
import java.time.LocalDateTime;

import java.util.List;

import com.entity.Complaint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplaintResponse {
    private Long id;
    private String trackingNumber;
    private String title;
    private String description;
    private Complaint.CrimeCategory category;
    private Complaint.ComplaintStatus status;
    private Complaint.Priority priority;
    private LocalDateTime incidentDateTime;
    private String incidentLocation;
    private String suspectPhone;
    private String suspectEmail;
    private String suspectBankAccount;
    private String suspectUpiId;
    private String suspectIpAddress;
    private Double financialLoss;
    private String bankName;
    private String transactionId;
    private String complainantName;
    private String complainantEmail;
    private String complainantPhone;
    private String assignedOfficerName;
    private String assignedOfficerBadge;
    private List<EvidenceResponse> evidences;
    private List<StatusHistoryResponse> statusHistory;
    private String resolutionNotes;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long daysPending;
    private int totalEvidenceFiles;
}
 