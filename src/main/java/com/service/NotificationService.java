package com.service;
import java.util.List;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.entity.Complaint;
import com.entity.Notification;
import com.entity.User;
import com.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
 
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
 
    private final NotificationRepository notifyRepo;
    private final JavaMailSender mailSender;
 
    @Async
    public void notifySubmitted(Complaint c) {
        save(c.getComplainant(),
             "Complaint Submitted",
             "Your complaint " + c.getTrackingNumber() + " has been submitted successfully.",
             Notification.NotificationType.COMPLAINT_SUBMITTED,
             c.getId().toString());
 
        sendEmail(c.getComplainant().getEmail(),
                  "Complaint Submitted - " + c.getTrackingNumber(),
                  "Dear " + c.getComplainant().getFullName() + ",\n\n"
                  + "Tracking No: " + c.getTrackingNumber()
                  + "\nCategory: " + c.getCategory()
                  + "\n\nSmart Crime Tracking System");
    }
 
    @Async
    public void notifyStatusChange(Complaint c,
                                   Complaint.ComplaintStatus from,
                                   Complaint.ComplaintStatus to) {
        save(c.getComplainant(),
             "Status Updated",
             "Complaint " + c.getTrackingNumber() + " status: " + from + " → " + to,
             Notification.NotificationType.STATUS_UPDATED,
             c.getId().toString());
 
        sendEmail(c.getComplainant().getEmail(),
                  "Status Update - " + c.getTrackingNumber(),
                  "Dear " + c.getComplainant().getFullName() + ",\n\n"
                  + "New Status: " + to.name().replace("_", " ")
                  + "\n\nSmart Crime Tracking System");
    }
 
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {
        return notifyRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }
 
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return notifyRepo.countByUserIdAndIsReadFalse(userId);
    }
 
    @Transactional
    public void markAllRead(Long userId) {
        notifyRepo.markAllRead(userId);
    }
 
    private void save(User user, String title, String msg,
                      Notification.NotificationType type, String entityId) {
        notifyRepo.save(Notification.builder()
                .user(user).title(title).message(msg)
                .type(type).relatedEntityId(entityId)
                .isRead(false).build());
    }
 
    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setFrom("noreply@crimetracking.gov.in");
            m.setTo(to);
            m.setSubject(subject);
            m.setText(body);
            mailSender.send(m);
        } catch (Exception e) {
            log.warn("Email failed to {}: {}", to, e.getMessage());
        }
    }
}
 
