package com.dto.request;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.entity.Suspect;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SuspectRequest {
	@NotBlank(message = "Full name is required")
    private String fullName;
 
    private String alias;
 
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) //here we change alos write iso chnage to pattren  @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfBirth;
 
    private Suspect.Gender gender;
    private String nationality;
    private String aadharNumber;
    private String phone;
 
    @Email(message = "Invalid email")
    private String email;
 
    private String lastKnownAddress;
    private String physicalDescription;
 
    @NotNull(message = "Status is required")
    private Suspect.SuspectStatus status;
 
    @NotNull(message = "Danger level is required")
    private Suspect.DangerLevel dangerLevel;
 
    private String modusOperandi;
    private String bankAccounts;
    private String ipAddresses;
    private String emailAddresses;
    private String phoneNumbers;
    private String websites;
    private String upiIds;
    private String notes;

}
