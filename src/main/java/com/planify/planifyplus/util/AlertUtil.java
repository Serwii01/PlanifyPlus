package com.planify.planifyplus.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.Optional;

public class AlertUtil {

    private static final String CSS_PATH = "/css/styles.css";
    private static final String ICON_PATH = "/img/images.png";

    private static Alert baseAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        //  Icono de la app
        ImageView icon = new ImageView(
                new Image(AlertUtil.class.getResourceAsStream(ICON_PATH))
        );
        icon.setFitWidth(48);
        icon.setFitHeight(48);
        alert.setGraphic(icon);

        //  Icono en la ventana
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(AlertUtil.class.getResourceAsStream(ICON_PATH)));

        //  CSS de la app
        alert.getDialogPane().getStylesheets().add(
                AlertUtil.class.getResource(CSS_PATH).toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("planify-alert");

        // ✅ Estilado del botón
        alert.getDialogPane().lookupButton(ButtonType.OK)
                .getStyleClass().add("planify-alert-button");

        return alert;
    }

    // ==========================
    // TIPOS DE ALERTA
    // ==========================

    public static void info(String title, String message) {
        baseAlert(Alert.AlertType.INFORMATION, title, message).showAndWait();
    }

    public static void error(String title, String message) {
        baseAlert(Alert.AlertType.ERROR, title, message).showAndWait();
    }

    public static boolean confirm(String title, String message) {
        Alert alert = baseAlert(Alert.AlertType.CONFIRMATION, title, message);

        ButtonType cancelar = new ButtonType("Cancelar", ButtonType.CANCEL.getButtonData());
        ButtonType aceptar = new ButtonType("Aceptar", ButtonType.OK.getButtonData());

        alert.getButtonTypes().setAll(cancelar, aceptar);

        // estilos botones
        Button btnAceptar = (Button) alert.getDialogPane().lookupButton(aceptar);
        btnAceptar.getStyleClass().add("planify-alert-button");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == aceptar;
    }
}
