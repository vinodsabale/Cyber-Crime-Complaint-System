package com.entity;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
 
@Entity
@Table(name = "complaint_status_history")
@EntityListeners(AuditingEntityListener.class)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplaintStatusHistory {
 
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    @ToString.Exclude
    private Complaint complaint;
 
    @Enumerated(EnumType.STRING)
    private Complaint.ComplaintStatus fromStatus;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Complaint.ComplaintStatus toStatus;
 
    @Column(columnDefinition = "TEXT")
    private String remarks;
 
    private String changedBy;
 
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime changedAt;
}
 