package hr.ekufrin.training.exception;

/**
 * Exception thrown when connection to database fails.
 */
public class ConnectionToDatabaseException extends Exception {
    public ConnectionToDatabaseException(String message) {
        super(message);
    }

    public ConnectionToDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionToDatabaseException(Throwable cause) {
        super(cause);
    }

    public ConnectionToDatabaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ConnectionToDatabaseException() {
    }
}
