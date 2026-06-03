package com.dto.response;
import java.time.LocalDateTime;

import com.entity.Complaint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StatusHistoryResponse {
    private Complaint.ComplaintStatus fromStatus;
    private Complaint.ComplaintStatus toStatus;
    private String remarks;
    private String changedBy;
    private LocalDateTime changedAt;
}