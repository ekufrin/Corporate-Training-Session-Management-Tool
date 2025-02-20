package hr.ekufrin.training.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a session.
 */
public abstract class Session extends IdentificationEntity {
    String name;
    LocalDateTime dateTime;
    Integer duration;

    /**
     * Constructs a new Session with the given id, name, dateTime and duration.
     * @param id - unique identifier of the session
     * @param name - name of the session
     * @param dateTime - date and time of the session
     * @param duration - duration in minutes of the session
     */
    protected Session(Long id, String name, LocalDateTime dateTime, Integer duration) {
        super(id);
        this.name = name;
        this.dateTime = dateTime;
        this.duration = duration;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Session session = (Session) o;
        return Objects.equals(name, session.name) && Objects.equals(dateTime, session.dateTime) && Objects.equals(duration, session.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, dateTime, duration);
    }
}
