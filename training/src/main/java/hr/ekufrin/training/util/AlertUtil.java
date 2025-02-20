package hr.ekufrin.training.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.controlsfx.control.Notifications;

/**
 * Utility class for showing alerts
 */
public class AlertUtil {

    private AlertUtil() {}

    /**
     * Shows an error alert
     * @param title - title of the alert
     * @param message - message of the alert
     */
    public static void showErrorAlert(String title, String message) {
        Platform.runLater(() -> {
            try{
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle(title);
                errorAlert.setHeaderText("An error occurred");
                errorAlert.setContentText(message);
                errorAlert.showAndWait();
            } catch (IllegalStateException e){
                Notifications.create()
                        .title(title)
                        .text(message)
                        .showError();
            }
        });

    }

    /**
     * Shows an error alert
     * @param title - title of the alert
     * @param header - header of the alert
     * @param message - message of the alert
     */
    public static void showErrorAlert(String title, String header, String message) {
        Platform.runLater(()->{
            try{
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle(title);
                errorAlert.setHeaderText(header);
                errorAlert.setContentText(message);
                errorAlert.showAndWait();
            }catch (IllegalStateException e){
                Notifications.create()
                        .title(title)
                        .text(header + " " + message)
                        .showError();
            }
        });
    }

    /**
     * Shows an information alert
     * @param title - title of the alert
     * @param message - message of the alert
     */
    public static void showInfoAlert(String title, String message) {
        Platform.runLater(() -> {
            try {
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle(title);
                infoAlert.setHeaderText("Information");
                infoAlert.setContentText(message);
                infoAlert.showAndWait();
            }catch (IllegalStateException e){
                Notifications.create()
                        .title(title)
                        .text(message)
                        .showInformation();
            }
        });
    }

    /**
     * Shows a confirmation alert
     * @param title  - title of the alert
     * @param header - header of the alert
     * @param message - message of the alert
     * @return - confirmation alert
     */
    public static Alert showConfirmationAlert(String title, String header, String message) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle(title);
        confirmationAlert.setHeaderText(header);
        confirmationAlert.setContentText(message);
        return confirmationAlert;
    }

    /**
     * Checks if the input is a string
     * @param input - input to check
     * @param message - message to append to errors if input is not string / is blank
     * @param errors - errors to append to
     */
    public static void checkStringInput(String input, String message, StringBuilder errors){
        if(input.isBlank()){
            errors.append(message);
        }
    }

    /**
     * Checks if the input is an integer
     * @param input - input to check
     * @param messageBlank - message to append to errors if input is blank
     * @param messageRange - message to append to errors if input is not in range
     * @param messageInvalidFormat - message to append to errors if input is not an integer
     * @param errors - errors to append to
     * @return - integer if input is valid, 0 otherwise
     */
    public static Integer checkIntegerInput(String input, String messageBlank, String messageRange, String messageInvalidFormat, StringBuilder errors){
        if(input.isBlank()){
            errors.append(messageBlank);
        } else {
            try {
                if(Integer.parseInt(input) > 0){
                    return Integer.parseInt(input);
                }else{
                    errors.append(messageRange);
                }
            } catch (NumberFormatException e) {
                errors.append(messageInvalidFormat);
            }
        }
        return 0;
    }

}
