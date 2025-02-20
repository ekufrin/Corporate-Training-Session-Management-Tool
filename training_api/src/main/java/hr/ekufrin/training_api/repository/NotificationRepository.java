package hr.ekufrin.training_api.repository;

import hr.ekufrin.training_api.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(value = """
                SELECT n.* 
                FROM notification n
                JOIN users_notifications un ON un.notification_id = n.id
                JOIN "user" u ON u.id = un.user_id
                WHERE u.email = :email AND un.is_read = false
            """, nativeQuery = true)
    List<Notification> findUnreadByEmail(String email);

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE users_notifications
            SET is_read = true, read_date = now()
            WHERE notification_id = :notificationId
            AND user_id = (SELECT id FROM "user" WHERE email = :email)
            """, nativeQuery = true)
    int markNotificationAsRead(String email, Long notificationId);
}

