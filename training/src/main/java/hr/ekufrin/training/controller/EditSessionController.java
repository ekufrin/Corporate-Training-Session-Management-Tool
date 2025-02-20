package hr.ekufrin.training.controller;

import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.model.*;
import hr.ekufrin.training.repository.UserRepository;
import hr.ekufrin.training.repository.TrainingSessionRepository;
import hr.ekufrin.training.util.AlertUtil;
import hr.ekufrin.training.util.LogUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * JavaFX controller for editing a training session.
 */
public class EditSessionController {
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField durationTextField;
    @FXML
    private TextField maxParticipantsTextField;
    @FXML
    private TextField locationTextField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField timeTextField;
    @FXML
    private ComboBox<String> trainerComboBox;

    private final UserRepository userRepository = new UserRepository();
    private final TrainingSessionRepository trainingSessionRepository = new TrainingSessionRepository();
    private Boolean showOneAlert = false;
    private TrainingSession session;

    private HashSet<User> trainers = new HashSet<>();

    /**
     * Sets the session to be edited.
     * @param session - session to be edited
     */
    public void setSession(TrainingSession session) {
        if(session != null){
            this.session = session;
            nameTextField.setText(session.getName());
            durationTextField.setText(String.valueOf(session.getDuration()));
            maxParticipantsTextField.setText(String.valueOf(session.getMaxParticipants()));
            locationTextField.setText(session.getLocation());
            datePicker.setValue(session.getDateTime().toLocalDate());
            timeTextField.setText(session.getDateTime().toLocalTime().toString().substring(0,5));
            trainerComboBox.getSelectionModel().select(session.getTrainer().toString());
        }
    }

    public void initialize() {
        try{
            trainers = new HashSet<>(userRepository.findAll().stream().filter(trainer -> trainer.getRole().getName().equalsIgnoreCase("Trainer")).toList());
        } catch (ConnectionToDatabaseException e) {
            AlertUtil.showErrorAlert("Error while loading trainers", e.getMessage());
            showOneAlert = true;
        }
        trainerComboBox.getItems().addAll(trainers.stream().map(User::toString).toList());
        Platform.runLater(() -> trainerComboBox.getSelectionModel().selectFirst());
    }

    /**
     * Checks if the input is valid and updates the training session in the database.
     */
    public void editSession() {
        Optional<TrainingSession> updatedSession = errorBuilder();
        updateInDatabase(updatedSession);
    }

    /**
     * Updates the training session in the database.
     * @param tempSession - session to update
     */
    private void updateInDatabase(Optional<TrainingSession> tempSession) {
        if(tempSession.isEmpty()){
            return;
        }
        Alert confirmationAlert = AlertUtil.showConfirmationAlert("Updating session",
                "Are you sure you want to update this session?",
                "Name: " + tempSession.get().getName() + "\n" +
                        "Duration: " + tempSession.get().getDuration() + "\n" +
                        "Max participants: " + tempSession.get().getMaxParticipants() + "\n" +
                        "Location: " + tempSession.get().getLocation() + "\n" +
                        "Date & Time: " + tempSession.get().displayDateTime() + "\n" +
                        "Trainer: " + tempSession.get().getTrainer().toString());
        confirmationAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            Optional<TrainingSession> oldSession = Optional.of(new TrainingSession(
                    session.getId(),
                    session.getName(),
                    session.getDateTime(),
                    session.getDuration(),
                    session.getTrainer(),
                    session.getMaxParticipants(),
                    session.getLocation(),
                    session.getStatus(),
                    session.getParticipants()
            ));
            tempSession.get().setName(tempSession.get().getName());
            tempSession.get().setDuration(tempSession.get().getDuration());
            tempSession.get().setMaxParticipants(tempSession.get().getMaxParticipants());
            tempSession.get().setLocation(tempSession.get().getLocation());
            tempSession.get().setDateTime(tempSession.get().getDateTime());
            tempSession.get().setTrainer(tempSession.get().getTrainer());
            try{
                trainingSessionRepository.update(tempSession);
                tempSession.get().sessionStatus();
            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while updating session", e.getMessage());
                return;
            }

            nameTextField.clear();
            durationTextField.clear();
            maxParticipantsTextField.clear();
            locationTextField.clear();
            datePicker.getEditor().clear();
            timeTextField.clear();
            trainerComboBox.getSelectionModel().clearSelection();
            confirmationAlert.close();

            AlertUtil.showInfoAlert("Session updated", "Session " + tempSession.get().getName() +" has been successfully updated!");

            LogUtil.saveChangesForTrainingSession(oldSession.get(), tempSession.get());
        }else{
            confirmationAlert.close();
        }
    }

    /**
     * Builds the training session from the input fields.
     * @return - training session
     */
    private Optional<TrainingSession> errorBuilder() {
        String name = nameTextField.getText();
        Integer duration = 0;
        Integer maxParticipants = 0;
        LocalDate date = null;
        String location = locationTextField.getText();
        String time = timeTextField.getText().trim();
        User trainer = trainers.stream().filter(t -> t.toString().equals(trainerComboBox.getValue())).findFirst().orElse(null);
        LocalDateTime dateTime = null;
        StringBuilder errors = new StringBuilder();

        AlertUtil.checkStringInput(name, "Name is required!\n", errors);
        AlertUtil.checkStringInput(location, "Location is required!\n", errors);
        AlertUtil.checkStringInput(time, "Time is required!\n", errors);
        duration = AlertUtil.checkIntegerInput(durationTextField.getText(), "Duration is required!\n", "Duration must be a positive number!\n", "Duration must be a valid integer!\n", errors);
        maxParticipants = AlertUtil.checkIntegerInput(maxParticipantsTextField.getText(), "Max participants is required!\n", "Max participants must be positive number!\n", "Max participants must be a valid integer!\n", errors);

        if (datePicker.getValue() == null) {
            errors.append("Date is required!\n");
        }
        else{
            date = datePicker.getValue();
        }
        if(datePicker.getValue() != null && !time.isEmpty()){
            if(!Pattern.matches("^\\d{2}:\\d{2}$", time.trim())){
                errors.append("Time must be in format HH:mm!\n");
            }else {
                time = time + ":00";
                dateTime = LocalDateTime.parse(date + "T" + time);
                if (dateTime.isBefore(LocalDateTime.now())) {
                    errors.append("Date and time must be in the future!\n");
                }
            }
        }
        if (trainer == null) {
            errors.append("Trainer is required!\n");
        }
        if(!errors.isEmpty() && Boolean.FALSE.equals(showOneAlert)) {
            AlertUtil.showErrorAlert("Error while adding Session",
                    "Please correct the following errors:",
                    errors.toString());
        }else if(Boolean.FALSE.equals(showOneAlert)){
            return Optional.of(new TrainingSession.Builder()
                    .setId(session.getId())
                    .setName(name)
                    .setDuration(duration)
                    .setMaxParticipants(maxParticipants)
                    .setLocation(location)
                    .setDateTime(dateTime)
                    .setTrainer(trainer)
                    .setParticipants(session.getParticipants())
                    .setStatus(session.getStatus())
                    .build());
        }
        return Optional.empty();
    }
}
