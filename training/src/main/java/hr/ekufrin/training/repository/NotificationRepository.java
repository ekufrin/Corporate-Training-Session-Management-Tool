package hr.ekufrin.training.repository;

import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.exception.NoDataFoundInDatabaseException;
import hr.ekufrin.training.generics.Repository;
import hr.ekufrin.training.model.User;
import hr.ekufrin.training.model.Notification;

import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

import static hr.ekufrin.training.HelloApplication.log;
import static hr.ekufrin.training.repository.DatabaseRepository.ERROR_CONNECTING_TO_DATABASE;

/**
 * Repository for the Notification entity
 */
public class NotificationRepository extends Repository<Notification> {
    /**
     * Saves the notification to the database
     * @param entity - entity to save
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    @Override
    public void save(Notification entity) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO notification (message) VALUES (?)")) {
            stmt.setString(1, entity.getMessage());
            stmt.executeUpdate();
        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and saving notification with message {}, with exception: {}", entity.getMessage(), e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and saving notification with message " + entity.getMessage());
        }
    }

    /**
     * Deletes the notification from the database
     * @param entity - notification to delete
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    @Override
    public void delete(Notification entity) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM notification WHERE id = ?")) {
            stmt.setLong(1, entity.getId());
            stmt.executeUpdate();
        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and deleting notification with id {}, with exception: {}", entity.getId(), e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and deleting notification with id " + entity.getId());
        }
    }

    /**
     * Finds a notification by its id
     * @param id - id of notification to find
     * @return - notification with the given id
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     * @throws NoDataFoundInDatabaseException - If there is no notification with the given id
     */
    @Override
    public Notification findById(Long id) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("SELECT id,message, sent_date FROM notification WHERE id = ?")) {
            stmt.setLong(1, id);
            ResultSet rS = stmt.executeQuery();
            if (rS.next()) {
                return new Notification(
                        rS.getLong(1),
                        rS.getString(2),
                        rS.getTimestamp(3).toLocalDateTime());
            } else {
                log.info("No notification found with id: {}", id);
                throw new NoDataFoundInDatabaseException("No notification found with id: " + id);
            }
        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and finding notification with id {}, with exception: {}", id, e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and finding notification with id " + id);
        }
    }

    /**
     * Finds all notifications in the database
     * @return - hashset of all notifications
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    @Override
    public HashSet<Notification> findAll() throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("SELECT id,message, sent_date FROM notification")) {
            return getNotifications(stmt);
        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and finding all notifications, with exception: {}", e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and finding all notifications");
        }
    }

    /**
     * Saves the notification to the database and sends it to the employees
     * @param entity - notification to save
     * @param recipients - set of employees to send the notification to
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     * @throws NoDataFoundInDatabaseException - If there is no notification id generated
     */
    public void saveNotificationForEmployees(Notification entity, Set<User> recipients) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO notification (message) VALUES (?) RETURNING id", Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO users_notifications (user_id, notification_id) VALUES (?, ?)")) {

            stmt.setString(1, entity.getMessage());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long notificationId = generatedKeys.getLong(1);
                    stmt2.setLong(2, notificationId);
                    for (User recipient : recipients) {
                        stmt2.setLong(1, recipient.getId());
                        stmt2.addBatch();
                    }
                    stmt2.executeBatch();
                }
                else{
                    log.error("No ID generated for notification with message: {}", entity.getMessage());
                    throw new NoDataFoundInDatabaseException("No notification id generated");
                }
            }

        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and saving notification with message {}, with exception: {}", entity.getMessage(), e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and saving notification with message " + entity.getMessage());
        }
    }

    /**
     * Marks the notification as read for the user
     * @param userId - id of the user
     * @param notificationId - id of the notification
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    public void markNotificationAsRead(Long userId, Long notificationId) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("UPDATE users_notifications SET is_read = TRUE, read_date = CURRENT_TIMESTAMP WHERE user_id = ? AND notification_id = ?")) {
            stmt.setLong(1, userId);
            stmt.setLong(2, notificationId);
            stmt.executeUpdate();
        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and marking notification with id {} as read for user with id {}, with exception: {}", notificationId, userId, e.getMessage());
            throw new ConnectionToDatabaseException("Please try again.");
        }
    }

    /**
     * Finds all notifications for the employee
     * @param userId - id of the employee
     * @return - set of notifications for the employee
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    public Set<Notification> findAllForEmployee(Long userId) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("SELECT n.id,n.message, n.sent_date FROM notification n JOIN users_notifications un ON n.id = un.notification_id WHERE un.user_id = ? AND un.is_read = FALSE")) {
            stmt.setLong(1, userId);
            return getNotifications(stmt);
        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and finding all notifications for user with id {}, with exception: {}", userId, e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and finding all notifications");
        }
    }

    /**
     * Builds notifications from the result set
     * @param stmt - prepared statement
     * @return - hashset of notifications
     * @throws SQLException - If there is an error while reading the result set
     */
    private HashSet<Notification> getNotifications(PreparedStatement stmt) throws SQLException {
        ResultSet rS = stmt.executeQuery();
        HashSet<Notification> notifications = new HashSet<>();
        while (rS.next()) {
            notifications.add(new Notification(
                    rS.getLong(1),
                    rS.getString(2),
                    rS.getTimestamp(3).toLocalDateTime()));
        }
        return notifications;
    }
}
