package hr.ekufrin.training.util;

import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.model.Log;
import hr.ekufrin.training.model.SessionManager;
import hr.ekufrin.training.model.TrainingSession;
import hr.ekufrin.training.model.User;
import hr.ekufrin.training.repository.LogRepository;

import java.time.LocalDateTime;

import static hr.ekufrin.training.HelloApplication.log;

/**
 * Utility class for saving logs
 */
public class LogUtil {
    private static final LogRepository logRepository = new LogRepository();
    private static final String UPDATED_USER = "Updated user ";
    private static final String UPDATED_SESSION = "Updated session ";

    private LogUtil() {}

    /**
     * Saves the changes made to the user
     * @param oldUser - old user details
     * @param newUser - new changed user details
     */
    public static void saveChangesForUser(User oldUser, User newUser) {
        if (!oldUser.getFirstName().equalsIgnoreCase(newUser.getFirstName())) {
            try {
                Log updatedUserLog = createLog(UPDATED_USER + oldUser.toString(), "First name", oldUser.getFirstName(), newUser.getFirstName());
                logRepository.save(updatedUserLog);
            } catch (ConnectionToDatabaseException e) {
                log.error("Error while saving log with first name changed, error:", e);
            }
        }
        if (!oldUser.getLastName().equalsIgnoreCase(newUser.getLastName())) {
            try {
                Log updatedUserLog = createLog(UPDATED_USER + oldUser.toString(), "Last name", oldUser.getLastName(), newUser.getLastName());
                logRepository.save(updatedUserLog);
            } catch (ConnectionToDatabaseException e) {
                log.error("Error while saving log with last name changed, error:", e);
            }
        }
        if (!oldUser.getEmail().equalsIgnoreCase(newUser.getEmail())) {
            try {
                Log updatedUserLog = createLog(UPDATED_USER + oldUser.toString(), "Email", oldUser.getEmail(), newUser.getEmail());
                logRepository.save(updatedUserLog);
            } catch (ConnectionToDatabaseException e) {
                log.error("Error while saving log with email changed, error:", e);
            }
        }
        if(!oldUser.getRole().equals(newUser.getRole())){
            try {
                Log updatedUserLog = createLog(UPDATED_USER + oldUser.toString(), "Role", oldUser.getRole().getName(), newUser.getRole().getName());
                logRepository.save(updatedUserLog);
            } catch (ConnectionToDatabaseException e) {
                log.error("Error while saving log with role changed, error:", e);
            }
        }
        if (!oldUser.getPassword().equalsIgnoreCase(newUser.getPassword())) {
            try {
                Log updatedUserLog = createLog(UPDATED_USER + oldUser.toString(), "Password", "****", "****");
                logRepository.save(updatedUserLog);
            } catch (ConnectionToDatabaseException e) {
                log.error("Error while saving log with password changed, error:", e);
            }

        }
    }

    /**
     * Saves the changes made to the training session
     * @param oldSession - old session details
     * @param newSession - new changed session details
     */
    public static void saveChangesForTrainingSession(TrainingSession oldSession, TrainingSession newSession){
        if (!oldSession.getName().equalsIgnoreCase(newSession.getName())) {
            try {
                Log updatedSessionLog = createLog(UPDATED_SESSION + oldSession.getName(), "Session name", oldSession.getName(), newSession.getName());
                logRepository.save(updatedSessionLog);
            } catch (ConnectionToDatabaseException e) {
                log.error("Error while saving log with session name changed, error:", e);
            }
        }
        if (!oldSession.getLocation().equalsIgnoreCase(newSession.getLocation())) {
            try {
                Log updatedSessionLog = createLog(UPDATED_SESSION + oldSession.getName(), "Location", oldSession.getLocation(), newSession.getLocation());
                logRepository.save(updatedSessionLog);
            } catch (ConnectionToDatabaseException e) {
                log.error("Error while saving log with session location changed, error:", e);
            }
        }
        if (!oldSession.getDateTime().equals(newSession.getDateTime())) {
            try {
                Log updatedSessionLog = createLog(UPDATED_SESSION + oldSession.getName(), "Date and time", oldSession.displayDateTime(), newSession.displayDateTime());
                logRepository.save(updatedSessionLog);
            } catch (ConnectionToDatabaseException e) {
                log.error("Error while saving log with session date and time changed, error:", e);
            }
        }
        if (!oldSession.getDuration().equals(newSession.getDuration())) {
            try {
                Log updatedSessionLog = createLog(UPDATED_SESSION + oldSession.getName(), "Duration", oldSession.getDuration().toString(), newSession.getDuration().toString());
                logRepository.save(updatedSessionLog);
            } catch (ConnectionToDatabaseException e) {
                log.error("Error while saving log with session duration changed, error:", e);
            }
        }
        if (!oldSession.getMaxParticipants().equals(newSession.getMaxParticipants())) {
            try {
                Log updatedSessionLog = createLog(UPDATED_SESSION + oldSession.getName(), "Max participants", oldSession.getMaxParticipants().toString(), newSession.getMaxParticipants().toString());
                logRepository.save(updatedSessionLog);
            } catch (ConnectionToDatabaseException e) {
                log.error("Error while saving log with session max participants changed, error:", e);
            }
        }
    }

    /**
     * Creates a log object
     * @param operation - operation that was performed
     * @param changedField - field that was changed
     * @param oldValue - old value
     * @param newValue - new value
     * @return - log object
     */
    private static Log createLog(String operation, String changedField, String oldValue, String newValue) {
            return new Log.Builder()
                    .setUserId(SessionManager.INSTANCE.getuserId())
                    .setOperation(operation)
                    .setTimestamp(LocalDateTime.now())
                    .setChangedField(changedField)
                    .setOldValue(oldValue)
                    .setNewValue(newValue)
                    .build();
    }
}
