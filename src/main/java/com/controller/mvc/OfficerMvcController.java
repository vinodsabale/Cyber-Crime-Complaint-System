package com.controller.mvc;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.entity.Complaint;
import com.service.ComplaintService;
import com.service.OfficerService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/officer")
@PreAuthorize("hasAnyRole('OFFICER','ADMIN','ANALYST')")
@RequiredArgsConstructor
public class OfficerMvcController {

    private final ComplaintService complaintService;
    private final OfficerService officerService;

    @GetMapping
    public String dashboard() {
        return "redirect:/officer/complaints";
    }

    @GetMapping("/complaints")
    public String myComplaints(
            Model model,
            @AuthenticationPrincipal UserDetails ud,
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "15")        int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC")      String dir,
            @RequestParam(required = false) Complaint.ComplaintStatus status) {

        Sort sort = dir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Show all complaints (officer sees all assigned; admin sees all)
        if (status != null) {
            model.addAttribute("complaints",
                    complaintService.filter(status, null, null, null, null, pageable));
        } else {
            model.addAttribute("complaints", complaintService.getAll(pageable));
        }

        model.addAttribute("statuses",      Complaint.ComplaintStatus.values());
        model.addAttribute("currentStatus", status);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir",    dir);
        model.addAttribute("officers", officerService.getAll(pageable));
        return "officer/complaints";
    }
}
