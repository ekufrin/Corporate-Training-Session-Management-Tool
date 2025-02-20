package hr.ekufrin.training.controller;

import hr.ekufrin.training.HelloApplication;
import hr.ekufrin.training.enums.SessionStatus;
import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.model.Log;
import hr.ekufrin.training.model.SessionManager;
import hr.ekufrin.training.model.TrainingSession;
import hr.ekufrin.training.repository.LogRepository;
import hr.ekufrin.training.repository.TrainingSessionRepository;
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
 * JavaFX controller for showing training sessions.
 */
public class ShowSessionsController {
    @FXML
    private TextField searchSessionTextField;
    @FXML
    private TableView<TrainingSession> sessionTableView;
    @FXML
    private TableColumn<TrainingSession,Long> idTableColumn;
    @FXML
    private TableColumn<TrainingSession,String> nameTableColumn;
    @FXML
    private TableColumn<TrainingSession, String> dateTableColumn;
    @FXML
    private TableColumn<TrainingSession,Integer> durationTableColumn;
    @FXML
    private TableColumn<TrainingSession, String> trainerTableColumn;
    @FXML
    private TableColumn<TrainingSession, Integer> participantsTableColumn;
    @FXML
    private TableColumn<TrainingSession, Integer> maxParticipantsTableColumn;
    @FXML
    private TableColumn<TrainingSession, String> locationTableColumn;
    @FXML
    private TableColumn<TrainingSession, SessionStatus> statusTableColumn;
    @FXML
    private ContextMenu sessionContextMenu;

    private final TrainingSessionRepository trainingSessionRepository = new TrainingSessionRepository();
    private final LogRepository logRepository = new LogRepository();
    private HashSet<TrainingSession> trainingSessions = new HashSet<>();
    private final Long userId = SessionManager.INSTANCE.getuserId();


    public void initialize(){
        setSessionTableView();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.minutes(1), _ -> setSessionTableView())
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Sets the training sessions table view.
     */
    private void setSessionTableView() {
        try{
            trainingSessions = new HashSet<>(trainingSessionRepository.findAll());
        } catch (ConnectionToDatabaseException e) {
            AlertUtil.showErrorAlert("Error while loading training sessions", e.getMessage());
        }
        idTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        nameTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        dateTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().displayDateTime()));
        durationTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDuration()));
        trainerTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTrainer().getFirstName() + " " + cellData.getValue().getTrainer().getLastName()));
        participantsTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getParticipants().size()));
        maxParticipantsTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getMaxParticipants()));
        locationTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocation()));
        statusTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStatus()));
        if(!trainingSessions.isEmpty()){
            MenuItem editMenuItem = new MenuItem("Edit");
            MenuItem deleteMenuItem = new MenuItem("Delete");
            sessionContextMenu.getItems().addAll(editMenuItem, deleteMenuItem);
            editMenuItem.setOnAction(_ -> editSession());
            deleteMenuItem.setOnAction(_ -> deleteSession());
        }

        searchSessions();
    }

    /**
     * Deletes the selected session.
     */
    private void deleteSession() {
        TrainingSession session = sessionTableView.getSelectionModel().getSelectedItem();
        Boolean isDeleted = Boolean.TRUE;

        Alert confirmationAlert = AlertUtil.showConfirmationAlert("Delete session","Deleting session", "Are you sure you want to delete session " + session.getName() + "?");
        confirmationAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.YES){
            try{
                trainingSessionRepository.delete(session);
                Log log = new Log.Builder()
                        .setUserId(userId)
                        .setOperation("Deleted session")
                        .setTimestamp(LocalDateTime.now())
                        .setChangedField("Session: " + session.getName())
                        .setNewValue("Session deleted")
                        .build();
                logRepository.save(log);
            } catch (ConnectionToDatabaseException e) {
                isDeleted = Boolean.FALSE;
                log.error("Error while deleting session, error {}", e.getMessage());
                AlertUtil.showErrorAlert("Error while deleting session", e.getMessage());
            }
            if(Boolean.TRUE.equals(isDeleted)){
                AlertUtil.showInfoAlert("Session deleted", "Session " + session.getName() + " has been successfully deleted!");
                trainingSessions.remove(session);
                searchSessions();
            }
        }
    }

    /**
     * Edits the selected session.
     */
    private void editSession() {
        TrainingSession session = sessionTableView.getSelectionModel().getSelectedItem();
        if(session.getStatus().equals(SessionStatus.FINISHED)){
            AlertUtil.showErrorAlert("Session finished", "Session " + session.getName() + " is already finished and can't be edited!");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("editSession.fxml"));
            Parent editSession = loader.load();

            EditSessionController editSessionController = loader.getController();
            editSessionController.setSession(session);
            Scene currentScene = sessionTableView.getScene();
            currentScene.setRoot(editSession);
        } catch (IOException e) {
            log.error("Error while loading Edit Session scene, error {}", e.getMessage());
        }
    }

    /**
     * Searches the training sessions based on the name or location.
     */
    public void searchSessions() {
        if (searchSessionTextField.getText().isEmpty()) {
            sessionTableView.setItems(FXCollections.observableArrayList(trainingSessions));
        } else {
            sessionTableView.setItems(FXCollections.observableArrayList(
                    trainingSessions.stream()
                            .filter(session -> session.getName().toLowerCase().contains(searchSessionTextField.getText().toLowerCase()) || session.getLocation().toLowerCase().contains(searchSessionTextField.getText().toLowerCase()))
                            .toList()
            ));
        }
    }
}
