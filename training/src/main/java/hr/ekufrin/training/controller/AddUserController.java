package hr.ekufrin.training.controller;

import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.model.User;
import hr.ekufrin.training.model.Log;
import hr.ekufrin.training.model.Role;
import hr.ekufrin.training.model.SessionManager;
import hr.ekufrin.training.repository.UserRepository;
import hr.ekufrin.training.repository.LogRepository;
import hr.ekufrin.training.repository.RoleRepository;
import hr.ekufrin.training.util.AlertUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * JavaFX controller for adding a new user.
 */
public class AddUserController {
    @FXML
    private TextField firstNameTextField;
    @FXML
    private TextField lastNameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private PasswordField passwordPasswordField;
    @FXML
    private ComboBox<String> roleComboBox;

    private final RoleRepository roleRepository = new RoleRepository();
    private final UserRepository userRepository = new UserRepository();

    private  HashSet<Role> roles = new HashSet<>();
    private Boolean showOneAlert = false;

    public void initialize() {
        try{
            roles = new HashSet<>(roleRepository.findAll());
        } catch (ConnectionToDatabaseException e) {
            AlertUtil.showErrorAlert("Error while loading roles", e.getMessage());
            showOneAlert = true;
        }
        roleComboBox.getItems().addAll(roles.stream().map(Role::getName).toList());
        Platform.runLater(() -> roleComboBox.getSelectionModel().selectFirst());

    }
/**
 * Checks if the input is valid and adds a new user to the database.
 */
    public void addUser() {
        String firstName = firstNameTextField.getText();
        String lastName = lastNameTextField.getText();
        String email = emailTextField.getText();
        String password = passwordPasswordField.getText();
        Role role = roles.stream().filter(r -> r.getName().equals(roleComboBox.getValue())).findFirst().orElse(null);

        StringBuilder errors = new StringBuilder();
        if (firstName.isEmpty()) {
            errors.append("First name is required!\n");
        }
        if (lastName.isEmpty()) {
            errors.append("Last name is required!\n");
        }
        if (email.isEmpty()) {
            errors.append("Email is required!\n");
        }
        else if(!Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", email.trim())){
            errors.append("Email must be in format: test@example.com\n");
        }
        if (password.isEmpty()) {
            errors.append("Password is required!\n");
        }
        if (role == null) {
            errors.append("Role is required!\n");
        }

        if(!errors.isEmpty() && Boolean.FALSE.equals(showOneAlert)){
            AlertUtil.showErrorAlert(
                    "Error while adding user",
                    "Please correct the following errors:",
                    errors.toString());
        } else if(Boolean.FALSE.equals(showOneAlert)) {
            assert role != null;
            addToDatabase(role, firstName, lastName, email, password);

        }
    }

    /**
     * Adds a new user to the database.
     * @param role - user role
     * @param firstName - user first name
     * @param lastName - user last name
     * @param email - user email
     * @param password - user password
     */
    private void addToDatabase(Role role, String firstName, String lastName, String email, String password) {
        Alert confirmationAlert = AlertUtil.showConfirmationAlert(
                "Adding new user",
                "Are you sure you want to add this user?",
                "First name: " + firstName + "\n" +
                        "Last name: " + lastName + "\n" +
                        "Email: " + email + "\n" +
                        "Role: " + role.getName());

        confirmationAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            User user = new User.Builder()
                    .setFirstName(firstName)
                    .setLastName(lastName)
                    .setEmail(email)
                    .setPassword(password)
                    .setRole(role)
                    .build();

            try {
                userRepository.save(user);
            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while saving user", e.getMessage());
            }
            firstNameTextField.clear();
            lastNameTextField.clear();
            emailTextField.clear();
            passwordPasswordField.clear();
            roleComboBox.getSelectionModel().clearSelection();
            confirmationAlert.close();

            AlertUtil.showInfoAlert("User added", "User " + user.toString() +" has been successfully added!");

            Log newUserLog = new Log.Builder()
                    .setUserId(SessionManager.INSTANCE.getuserId())
                    .setOperation("Added new user")
                    .setTimestamp(LocalDateTime.now())
                    .setChangedField("User: " + user.toString())
                    .setNewValue("User added")
                    .build();

            LogRepository logRepository = new LogRepository();
            try {
                logRepository.save(newUserLog);
            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while saving log", e.getMessage());
            }
        }
        else{
            confirmationAlert.close();
        }
    }

}
