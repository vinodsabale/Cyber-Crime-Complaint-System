package com.dto.response;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.entity.Suspect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SuspectResponse {
    private Long id;
    private String suspectCode;
    private String fullName;
    private String alias;
    private LocalDate dateOfBirth;
    private Suspect.Gender gender;
    private String nationality;
    private String phone;
    private String email;
    private String lastKnownAddress;
    private String physicalDescription;
    private String photoUrl;
    private Suspect.SuspectStatus status;
    private Suspect.DangerLevel dangerLevel;
    private String modusOperandi;
    private String bankAccounts;
    private String ipAddresses;
    private String emailAddresses;
    private String phoneNumbers;
    private String websites;
    private String upiIds;
    private Integer totalCases;
    private boolean arrested;
    private LocalDate arrestDate;
    private String notes;
    private LocalDateTime createdAt;
    private int linkedComplaintsCount;
}
 