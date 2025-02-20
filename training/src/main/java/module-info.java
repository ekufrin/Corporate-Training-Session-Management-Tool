module hr.ekufrin.training {
    requires javafx.fxml;
    requires jbcrypt;
    requires java.sql.rowset;
    requires org.slf4j;
    requires org.controlsfx.controls;


    opens hr.ekufrin.training to javafx.fxml;
    opens hr.ekufrin.training.model to javafx.fxml;
    opens hr.ekufrin.training.controller to javafx.fxml;
    exports hr.ekufrin.training;
    exports hr.ekufrin.training.controller;
}