package hr.ekufrin.training_api.repository;

import hr.ekufrin.training_api.enums.SessionStatus;
import hr.ekufrin.training_api.model.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
    List<TrainingSession> findByStatus(SessionStatus status);
}
