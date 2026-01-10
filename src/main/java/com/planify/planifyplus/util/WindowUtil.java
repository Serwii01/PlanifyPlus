package com.planify.planifyplus.util;
import javafx.application.Platform;
import javafx.stage.Stage;

public class WindowUtil {

    private WindowUtil() {}

    public static void forceMaximize(Stage stage) {
        if (stage == null) return;

        // Windows/JavaFX-proof: obliga a recalcular el tamaño al cambiar Scene
        stage.setMaximized(false);
        stage.show();

        Platform.runLater(() -> {
            stage.setMaximized(true);
            stage.toFront();
        });
    }
}
