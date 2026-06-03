package com.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
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
@Table(name = "wanted")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wanted implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String alias;
    private String description;
    private String lastKnownLocation;
    private String photoUrl;
    private Double reward;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WantedStatus status = WantedStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DangerLevel dangerLevel = DangerLevel.MEDIUM;

    // Complaint शी by default link
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    @ToString.Exclude
    private Complaint complaint;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum WantedStatus { ACTIVE, ARRESTED, DECEASED, CLEARED }
    public enum DangerLevel  { LOW, MEDIUM, HIGH, EXTREME }
}