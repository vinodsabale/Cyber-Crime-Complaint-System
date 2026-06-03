package com.dto.response;
import lombok.*;

import java.util.Map;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardStats {
    private Long totalComplaints;
    private Long pendingComplaints;
    private Long resolvedComplaints;
    private Long rejectedComplaints;
    private Long underInvestigation;
    private Long totalSuspects;
    private Long wantedSuspects;
    private Long arrestedSuspects;
    private Long totalOfficers;
    private Long activeOfficers;
    private Long totalUsers;
    private Long todayComplaints;
    private Long thisMonthComplaints;
    private Double totalFinancialLoss;
    private Map<String, Long> complaintsByCategory;
    private Map<String, Long> complaintsByStatus;
    private Map<String, Long> complaintsByMonth;
    private Map<String, Long> suspectsByDangerLevel;
}
 