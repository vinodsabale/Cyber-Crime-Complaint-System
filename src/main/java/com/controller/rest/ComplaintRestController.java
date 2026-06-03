package com.controller.rest;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dto.request.ComplaintRequest;
import com.dto.response.ApiResponse;
import com.dto.response.ComplaintResponse;
import com.dto.response.DashboardStats;
import com.entity.Complaint;
import com.service.ComplaintService;
import com.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
 
@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaint API")
@SecurityRequirement(name = "bearerAuth")
public class ComplaintRestController {
 
    private final ComplaintService complaintService;
    private final UserService userService;
 
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Submit complaint with file upload")
    public ResponseEntity<ApiResponse<ComplaintResponse>> submit(
            @Valid @ModelAttribute ComplaintRequest req,
            @AuthenticationPrincipal UserDetails ud) {
        Long userId = userService.getByEmail(ud.getUsername()).getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        complaintService.submit(req, userId),
                        "Complaint submitted successfully"));
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(complaintService.getById(id)));
    }
 
    @GetMapping("/track/{trackingNum}")
    @Operation(summary = "Track complaint by tracking number")
    public ResponseEntity<ApiResponse<ComplaintResponse>> track(@PathVariable String trackingNum) {
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.getByTrackingNumber(trackingNum)));
    }
 
    @GetMapping
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN','ANALYST')")
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> getAll(
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "10")        int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC")      String dir) {
        Sort sort = dir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.getAll(PageRequest.of(page, size, sort))));
    }
 
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> myComplaints(
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = userService.getByEmail(ud.getUsername()).getId();
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.getByUser(userId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }
 
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.search(q,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }
 
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> filter(
            @RequestParam(required = false) Complaint.ComplaintStatus status,
            @RequestParam(required = false) Complaint.CrimeCategory category,
            @RequestParam(required = false) Complaint.Priority priority,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "10")        int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC")      String dir) {
        Sort sort = dir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.filter(status, category, priority, from, to,
                        PageRequest.of(page, size, sort))));
    }
 
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.update(id, req), "Complaint updated"));
    }
 
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        complaintService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Complaint deleted"));
    }
 
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam Complaint.ComplaintStatus status,
            @RequestParam(required = false) String remarks,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.updateStatus(id, status, remarks, ud.getUsername()),
                "Status updated"));
    }
 
    @PatchMapping("/{id}/assign/{officerId}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> assign(
            @PathVariable Long id,
            @PathVariable Long officerId) {
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.assignOfficer(id, officerId), "Officer assigned"));
    }
 
    @PostMapping("/{id}/evidence")
    public ResponseEntity<ApiResponse<ComplaintResponse>> uploadEvidence(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(required = false) String description,
            @AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.uploadEvidence(id, files, description, ud.getUsername()),
                files.size() + " file(s) uploaded"));
    }
 
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN','ANALYST')")
    public ResponseEntity<ApiResponse<DashboardStats>> stats() {
        return ResponseEntity.ok(ApiResponse.success(complaintService.getDashboardStats()));
    }
 
    @GetMapping("/unassigned")
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> unassigned() {
        return ResponseEntity.ok(ApiResponse.success(complaintService.getUnassigned()));
    }
 
    @GetMapping("/critical")
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN','ANALYST')")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> critical() {
        return ResponseEntity.ok(ApiResponse.success(complaintService.getCritical()));
    }
}
 
 