package com.planify.planifyplus;

import com.planify.planifyplus.service.ActividadService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PlanifyApp extends Application {

    private ActividadService actividadService = new ActividadService();

    @Override
    public void start(Stage primaryStage) throws Exception {
        // API key de Google Maps para GMapsFX
        System.setProperty("gmapsfx.api.key", "AIzaSyAZC1YxJ7ZDdVyI6NBaA1TYj2TDdPC_3tg"); // [web:14][web:91]

        // Crear actividades predeterminadas al iniciar la aplicación
        actividadService.inicializarActividadesPredeterminadas();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/inicio.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        primaryStage.setTitle("PlanifyPlus - Inicio");
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(event -> {
            actividadService.limpiarActividadesPredeterminadas();
            actividadService.cerrar();
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
