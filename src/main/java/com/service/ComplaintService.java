package com.service;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.dto.request.ComplaintRequest;
import com.dto.response.ComplaintResponse;
import com.dto.response.DashboardStats;
import com.entity.Complaint;
 
public interface ComplaintService {
    ComplaintResponse submit(ComplaintRequest req, Long userId);
    ComplaintResponse getById(Long id);
    ComplaintResponse getByTrackingNumber(String trackingNum);
    Page<ComplaintResponse> getAll(Pageable pageable);
    Page<ComplaintResponse> getByUser(Long userId, Pageable pageable);
    Page<ComplaintResponse> search(String query, Pageable pageable);
    Page<ComplaintResponse> filter(
            Complaint.ComplaintStatus status,
            Complaint.CrimeCategory category,
            Complaint.Priority priority,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);
    ComplaintResponse updateStatus(Long id, Complaint.ComplaintStatus status, String remarks, String changedBy);
    ComplaintResponse assignOfficer(Long complaintId, Long officerId);
    ComplaintResponse update(Long id, ComplaintRequest req);
    void delete(Long id);
    DashboardStats getDashboardStats();
    List<ComplaintResponse> getUnassigned();
    List<ComplaintResponse> getCritical();
    ComplaintResponse uploadEvidence(Long complaintId, List<MultipartFile> files, String desc, String uploadedBy);
}
 
 