package hr.ekufrin.training.repository;

import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.exception.SessionJoiningLeavingException;
import hr.ekufrin.training.generics.Registration;
import hr.ekufrin.training.model.User;
import hr.ekufrin.training.model.TrainingSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static hr.ekufrin.training.HelloApplication.log;
import static hr.ekufrin.training.repository.DatabaseRepository.ERROR_CONNECTING_TO_DATABASE;

/**
 * Repository for the Registration entity
 */
public class RegistrationRepository {
    /**
     * Finds a registration by user and session
     * @param user - user to find registration for
     * @param session - session to find registration for
     * @return - registration for the given user and session
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    public Optional<Registration<TrainingSession, User>> findRegistration(User user, TrainingSession session) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("SELECT CTID FROM users_sessions WHERE user_id = ? AND session_id = ?")) {

            stmt.setLong(1, user.getId());
            stmt.setLong(2, session.getId());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new Registration<>(session, user));
            }
            return Optional.empty();
        } catch (SQLException | IOException e) {
            log.error("Error finding registration for user {} and session {}, error: {}", user, session.getId(), e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and finding registration for user " + user.toString() + " and session " + session.getId());
        }
    }

    /**
     * Joins a user to a session
     * @param user - user to join
     * @param session - session to join
     * @return - true if the user joined the session, false otherwise
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     * @throws SessionJoiningLeavingException - If the user is already registered in the session
     */
    public Boolean joinSession(User user, TrainingSession session) throws ConnectionToDatabaseException {
        if (findRegistration(user, session).isPresent()) {
            log.error("User {} already joined session {}", user, session.getId());
            throw new SessionJoiningLeavingException("User is already registered in this session.");
        }

        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO users_sessions (user_id, session_id) VALUES (?, ?)")) {

            stmt.setLong(1, user.getId());
            stmt.setLong(2, session.getId());

            if (stmt.executeUpdate() > 0) {
                session.getParticipants().add(user);
                return true;
            } else {
                return false;
            }
        } catch (SQLException | IOException e) {
            log.error("Error joining session for user {} in session {}: {}", user, session.getId(), e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and joining session for user " + user.toString() + " in session " + session.getId());
        }
    }

    /**
     * Leaves a session for a user
     * @param user - user to leave the session
     * @param session - session to leave
     * @return - true if the user left the session, false otherwise
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     * @throws SessionJoiningLeavingException - If the user is not registered in the session
     */
    public Boolean leaveSession(User user, TrainingSession session) throws ConnectionToDatabaseException {
        Optional<Registration<TrainingSession, User>> registration = findRegistration(user, session);
        if (registration.isEmpty()) {
            log.error("User {} is not registered in session {}", user, session.getId());
            throw new SessionJoiningLeavingException("User is not registered in this session.");
        }

        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users_sessions WHERE user_id = ? AND session_id = ?")) {

            stmt.setLong(1, user.getId());
            stmt.setLong(2, session.getId());

            if (stmt.executeUpdate() > 0) {
                session.getParticipants().remove(user);
                return true;
            } else {
                return false;
            }
        } catch (SQLException | IOException e) {
            log.error("Error leaving session for user {} in session {}: {}", user, session.getId(), e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and leaving session for user " + user.toString() + " in session " + session.getId());
        }
    }
}
