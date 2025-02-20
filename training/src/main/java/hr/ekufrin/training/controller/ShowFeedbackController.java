package hr.ekufrin.training.controller;

import hr.ekufrin.training.HelloApplication;
import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.model.Feedback;
import hr.ekufrin.training.model.SessionManager;
import hr.ekufrin.training.model.TrainingSession;
import hr.ekufrin.training.repository.FeedbackRepository;
import hr.ekufrin.training.util.AlertUtil;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import static hr.ekufrin.training.HelloApplication.log;

/**
 * JavaFX controller for showing feedbacks.
 */
public class ShowFeedbackController {
    @FXML
    private TableView<Feedback> finishedFeedbackTableView;
    @FXML
    private TableColumn<Feedback,Long> idFinishedFeedbackTableColumn;
    @FXML
    private TableColumn<Feedback,String> sessionNameFinishedFeedbackTableColumn;
    @FXML
    private TableColumn<Feedback, Integer> ratingFinishedFeedbackTableColumn;
    @FXML
    private TableColumn<Feedback, String> commentFinishedFeedbackTableColumn;
    @FXML
    private TableColumn<Feedback, String> dateTimeFinishedFeedbackTableColumn;
    @FXML
    private TableView<TrainingSession> waitingFeedbackTableView;
    @FXML
    private TableColumn<TrainingSession, String> sessionNameWaitingFeedbackTableColumn;
    @FXML
    private TableColumn<TrainingSession, String> dateTimeWaitingFeedbackTableColumn;
    @FXML
    private TableColumn<TrainingSession, String> locationWaitingFeedbackTableColumn;
    @FXML
    private ContextMenu waitingFeedbackContextMenu;
    @FXML
    private GridPane gridPane;
    @FXML
    private StackPane finishedFeedbackStackPane;
    @FXML
    private StackPane waitingFeedbackStackPane;

    private final Long userId = SessionManager.INSTANCE.getuserId();
    private final String employeeRole = SessionManager.INSTANCE.getUserRole();
    private final FeedbackRepository feedbackRepository = new FeedbackRepository();
    private final Set<Feedback> finishedFeedbacks = new HashSet<>();

    public void initialize() {
        setFeedbackTableView();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.minutes(1), _ -> setFeedbackTableView())
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Sets the feedback table view based on user role.
     */
    private void setFeedbackTableView() {
        if ("Trainer".equalsIgnoreCase(employeeRole)) {
            gridPane.getChildren().remove(waitingFeedbackStackPane);
            GridPane.setRowSpan(finishedFeedbackStackPane, 3);
            try{
                finishedFeedbacks.addAll(feedbackRepository.findAllFinishedFeedbackByTrainerId(userId));
                finishedFeedbackTableView.getItems().addAll(finishedFeedbacks);
            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while fetching", "Error while fetching all feedbacks for trainer.");
            }
        }else{
            try{
                finishedFeedbacks.addAll(feedbackRepository.findAllFinishedFeedbackByuserId(userId));
                Set<TrainingSession> employeeWaitingFeedbacks = feedbackRepository.findAllWaitingFeedbackByuserId(userId);
                finishedFeedbackTableView.getItems().addAll(finishedFeedbacks);
                waitingFeedbackTableView.getItems().addAll(employeeWaitingFeedbacks);
                if(!employeeWaitingFeedbacks.isEmpty()){
                    MenuItem addFeedbackMenuItem = getMenuItem();
                    waitingFeedbackContextMenu.getItems().add(addFeedbackMenuItem);
                }

            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while fetching", "Error while fetching all finished and waiting feedbacks.");
            }

        }


        idFinishedFeedbackTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().id()));
        sessionNameFinishedFeedbackTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().trainingSession().getName()));
        ratingFinishedFeedbackTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().rating()));
        commentFinishedFeedbackTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().comment()));
        dateTimeFinishedFeedbackTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().timestamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
        sessionNameWaitingFeedbackTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        dateTimeWaitingFeedbackTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().displayDateTime()));
        locationWaitingFeedbackTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocation()));
    }

    /**
     * Exports feedbacks to a file.
     */
    public void exportFeedbacks(){
        Boolean feedbacksSaved = feedbackRepository.saveFeedbacksToFile(finishedFeedbacks);
        if(Boolean.TRUE.equals(feedbacksSaved)){
            AlertUtil.showInfoAlert("Success", "Feedbacks successfully saved to file.");
        }else{
            AlertUtil.showErrorAlert("Error while exporting", "No feedbacks to export.");
        }
    }

    /**
     * Switches to the scene for adding feedback.
     * @return - menu item for adding feedback
     */
    private MenuItem getMenuItem() {
        MenuItem addFeedbackMenuItem = new MenuItem("Add feedback");
        addFeedbackMenuItem.setOnAction(event -> {
            try{
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("addFeedback.fxml"));
                Parent addFeedback = loader.load();
                AddFeedbackController addFeedbackController = loader.getController();
                addFeedbackController.setSession(waitingFeedbackTableView.getSelectionModel().getSelectedItem());
                Scene currentScene = waitingFeedbackTableView.getScene();
                currentScene.setRoot(addFeedback);
            } catch (IOException e) {
                log.error("Error while switching from Show Feedback to Add Feedback scene, error: {}", e.getMessage());
            }
        });
        return addFeedbackMenuItem;
    }
}
