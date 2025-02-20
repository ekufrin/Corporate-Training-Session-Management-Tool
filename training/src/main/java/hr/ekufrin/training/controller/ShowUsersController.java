package hr.ekufrin.training.controller;

import hr.ekufrin.training.HelloApplication;
import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.model.User;
import hr.ekufrin.training.model.Log;
import hr.ekufrin.training.model.SessionManager;
import hr.ekufrin.training.repository.UserRepository;
import hr.ekufrin.training.repository.LogRepository;
import hr.ekufrin.training.util.AlertUtil;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static hr.ekufrin.training.HelloApplication.log;
/**
 * JavaFX controller for showing users.
 */
public class ShowUsersController {
    @FXML
    private TextField searchTextField;
    @FXML
    private TableView<User> usersTableView;
    @FXML
    private TableColumn<User,Long> idTableColumn;
    @FXML
    private TableColumn<User,String> firstNameTableColumn;
    @FXML
    private TableColumn<User,String> lastNameTableColumn;
    @FXML
    private TableColumn<User,String> emailTableColumn;
    @FXML
    private TableColumn<User, String> roleTableColumn;
    @FXML
    private MenuItem editContextMenuItem;
    @FXML
    private MenuItem deleteContextMenuItem;

    private final UserRepository userRepository = new UserRepository();
    private final LogRepository logRepository = new LogRepository();
    private HashSet<User> users = new HashSet<>();
    private final Long userId = SessionManager.INSTANCE.getuserId();


    public void initialize(){
        setUsersTableView();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.minutes(1), _ -> setUsersTableView())
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Sets the users table view.
     */
    private void setUsersTableView() {
        try{
            users = new HashSet<>(userRepository.findAll());
        } catch (ConnectionToDatabaseException e) {
            AlertUtil.showErrorAlert("Error while loading users", e.getMessage());
        }
        idTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        firstNameTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
        lastNameTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));
        emailTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        roleTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole().getName()));
        editContextMenuItem.setOnAction(_ -> editUser());
        deleteContextMenuItem.setOnAction(_ -> deleteUser());
        searchUsers();
    }

    /**
     * Deletes the selected user.
     */
    private void deleteUser() {
        User user = usersTableView.getSelectionModel().getSelectedItem();
        Boolean isDeleted = Boolean.TRUE;

        if(user.getId().equals(userId)){
            AlertUtil.showErrorAlert("Error while deleting user", "You can't delete yourself!");
            return;
        }

        Alert confirmationAlert = AlertUtil.showConfirmationAlert("Delete user","Deleting user", "Are you sure you want to delete user " + user.toString() + "?");
        confirmationAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.YES){
            try{
                userRepository.delete(user);
                Log log = new Log.Builder()
                        .setUserId(userId)
                        .setOperation("Deleted user")
                        .setTimestamp(LocalDateTime.now())
                        .setChangedField("User: " + user.toString())
                        .setNewValue("User deleted")
                        .build();
                logRepository.save(log);
            } catch (ConnectionToDatabaseException e) {
                isDeleted = Boolean.FALSE;
                log.error("Error while deleting user, error {}", e.getMessage());
                AlertUtil.showErrorAlert("Error while deleting user", e.getMessage());
            }
            if(Boolean.TRUE.equals(isDeleted)){
                AlertUtil.showInfoAlert("User deleted", "User " + user.toString() + " has been successfully deleted!");
                users.remove(user);
                searchUsers();
            }
        }


    }

    /**
     * Edits the selected user.
     */
    private void editUser() {
        User user = usersTableView.getSelectionModel().getSelectedItem();
        if(user.getId().equals(userId)){
            AlertUtil.showErrorAlert("Error while editing user", "You can't edit yourself!", "Use Edit profile from Menu bar instead.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("editUser.fxml"));
            Parent editUser = loader.load();

            EditUserController editUserController = loader.getController();
            editUserController.setEmployee(user);
            Scene currentScene = usersTableView.getScene();
            currentScene.setRoot(editUser);
        } catch (IOException e) {
            log.error("Error while loading Edit User scene, error {}", e.getMessage());
        }
    }

    /**
     * Searches users by first name or last name.
     */
    public void searchUsers() {
        if (searchTextField.getText().isEmpty()) {
            usersTableView.setItems(FXCollections.observableArrayList(users));
        } else {
            usersTableView.setItems(FXCollections.observableArrayList(
                    users.stream()
                            .filter(employee -> employee.getFirstName().toLowerCase().contains(searchTextField.getText().toLowerCase()) ||
                                    employee.getLastName().toLowerCase().contains(searchTextField.getText().toLowerCase()))
                            .toList()
            ));
        }
    }
}
