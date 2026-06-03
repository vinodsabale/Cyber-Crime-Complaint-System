package com.controller.rest;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dto.request.SuspectRequest;
import com.dto.response.ApiResponse;
import com.dto.response.SuspectResponse;
import com.entity.Suspect;
import com.service.SuspectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
 
@RestController
@RequestMapping("/api/suspects")
@RequiredArgsConstructor
@Tag(name = "Suspect API")
@SecurityRequirement(name = "bearerAuth")
public class SuspectRestController {
 
    private final SuspectService suspectService;
 
    @PostMapping
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<SuspectResponse>> add(
            @Valid @RequestBody SuspectRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(suspectService.add(req), "Suspect added"));
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SuspectResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(suspectService.getById(id)));
    }
 
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<SuspectResponse>> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(suspectService.getByCode(code)));
    }
 
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SuspectResponse>>> getAll(
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "10")        int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC")      String dir) {
        Sort sort = dir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(ApiResponse.success(
                suspectService.getAll(PageRequest.of(page, size, sort))));
    }
 
    @GetMapping("/search")
    @Operation(summary = "Search by name, phone, email, UPI, bank, IP")
    public ResponseEntity<ApiResponse<Page<SuspectResponse>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                suspectService.search(q,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }
 
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Page<SuspectResponse>>> filter(
            @RequestParam(required = false) Suspect.SuspectStatus status,
            @RequestParam(required = false) Suspect.DangerLevel level,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                suspectService.filter(status, level, PageRequest.of(page, size))));
    }
 
    @GetMapping("/match")
    @Operation(summary = "Match suspect by complaint suspect details")
    public ResponseEntity<ApiResponse<List<SuspectResponse>>> match(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String upiId,
            @RequestParam(required = false) String bankAccount) {
        return ResponseEntity.ok(ApiResponse.success(
                suspectService.searchBySuspectDetails(phone, email, upiId, bankAccount)));
    }
 
    @GetMapping("/wanted")
    public ResponseEntity<ApiResponse<List<SuspectResponse>>> wanted() {
        return ResponseEntity.ok(ApiResponse.success(suspectService.getWanted()));
    }
 
    @GetMapping("/most-wanted")
    public ResponseEntity<ApiResponse<List<SuspectResponse>>> mostWanted() {
        return ResponseEntity.ok(ApiResponse.success(suspectService.getMostWanted()));
    }
 
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<SuspectResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SuspectRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                suspectService.update(id, req), "Suspect updated"));
    }
 
    @PatchMapping("/{id}/arrest")
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<SuspectResponse>> arrest(
            @PathVariable Long id,
            @RequestParam String details) {
        return ResponseEntity.ok(ApiResponse.success(
                suspectService.arrest(id, details), "Suspect arrested"));
    }
 
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        suspectService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Suspect deleted"));
    }
}
 