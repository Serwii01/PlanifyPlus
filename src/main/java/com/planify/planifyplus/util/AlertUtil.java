package com.planify.planifyplus.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Utilidad para mostrar alertas JavaFX con estilos e icono de la aplicación.
 */
public class AlertUtil {

    /** Ruta del CSS global usado por los diálogos. */
    private static final String CSS_PATH = "/css/styles.css";

    /** Icono usado en el diálogo y en la ventana. */
    private static final String ICON_PATH = "/img/images.png";

    /**
     * Crea una alerta base con el estilo e icono de la aplicación.
     */
    private static Alert baseAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ImageView icon = new ImageView(
                new Image(AlertUtil.class.getResourceAsStream(ICON_PATH))
        );
        icon.setFitWidth(48);
        icon.setFitHeight(48);
        alert.setGraphic(icon);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(AlertUtil.class.getResourceAsStream(ICON_PATH)));

        alert.getDialogPane().getStylesheets().add(
                AlertUtil.class.getResource(CSS_PATH).toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("planify-alert");

        Button okBtn = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okBtn != null) {
            okBtn.getStyleClass().add("planify-alert-button");
        }

        return alert;
    }

    /**
     * Muestra una alerta informativa.
     */
    public static void info(String title, String message) {
        baseAlert(Alert.AlertType.INFORMATION, title, message).showAndWait();
    }

    /**
     * Muestra una alerta de error.
     */
    public static void error(String title, String message) {
        baseAlert(Alert.AlertType.ERROR, title, message).showAndWait();
    }

    /**
     * Muestra una alerta de confirmación (Aceptar/Cancelar).
     *
     * @return true si el usuario acepta
     */
    public static boolean confirm(String title, String message) {
        Alert alert = baseAlert(Alert.AlertType.CONFIRMATION, title, message);

        ButtonType cancelar = new ButtonType("Cancelar", ButtonType.CANCEL.getButtonData());
        ButtonType aceptar = new ButtonType("Aceptar", ButtonType.OK.getButtonData());

        alert.getButtonTypes().setAll(cancelar, aceptar);

        Button btnAceptar = (Button) alert.getDialogPane().lookupButton(aceptar);
        if (btnAceptar != null) {
            btnAceptar.getStyleClass().add("planify-alert-button");
        }

        Button btnCancelar = (Button) alert.getDialogPane().lookupButton(cancelar);
        if (btnCancelar != null) {
            btnCancelar.getStyleClass().add("planify-alert-button");
        }

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == aceptar;
    }
}
