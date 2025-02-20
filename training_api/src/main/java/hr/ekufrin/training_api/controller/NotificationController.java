package hr.ekufrin.training_api.controller;

import hr.ekufrin.training_api.model.Notification;
import hr.ekufrin.training_api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@RequestParam String email) {
        List<Notification> notifications = notificationService.getUnreadNotificationsForUser(email);
        if (notifications.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/mark-as-read")
    public ResponseEntity<Void> markNotificationAsRead(@RequestParam String email, @RequestParam Long notificationId) {
        boolean updated = notificationService.markAsRead(email, notificationId);
        if (updated) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}

