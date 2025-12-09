package com.planify.planifyplus.controller;

import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.util.Sesion;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;

public class ActividadController {

    @FXML private WebView webViewMapa;
    @FXML private Label lblTipo;
    @FXML private Label lblTitulo;
    @FXML private Label lblDescripcion;
    @FXML private Label lblFecha;
    @FXML private Label lblHora;
    @FXML private Label lblUbicacionCaja;
    @FXML private Label lblCiudadCaja;
    @FXML private Label lblPlazas;
    @FXML private Label lblDebeIniciarSesion;
    @FXML private Button btnInscribirse;
    @FXML private Button btnDenunciar;
    @FXML private Button btnVolver;

    private ActividadDTO actividad;
    private WebEngine webEngine;

    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy");
    private final DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");

    // Variable para evitar múltiples ejecuciones del script
    private boolean mapaYaActualizado = false;

    @FXML
    public void initialize() {
        // Para la API
        webEngine = webViewMapa.getEngine();

        // Habilitar JavaScript console logs (para debugging)
        webEngine.setJavaScriptEnabled(true);

        // HTML de la API
        URL url = getClass().getResource("/API/map-crear-actividad.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
            System.out.println("Cargando mapa desde: " + url.toExternalForm());
        } else {
            System.err.println("ERROR: No se encontró el archivo map-crear-actividad.html");
        }

        // Para volver al inicio
        btnVolver.setOnAction(e -> volverAInicio());
        configurarInscripcionSegunSesion();
    }

    public void setActividad(ActividadDTO actividad) {
        this.actividad = actividad;
        if (actividad == null) return;

        lblTitulo.setText(actividad.getTitulo());
        lblDescripcion.setText(actividad.getDescripcion());

        // Tipo con color similar a Inicio
        String tipoStr = actividad.getTipo().toString();
        lblTipo.setText(
                tipoStr.substring(0, 1).toUpperCase() + tipoStr.substring(1).toLowerCase()
        );

        // Fecha y hora
        lblFecha.setText(actividad.getFechaHoraInicio().format(formatoFecha));
        lblHora.setText(actividad.getFechaHoraInicio().format(formatoHora));

        // Ubicación / ciudad
        String ubicacion = actividad.getUbicacion() != null ? actividad.getUbicacion() : "";
        String ciudad = actividad.getCiudad() != null ? actividad.getCiudad() : "";
        lblUbicacionCaja.setText(ubicacion);
        lblCiudadCaja.setText(ciudad);

        // Plazas (de momento 1 inscrito fijo como en el mockup)
        lblPlazas.setText("1 / " + actividad.getAforo() + " personas inscritas");

        // Cuando el HTML del mapa haya cargado, pasarle lat/lng
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED && !mapaYaActualizado) {
                actualizarMapa();
            } else if (newState == Worker.State.FAILED) {
                System.err.println("ERROR: Falló la carga del WebView");
            }
        });
    }

    private void actualizarMapa() {
        if (actividad == null || actividad.getLatitud() == null || actividad.getLongitud() == null) {
            System.out.println("⚠️ No hay coordenadas para mostrar en el mapa");
            return;
        }

        double lat = actividad.getLatitud().doubleValue();
        double lng = actividad.getLongitud().doubleValue();
        String label = actividad.getUbicacion() != null ? actividad.getUbicacion() : "Ubicación de la actividad";

        // Escapar comillas en el label
        label = label.replace("'", "\\'").replace("\"", "\\\"");

        System.out.println("📍 Actualizando mapa con coordenadas:");
        System.out.println("   Latitud: " + lat);
        System.out.println("   Longitud: " + lng);
        System.out.println("   Ubicación: " + label);

        // Llamar función JavaScript para centrar mapa y añadir marcador
        String script = String.format(
                "if (typeof updateMapLocation === 'function') { " +
                        "    updateMapLocation(%f, %f, '%s'); " +
                        "    console.log('✅ Mapa actualizado desde Java'); " +
                        "} else { " +
                        "    console.error('❌ Función updateMapLocation no encontrada'); " +
                        "}",
                lat, lng, label
        );

        try {
            Object result = webEngine.executeScript(script);
            System.out.println("✅ Script ejecutado correctamente. Resultado: " + result);
            mapaYaActualizado = true;
        } catch (Exception e) {
            System.err.println("❌ Error al ejecutar script de mapa: " + e.getMessage());
            e.printStackTrace();

            // Intentar de nuevo después de un pequeño delay
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    javafx.application.Platform.runLater(() -> {
                        try {
                            webEngine.executeScript(script);
                            System.out.println("✅ Script ejecutado en segundo intento");
                            mapaYaActualizado = true;
                        } catch (Exception ex) {
                            System.err.println("❌ Error en segundo intento: " + ex.getMessage());
                        }
                    });
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }).start();
        }
    }

    private void configurarInscripcionSegunSesion() {
        // Detecta si el user está logueado
        boolean loggedIn = Sesion.getUsuarioActual() != null;

        // Visibilidad de los botones dependiendo de la sesión
        btnInscribirse.setDisable(!loggedIn);
        lblDebeIniciarSesion.setVisible(!loggedIn);

        if (loggedIn) {
            lblDebeIniciarSesion.setManaged(false);
        }

        // Simple estética, sin terminar de implementar
        btnInscribirse.setOnAction(e -> {
            if (!loggedIn) return;
            btnInscribirse.setText("Inscrito");
            btnInscribirse.setDisable(true);
        });
    }

    // Método para volver a inicio
    private void volverAInicio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/inicio.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) webViewMapa.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
