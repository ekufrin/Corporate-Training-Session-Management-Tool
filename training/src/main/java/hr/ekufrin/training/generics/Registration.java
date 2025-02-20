package hr.ekufrin.training.generics;

import hr.ekufrin.training.model.User;
import hr.ekufrin.training.model.TrainingSession;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a registration of a user to a training session.
 * @param <T> Training session
 * @param <U> User
 */
public class Registration <T extends TrainingSession, U extends User> {
    private final T trainingSession;
    private final U user;
    private final LocalDateTime registrationDate;


    public Registration(T trainingSession, U user) {
        this.trainingSession = trainingSession;
        this.user = user;
        this.registrationDate = LocalDateTime.now();
    }

    public T getTrainingSession() {
        return trainingSession;
    }

    public U getUser() {
        return user;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Registration<?, ?> that = (Registration<?, ?>) o;
        return Objects.equals(trainingSession, that.trainingSession) && Objects.equals(user, that.user) && Objects.equals(registrationDate, that.registrationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), trainingSession, user, registrationDate);
    }

    @Override
    public String toString() {
        return "Registration{" +
                "trainingSession=" + trainingSession +
                ", user=" + user +
                ", registrationDate=" + registrationDate +
                '}';
    }
}
