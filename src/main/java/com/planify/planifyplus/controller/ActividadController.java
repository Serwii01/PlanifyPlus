package com.planify.planifyplus.controller;

import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.util.Sesion;
import javafx.application.Platform;
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
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ActividadController {

    @FXML private WebView webViewMap;

    // UI
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
    private WebEngine mapEngine;
    private boolean mapaListo = false;

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
    private final DateTimeFormatter formatoHora =
            DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        Platform.runLater(this::initMap);
        btnVolver.setOnAction(e -> volverAInicio());
        configurarInscripcionSegunSesion();
    }

    private void initMap() {
        mapEngine = webViewMap.getEngine();
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8" />
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <style>
                    html, body, #map { height: 100%; margin: 0; padding: 0; border-radius: 12px; }
                    #map { background: #f8fafc; }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var map = L.map('map').setView([40.4168, -3.7038], 13);
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '© OpenStreetMap contributors'
                    }).addTo(map);
                    
                    // Función pública para centrar en coordenadas
                    window.planifyMap = {
                        setLocation: function(lat, lon, zoom) {
                            var pos = [lat, lon];
                            map.setView(pos, zoom || 15);
                            
                            // Remover marcador anterior si existe
                            if (window.activityMarker) {
                                map.removeLayer(window.activityMarker);
                            }
                            
                            // Añadir nuevo marcador
                            window.activityMarker = L.marker(pos)
                                .addTo(map)
                                .bindPopup('<b>Ubicación de la actividad</b><br>')
                                .openPopup();
                        },
                        getCenter: function() {
                            return map.getCenter();
                        }
                    };
                    
                    console.log('Mapa listo para recibir coordenadas');
                </script>
            </body>
            </html>
            """;

        mapEngine.loadContent(html);

        // Listener crítico: cuando el mapa esté listo
        mapEngine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED) {
                mapaListo = true;
                System.out.println("✅ Mapa WebView completamente listo");

                // Centrar inmediatamente si ya tenemos la actividad
                if (actividad != null) {
                    actualizarMapaConActividad();
                }
            }
        });
    }

    public void setActividad(ActividadDTO actividad) {
        System.out.println("📍 Recibiendo actividad: " + actividad.getTitulo());
        this.actividad = actividad;

        if (actividad == null) {
            System.out.println("⚠️ Actividad null");
            return;
        }

        // Actualizar UI
        lblTitulo.setText(actividad.getTitulo());
        lblDescripcion.setText(actividad.getDescripcion());

        String tipoStr = actividad.getTipo().toString();
        lblTipo.setText(tipoStr.substring(0, 1).toUpperCase() + tipoStr.substring(1).toLowerCase());

        lblFecha.setText(actividad.getFechaHoraInicio().format(formatoFecha));
        lblHora.setText(actividad.getFechaHoraInicio().format(formatoHora));

        lblUbicacionCaja.setText(actividad.getUbicacion() != null ? actividad.getUbicacion() : "Sin ubicación");
        lblCiudadCaja.setText(actividad.getCiudad() != null ? actividad.getCiudad() : "Sin ciudad");

        lblPlazas.setText("1 / " + actividad.getAforo() + " personas inscritas");

        System.out.println("📍 Coordenadas recibidas - Lat: " +
                (actividad.getLatitud() != null ? actividad.getLatitud() : "NULL") +
                ", Lon: " + (actividad.getLongitud() != null ? actividad.getLongitud() : "NULL"));

        // Actualizar mapa si está listo
        if (mapaListo) {
            actualizarMapaConActividad();
        }
    }

    private void actualizarMapaConActividad() {
        if (!mapaListo || actividad == null) {
            System.out.println("⏳ Esperando mapa listo o actividad null");
            return;
        }

        if (actividad.getLatitud() == null || actividad.getLongitud() == null) {
            System.out.println("⚠️ Sin coordenadas válidas, centrando en Madrid");
            // Fallback a Madrid
            mapEngine.executeScript("planifyMap.setLocation(40.4168, -3.7038, 12);");
            return;
        }

        double lat = actividad.getLatitud().doubleValue();
        double lon = actividad.getLongitud().doubleValue();

        System.out.println("🎯 Centrando mapa en: " + lat + ", " + lon);

        // EJECUTAR JavaScript para centrar
        String jsCall = "planifyMap.setLocation(" + lat + ", " + lon + ", 16);";
        mapEngine.executeScript(jsCall);
    }

    private void configurarInscripcionSegunSesion() {
        boolean loggedIn = Sesion.getUsuarioActual() != null;
        btnInscribirse.setDisable(!loggedIn);
        lblDebeIniciarSesion.setVisible(!loggedIn);
        if (loggedIn) {
            lblDebeIniciarSesion.setManaged(false);
        }
        btnInscribirse.setOnAction(e -> {
            if (!loggedIn) return;
            btnInscribirse.setText("Inscrito ✓");
            btnInscribirse.setStyle("-fx-background-color: #10b981;");
            btnInscribirse.setDisable(true);
        });
    }

    private void volverAInicio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/inicio.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) webViewMap.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("PlanifyPlus - Inicio");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("❌ Error volver inicio: " + ex.getMessage());
        }
    }
}
