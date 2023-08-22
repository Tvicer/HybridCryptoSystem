module com.example.cryptocoursework {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.cryptocoursework to javafx.fxml;
    exports com.example.cryptocoursework;
}