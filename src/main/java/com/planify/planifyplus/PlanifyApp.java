package com.planify.planifyplus;

import com.planify.planifyplus.service.ActividadService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class PlanifyApp extends Application {

    private ActividadService actividadService = new ActividadService();

    @Override
    public void start(Stage primaryStage) throws Exception {

        actividadService.inicializarActividadesPredeterminadas();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/inicio.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        primaryStage.setTitle("PlanifyPlus - Inicio");
        primaryStage.setScene(scene);

        // ✅ Pantalla completa REAL (no ventana maximizada)
        primaryStage.setFullScreenExitHint(""); // quita el mensaje de "Pulsa ESC..."
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); // desactiva ESC
        primaryStage.show();
        Platform.runLater(() -> primaryStage.setFullScreen(true));

        primaryStage.setOnCloseRequest(event -> {
            actividadService.limpiarActividadesPredeterminadas();
            actividadService.cerrar();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
