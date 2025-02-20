package hr.ekufrin.training.controller;

import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.model.*;
import hr.ekufrin.training.repository.UserRepository;
import hr.ekufrin.training.repository.LogRepository;
import hr.ekufrin.training.repository.NotificationRepository;
import hr.ekufrin.training.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
/**
 * JavaFX controller for adding notifications.
 */
public class AddNotificationController {
    @FXML
    private TextField messageTextField;
    @FXML
    private VBox recipientsVBox;

    private final UserRepository userRepository = new UserRepository();
    private HashSet<User> employees = new HashSet<>();
    private Boolean showOneAlert = false;

    public void initialize() {
        try {
            employees = new HashSet<>(userRepository.findAll().stream().filter(employee -> employee.getRole().getName().equalsIgnoreCase("Employee")).toList());
        } catch (ConnectionToDatabaseException e) {
            AlertUtil.showErrorAlert("Error while loading employees", e.getMessage());
            showOneAlert = true;
        }
        recipientsVBox.getChildren().addAll(employees.stream().map(employee -> {
            CheckBox checkBox = new CheckBox(employee.toString());
            checkBox.setId(employee.getId().toString());
            return checkBox;
        }).toList());
    }

    /**
     * Checks if the message and recipients are valid and adds a new notification to the database.
     */
    public void addNotification() {
        String message = messageTextField.getText();
        HashSet<User> recipients = new HashSet<>(recipientsVBox.getChildren().stream()
                .filter(CheckBox.class::isInstance)
                .map(CheckBox.class::cast)
                .filter(CheckBox::isSelected)
                .map(checkBox -> employees.stream().filter(employee -> employee.getId().equals(Long.parseLong(checkBox.getId()))).findFirst().orElse(null))
                .toList());

        StringBuilder errors = new StringBuilder();

        if (message.trim().isEmpty()) {
            errors.append("Message is required!\n");
        }
        if (recipients.isEmpty()) {
            errors.append("Select recipients!\n");
        }

        if (!errors.isEmpty() && Boolean.FALSE.equals(showOneAlert)) {
            AlertUtil.showErrorAlert(
                    "Error while adding Notification",
                    "Please correct the following errors:",
                    errors.toString());
        } else if (Boolean.FALSE.equals(showOneAlert)) {
            addToDatabase(message, recipients);
        }
    }

    /**
     * Adds a new notification to the database.
     * @param message - notification message
     * @param recipients - notification recipients
     */
    private void addToDatabase(String message, HashSet<User> recipients) {
        Alert confirmationAlert = AlertUtil.showConfirmationAlert(
                "Adding new notification",
                "Are you sure you want to send this notification?",
                "Message: " + message + "\nRecipients: " +
                        recipients.stream().map(User::toString).collect(Collectors.joining(", ")));

        confirmationAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            Notification notification = new Notification.Builder()
                    .setMessage(message)
                    .build();

            NotificationRepository notificationRepository = new NotificationRepository();
            try {
                notificationRepository.saveNotificationForEmployees(notification, recipients);
            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while saving notification", e.getMessage());
            }

            messageTextField.clear();
            recipientsVBox.getChildren().forEach(node -> {
                if (node instanceof CheckBox checkBox) {
                    checkBox.setSelected(false);
                }
            });
            confirmationAlert.close();

            AlertUtil.showInfoAlert(
                    "Notification added",
                    "Notification with message: " + notification.getMessage() + " has been successfully added!");

            Log newSessionLog = new Log.Builder()
                    .setUserId(SessionManager.INSTANCE.getuserId())
                    .setOperation("Added new notification")
                    .setTimestamp(LocalDateTime.now())
                    .setChangedField("Notification: " + notification.getMessage())
                    .setNewValue("Notification added")
                    .build();

            LogRepository logRepository = new LogRepository();
            try {
                logRepository.save(newSessionLog);
            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while saving log", e.getMessage());
            }
        } else {
            confirmationAlert.close();
        }
    }
}
