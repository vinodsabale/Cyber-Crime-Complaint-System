package com.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
 
@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "phone")
    })
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {
 
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false)
    private String fullName;
 
    @Column(nullable = false, unique = true)
    private String email;
 
    @Column(nullable = false)
    private String password;
 
    @Column(nullable = false, unique = true)
    private String phone;
 
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String aadharNumber;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.CITIZEN;
 
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;
 
    private boolean emailVerified = false;
    private String refreshToken;
    private String resetToken;
    private LocalDateTime resetTokenExpiry;
    private int failedLoginAttempts = 0;
    private LocalDateTime lockedUntil;
 
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
 
    @LastModifiedDate
    private LocalDateTime updatedAt;
 
    @OneToMany(mappedBy = "complainant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<Complaint> complaints = new ArrayList<>();
 
    public enum Role { CITIZEN, OFFICER, ANALYST, ADMIN }
    public enum UserStatus { ACTIVE, INACTIVE, SUSPENDED, LOCKED }
}