package hr.ekufrin.training.controller;


import hr.ekufrin.training.enums.SessionStatus;
import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.exception.SessionJoiningLeavingException;
import hr.ekufrin.training.exception.TrainingSessionFullException;
import hr.ekufrin.training.model.*;
import hr.ekufrin.training.repository.*;
import hr.ekufrin.training.util.AlertUtil;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * JavaFX controller for the dashboard.
 */
public class DashboardController {

    @FXML
    private Text welcomeNameText;
    @FXML
    private Text timeText;
    @FXML
    private Text roleText;

    @FXML
    private TableView<TrainingSession> sessionsTableView;
    @FXML
    private TableColumn<TrainingSession, Long> idSessionTableColumn;
    @FXML
    private TableColumn<TrainingSession, String> nameSessionTableColumn;
    @FXML
    private TableColumn<TrainingSession, String> dateTimeSessionTableColumn;
    @FXML
    private TableColumn<TrainingSession, Integer> durationSessionTableColumn;
    @FXML
    private TableColumn<TrainingSession, String> trainerSessionTableColumn;
    @FXML
    private TableColumn<TrainingSession, Integer> participantsSessionTableColumn;
    @FXML
    private TableColumn<TrainingSession, Integer> maxParticipantsSessionTableColumn;
    @FXML
    private TableColumn<TrainingSession, String> locationSessionTableColumn;
    @FXML
    private TableColumn<TrainingSession, String> statusSessionTableColumn;

    @FXML
    private TableView<Notification> notificationsTableView;
    @FXML
    private TableColumn<Notification, Long> idNotificationTableColumn;
    @FXML
    private TableColumn<Notification, String> messageNotificationTableColumn;
    @FXML
    private TableColumn<Notification, String> dateTimeNotificationTableColumn;
    @FXML
    private ContextMenu notificationsContextMenu;
    @FXML
    private ContextMenu sessionsContextMenu;

    private final Long userId = SessionManager.INSTANCE.getuserId();
    private final String employeeRole = SessionManager.INSTANCE.getUserRole();
    private Set<TrainingSession> trainingSessions = new HashSet<>();
    private final TrainingSessionRepository trainingSessionRepository = new TrainingSessionRepository();
    private final LogRepository logRepository = new LogRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final RegistrationRepository registrationRepository = new RegistrationRepository();

    /**
     * Initializes the dashboard based on user role.
     */
    public void initialize() {
        try {
            Optional<User> employeeOpt = Optional.ofNullable(new UserRepository().findById(userId));
            employeeOpt.ifPresentOrElse(this::updateUIWithEmployeeData, () -> welcomeNameText.setText("Welcome!"));
            Timeline timeline = new Timeline(new KeyFrame(Duration.minutes(1), _ -> employeeOpt.ifPresentOrElse(this::updateUIWithEmployeeData, () -> welcomeNameText.setText("Welcome!"))));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        } catch (ConnectionToDatabaseException e) {
            AlertUtil.showErrorAlert("Error while loading dashboard", e.getMessage());
        }


        if ("Employee".equalsIgnoreCase(employeeRole)) {
            MenuItem joinSessionMenuItem = new MenuItem("Join session");
            MenuItem leaveSessionMenuItem = new MenuItem("Leave session");
            MenuItem markNotificationAsReadMenuItem = new MenuItem("Mark as read");
            sessionsContextMenu.getItems().setAll(joinSessionMenuItem, leaveSessionMenuItem);
            notificationsContextMenu.getItems().setAll(markNotificationAsReadMenuItem);
            joinSessionMenuItem.setOnAction(_ -> {
                try {
                    joinSession();
                } catch (TrainingSessionFullException e) {
                    AlertUtil.showErrorAlert("Session is not available for joining", "Session is full!");
                }
            });
            leaveSessionMenuItem.setOnAction(_ -> leftSession());
            markNotificationAsReadMenuItem.setOnAction(_ -> markNotificationAsRead());
        }
    }

