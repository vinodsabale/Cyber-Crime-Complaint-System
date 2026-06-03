package com.controller.mvc;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dto.request.SuspectRequest;
import com.entity.Suspect;
import com.service.SuspectService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
 
@Controller
@RequestMapping("/suspects")
@RequiredArgsConstructor
public class SuspectMvcController {
 
    private final SuspectService suspectService;
 
    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0")         int page,
                       @RequestParam(defaultValue = "10")        int size,
                       @RequestParam(defaultValue = "createdAt") String sortBy,
                       @RequestParam(defaultValue = "DESC")      String dir,
                       @RequestParam(required = false)           String q,
                       @RequestParam(required = false) Suspect.SuspectStatus status,
                       @RequestParam(required = false) Suspect.DangerLevel level) {
 
        Sort sort = dir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
 
        if (q != null && !q.isBlank()) {
            model.addAttribute("suspects", suspectService.search(q, pageable));
            model.addAttribute("q", q);
        } else if (status != null || level != null) {
            model.addAttribute("suspects", suspectService.filter(status, level, pageable));
        } else {
            model.addAttribute("suspects", suspectService.getAll(pageable));
        }
 
        model.addAttribute("statuses",     Suspect.SuspectStatus.values());
        model.addAttribute("dangerLevels", Suspect.DangerLevel.values());
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentLevel",  level);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        return "suspect/list";
    }
 
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("suspect", suspectService.getById(id));
        return "suspect/detail";
    }
 
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("suspect",      new SuspectRequest());
        model.addAttribute("statuses",     Suspect.SuspectStatus.values());
        model.addAttribute("dangerLevels", Suspect.DangerLevel.values());
        model.addAttribute("genders",      Suspect.Gender.values());
        return "suspect/form";
    }
 
    @PostMapping("/new")
    public String create(
            @Valid @ModelAttribute("suspect") SuspectRequest req,
            BindingResult result,
            RedirectAttributes ra,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("statuses",     Suspect.SuspectStatus.values());
            model.addAttribute("dangerLevels", Suspect.DangerLevel.values());
            model.addAttribute("genders",      Suspect.Gender.values());
            return "suspect/form";
        }
        var saved = suspectService.add(req);
        ra.addFlashAttribute("success",
                "Suspect added with code: " + saved.getSuspectCode());
        return "redirect:/suspects/" + saved.getId();
    }
 
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("suspect",      suspectService.getById(id));
        model.addAttribute("statuses",     Suspect.SuspectStatus.values());
        model.addAttribute("dangerLevels", Suspect.DangerLevel.values());
        model.addAttribute("genders",      Suspect.Gender.values());
        return "suspect/form";
    }
 
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("suspect") SuspectRequest req,
            BindingResult result,
            RedirectAttributes ra,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("statuses",     Suspect.SuspectStatus.values());
            model.addAttribute("dangerLevels", Suspect.DangerLevel.values());
            model.addAttribute("genders",      Suspect.Gender.values());
            return "suspect/form";
        }
        suspectService.update(id, req);
        ra.addFlashAttribute("success", "Suspect updated successfully");
        return "redirect:/suspects/" + id;
    }
 
    @PostMapping("/{id}/arrest")
    public String arrest(
            @PathVariable Long id,
            @RequestParam String details,
            RedirectAttributes ra) {
        suspectService.arrest(id, details);
        ra.addFlashAttribute("success", "Arrest recorded successfully");
        return "redirect:/suspects/" + id;
    }
 
    @GetMapping("/wanted")
    public String wanted(Model model) {
        model.addAttribute("suspects", suspectService.getWanted());
        return "suspect/wanted";
    }
 
    @GetMapping("/most-wanted")
    public String mostWanted(Model model) {
        model.addAttribute("suspects", suspectService.getMostWanted());
        return "suspect/wanted";
    }
}
 