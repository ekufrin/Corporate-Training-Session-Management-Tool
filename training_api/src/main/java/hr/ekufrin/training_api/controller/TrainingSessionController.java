package hr.ekufrin.training_api.controller;

import hr.ekufrin.training_api.model.TrainingSession;
import hr.ekufrin.training_api.service.TrainingSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/training-session")
@RequiredArgsConstructor
public class TrainingSessionController {
    private final TrainingSessionService trainingSessionService;

    @GetMapping("/active")
    public ResponseEntity<List<TrainingSession>> getActiveTrainingSessions() {
        return ResponseEntity.ok(trainingSessionService.getActiveTrainingSessions());
    }
}
