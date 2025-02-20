package hr.ekufrin.training_api.service;

import hr.ekufrin.training_api.model.Notification;
import hr.ekufrin.training_api.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public List<Notification> getUnreadNotificationsForUser(String email) {
        return notificationRepository.findUnreadByEmail(email);
    }

    public boolean markAsRead(String email, Long notificationId) {
        return notificationRepository.markNotificationAsRead(email, notificationId) > 0;
    }
}
