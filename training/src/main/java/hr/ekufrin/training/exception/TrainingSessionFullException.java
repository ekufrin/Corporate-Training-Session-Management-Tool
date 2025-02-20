package hr.ekufrin.training.exception;

/**
 * Exception thrown when user tries to join a training session that is already full.
 */
public class TrainingSessionFullException extends Exception {
    public TrainingSessionFullException(String message) {
        super(message);
    }

    public TrainingSessionFullException(String message, Throwable cause) {
        super(message, cause);
    }

    public TrainingSessionFullException(Throwable cause) {
        super(cause);
    }

    public TrainingSessionFullException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public TrainingSessionFullException() {
    }
}
