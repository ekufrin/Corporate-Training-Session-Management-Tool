package hr.ekufrin.training.exception;

/**
 * Exception thrown when no data is found in the database.
 */
public class NoDataFoundInDatabaseException extends RuntimeException {
    public NoDataFoundInDatabaseException(String message) {
        super(message);
    }

    public NoDataFoundInDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoDataFoundInDatabaseException(Throwable cause) {
        super(cause);
    }

    public NoDataFoundInDatabaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NoDataFoundInDatabaseException() {
    }
}
