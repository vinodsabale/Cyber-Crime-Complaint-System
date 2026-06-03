package com.controller.mvc;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.service.ComplaintService;
import com.service.NotificationService;
import com.service.SuspectService;
import com.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ComplaintService complaintService;
    private final SuspectService suspectService;
    private final NotificationService notifyService;
    private final UserService userService;

    @GetMapping("/")
    public String home() { return "redirect:/dashboard"; }

    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            @AuthenticationPrincipal UserDetails ud,
                            HttpServletRequest request) {
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("stats",      complaintService.getDashboardStats());
        model.addAttribute("critical",   complaintService.getCritical());
        model.addAttribute("unassigned", complaintService.getUnassigned());
        model.addAttribute("mostWanted", suspectService.getMostWanted());
        if (ud != null) {
            Long userId = userService.getByEmail(ud.getUsername()).getId();
            model.addAttribute("unreadCount", notifyService.getUnreadCount(userId));
        }
        return "dashboard/index";
    }

    @GetMapping("/access-denied")
    public String accessDenied() { return "error/403"; }
}