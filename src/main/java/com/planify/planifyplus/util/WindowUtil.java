package com.planify.planifyplus.util;

import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Utilidad para forzar el redimensionado de ventanas en JavaFX.
 */
public class WindowUtil {

    private WindowUtil() {}

    /**
     * Fuerza la ventana a maximizarse correctamente.
     * Se utiliza para evitar problemas de tamaño al cambiar de escena.
     */
    public static void forceMaximize(Stage stage) {
        if (stage == null) return;

        // Forzar recalculado del tamaño al cambiar de Scene
        stage.setMaximized(false);
        stage.show();

        Platform.runLater(() -> {
            stage.setMaximized(true);
            stage.toFront();
        });
    }
}
