package hr.ekufrin.training.controller;

import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.model.User;
import hr.ekufrin.training.model.Role;
import hr.ekufrin.training.model.SessionManager;
import hr.ekufrin.training.repository.UserRepository;
import hr.ekufrin.training.repository.RoleRepository;
import hr.ekufrin.training.util.AlertUtil;
import hr.ekufrin.training.util.LogUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.util.HashSet;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * JavaFX controller for editing a user.
 */
public class EditUserController {
    @FXML
    private Text editUserText;
    @FXML
    private Text roleText;
    @FXML
    private TextField firstNameTextField;
    @FXML
    private TextField lastNameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private CheckBox changePasswordCheckBox;
    @FXML
    private PasswordField passwordPasswordField;
    @FXML
    private ComboBox<String> roleComboBox;

    private final RoleRepository roleRepository = new RoleRepository();
    private final UserRepository userRepository = new UserRepository();
    private final Long userId = SessionManager.INSTANCE.getuserId();
    private User user;

    private HashSet<Role> roles = new HashSet<>();
    private Boolean showOneAlert = false;

    /**
     * Sets the user to be edited.
     * @param user - user to be edited
     */
    public void setEmployee(User user) {
        if (user != null) {
            this.user = user;
            firstNameTextField.setText(user.getFirstName());
            lastNameTextField.setText(user.getLastName());
            emailTextField.setText(user.getEmail());
            roleComboBox.setValue(user.getRole().getName());

            if (userId.equals(user.getId())) {
                roleComboBox.setVisible(false);
                roleText.setVisible(false);
                editUserText.setText("Edit profile");
            }
        }

    }

    public void initialize() {
        try {
            roles = new HashSet<>(roleRepository.findAll());
        } catch (ConnectionToDatabaseException e) {
            AlertUtil.showErrorAlert("Error while loading roles", e.getMessage());
            showOneAlert = true;
        }
        roleComboBox.getItems().addAll(roles.stream().map(Role::getName).toList());


        changePasswordCheckBox.setOnAction(event -> {
            if (changePasswordCheckBox.isSelected()) {
                passwordPasswordField.setDisable(false);
            } else {
                passwordPasswordField.setDisable(true);
                passwordPasswordField.clear();
            }
        });
    }

    /**
     * Checks if the input is valid and updates the user in the database.
     */
    public void editUser() {
        String firstName = firstNameTextField.getText();
        String lastName = lastNameTextField.getText();
        String email = emailTextField.getText();
        String password;
        Boolean changePassword = changePasswordCheckBox.isSelected();
        Role role = roles.stream().filter(r -> r.getName().equals(roleComboBox.getValue())).findFirst().orElse(user.getRole());

        StringBuilder errors = new StringBuilder();

        if (Boolean.TRUE.equals(changePassword)) {
            password = passwordPasswordField.getText();
            if (password.isEmpty()) {
                errors.append("Password is required!\n");
            }
        } else {
            password = user.getPassword();
        }

        if (firstName.isEmpty()) {
            errors.append("First name is required!\n");
        }
        if (lastName.isEmpty()) {
            errors.append("Last name is required!\n");
        }
        if (email.isEmpty()) {
            errors.append("Email is required!\n");
        } else if (!Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", email.trim())) {
            errors.append("Email must be in format: test@example.com\n");
        }
        if (role == null) {
            errors.append("Role is required!\n");
        }

        if (!errors.isEmpty() && Boolean.FALSE.equals(showOneAlert)) {
            AlertUtil.showErrorAlert(
                    "Error while updating user",
                    "Please correct the following errors:",
                    errors.toString());
        } else if (Boolean.FALSE.equals(showOneAlert)) {
            assert role != null;
            updateInDatabase(role, firstName, lastName, email, password);

        }
    }

    /**
     * Updates the user in the database.
     * @param role - user role
     * @param firstName - user first name
     * @param lastName - user last name
     * @param email - user email
     * @param password - user password
     */
    private void updateInDatabase(Role role, String firstName, String lastName, String email, String password) {
        Alert confirmationAlert = AlertUtil.showConfirmationAlert(
                "Updating user",
                "Are you sure you want to update this user?",
                "First name: " + firstName + "\n" +
                        "Last name: " + lastName + "\n" +
                        "Email: " + email + "\n" +
                        "Role: " + role.getName());

        confirmationAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            User oldUser = new User(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword(), user.getRole());
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(role);

            try {
                Boolean isUpdated = userRepository.update(user);
                if (Boolean.TRUE.equals(isUpdated)) {
                    firstNameTextField.clear();
                    lastNameTextField.clear();
                    emailTextField.clear();
                    passwordPasswordField.clear();
                    changePasswordCheckBox.setSelected(false);
                    roleComboBox.getSelectionModel().clearSelection();
                    confirmationAlert.close();

                    AlertUtil.showInfoAlert("User updated", "User " + user.toString() + " has been successfully updated!");

                    LogUtil.saveChangesForUser(oldUser, user);

                }
            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while updating user", e.getMessage());
            }

        } else {
            confirmationAlert.close();
        }
    }

}
