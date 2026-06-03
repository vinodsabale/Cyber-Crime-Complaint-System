package com.controller.mvc;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.entity.Officer;
import com.entity.User;
import com.entity.Wanted;
import com.repository.WantedRepository;
import com.service.OfficerService;
import com.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import com.repository.ComplaintRepository;
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
	private final WantedRepository wantedRepo;
    private final UserService userService;
    private final OfficerService officerService;
    private final ComplaintRepository complaintRepo;

    // ── USERS ─────────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public String users(Model model,
                        HttpServletRequest request,
                        @RequestParam(defaultValue = "0")          int page,
                        @RequestParam(defaultValue = "15")         int size,
                        @RequestParam(defaultValue = "createdAt")  String sortBy,
                        @RequestParam(defaultValue = "DESC")       String dir) {
        Sort sort = dir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        model.addAttribute("users",   userService.getAll(pageable));
        model.addAttribute("roles",   User.Role.values());
        model.addAttribute("statuses", User.UserStatus.values());
        model.addAttribute("currentUri", request.getRequestURI());
        return "admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.delete(id);
            ra.addFlashAttribute("success", "User deleted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Cannot delete user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ── OFFICERS ──────────────────────────────────────────────────────────────

    @GetMapping("/officers")
    public String officers(Model model,
                           HttpServletRequest request,
                           @RequestParam(defaultValue = "0")         int page,
                           @RequestParam(defaultValue = "15")        int size,
                           @RequestParam(defaultValue = "createdAt") String sortBy,
                           @RequestParam(defaultValue = "DESC")      String dir) {
        Sort sort = dir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        model.addAttribute("officers",  officerService.getAll(pageable));
        model.addAttribute("ranks",     Officer.Rank.values());
        model.addAttribute("statuses",  Officer.OfficerStatus.values());
        model.addAttribute("currentUri", request.getRequestURI());
        return "admin/officers";
    }

    @GetMapping("/officers/new")
    public String newOfficerForm(Model model, HttpServletRequest request) {
        model.addAttribute("officer", new Officer());
        model.addAttribute("ranks",   Officer.Rank.values());
        model.addAttribute("statuses", Officer.OfficerStatus.values());
        model.addAttribute("currentUri", request.getRequestURI());
        return "admin/officer-form";
    }

    @PostMapping("/officers/new")
    public String saveOfficer(@ModelAttribute Officer officer, RedirectAttributes ra) {
        try {
            officerService.save(officer);
            ra.addFlashAttribute("success", "Officer added successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error saving officer: " + e.getMessage());
        }
        return "redirect:/admin/officers";
    }

    @GetMapping("/officers/{id}/edit")
    public String editOfficerForm(@PathVariable Long id, Model model, HttpServletRequest request) {
        model.addAttribute("officer",  officerService.getById(id));
        model.addAttribute("ranks",    Officer.Rank.values());
        model.addAttribute("statuses", Officer.OfficerStatus.values());
        model.addAttribute("currentUri", request.getRequestURI());
        return "admin/officer-form";
    }

    @PostMapping("/officers/{id}/edit")
    public String updateOfficer(@PathVariable Long id,
                                @ModelAttribute Officer officer,
                                RedirectAttributes ra) {
        try {
            officer.setId(id);
            officerService.save(officer);
            ra.addFlashAttribute("success", "Officer updated successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error updating officer: " + e.getMessage());
        }
        return "redirect:/admin/officers";
    }

    @PostMapping("/officers/{id}/delete")
    public String deleteOfficer(@PathVariable Long id, RedirectAttributes ra) {
        try {
            officerService.delete(id);
            ra.addFlashAttribute("success", "Officer deleted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Cannot delete officer: " + e.getMessage());
        }
        return "redirect:/admin/officers";
    }
 // ── WANTED ────────────────────────────────────────────────────────────────

    @GetMapping("/wanted")
    public String wantedList(Model model,
                             HttpServletRequest request,
                             @RequestParam(defaultValue = "0")          int page,
                             @RequestParam(defaultValue = "15")         int size,
                             @RequestParam(required = false)            String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<?> wantedPage;
        if (status != null && !status.isEmpty())
            wantedPage = wantedRepo.findByStatusWithComplaint(Wanted.WantedStatus.valueOf(status), pageable);
        else
            wantedPage = wantedRepo.findAllWithComplaint(pageable);
        model.addAttribute("wantedList",   wantedPage);
        model.addAttribute("statuses",     Wanted.WantedStatus.values());
        model.addAttribute("dangerLevels", Wanted.DangerLevel.values());
        model.addAttribute("currentUri",   request.getRequestURI());
        return "admin/wanted-list";
    }

    @PostMapping("/wanted/{id}/delete")
    public String deleteWanted(@PathVariable Long id, RedirectAttributes ra) {
        try {
            wantedRepo.deleteById(id);
            ra.addFlashAttribute("success", "Wanted person removed.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/wanted";
    }
    @GetMapping("/wanted/new")
    public String newWantedForm(Model model, HttpServletRequest request) {
        model.addAttribute("wanted", new Wanted());
        model.addAttribute("statuses",     Wanted.WantedStatus.values());
        model.addAttribute("dangerLevels", Wanted.DangerLevel.values());
        model.addAttribute("complaints",   complaintRepo.findAll());
        model.addAttribute("currentUri",   request.getRequestURI());
        return "admin/wanted-form";
    }

    @PostMapping("/wanted/new")
    public String saveWanted(@ModelAttribute Wanted wanted, RedirectAttributes ra) {
        try {
            wantedRepo.save(wanted);
            ra.addFlashAttribute("success", "Wanted person added successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/wanted";
    }
}
