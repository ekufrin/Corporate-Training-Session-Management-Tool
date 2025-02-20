package hr.ekufrin.training.exception;

/**
 * Exception thrown when user tries to join or leave session that is not in the correct state.
 */
public class SessionJoiningLeavingException extends RuntimeException {
    public SessionJoiningLeavingException(String message) {
        super(message);
    }

    public SessionJoiningLeavingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionJoiningLeavingException(Throwable cause) {
        super(cause);
    }

    public SessionJoiningLeavingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SessionJoiningLeavingException() {
    }
}
