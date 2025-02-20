package hr.ekufrin.training;

import hr.ekufrin.training.controller.LoginScreenController;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of the application
 */
public class HelloApplication extends Application {

    public static final Logger log = LoggerFactory.getLogger(HelloApplication.class);

    @Override
    public void start(Stage stage) {
        stage.setScene(new LoginScreenController().start());
        stage.setTitle("Training App by ekufrin");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {launch();}
}