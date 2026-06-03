package com.dto.request;
import java.time.LocalDateTime;

import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.entity.Complaint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplaintRequest {
 
    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 200)
    private String title;
 
    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 5000)
    private String description;
 
    @NotNull(message = "Category is required")
    private Complaint.CrimeCategory category;
 
    @NotNull(message = "Incident date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime incidentDateTime;
 
    @NotBlank(message = "Location is required")
    private String incidentLocation;
 
    private String suspectPhone;
    private String suspectEmail;
    private String suspectBankAccount;
    private String suspectUpiId;
    private String suspectIpAddress;
    private String suspectWebsite;
 
    @PositiveOrZero
    private Double financialLoss;
    private String bankName;
    private String transactionId;
 
    private List<MultipartFile> evidenceFiles;
}
 
 