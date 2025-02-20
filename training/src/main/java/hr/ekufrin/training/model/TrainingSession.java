package hr.ekufrin.training.model;

import hr.ekufrin.training.enums.SessionStatus;
import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.interfaces.Schedulable;
import hr.ekufrin.training.repository.TrainingSessionRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static hr.ekufrin.training.HelloApplication.log;

/**
 * Represents a training session.
 */
public class TrainingSession extends Session implements Schedulable {
    private User trainer;
    private Integer maxParticipants;
    private String location;
    private SessionStatus status;
    private Set<User> participants;

    /**
     * Constructs a new TrainingSession with the given id, name, dateTime, duration, trainer, maxParticipants, location, status and participants.
     * @param id - unique identifier of the training session
     * @param name - name of the training session
     * @param dateTime - date and time of the training session
     * @param duration - duration in minutes of the training session
     * @param trainer - trainer of the training session
     * @param maxParticipants - maximum number of participants of the training session
     * @param location - location of the training session
     * @param status - status of the training session
     * @param participants - participants of the training session
     */
    public TrainingSession(Long id, String name, LocalDateTime dateTime, Integer duration, User trainer, Integer maxParticipants, String location, SessionStatus status, Set<User> participants) {
        super(id, name, dateTime, duration);
        this.trainer = trainer;
        this.maxParticipants = maxParticipants;
        this.location = location;
        this.status = status;
        this.participants = participants;
    }

    public static class Builder {
        private Long id;
        private String name;
        private LocalDateTime dateTime;
        private Integer duration;
        private User trainer;
        private Integer maxParticipants;
        private String location;
        private SessionStatus status;
        private Set<User> participants;

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public Builder setDuration(Integer duration) {
            this.duration = duration;
            return this;
        }

        public Builder setTrainer(User trainer) {
            this.trainer = trainer;
            return this;
        }

        public Builder setMaxParticipants(Integer maxParticipants) {
            this.maxParticipants = maxParticipants;
            return this;
        }

        public Builder setLocation(String location) {
            this.location = location;
            return this;
        }

        public Builder setStatus(SessionStatus status) {
            this.status = status;
            return this;
        }

        public Builder setParticipants(Set<User> participants) {
            this.participants = participants;
            return this;
        }


        public TrainingSession build() {
            return new TrainingSession(id, name, dateTime, duration, trainer, maxParticipants, location, status, participants);
        }

    }

    /**
     * Returns the date and time of the training session in the format "dd.MM.yyyy. HH:mm".
     * @return - date and time of the training session
     */
    public String displayDateTime() {
        return this.dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm"));
    }


    /**
     * Updates the status of the training session.
     * If the current time is after beginning of the session + duration, status is set to FINISHED.
     * If the number of participants is equal to the maximum number of participants, status is set to FULL.
     * Otherwise, status is set to SCHEDULED.
     */
    @Override
    public void sessionStatus() {
        TrainingSessionRepository trainingSessionRepository = new TrainingSessionRepository();
        LocalDateTime sessionEnd = this.dateTime.plusMinutes(this.duration);
        if (LocalDateTime.now().isAfter(sessionEnd)) {
            this.status = SessionStatus.FINISHED;
        } else if (this.participants.size() >= this.maxParticipants) {
            this.status = SessionStatus.FULL;
        } else {
            this.status = SessionStatus.SCHEDULED;
        }
        try {
            trainingSessionRepository.update(Optional.of(this));
        } catch (ConnectionToDatabaseException e) {
            log.error("Error while updating session status on Session with id {}, error {}", this.getId(), e.getMessage());
        }
    }


    public User getTrainer() {
        return trainer;
    }

    public void setTrainer(User trainer) {
        this.trainer = trainer;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }


    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TrainingSession that = (TrainingSession) o;
        return Objects.equals(trainer, that.trainer) && Objects.equals(maxParticipants, that.maxParticipants) && Objects.equals(location, that.location) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), trainer, maxParticipants, location, status);
    }
}
