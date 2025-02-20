package hr.ekufrin.training.controller;

import hr.ekufrin.training.HelloApplication;
import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.model.User;
import hr.ekufrin.training.model.Log;
import hr.ekufrin.training.model.SessionManager;
import hr.ekufrin.training.repository.UserRepository;
import hr.ekufrin.training.repository.LogRepository;
import hr.ekufrin.training.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static hr.ekufrin.training.HelloApplication.log;

/**
 * JavaFX controller for the menu bar.
 */
public class MenuBarController {

    @FXML
    private MenuBar menuBar;
    @FXML
    private Menu menuUsers;
    @FXML
    private Menu menuSessions;
    @FXML
    private Menu menuNotifications;
    @FXML
    private Menu menuLogs;

    @FXML
    private MenuItem openDashboardMenuItem;
    @FXML
    private MenuItem showUsersMenuItem;
    @FXML
    private MenuItem addUserMenuItem;
    @FXML
    private MenuItem showSessionsMenuItem;
    @FXML
    private MenuItem addSessionMenuItem;
    @FXML
    private MenuItem showNotificationsMenuItem;
    @FXML
    private MenuItem addNotificationMenuItem;
    @FXML
    private MenuItem showFeedbackMenuItem;
    @FXML
    private MenuItem showLogsMenuItem;
    @FXML
    private MenuItem editProfileMenuItem;
    @FXML
    private MenuItem logoutMenuItem;

    private final LogRepository logRepository = new LogRepository();
    private final UserRepository userRepository = new UserRepository();

    /**
     * Initializes the menu bar.
     * Sets the actions for the menu items.
     */
    public void initialize() {
        openDashboardMenuItem.setOnAction(event -> openScene("dashboard.fxml"));
        showUsersMenuItem.setOnAction(event -> openScene("showUsers.fxml"));
        addUserMenuItem.setOnAction(event -> openScene("addUser.fxml"));
        showSessionsMenuItem.setOnAction(event -> openScene("showSessions.fxml"));
        addSessionMenuItem.setOnAction(event -> openScene("addSession.fxml"));
        showNotificationsMenuItem.setOnAction(event -> openScene("showNotifications.fxml"));
        addNotificationMenuItem.setOnAction(event -> openScene("addNotification.fxml"));
        showFeedbackMenuItem.setOnAction(event -> openScene("showFeedback.fxml"));
        showLogsMenuItem.setOnAction(event -> openScene("showLogs.fxml"));
        editProfileMenuItem.setOnAction(event -> editProfile());
        logoutMenuItem.setOnAction(event -> logout());

        String role = SessionManager.INSTANCE.getUserRole();
        if("Employee".equalsIgnoreCase(role)){
            menuUsers.setVisible(false);
            menuSessions.setVisible(false);
            menuNotifications.setVisible(false);
            menuLogs.setVisible(false);
        }
    }

    /**
     * Opens the edit user profile scene.
     */
    private void editProfile() {
        try {
            User user = userRepository.findById(SessionManager.INSTANCE.getuserId());
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("editUser.fxml"));
            Parent editUser = loader.load();

            EditUserController editUserController = loader.getController();
            editUserController.setEmployee(user);
            Scene currentScene = menuBar.getScene();
            currentScene.setRoot(editUser);
        } catch (IOException e) {
            log.error("Error while loading Edit User scene, error {}", e.getMessage());
        } catch (ConnectionToDatabaseException e) {
            log.error("Error while loading user, error {}", e.getMessage());
            AlertUtil.showErrorAlert("Error while loading user", e.getMessage());
        }
    }

    /**
     * Logs out the user.
     */
    private void logout() {
        Alert logoutAlert = AlertUtil.showConfirmationAlert(
                "Logout",
                "Are you sure you want to logout?",
                "All unsaved changes will be lost.");

        logoutAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.CANCEL);
        Optional<ButtonType> result = logoutAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            openScene("loginScreen.fxml");
            Log logoutLog = new Log.Builder()
                    .setUserId(SessionManager.INSTANCE.getuserId())
                    .setChangedField("logout")
                    .setOperation("logout")
                    .setNewValue("success")
                    .setTimestamp(LocalDateTime.now())
                    .build();

            try {
                logRepository.save(logoutLog);
            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while saving log", e.getMessage());
            }

        }
        else{
            logoutAlert.close();
        }
    }

    /**
     * Opens the scene with the given fxml file.
     * @param fxmlFile - fxml file
     */
    private void openScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlFile));
            Stage stage = (Stage) menuBar.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 800, 600));
        } catch (IOException e) {
            log.error("Error while loading scene, error {}", e.getMessage());
        }
    }

}
