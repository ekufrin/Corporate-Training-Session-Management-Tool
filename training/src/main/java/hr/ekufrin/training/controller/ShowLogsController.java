package hr.ekufrin.training.controller;

import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.model.User;
import hr.ekufrin.training.model.Log;
import hr.ekufrin.training.repository.UserRepository;
import hr.ekufrin.training.repository.LogRepository;
import hr.ekufrin.training.util.AlertUtil;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JavaFX controller for showing logs.
 */
public class ShowLogsController {
    @FXML
    private ComboBox<String> userComboBox;
    @FXML
    private TextField searchLogsTextField;
    @FXML
    private TableView<Log> logsTableView;
    @FXML
    private TableColumn<Log, String> dateTimeTableColumn;
    @FXML
    private TableColumn<Log, String> userTableColumn;
    @FXML
    private TableColumn<Log, String> changedFieldTableColumn;
    @FXML
    private TableColumn<Log, String> oldValueTableColumn;
    @FXML
    private TableColumn<Log, String> newValueTableColumn;
    @FXML
    private TableColumn<Log, String> operationTableColumn;

    private final LogRepository logRepository = new LogRepository();
    private final UserRepository userRepository = new UserRepository();

    private HashSet<Log> logs = new HashSet<>();
    private HashMap<Long, User> employeeCache;

    public void initialize() {
        setLogsTableView();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.minutes(1), _ -> setLogsTableView())
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Sets the logs table view.
     */
    private void setLogsTableView() {
        new Thread(() -> {
            HashSet<User> users = new HashSet<>();
            try {
                logs = logRepository.findAll();
                users = userRepository.findAll();
            } catch (ConnectionToDatabaseException e) {
                Platform.runLater(() -> AlertUtil.showErrorAlert("Error while loading logs", e.getMessage()));
                if (logs.isEmpty()) {
                    logs = (HashSet<Log>) logRepository.readFromBinaryFile();
                    Platform.runLater(() -> AlertUtil.showInfoAlert("Couldn't connect to database", "Showing cached data."));
                }
            }

            employeeCache = users.stream().collect(Collectors.toMap(User::getId, user -> user, (a, _) -> a, HashMap::new));

            HashSet<User> finalUsers = users;
            Platform.runLater(() -> {
                userComboBox.setItems(FXCollections.observableArrayList(finalUsers.stream().map(User::toString).toList()));

                dateTimeTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                        cellData.getValue().getTimestamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss"))));

                userTableColumn.setCellValueFactory(cellData -> {
                    User user = employeeCache.get(cellData.getValue().getUserId());
                    return new SimpleStringProperty(user != null ? user.toString() : "Unknown");
                });

                changedFieldTableColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getChangedField()));

                oldValueTableColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getOldValue()));

                newValueTableColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getNewValue()));

                operationTableColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getOperation()));

                logsTableView.setItems(FXCollections.observableArrayList(logs));
                logsTableView.refresh();

            });
        }).start();
    }


    /**
     * Searches logs based on user input.
     */
    public void searchLogs() {
        List<Log> filteredLogs;

        if (userComboBox.getValue() != null) {
            User selectedUser = employeeCache.values().stream()
                    .filter(employee -> employee.toString().equals(userComboBox.getValue()))
                    .findFirst().orElse(null);

            if (selectedUser != null) {
                filteredLogs = logs.stream()
                        .filter(log -> log.getUserId().equals(selectedUser.getId()))
                        .toList();
            } else {
                filteredLogs = List.of();
            }
        } else {
            if (searchLogsTextField.getText().isEmpty()) {
                filteredLogs = List.copyOf(logs);
            } else {
                filteredLogs = logs.stream()
                        .filter(log -> log.getChangedField().contains(searchLogsTextField.getText()))
                        .toList();
            }
        }

        logsTableView.setItems(FXCollections.observableArrayList(filteredLogs));
    }
}
