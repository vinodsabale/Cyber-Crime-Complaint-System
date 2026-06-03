package com.controller.rest;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dto.response.ApiResponse;
import com.entity.Evidence;
import com.exception.ResourceNotFoundException;
import com.repository.EvidenceRepository;
import com.service.FileStorageService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
 
@RestController
@RequestMapping("/api/evidence")
@RequiredArgsConstructor
@Tag(name = "Evidence API")
public class EvidenceRestController {
 
    private final EvidenceRepository evidenceRepo;
    private final FileStorageService fileService;
 
    @GetMapping("/complaint/{complaintId}")
    public ResponseEntity<ApiResponse<List<Evidence>>> byComplaint(
            @PathVariable Long complaintId) {
        return ResponseEntity.ok(ApiResponse.success(
                evidenceRepo.findByComplaintId(complaintId)));
    }
 
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Evidence ev = evidenceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found: " + id));
        Resource resource = fileService.load(ev.getFilePath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        ev.getFileType() != null
                        ? ev.getFileType()
                        : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + ev.getOriginalFileName() + "\"")
                .body(resource);
    }
 
    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<Evidence>> verify(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails ud) {
        Evidence ev = evidenceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found"));
        ev.setVerified(true);
        ev.setVerifiedBy(ud.getUsername());
        ev.setVerifiedAt(LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success(
                evidenceRepo.save(ev), "Evidence verified"));
    }
 
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Evidence ev = evidenceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found"));
        fileService.delete(ev.getFilePath());
        evidenceRepo.delete(ev);
        return ResponseEntity.ok(ApiResponse.success(null, "Evidence deleted"));
    }
}
 
 