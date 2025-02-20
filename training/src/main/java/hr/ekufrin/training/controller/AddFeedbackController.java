package hr.ekufrin.training.controller;

import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.model.Feedback;
import hr.ekufrin.training.model.Log;
import hr.ekufrin.training.model.SessionManager;
import hr.ekufrin.training.model.TrainingSession;
import hr.ekufrin.training.repository.UserRepository;
import hr.ekufrin.training.repository.FeedbackRepository;
import hr.ekufrin.training.repository.LogRepository;
import hr.ekufrin.training.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * JavaFX controller for adding feedback to a training session.
 */
public class AddFeedbackController {
    @FXML
    TextField sessionNameTextField;
    @FXML
    Slider rateSlider;
    @FXML
    TextField commentTextField;

    TrainingSession session;
    FeedbackRepository feedbackRepository = new FeedbackRepository();



    public void initialize() {
        if(session != null){
            sessionNameTextField.setText(session.getName());
        }
    }

    /**
     * Adds feedback to the training session.
     */
    public void addFeedback(){
        Double rate = rateSlider.getValue();
        String comment = commentTextField.getText();

        Alert confirmationAlert = AlertUtil.showConfirmationAlert("Adding feedback",
                "Are you sure you want to add this feedback?",
                "Session name: " + session.getName() + "\n" +
                            "Rate: " + rate + "\n" +
                            "Comment: " + comment + "\n");
        confirmationAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try{
                Feedback feedback = new Feedback.Builder()
                        .trainingSession(session)
                        .user(new UserRepository().findById(SessionManager.INSTANCE.getuserId()))
                        .rating(rate.intValue())
                        .comment(comment)
                        .timestamp(LocalDateTime.now())
                        .build();
                feedbackRepository.save(feedback);
            }catch (ConnectionToDatabaseException e){
                AlertUtil.showErrorAlert("Error while saving feedback", e.getMessage());
            }

            commentTextField.clear();

            AlertUtil.showInfoAlert("Feedback added", "Feedback for session: " + session.getName() +" has been successfully added!");

            Log newFeedbackLog = new Log.Builder()
                    .setUserId(SessionManager.INSTANCE.getuserId())
                    .setOperation("Added new feedback")
                    .setTimestamp(LocalDateTime.now())
                    .setChangedField("Feedback added for: " + session.getName())
                    .setNewValue("With rating: " + rate)
                    .build();

            LogRepository logRepository = new LogRepository();
            try {
                logRepository.save(newFeedbackLog);
            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while saving log", e.getMessage());
            }
        }

    }
    /**
     * Sets the training session for which the feedback is added.
     * @param session training session
     */
    public void setSession(TrainingSession session) {
        this.session = session;

        if(session != null){
            sessionNameTextField.setText(session.getName());
        }
    }
}
