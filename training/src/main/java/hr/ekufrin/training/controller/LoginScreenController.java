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
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * JavaFX controller for the login screen.
 */
public class LoginScreenController {
    private static final Logger log = LoggerFactory.getLogger(LoginScreenController.class);
    @FXML
    private TextField emailTextField;
    @FXML
    private PasswordField passwordPasswordField;

    /**
     * Checks if the input is valid and logs the user in.
     */
    public void login() {
        UserRepository userRepository = new UserRepository();
        String email = emailTextField.getText();
        String password = passwordPasswordField.getText();
        Boolean errorHappened = false;

        Optional<Long> userId = Optional.empty();
        try{
            userId = userRepository.login(email, password);
        } catch (ConnectionToDatabaseException e) {
            errorHappened = true;
            AlertUtil.showErrorAlert("Error while logging in", e.getMessage());
        }

        if(Boolean.TRUE.equals(errorHappened)){
            return;
        }
        if (userId.isPresent()) {
            Log loginLog = new Log.Builder().setUserId(userId.get()).setChangedField("login").setOperation("login").setNewValue("success").setTimestamp(LocalDateTime.now()).build();
                try {
                    new LogRepository().save(loginLog);
                } catch (ConnectionToDatabaseException e) {
                    log.error("Error while saving log {}", e.getMessage());
                }
            openDashboard(userId.get());
        } else {
            AlertUtil.showErrorAlert("Login failed", "Invalid username or password");

            emailTextField.clear();
            passwordPasswordField.clear();
        }
    }

    public Scene start(){
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("loginScreen.fxml"));

        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), 800, 600);
        } catch (IOException e) {
            log.error("Error while loading login screen, error: {}", e.getMessage());
        }
        return scene;
    }

    /**
     * Opens the dashboard for the user after successful login.
     * @param userId- user id
     */
    private void openDashboard(Long userId) {
        try {
            User user = new UserRepository().findById(userId);
            SessionManager.INSTANCE.setSession(user);
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailTextField.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();

        } catch (IOException e) {
            log.error("Error while loading dashboard, error: {}", e.getMessage());
        } catch (ConnectionToDatabaseException e) {
            AlertUtil.showErrorAlert("Error while loading user", e.getMessage());
        }
    }




}
