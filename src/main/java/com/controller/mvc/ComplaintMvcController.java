package com.controller.mvc;
import java.util.List;


import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dto.request.ComplaintRequest;
import com.entity.Complaint;
import com.service.ComplaintService;
import com.service.OfficerService;
import com.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
 
@Controller
@RequestMapping("/complaints")
@RequiredArgsConstructor
public class ComplaintMvcController {
 
    private final ComplaintService complaintService;
    private final UserService userService;
    private final OfficerService officerService;
 
    @GetMapping
    public String list(Model model,
                       HttpServletRequest request,
                       @RequestParam(defaultValue = "0")         int page,
                       @RequestParam(defaultValue = "10")        int size,
                       @RequestParam(defaultValue = "createdAt") String sortBy,
                       @RequestParam(defaultValue = "DESC")      String dir,
                       @RequestParam(required = false)           String q,
                       @RequestParam(required = false) Complaint.ComplaintStatus status,
                       @RequestParam(required = false) Complaint.CrimeCategory category,
                       @RequestParam(required = false) Complaint.Priority priority) {
 
        Sort sort = dir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
 
        if (q != null && !q.isBlank()) {
            model.addAttribute("complaints", complaintService.search(q, pageable));
            model.addAttribute("q", q);
        } else if (status != null || category != null || priority != null) {
            model.addAttribute("complaints",
                    complaintService.filter(status, category, priority, null, null, pageable));
        } else {
            model.addAttribute("complaints", complaintService.getAll(pageable));
        }
 
        model.addAttribute("statuses",        Complaint.ComplaintStatus.values());
        model.addAttribute("categories",      Complaint.CrimeCategory.values());
        model.addAttribute("priorities",      Complaint.Priority.values());
        model.addAttribute("currentStatus",   status);
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentPriority", priority);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        model.addAttribute("currentUri", request.getRequestURI());
        return "complaints/list";
    }
 
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpServletRequest request) {
        model.addAttribute("complaint", complaintService.getById(id));
        model.addAttribute("statuses",  Complaint.ComplaintStatus.values());
        model.addAttribute("officers",  officerService.getAvailableOfficers());
        model.addAttribute("currentUri", request.getRequestURI());
        return "complaints/detail";
    }
 
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("complaint",  new ComplaintRequest());
        model.addAttribute("categories", Complaint.CrimeCategory.values());
        return "complaints/form";
    }
 
    @PostMapping("/new")
    public String submitNew(
            @Valid @ModelAttribute("complaint") ComplaintRequest req,
            BindingResult result,
            @RequestParam(value = "evidenceFiles", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails ud,
            RedirectAttributes ra,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", Complaint.CrimeCategory.values());
            return "complaints/form";
        }
        if (files != null) req.setEvidenceFiles(files);
        Long userId = userService.getByEmail(ud.getUsername()).getId();
        var saved = complaintService.submit(req, userId);
        ra.addFlashAttribute("success",
                "Complaint submitted! Tracking No: " + saved.getTrackingNumber());
        return "redirect:/complaints/" + saved.getId();
    }
 
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var c = complaintService.getById(id);
        ComplaintRequest req = new ComplaintRequest();
        req.setTitle(c.getTitle());
        req.setDescription(c.getDescription());
        req.setCategory(c.getCategory());
        req.setIncidentLocation(c.getIncidentLocation());
        req.setIncidentDateTime(c.getIncidentDateTime());
        model.addAttribute("complaint", req);
        model.addAttribute("id", id);
        model.addAttribute("categories", Complaint.CrimeCategory.values());
        return "complaints/form";
    }
 
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("complaint") ComplaintRequest req,
            BindingResult result,
            RedirectAttributes ra,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", Complaint.CrimeCategory.values());
            return "complaints/form";
        }
        complaintService.update(id, req);
        ra.addFlashAttribute("success", "Complaint updated successfully");
        return "redirect:/complaints/" + id;
    }
 
    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam Complaint.ComplaintStatus status,
            @RequestParam(required = false) String remarks,
            @AuthenticationPrincipal UserDetails ud,
            RedirectAttributes ra) {
        complaintService.updateStatus(id, status, remarks, ud.getUsername());
        ra.addFlashAttribute("success",
                "Status updated to: " + status.name().replace("_", " "));
        return "redirect:/complaints/" + id;
    }
 
    @PostMapping("/{id}/assign")
    public String assignOfficer(
            @PathVariable Long id,
            @RequestParam Long officerId,
            RedirectAttributes ra) {
        complaintService.assignOfficer(id, officerId);
        ra.addFlashAttribute("success", "Officer assigned successfully");
        return "redirect:/complaints/" + id;
    }
 
    @PostMapping("/{id}/evidence")
    public String uploadEvidence(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(required = false) String description,
            @AuthenticationPrincipal UserDetails ud,
            RedirectAttributes ra) {
        complaintService.uploadEvidence(id, files, description, ud.getUsername());
        ra.addFlashAttribute("success", files.size() + " evidence file(s) uploaded");
        return "redirect:/complaints/" + id;
    }
 
    @GetMapping("/track")
    public String trackForm() { return "complaints/track"; }
 
    @PostMapping("/track")
    public String trackResult(@RequestParam String trackingNumber, Model model) {
        try {
            model.addAttribute("complaint",
                    complaintService.getByTrackingNumber(trackingNumber));
        } catch (Exception e) {
            model.addAttribute("error",
                    "Complaint not found with tracking number: " + trackingNumber);
        }
        return "complaints/track";
    }
}