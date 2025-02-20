package hr.ekufrin.training.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a notification that can be sent to the user.
 */
public class Notification extends IdentificationEntity{
    private String message;
    private LocalDateTime timestamp;

    /**
     * Constructs a new Notification with the given id, message and timestamp.
     * @param id - unique identifier of the notification
     * @param message - message of the notification
     * @param timestamp - timestamp of the notification
     */
    public Notification(Long id, String message, LocalDateTime timestamp) {
        super(id);
        this.message = message;
        this.timestamp = timestamp;
    }

    public static class Builder{
        private Long id;
        private String message;
        private LocalDateTime timestamp;

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }


        public Builder setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Notification build(){
            return new Notification(id, message, timestamp);
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Notification that = (Notification) o;
        return Objects.equals(message, that.message) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), message, timestamp);
    }
}
