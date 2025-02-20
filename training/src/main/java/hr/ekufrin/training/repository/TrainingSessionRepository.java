package hr.ekufrin.training.repository;

import hr.ekufrin.training.enums.SessionStatus;
import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.exception.NoDataFoundInDatabaseException;
import hr.ekufrin.training.generics.Repository;
import hr.ekufrin.training.model.User;
import hr.ekufrin.training.model.SessionManager;
import hr.ekufrin.training.model.TrainingSession;

import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static hr.ekufrin.training.HelloApplication.log;

/**
 * Repository for the TrainingSession entity
 */
public class TrainingSessionRepository extends Repository<TrainingSession> {
    /**
     * Saves the training session to the database
     * @param entity - entity to save
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    @Override
    public void save(TrainingSession entity) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO training_session (name, date, duration, trainer_id, max_participants, location) " +
                             "VALUES (?, ?, ?, ?, ?, ?)")) {

            stmt.setString(1, entity.getName());
            stmt.setTimestamp(2, Timestamp.valueOf(entity.getDateTime()));
            stmt.setInt(3, entity.getDuration());
            stmt.setLong(4, entity.getTrainer().getId());
            stmt.setInt(5, entity.getMaxParticipants());
            stmt.setString(6, entity.getLocation());
            stmt.executeUpdate();

        } catch (SQLException | IOException e) {
            log.error(DatabaseRepository.ERROR_CONNECTING_TO_DATABASE + "and saving training session {}, with exception: {}", entity.getName(), e.getMessage());
            throw new ConnectionToDatabaseException(DatabaseRepository.ERROR_CONNECTING_TO_DATABASE + "and saving training session " + entity.getName());
        }
    }

    /**
     * Deletes the training session from the database
     * @param entity - training session to delete
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    @Override
    public void delete(TrainingSession entity) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM training_session WHERE id = ?");
             PreparedStatement stmt2 = conn.prepareStatement("DELETE FROM users_sessions WHERE session_id = ?")) {
            stmt.setLong(1, entity.getId());
            stmt2.setLong(1, entity.getId());
            stmt2.executeUpdate();
            stmt.executeUpdate();
        } catch (SQLException | IOException e) {
            log.error(DatabaseRepository.ERROR_CONNECTING_TO_DATABASE + "and deleting training session {}, with exception: {}", entity.getName(), e.getMessage());
            throw new ConnectionToDatabaseException(DatabaseRepository.ERROR_CONNECTING_TO_DATABASE + "and deleting training session " + entity.getName());
        }
    }

    /**
     * Finds a training session by its id
     * @param id - id of training session to find
     * @return - training session with the given id
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     * @throws NoDataFoundInDatabaseException - If there is no training session with the given id
     */
    @Override
    public TrainingSession findById(Long id) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("SELECT id,name,date,duration,trainer_id,max_participants,location,status FROM training_session WHERE id = ?");
             PreparedStatement stmt2 = conn.prepareStatement("SELECT user_id FROM users_sessions WHERE session_id = ?")) {
            stmt.setLong(1, id);
            stmt2.setLong(1, id);
            ResultSet rS = stmt.executeQuery();
            ResultSet rS2 = stmt2.executeQuery();

            Set<User> participants = new HashSet<>();
            while (rS2.next()) {
                participants.add(new UserRepository().findById(rS2.getLong("user_id")));
            }

            if (rS.next()) {
                return new TrainingSession(rS.getLong("id"),
                        rS.getString("name"),
                        rS.getTimestamp("date").toLocalDateTime(),
                        rS.getInt("duration"),
                        new UserRepository().findById(rS.getLong("trainer_id")),
                        rS.getInt("max_participants"),
                        rS.getString("location"),
                        SessionStatus.valueOf(rS.getString("status")),
                        participants);
            } else {
                log.info("No training session found with id {}", id);
                throw new NoDataFoundInDatabaseException("No training session found with id " + id);
            }
        } catch (SQLException | IOException e) {
            log.error(DatabaseRepository.ERROR_CONNECTING_TO_DATABASE + "and finding training session with id {}, with exception: {}", id, e.getMessage());
            throw new ConnectionToDatabaseException(DatabaseRepository.ERROR_CONNECTING_TO_DATABASE + "and finding training session with id " + id);
        }
    }

    /**
     * Finds all training sessions in the database
     * @return - hashset of all training sessions
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    @Override
    public HashSet<TrainingSession> findAll() throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("SELECT id,name,date,duration,trainer_id,max_participants,location,status FROM training_session WHERE trainer_id=?")) {
            stmt.setLong(1, SessionManager.INSTANCE.getuserId());
            ResultSet rS = stmt.executeQuery();
            return (HashSet<TrainingSession>) getTrainingSessions(rS);
        } catch (SQLException | IOException e) {
            log.error(DatabaseRepository.ERROR_CONNECTING_TO_DATABASE + "and finding all training sessions, with exception: {}", e.getMessage());
            throw new ConnectionToDatabaseException(DatabaseRepository.ERROR_CONNECTING_TO_DATABASE + "and finding all training sessions");
        }
    }

    /**
     * Finds all active training sessions in the database
     * @return - set of all active training sessions
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    public Set<TrainingSession> findAllActiveSessions() throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             Statement stmt = conn.createStatement()) {
            ResultSet rS = stmt.executeQuery("SELECT id,name,date,duration,trainer_id,max_participants,location,status FROM training_session WHERE status = 'SCHEDULED' OR status = 'FULL'");
            return getTrainingSessions(rS);
        } catch (SQLException | IOException e) {
            log.error(DatabaseRepository.ERROR_CONNECTING_TO_DATABASE + "and finding all ACTIVE training sessions, with exception: {}", e.getMessage());
            throw new ConnectionToDatabaseException(DatabaseRepository.ERROR_CONNECTING_TO_DATABASE + "and finding all ACTIVE training sessions");
        }
    }

    /**
     * Builds training sessions from the result set
     * @param rS - result set to build training sessions from
     * @return - set of training sessions
     * @throws SQLException - If there is an error while reading the result set
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    public Set<TrainingSession> getTrainingSessions(ResultSet rS) throws SQLException, ConnectionToDatabaseException {
        Set<TrainingSession> trainingSessions = new HashSet<>();
        UserRepository userRepository = new UserRepository();

        while (rS.next()) {
            Set<User> participants = new HashSet<>();
            User trainer;
            try (Connection conn = DatabaseRepository.connectToDB();
                 PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM users_sessions WHERE session_id = ?")) {
                stmt.setLong(1, rS.getLong("id"));
                ResultSet rS2 = stmt.executeQuery();
                while (rS2.next()) {
                    participants.add(userRepository.findById(rS2.getLong("user_id")));
                }
                trainer = userRepository.findById(rS.getLong("trainer_id"));
            } catch (ConnectionToDatabaseException e) {
                log.error("Error while loading trainer for training session with id {}", rS.getLong("id"));
                break;
            } catch (IOException e) {
                log.error(DatabaseRepository.ERROR_CONNECTING_TO_DATABASE + "and getting training sessions with participants, with exception: {}", e.getMessage());
                throw new ConnectionToDatabaseException(DatabaseRepository.ERROR_CONNECTING_TO_DATABASE + "and getting training sessions with participants");
            }
            TrainingSession tempSession = new TrainingSession(rS.getLong("id"),
                    rS.getString("name"),
                    rS.getTimestamp("date").toLocalDateTime(),
                    rS.getInt("duration"),
                    trainer,
                    rS.getInt("max_participants"),
                    rS.getString("location"),
                    SessionStatus.valueOf(rS.getString("status")),
                    participants);
            tempSession.sessionStatus();
            trainingSessions.add(tempSession);
        }
        return trainingSessions;
    }

    /**
     * Updates the training session in the database
     * @param entity - training session to update
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    public void update(Optional<TrainingSession> entity) throws ConnectionToDatabaseException {
        String name = null;
        if (entity.isEmpty()) {
            return;
        }

        TrainingSession session = entity.get();
        name = session.getName();

        try (Connection conn = DatabaseRepository.connectToDB()) {
            conn.setAutoCommit(false);

            Integer currentParticipantsInDB = 0;
            try (PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) FROM users_sessions WHERE session_id = ?")) {
                countStmt.setLong(1, session.getId());
                ResultSet rs = countStmt.executeQuery();
                if (rs.next()) {
                    currentParticipantsInDB = rs.getInt(1);
                }
            }

            if (session.getParticipants().size() > currentParticipantsInDB) {
                try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO users_sessions (user_id, session_id) VALUES (?, ?) ON CONFLICT DO NOTHING")) {
                    for (User participant : session.getParticipants()) {
                        insertStmt.setLong(1, participant.getId());
                        insertStmt.setLong(2, session.getId());
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE training_session SET name = ?, date = ?, duration = ?, trainer_id = ?, max_participants = ?, location = ?, status = ? WHERE id = ?")) {
                stmt.setString(1, session.getName());
                stmt.setTimestamp(2, Timestamp.valueOf(session.getDateTime()));
                stmt.setInt(3, session.getDuration());
                stmt.setLong(4, session.getTrainer().getId());
                stmt.setInt(5, session.getMaxParticipants());
                stmt.setString(6, session.getLocation());
                stmt.setObject(7, session.getStatus(), java.sql.Types.OTHER);
                stmt.setLong(8, session.getId());
                stmt.executeUpdate();
            }

            conn.commit();

        } catch (SQLException | IOException e) {
            log.error(DatabaseRepository.ERROR_CONNECTING_TO_DATABASE + "and updating training session {}, with exception: {}", name, e.getMessage());
            throw new ConnectionToDatabaseException(DatabaseRepository.ERROR_CONNECTING_TO_DATABASE + " and updating training session " + name);
        }
    }


}
