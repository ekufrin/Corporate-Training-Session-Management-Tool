package hr.ekufrin.training.repository;

import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.exception.NoDataFoundInDatabaseException;
import hr.ekufrin.training.generics.Repository;
import hr.ekufrin.training.model.Feedback;
import hr.ekufrin.training.model.PositiveFeedback;
import hr.ekufrin.training.model.TrainingSession;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static hr.ekufrin.training.HelloApplication.log;
import static hr.ekufrin.training.repository.DatabaseRepository.ERROR_CONNECTING_TO_DATABASE;

/**
 * Repository for feedbacks
 */
public class FeedbackRepository extends Repository<Feedback> {
    /**
     * Saves feedback to the database
     * @param entity - entity to save
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    @Override
    public void save(Feedback entity) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO feedback(session_id,user_id,rating,comment,timestamp) VALUES (?,?,?,?,?)")) {

            stmt.setLong(1, entity.trainingSession().getId());
            stmt.setLong(2, entity.employee().getId());
            stmt.setInt(3, entity.rating());
            stmt.setString(4, entity.comment());
            stmt.setTimestamp(5, Timestamp.valueOf(entity.timestamp()));
            stmt.executeUpdate();

        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and saving feedback {} for trainer {}, with exception: {}", entity.comment(), entity.employee().toString(), e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and saving feedback " + entity.comment() + " for trainer " + entity.employee().toString());
        }
    }

    /**
     * Deletes feedback from the database
     * @param entity - entity to delete
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    @Override
    public void delete(Feedback entity) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM feedback WHERE id = ?")) {

            stmt.setLong(1, entity.id());
            stmt.executeUpdate();

        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and deleting feedback {} for trainer {}, with exception: {}", entity.comment(), entity.employee().toString(), e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and deleting feedback " + entity.comment() + " for trainer " + entity.employee().toString());
        }
    }

    /**
     * Fetch feedback from the database by id
     * @param id - id of entity
     * @return - feedback with the given id
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     * @throws NoDataFoundInDatabaseException - If there is no feedback with the given id
     */
    @Override
    public Feedback findById(Long id) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("SELECT id,session_id,user_id,rating,comment,timestamp FROM feedback WHERE id = ?")) {

            stmt.setLong(1, id);
            ResultSet rS = stmt.executeQuery();
            if (rS.next()) {
                return new Feedback.Builder()
                        .id(rS.getLong("id"))
                        .trainingSession(new TrainingSessionRepository().findById(rS.getLong("session_id")))
                        .user(new UserRepository().findById(rS.getLong("user_id")))
                        .rating(rS.getInt("rating"))
                        .comment(rS.getString("comment"))
                        .timestamp(rS.getTimestamp("timestamp").toLocalDateTime())
                        .build();
            } else {
                log.info("No feedback found with id {}", id);
                throw new NoDataFoundInDatabaseException("No feedback found with id " + id);
            }

        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and finding feedback with id {}, with exception: {}", id, e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and finding feedback with id" + id);
        }
    }

    /**
     * Fetch all feedbacks from the database
     * @return - hashset of all feedbacks
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    @Override
    public HashSet<Feedback> findAll() throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             Statement stmt = conn.createStatement()) {

            ResultSet rS = stmt.executeQuery("SELECT id,session_id,user_id,rating,comment,timestamp FROM feedback");
            return (HashSet<Feedback>) getFeedbacks(rS);

        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and finding all feedbacks, with exception: {}", e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and finding all feedbacks");
        }
    }

    /**
     * Fetch all feedbacks from the database given by the user
     * @param userId - id of the user
     * @return - set of all feedbacks given by the user
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    public Set<Feedback> findAllFinishedFeedbackByuserId(Long userId) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("SELECT id,session_id,user_id,rating,comment,timestamp FROM feedback WHERE user_id = ?")) {

            stmt.setLong(1, userId);
            ResultSet rS = stmt.executeQuery();
            return getFeedbacks(rS);

        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and finding all feedbacks for user with id {}, with exception: {}", userId, e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and finding all feedbacks for user with id " + userId);
        }
    }

    /**
     * Fetch all training sessions from database that are finished and the user has not given feedback for
     * @param userId - id of the user
     * @return - set of all training sessions that are finished and the user has not given feedback for
     * @throws ConnectionToDatabaseException
     */
    public Set<TrainingSession> findAllWaitingFeedbackByuserId(Long userId) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("SELECT id,name,date,duration,trainer_id,max_participants,location,status FROM training_session WHERE status = 'FINISHED' AND NOT EXISTS (SELECT * FROM feedback WHERE session_id = training_session.id AND user_id = ?)")) {
            stmt.setLong(1, userId);
            ResultSet rS = stmt.executeQuery();
            HashSet<TrainingSession> trainingSessions = (HashSet<TrainingSession>) new TrainingSessionRepository().getTrainingSessions(rS);
            return trainingSessions.stream()
                    .filter(trainingSession -> trainingSession.getParticipants() != null)
                    .filter(trainingSession -> trainingSession.getParticipants().stream()
                            .anyMatch(participant -> participant != null && Objects.equals(participant.getId(), userId)))
                    .collect(Collectors.toSet());

        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and finding all waiting feedbacks for user with id {}, with exception: {}", userId, e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and finding all waiting feedbacks for user with id " + userId);

        }

    }

    /**
     * Fetch all feedbacks from the database for trainer
     * @param trainerId - id of the trainer
     * @return - set of all feedbacks for the trainer
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    public Set<Feedback> findAllFinishedFeedbackByTrainerId(Long trainerId) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("SELECT id,session_id,user_id,rating,comment,timestamp FROM feedback WHERE session_id IN (SELECT id FROM training_session WHERE trainer_id = ?)")) {

            stmt.setLong(1, trainerId);
            ResultSet rS = stmt.executeQuery();
            return getFeedbacks(rS);

        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and finding all feedbacks for trainer with id {}, with exception: {}", trainerId, e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and finding all feedbacks for trainer with id " + trainerId);
        }
    }

    /**
     * Builds feedbacks from the result set
     * @param rS - result set
     * @return - set of feedbacks
     * @throws SQLException - If there is an error while fetching data from the result set
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    private Set<Feedback> getFeedbacks(ResultSet rS) throws SQLException, ConnectionToDatabaseException {
        HashSet<Feedback> feedbacks = new HashSet<>();
        while (rS.next()) {
            feedbacks.add(new Feedback.Builder()
                    .id(rS.getLong("id"))
                    .trainingSession(new TrainingSessionRepository().findById(rS.getLong("session_id")))
                    .user(new UserRepository().findById(rS.getLong("user_id")))
                    .rating(rS.getInt("rating"))
                    .comment(rS.getString("comment"))
                    .timestamp(rS.getTimestamp("timestamp").toLocalDateTime())
                    .build());
        }
        return feedbacks;
    }

    /**
     * Saves feedbacks to text file
     * @param feedbacks - set of feedbacks
     * @return - true if feedbacks are saved to file, false otherwise
     */
    public boolean saveFeedbacksToFile(Set<Feedback> feedbacks) {
        if (feedbacks.isEmpty()) {
            log.error("No feedbacks to save to file");
            return false;
        }
        try (PrintWriter writer = new PrintWriter("dat/feedbacks.txt")) {
            Map<String, Long> feedbacksByTrainingSession = feedbacks.stream()
                    .collect(Collectors.groupingBy(feedback -> feedback.feedbackType() instanceof PositiveFeedback ? "Positive" : "Negative", Collectors.counting()));
            writer.println("#################################################");
            writer.println("Feedbacks statistics:");
            writer.println("Positive feedbacks: " + feedbacksByTrainingSession.getOrDefault("Positive",0L));
            writer.println("Negative feedbacks: " + feedbacksByTrainingSession.getOrDefault("Negative",0L));
            writer.println("#################################################");
            feedbacks.stream()
                    .sorted(Comparator.comparing(feedback -> !(feedback.feedbackType() instanceof PositiveFeedback)))
                    .forEach(feedback -> {
                        String comment = feedback.comment().isEmpty() ? "### No comment given ###" : feedback.comment();
                        writer.println("Feedback id: " + feedback.id());
                        writer.println("Training session: " + feedback.trainingSession().getName());
                        writer.println("Training session date & location : " + feedback.trainingSession().displayDateTime() + " @ " + feedback.trainingSession().getLocation());
                        writer.println("Rating (1-5): " + feedback.rating());
                        writer.println("Feedback type: " + feedback.feedbackType().getFeedback());
                        writer.println("Comment: " + comment);
                        writer.println("Feedback given @ " + feedback.timestamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm")));
                        writer.println("-------------------------------------------------");
                    });
            return true;
        } catch (FileNotFoundException e) {
            log.error("Error while saving feedbacks to file, error: {}", e.getMessage());
            return false;
        }
    }
}
