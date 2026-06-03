package com.entity;

import java.io.Serializable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name="complaints")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint implements Serializable{
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(unique = true, nullable = false)
    private String trackingNumber;
 
    @Column(nullable = false)
    private String title;
 
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CrimeCategory category;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintStatus status = ComplaintStatus.SUBMITTED;
 
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;
 
    private LocalDateTime incidentDateTime;
    private String incidentLocation;
    private String suspectPhone;
    private String suspectEmail;
    private String suspectBankAccount;
    private String suspectUpiId;
    private String suspectIpAddress;
    private String suspectWebsite;
    private Double financialLoss;
    private String bankName;
    private String transactionId;
    private String resolutionNotes;
    private LocalDateTime resolvedAt;
    private String rejectionReason;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complainant_id", nullable = false)
    @ToString.Exclude
    private User complainant;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id")
    @ToString.Exclude
    private Officer assignedOfficer;
 
    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Evidence> evidences = new ArrayList<>();
 
    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<ComplaintStatusHistory> statusHistories = new ArrayList<>();
 
    @ManyToMany
    @JoinTable(name = "complaint_suspect",
        joinColumns = @JoinColumn(name = "complaint_id"),
        inverseJoinColumns = @JoinColumn(name = "suspect_id"))
    @Builder.Default
    @ToString.Exclude
    private List<Suspect> linkedSuspects = new ArrayList<>();
 
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
 
    @LastModifiedDate
    private LocalDateTime updatedAt;
 
    @CreatedBy
    private String createdBy;
 
    public enum CrimeCategory {
        MURDER, ROBBERY, THEFT, ASSAULT, FRAUD, KIDNAPPING,
        CYBERCRIME, DRUG_TRAFFICKING, DOMESTIC_VIOLENCE,
        EXTORTION, ARSON, MISSING_PERSON, OTHER
    }
 
    public enum ComplaintStatus {
        SUBMITTED, UNDER_REVIEW, ASSIGNED, UNDER_INVESTIGATION,
        RESOLVED, CLOSED, REJECTED, ESCALATED, REOPENED
    }
 
    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Wanted> wantedPersons = new ArrayList<>();

}
