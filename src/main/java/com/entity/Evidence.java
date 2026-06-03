package com.entity;
import java.io.Serializable;
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
@Table(name = "evidences")
@EntityListeners(AuditingEntityListener.class)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Evidence implements Serializable {
 
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    @ToString.Exclude
    private Complaint complaint;
 
    @Column(nullable = false)
    private String originalFileName;
 
    @Column(nullable = false)
    private String storedFileName;
 
    @Column(nullable = false)
    private String filePath;
 
    @Column(nullable = false)
    private String fileType;
 
    private Long fileSize;
 
    @Enumerated(EnumType.STRING)
    private EvidenceType evidenceType;
 
    private String description;
    private String uploadedBy;
    private boolean verified = false;
    private String verifiedBy;
    private LocalDateTime verifiedAt;
 
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime uploadedAt;
 
    public enum EvidenceType {
        SCREENSHOT, DOCUMENT, BANK_STATEMENT, PHOTO, VIDEO, OTHER
    }
}
 