module com.magnariuk.lemonkatoolsubs {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires com.google.gson;
    requires java.desktop;

    opens com.magnariuk.lemonkatoolsubs to javafx.fxml;


    opens com.magnariuk.lemonkatoolsubs.data.classes to com.google.gson;

    exports com.magnariuk.lemonkatoolsubs;

}