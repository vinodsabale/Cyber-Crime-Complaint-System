package com.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

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

    public ComplaintRequest() {}

    // Constructor without validation annotations
    public ComplaintRequest(String title, String description, Complaint.CrimeCategory category,
                            LocalDateTime incidentDateTime, String incidentLocation,
                            String suspectPhone, String suspectEmail, String suspectBankAccount,
                            String suspectUpiId, String suspectIpAddress, String suspectWebsite,
                            Double financialLoss, String bankName, String transactionId,
                            List<MultipartFile> evidenceFiles) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.incidentDateTime = incidentDateTime;
        this.incidentLocation = incidentLocation;
        this.suspectPhone = suspectPhone;
        this.suspectEmail = suspectEmail;
        this.suspectBankAccount = suspectBankAccount;
        this.suspectUpiId = suspectUpiId;
        this.suspectIpAddress = suspectIpAddress;
        this.suspectWebsite = suspectWebsite;
        this.financialLoss = financialLoss;
        this.bankName = bankName;
        this.transactionId = transactionId;
        this.evidenceFiles = evidenceFiles;
    }

    // Getters and setters...

    @Override
    public int hashCode() {
        return Objects.hash(bankName, category, description, financialLoss, incidentDateTime,
                incidentLocation, suspectBankAccount, suspectEmail, suspectIpAddress,
                suspectPhone, suspectUpiId, suspectWebsite, title, transactionId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ComplaintRequest other)) return false;
        return Objects.equals(bankName, other.bankName)
                && category == other.category
                && Objects.equals(description, other.description)
                && Objects.equals(financialLoss, other.financialLoss)
                && Objects.equals(incidentDateTime, other.incidentDateTime)
                && Objects.equals(incidentLocation, other.incidentLocation)
                && Objects.equals(suspectBankAccount, other.suspectBankAccount)
                && Objects.equals(suspectEmail, other.suspectEmail)
                && Objects.equals(suspectIpAddress, other.suspectIpAddress)
                && Objects.equals(suspectPhone, other.suspectPhone)
                && Objects.equals(suspectUpiId, other.suspectUpiId)
                && Objects.equals(suspectWebsite, other.suspectWebsite)
                && Objects.equals(title, other.title)
                && Objects.equals(transactionId, other.transactionId);
    }
}
