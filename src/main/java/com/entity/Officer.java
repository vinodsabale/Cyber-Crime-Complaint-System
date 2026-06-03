package com.entity;
import java.io.Serializable;
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
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "officers")
@EntityListeners(AuditingEntityListener.class)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Officer implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_number", unique = true, nullable = false)
    private String badgeNumber;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "phone", unique = true, nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "officer_rank")
    private Rank rank;

    @Column(name = "department")
    private String department;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "station_name")
    private String stationName;

    @Column(name = "district")
    private String district;

    @Column(name = "officer_state")
    private String state;

    @Enumerated(EnumType.STRING)
    @Column(name = "officer_status")
    private OfficerStatus status = OfficerStatus.ACTIVE;

    @Column(name = "max_case_load")
    private Integer maxCaseLoad = 20;

    @Column(name = "current_case_load")
    private Integer currentCaseLoad = 0;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User userAccount;

    @OneToMany(mappedBy = "assignedOfficer", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<Complaint> assignedComplaints = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Rank { CONSTABLE, ASI, SI, INSPECTOR, DSP, SP, DIG, IG }
    public enum OfficerStatus { ACTIVE, INACTIVE, ON_LEAVE, SUSPENDED }
}