package com.entity;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
 
@Entity
@Table(name = "suspects")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Suspect implements Serializable {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(unique = true, nullable = false)
    private String suspectCode;
 
    @Column(nullable = false)
    private String fullName;
 
    private String alias;
    private LocalDate dateOfBirth;
 
    @Enumerated(EnumType.STRING)
    private Gender gender;
 
    private String nationality = "Indian";
    private String aadharNumber;
    private String phone;
    private String email;
    private String lastKnownAddress;
 
    @Column(columnDefinition = "TEXT")
    private String physicalDescription;
 
    private String photoUrl;
 
    @Enumerated(EnumType.STRING)
    private SuspectStatus status;
 
    @Enumerated(EnumType.STRING)
    private DangerLevel dangerLevel;
 
    @Column(columnDefinition = "TEXT")
    private String modusOperandi;
 
    private String bankAccounts;
    private String ipAddresses;
    private String emailAddresses;
    private String phoneNumbers;
    private String websites;
    private String upiIds;
 
    private Integer totalCases = 0;
    private boolean arrested = false;
    private LocalDate arrestDate;
    private String arrestDetails;
 
    @Column(columnDefinition = "TEXT")
    private String notes;
 
    @ManyToMany(mappedBy = "linkedSuspects")
    @Builder.Default
    @ToString.Exclude
    private List<Complaint> complaints = new ArrayList<>();
 
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
 
    @LastModifiedDate
    private LocalDateTime updatedAt;
 
    public enum Gender { MALE, FEMALE, OTHER, UNKNOWN }
    public enum SuspectStatus { IDENTIFIED, WANTED, ARRESTED, BAILED, ACQUITTED, CONVICTED, ABSCONDING }
    public enum DangerLevel { LOW, MODERATE, HIGH, VERY_HIGH, MOST_WANTED }
}
