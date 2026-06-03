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
@Table(name = "notifications")
@EntityListeners(AuditingEntityListener.class)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
 
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;
 
    @Column(nullable = false)
    private String title;
 
    @Column(columnDefinition = "TEXT")
    private String message;
 
    @Enumerated(EnumType.STRING)
    private NotificationType type;
 
    private String relatedEntityId;
    private Boolean isRead = false;
    private LocalDateTime readAt;
 
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
 
    public enum NotificationType {
        COMPLAINT_SUBMITTED, STATUS_UPDATED, OFFICER_ASSIGNED, RESOLVED, GENERAL
    }
}