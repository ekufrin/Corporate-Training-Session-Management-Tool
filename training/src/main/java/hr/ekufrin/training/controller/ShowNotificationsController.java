package hr.ekufrin.training.controller;

import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.model.Log;
import hr.ekufrin.training.model.Notification;
import hr.ekufrin.training.model.SessionManager;
import hr.ekufrin.training.repository.LogRepository;
import hr.ekufrin.training.repository.NotificationRepository;
import hr.ekufrin.training.util.AlertUtil;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;

import static hr.ekufrin.training.HelloApplication.log;

/**
 * JavaFX controller for showing notifications.
 */
public class ShowNotificationsController {
    @FXML
    private TableView<Notification> notificationsTableView;
    @FXML
    private TableColumn<Notification, Long> idTableColumn;
    @FXML
    private TableColumn<Notification, String> messageTableColumn;
    @FXML
    private TableColumn<Notification, String> dateTimeTableColumn;
    @FXML
    private TextField searchNotificationsTextField;
    @FXML
    private ContextMenu notificationsContextMenu;

    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final LogRepository logRepository = new LogRepository();
    private final Long userId = SessionManager.INSTANCE.getuserId();
    private HashSet<Notification> notifications = new HashSet<>();

    public void initialize() {
        setNotificationsTableView();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.minutes(1), _ -> setNotificationsTableView())
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Sets the notifications table view.
     */
    private void setNotificationsTableView() {
        try{
            notifications = new HashSet<>(notificationRepository.findAll());
        } catch (ConnectionToDatabaseException e) {
            AlertUtil.showErrorAlert("Error while loading notifications", e.getMessage());
        }
        idTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        messageTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMessage()));
        dateTimeTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTimestamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
        searchNotifications();

        if(!notifications.isEmpty()){
            MenuItem deleteMenuItem = new MenuItem("Delete");
            deleteMenuItem.setOnAction(_ -> deleteNotification());
            notificationsContextMenu.getItems().add(deleteMenuItem);
        }
    }

    /**
     * Deletes the selected notification.
     */
    private void deleteNotification() {
        Notification notification = notificationsTableView.getSelectionModel().getSelectedItem();
        Boolean isDeleted = Boolean.TRUE;
        if(notification != null){
            Alert confirmationAlert = AlertUtil.showConfirmationAlert("Delete notification","Deleting notification", "Are you sure you want to delete notification with ID " + notification.getId() + "?");
            confirmationAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.YES){
                try{
                    notificationRepository.delete(notification);
                    Log log = new Log.Builder()
                            .setUserId(userId)
                            .setOperation("Deleted notification")
                            .setTimestamp(LocalDateTime.now())
                            .setChangedField("Notification with ID: " + notification.getId())
                            .setNewValue("Notification deleted")
                            .build();
                    logRepository.save(log);
                    notifications.remove(notification);
                    searchNotifications();
                } catch (ConnectionToDatabaseException e) {
                    isDeleted = Boolean.FALSE;
                    log.error("Error while deleting notification, error {}", e.getMessage());
                    AlertUtil.showErrorAlert("Error while deleting notification", e.getMessage());
                }
            }
            if(Boolean.TRUE.equals(isDeleted)){
                AlertUtil.showInfoAlert("Notification deleted", "Notification with ID " + notification.getId() + " has been successfully deleted!");
            }

        }
    }

    /**
     * Searches notifications by message.
     */
    public void searchNotifications(){
        if(searchNotificationsTextField.getText().isEmpty()){
            notificationsTableView.setItems(FXCollections.observableArrayList(notifications));
        } else {
            notificationsTableView.setItems(FXCollections.observableArrayList(
                    notifications.stream().filter(notification -> notification.getMessage().contains(searchNotificationsTextField.getText())).toList()));
        }
    }
}
