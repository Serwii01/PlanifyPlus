// src/main/java/com/planify/planifyplus/controller/ActividadController.java
package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.ActividadDAO;
import com.planify.planifyplus.dao.DenunciaActividadDAO;
import com.planify.planifyplus.dao.InscripcionDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.UsuarioDTO;
import com.planify.planifyplus.util.Sesion;
import com.planify.planifyplus.util.ViewUtil;
import com.planify.planifyplus.util.WindowUtil;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ActividadController {

    @FXML private WebView webViewMap;

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
    private boolean mapaActualizado = false;

    private int intentosMapa = 0;
    private static final int MAX_INTENTOS_MAPA = 6;

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
    private final DateTimeFormatter formatoHora =
            DateTimeFormatter.ofPattern("HH:mm");

    private final ActividadDAO actividadDAO = new ActividadDAO();
    private final DenunciaActividadDAO denunciaDAO = new DenunciaActividadDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

    @FXML
    public void initialize() {
        btnVolver.setOnAction(e -> volverAInicio());
        Platform.runLater(this::initMap);
    }

    private void initMap() {
        if (webViewMap == null) return;

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
                    
                    window.planifyMap = {
                        setLocation: function(lat, lon, zoom) {
                            var pos = [lat, lon];
                            map.setView(pos, zoom || 15);
                            if (window.activityMarker) map.removeLayer(window.activityMarker);
                            window.activityMarker = L.marker(pos)
                                .addTo(map)
                                .bindPopup('<b>🅿️ Ubicación de la actividad</b>')
                                .openPopup();
                        }
                    };
                </script>
            </body>
            </html>
            """;

        mapEngine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED) {
                mapaListo = true;
                actualizarMapaConActividadConReintento();
            }
        });

        mapEngine.loadContent(html);
    }

    public void setActividad(ActividadDTO actividad) {
        this.actividad = actividad;
        mapaActualizado = false;
        intentosMapa = 0;

        if (actividad == null) return;

        lblTitulo.setText(actividad.getTitulo());
        lblDescripcion.setText(actividad.getDescripcion());

        String tipoStr = actividad.getTipo().toString();
        lblTipo.setText(tipoStr.substring(0, 1).toUpperCase() + tipoStr.substring(1).toLowerCase());

        lblFecha.setText(actividad.getFechaHoraInicio().format(formatoFecha));
        lblHora.setText(actividad.getFechaHoraInicio().format(formatoHora));
        lblUbicacionCaja.setText(actividad.getUbicacion() != null ? actividad.getUbicacion() : "Sin ubicación");
        lblCiudadCaja.setText(actividad.getCiudad() != null ? actividad.getCiudad() : "Sin ciudad");

        if (actividad.getId() != null) {
            long inscritos = inscripcionDAO.contarInscritos(actividad.getId());
            lblPlazas.setText(inscritos + " / " + actividad.getAforo() + " personas inscritas");
        } else {
            lblPlazas.setText("0 / " + actividad.getAforo() + " personas inscritas");
        }

        // ✅ IMPORTANTÍSIMO: esto es lo que hace que dentro salga "Inscrito" si ya lo estás
        actualizarEstadoInscripcion();

        configurarBotonDenunciarSegunSesionYActividad();
        actualizarMapaConActividadConReintento();
    }

    private void actualizarEstadoInscripcion() {
        boolean loggedIn = Sesion.getUsuarioActual() != null;

        if (Sesion.esAdmin()) {
            btnInscribirse.setVisible(false);
            btnInscribirse.setManaged(false);
            lblDebeIniciarSesion.setVisible(false);
            lblDebeIniciarSesion.setManaged(false);
            return;
        }

        btnInscribirse.setVisible(true);
        btnInscribirse.setManaged(true);

        if (!loggedIn) {
            btnInscribirse.setDisable(true);
            btnInscribirse.setText("Inscribirse a esta actividad");
            btnInscribirse.setOnAction(null);

            lblDebeIniciarSesion.setVisible(true);
            lblDebeIniciarSesion.setManaged(true);
            return;
        }

        lblDebeIniciarSesion.setVisible(false);
        lblDebeIniciarSesion.setManaged(false);

        if (actividad == null || actividad.getId() == null) return;

        long userId = Sesion.getUsuarioActual().getId();
        boolean inscrito = inscripcionDAO.estaInscrito(userId, actividad.getId());

        if (inscrito) {
            btnInscribirse.setText("Inscrito ✓");
            btnInscribirse.setDisable(true);
            btnInscribirse.setOnAction(null);
        } else {
            btnInscribirse.setText("Inscribirse a esta actividad");

            long inscritos = inscripcionDAO.contarInscritos(actividad.getId());
            boolean lleno = inscritos >= actividad.getAforo();
            btnInscribirse.setDisable(lleno);

            btnInscribirse.setOnAction(e -> {
                long ahora = inscripcionDAO.contarInscritos(actividad.getId());
                if (ahora >= actividad.getAforo()) {
                    btnInscribirse.setDisable(true);
                    return;
                }

                inscripcionDAO.inscribir(Sesion.getUsuarioActual(), actividad);

                long nuevos = inscripcionDAO.contarInscritos(actividad.getId());
                lblPlazas.setText(nuevos + " / " + actividad.getAforo() + " personas inscritas");
                actualizarEstadoInscripcion();
            });
        }
    }

    private void actualizarMapaConActividadConReintento() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::actualizarMapaConActividadConReintento);
            return;
        }

        if (actividad == null) return;
        if (mapEngine == null) return;
        if (!mapaListo) return;
        if (mapaActualizado) return;

        boolean existePlanifyMap = false;
        try {
            Object res = mapEngine.executeScript("typeof planifyMap !== 'undefined'");
            if (res instanceof Boolean b) existePlanifyMap = b;
        } catch (Exception ignored) {}

        if (!existePlanifyMap) {
            reintentarMapa();
            return;
        }

        if (actividad.getLatitud() == null || actividad.getLongitud() == null) {
            try {
                mapEngine.executeScript("planifyMap.setLocation(40.4168, -3.7038, 12);");
                mapaActualizado = true;
            } catch (Exception e) {
                reintentarMapa();
            }
            return;
        }

        double lat = actividad.getLatitud().doubleValue();
        double lon = actividad.getLongitud().doubleValue();

        try {
            String jsCall = "planifyMap.setLocation(" + lat + ", " + lon + ", 16);";
            mapEngine.executeScript(jsCall);
            mapaActualizado = true;
        } catch (Exception e) {
            reintentarMapa();
        }
    }

    private void reintentarMapa() {
        if (intentosMapa >= MAX_INTENTOS_MAPA) return;
        intentosMapa++;

        PauseTransition pt = new PauseTransition(Duration.millis(180));
        pt.setOnFinished(e -> actualizarMapaConActividadConReintento());
        pt.play();
    }

    private void configurarBotonDenunciarSegunSesionYActividad() {
        if (btnDenunciar == null) return;

        UsuarioDTO usuario = Sesion.getUsuarioActual();
        boolean loggedIn = usuario != null;

        if (!loggedIn) {
            btnDenunciar.setVisible(true);
            btnDenunciar.setManaged(true);
            btnDenunciar.setDisable(true);
            btnDenunciar.setText("Inicia sesión para denunciar");
            return;
        }

        if (Sesion.esAdmin()) {
            btnDenunciar.setVisible(false);
            btnDenunciar.setManaged(false);
            return;
        }

        btnDenunciar.setVisible(true);
        btnDenunciar.setManaged(true);

        if (actividad == null || actividad.getId() == null) {
            btnDenunciar.setDisable(true);
            btnDenunciar.setText("No disponible");
            return;
        }

        long idUsuario = usuario.getId();
        long idActividad = actividad.getId();
        boolean yaDenunciada = denunciaDAO.existeDenuncia(idUsuario, idActividad);

        if (yaDenunciada) {
            btnDenunciar.setDisable(true);
            btnDenunciar.setText("Ya denunciada");
        } else {
            btnDenunciar.setDisable(false);
            btnDenunciar.setText("Denunciar actividad");
            btnDenunciar.setOnAction(e -> manejarDenuncia());
        }
    }

    private void manejarDenuncia() {
        UsuarioDTO usuario = Sesion.getUsuarioActual();
        if (usuario == null || actividad == null || actividad.getId() == null || Sesion.esAdmin()) return;

        long idUsuario = usuario.getId();
        long idActividad = actividad.getId();

        if (denunciaDAO.existeDenuncia(idUsuario, idActividad)) {
            btnDenunciar.setDisable(true);
            btnDenunciar.setText("Ya denunciada");
            return;
        }

        denunciaDAO.crearDenuncia(idUsuario, idActividad);
        actividadDAO.incrementarDenuncias(idActividad);

        btnDenunciar.setDisable(true);
        btnDenunciar.setText("Denuncia enviada ✓");
    }

    private void volverAInicio() {
        try {
            Stage stage = (Stage) webViewMap.getScene().getWindow();
            Parent root = ViewUtil.loadFXML(getClass(), "/vistas/Inicio.fxml");

            Scene scene = new Scene(root);
            var css = getClass().getResource("/css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.setTitle("PlanifyPlus - Inicio");
            WindowUtil.forceMaximize(stage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