    /**
     * Updates the dashboard with employee data.
     * Shows welcome message, current date and time, role, active sessions and notifications.
     * @param user - employee or trainer
     */
    private void updateUIWithEmployeeData(User user) {
        LocalDateTime dateTime = LocalDateTime.now();
        welcomeNameText.setText(getGreetingMessage(user, dateTime));
        timeText.setText("Date & Time: " + dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm")));
        roleText.setText("Role: " + employeeRole);
        setSessionsTableView();
        setNotificationsTableView();
    }

    /**
     * Returns a greeting message based on the current time of day.
     * @param user - user first and last name
     * @param dateTime - current date and time
     * @return - greeting message
     */
    private String getGreetingMessage(User user, LocalDateTime dateTime) {
        if(dateTime.getHour() < 12) {return "Good morning, " + user;}
        else if(dateTime.getHour() < 18) {return "Good afternoon, " + user;}
        else {return "Good evening, " + user;}
    }

    /**
     * Leaves the selected session.
     */
    private void leftSession() {
        TrainingSession trainingSession = sessionsTableView.getSelectionModel().getSelectedItem();
        if (trainingSession != null && !trainingSession.getStatus().equals(SessionStatus.FINISHED)) {
            try {
                if (Boolean.TRUE.equals(registrationRepository.leaveSession(SessionManager.INSTANCE.getUser(), trainingSession))) {
                    trainingSession.sessionStatus();
                    AlertUtil.showInfoAlert("Session left", "You have successfully left session: " + trainingSession.getName());
                    logRepository.save(new Log.Builder().setUserId(userId).setOperation("Left session " + trainingSession.getName()).setChangedField("Session").setNewValue("Left").setTimestamp(LocalDateTime.now()).build());
                }
            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while leaving session", e.getMessage());
            } catch (SessionJoiningLeavingException e) {
                AlertUtil.showErrorAlert("Error while leaving session", "You are not in this session!");
            }
            setSessionsTableView();
        }
    }

    /**
     * Marks the selected notification as read.
     */
    private void markNotificationAsRead() {
        Notification notification = notificationsTableView.getSelectionModel().getSelectedItem();
        if (notification != null) {
            try {
                notificationRepository.markNotificationAsRead(userId, notification.getId());
            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while marking notification as read", e.getMessage());
            }
            setNotificationsTableView();
        }
    }

    /**
     * Joins the selected session.
     * @throws TrainingSessionFullException - if the session is full
     */
    private void joinSession() throws TrainingSessionFullException {
        TrainingSession trainingSession = sessionsTableView.getSelectionModel().getSelectedItem();
        if (trainingSession != null && trainingSession.getStatus().equals(SessionStatus.SCHEDULED)) {
            try {
                if (Boolean.TRUE.equals(registrationRepository.joinSession(SessionManager.INSTANCE.getUser(), trainingSession))) {
                    trainingSession.sessionStatus();
                    AlertUtil.showInfoAlert("Session joined", "You have successfully joined session: " + trainingSession.getName());
                    logRepository.save(new Log.Builder().setUserId(userId).setOperation("Joined session " + trainingSession.getName()).setChangedField("Session").setNewValue("Joined").setTimestamp(LocalDateTime.now()).build());
                }
            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while joining session", e.getMessage());
            } catch (SessionJoiningLeavingException e) {
                AlertUtil.showErrorAlert("Error while joining session", "You have already joined this session!");
            }
            setSessionsTableView();
        } else {
            throw new TrainingSessionFullException("Session is full!");
        }
    }

    /**
     * Sets the training sessions table view.
     */
    public void setSessionsTableView() {
        try {
            trainingSessions = new HashSet<>(trainingSessionRepository.findAllActiveSessions());
        } catch (ConnectionToDatabaseException e) {
            AlertUtil.showErrorAlert("Error while loading active sessions", e.getMessage());
        }
        sessionsTableView.getItems().setAll(trainingSessions);
        idSessionTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        nameSessionTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        dateTimeSessionTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().displayDateTime()));
        durationSessionTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDuration()));
        trainerSessionTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTrainer().toString()));
        participantsSessionTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getParticipants().size()));
        maxParticipantsSessionTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getMaxParticipants()));
        locationSessionTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocation()));
        statusSessionTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));
    }

    /**
     * Sets the notifications table view.
     */
    public void setNotificationsTableView() {
            notificationsTableView.getItems().clear();
            try {
                if ("Employee".equalsIgnoreCase(employeeRole)) {
                    notificationsTableView.getItems().addAll(notificationRepository.findAllForEmployee(userId));
                } else {
                    notificationsTableView.getItems().addAll(notificationRepository.findAll());
                }
            } catch (ConnectionToDatabaseException e) {
                AlertUtil.showErrorAlert("Error while loading notifications", e.getMessage());
            }
            idNotificationTableColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
            messageNotificationTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMessage()));
            dateTimeNotificationTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTimestamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm"))));
    }

}