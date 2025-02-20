package hr.ekufrin.training.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a log entity.
 */
public class Log extends IdentificationEntity implements Serializable {
    LocalDateTime timestamp;
    Long userId;
    String changedField;
    String oldValue;
    String newValue;
    String operation;

    protected Log() {
    }

    /**
     * Log constructor
     * @param id - unique identifier
     * @param timestamp - time of the log
     * @param userId - user id
     * @param changedField - field that was changed
     * @param oldValue - old value
     * @param newValue - new value
     * @param operation - operation that was performed
     */
    public Log(Long id, LocalDateTime timestamp, Long userId, String changedField, String oldValue, String newValue, String operation) {
        super(id);
        this.timestamp = timestamp;
        this.userId = userId;
        this.changedField = changedField;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.operation = operation;
    }

    public static class Builder {
        private Long id;
        private LocalDateTime timestamp;
        private Long userId;
        private String changedField;
        private String oldValue;
        private String newValue;
        private String operation;

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Builder setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder setUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder setChangedField(String changedField) {
            this.changedField = changedField;
            return this;
        }

        public Builder setOldValue(String oldValue) {
            this.oldValue = oldValue;
            return this;
        }

        public Builder setNewValue(String newValue) {
            this.newValue = newValue;
            return this;
        }

        public Builder setOperation(String operation) {
            this.operation = operation;
            return this;
        }

        public Log build() {
            return new Log(id, timestamp, userId, changedField, oldValue, newValue, operation);
        }
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getChangedField() {
        return changedField;
    }

    public void setChangedField(String changedField) {
        this.changedField = changedField;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Log log = (Log) o;
        return Objects.equals(timestamp, log.timestamp) && Objects.equals(userId, log.userId) && Objects.equals(changedField, log.changedField) && Objects.equals(oldValue, log.oldValue) && Objects.equals(newValue, log.newValue) && Objects.equals(operation, log.operation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), timestamp, userId, changedField, oldValue, newValue, operation);
    }
}
