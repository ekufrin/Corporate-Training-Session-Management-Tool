package hr.ekufrin.training_api.service;

import hr.ekufrin.training_api.enums.SessionStatus;
import hr.ekufrin.training_api.model.TrainingSession;
import hr.ekufrin.training_api.repository.TrainingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingSessionService {
    private final TrainingSessionRepository trainingSessionRepository;

    public List<TrainingSession> getActiveTrainingSessions() {
        return trainingSessionRepository.findByStatus(SessionStatus.SCHEDULED);
    }
}
