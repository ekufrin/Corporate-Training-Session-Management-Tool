package hr.ekufrin.training.controller;

import hr.ekufrin.training.enums.SessionStatus;
import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.model.*;
import hr.ekufrin.training.repository.UserRepository;
import hr.ekufrin.training.repository.LogRepository;
import hr.ekufrin.training.repository.NotificationRepository;
import hr.ekufrin.training.repository.TrainingSessionRepository;
import hr.ekufrin.training.util.AlertUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * JavaFX controller for adding a new training session.
 */
public class AddSessionController {
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
    private final NotificationRepository notificationRepository = new NotificationRepository();
    private Boolean showOneAlert = false;

    private HashSet<User> trainers = new HashSet<>();

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
     * Checks if the input is valid and adds a new training session to the database.
     */
    public void addSession() {
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
        AlertUtil.checkStringInput(time, "Time is required!\n", errors);
        AlertUtil.checkStringInput(location, "Location is required!\n", errors);
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
            addToDatabase(name, duration, maxParticipants, location, date, time, trainer, dateTime);
        }
    }

    /**
     * Adds a new training session to the database.
     * @param name - session name
     * @param duration - session duration
     * @param maxParticipants - session max participants
     * @param location - session location
     * @param date - session date
     * @param time - session time
     * @param trainer - session trainer
     * @param dateTime - session date and time
     */
    private void addToDatabase(String name, Integer duration, Integer maxParticipants, String location, LocalDate date, String time, User trainer, LocalDateTime dateTime) {
        Alert confirmationAlert = AlertUtil.showConfirmationAlert("Adding new session",
                "Are you sure you want to add this session?",
                "Name: " + name + "\n" +
                        "Duration: " + duration + "\n" +
                        "Max participants: " + maxParticipants + "\n" +
                        "Location: " + location + "\n" +
                        "Date: " + date + "\n" +
                        "Time: " + time + "\n" +
                        "Trainer: " + trainer);
        confirmationAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            TrainingSession session = new TrainingSession.Builder()
                    .setName(name)
                    .setDuration(duration)
                    .setMaxParticipants(maxParticipants)
                    .setLocation(location)
                    .setDateTime(dateTime)
                    .setTrainer(trainer)
                    .setStatus(SessionStatus.SCHEDULED)
                    .build();
            try{
                trainingSessionRepository.save(session);
            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while saving session", e.getMessage());
            }

            HashSet<User> recipients = new HashSet<>();
            try{
                recipients = userRepository.findAll().stream().filter(e -> "Employee".equalsIgnoreCase(e.getRole().getName())).collect(Collectors.toCollection(HashSet::new));
            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while loading employees", e.getMessage());
            }

            Notification notification = new Notification.Builder()
                    .setMessage("New session " + session.getName() + " has been added!")
                    .build();

            try {
                notificationRepository.saveNotificationForEmployees(notification, recipients);
            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while saving notification", e.getMessage());
            }

            nameTextField.clear();
            durationTextField.clear();
            maxParticipantsTextField.clear();
            locationTextField.clear();
            datePicker.getEditor().clear();
            timeTextField.clear();
            trainerComboBox.getSelectionModel().clearSelection();
            confirmationAlert.close();

            AlertUtil.showInfoAlert("Session added", "Session " + session.getName() +" has been successfully added!");

            Log newSessionLog = new Log.Builder()
                    .setUserId(SessionManager.INSTANCE.getuserId())
                    .setOperation("Added new session")
                    .setTimestamp(LocalDateTime.now())
                    .setChangedField("Session: " + session.getName())
                    .setNewValue("Session added")
                    .build();

            LogRepository logRepository = new LogRepository();
            try {
                logRepository.save(newSessionLog);
            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while saving log", e.getMessage());
            }
        }else{
            confirmationAlert.close();
        }
    }
}
