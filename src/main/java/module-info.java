module com.planify.planifyplus {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.planify.planifyplus to javafx.fxml;
    exports com.planify.planifyplus;
}