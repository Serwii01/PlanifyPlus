package com.planify.planifyplus.controller;

<<<<<<< HEAD
import com.planify.planifyplus.dao.InscripcionDAO;
=======
import com.planify.planifyplus.dao.ActividadDAO;
import com.planify.planifyplus.dao.DenunciaActividadDAO;
>>>>>>> origin/main
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.UsuarioDTO;
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
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
<<<<<<< HEAD
import java.util.Timer;
import java.util.TimerTask;
=======
>>>>>>> origin/main

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
    private boolean mapaActualizado = false; // DE IVAN

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
    private final DateTimeFormatter formatoHora =
            DateTimeFormatter.ofPattern("HH:mm");

    private final ActividadDAO actividadDAO = new ActividadDAO();
    private final DenunciaActividadDAO denunciaDAO = new DenunciaActividadDAO();

    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

    @FXML
    public void initialize() {
        Platform.runLater(this::initMap);
        btnVolver.setOnAction(e -> volverAInicio());
<<<<<<< HEAD

        webEngine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED) {
                webEngine.executeScript("window.onWebViewReady();");
                mapaCargadoYListo = true;

=======
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
                    console.log('Mapa listo');
                </script>
            </body>
            </html>
            """;

        mapEngine.loadContent(html);

        mapEngine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED) {
                mapaListo = true;
                System.out.println("✅ Mapa listo");
>>>>>>> origin/main
                if (actividad != null) {
                    actualizarMapaConActividad();
                }
            }
        });
    }

    public void setActividad(ActividadDTO actividad) {
        System.out.println("📍 Actividad: " + actividad.getTitulo());
        this.actividad = actividad;
        if (actividad == null) return;

<<<<<<< HEAD
=======
        // UI
>>>>>>> origin/main
        lblTitulo.setText(actividad.getTitulo());
        lblDescripcion.setText(actividad.getDescripcion());

        String tipoStr = actividad.getTipo().toString();
        lblTipo.setText(tipoStr.substring(0, 1).toUpperCase() + tipoStr.substring(1).toLowerCase());
<<<<<<< HEAD

        lblFecha.setText(actividad.getFechaHoraInicio().format(formatoFecha));
        lblHora.setText(actividad.getFechaHoraInicio().format(formatoHora));

        String ubicacion = actividad.getUbicacion() != null ? actividad.getUbicacion() : "";
        String ciudad = actividad.getCiudad() != null ? actividad.getCiudad() : "";
        lblUbicacionCaja.setText(ubicacion);
        lblCiudadCaja.setText(ciudad);

        actualizarPlazas();
        configurarInscripcionSegunSesion();

        if (mapaCargadoYListo) {
=======
        lblFecha.setText(actividad.getFechaHoraInicio().format(formatoFecha));
        lblHora.setText(actividad.getFechaHoraInicio().format(formatoHora));
        lblUbicacionCaja.setText(actividad.getUbicacion() != null ? actividad.getUbicacion() : "Sin ubicación");
        lblCiudadCaja.setText(actividad.getCiudad() != null ? actividad.getCiudad() : "Sin ciudad");
        lblPlazas.setText("1 / " + actividad.getAforo() + " personas inscritas");

        System.out.println("📍 Lat: " + (actividad.getLatitud() != null ? actividad.getLatitud() : "NULL") +
                          " Lon: " + (actividad.getLongitud() != null ? actividad.getLongitud() : "NULL"));

        mapEngine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED && !mapaActualizado) {
                actualizarMapaConActividad();
            }
        });

        configurarInscripcionSegunSesion();
        configurarBotonDenunciarSegunSesionYActividad();
        
        if (mapaListo) {
>>>>>>> origin/main
            actualizarMapaConActividad();
        }
    }

    private void actualizarPlazas() {
        if (actividad == null || actividad.getId() == null) return;
        long inscritos = inscripcionDAO.contarInscritos(actividad.getId());
        lblPlazas.setText(inscritos + " / " + actividad.getAforo() + " personas inscritas");
        btnInscribirse.setDisable(inscritos >= actividad.getAforo() && !usuarioYaInscrito());
    }

    private boolean usuarioYaInscrito() {
        if (!Sesion.haySesion() || actividad == null) return false;
        return inscripcionDAO.estaInscrito(Sesion.getIdUsuario(), actividad.getId());
    }

    private void configurarInscripcionSegunSesion() {
        boolean loggedIn = Sesion.getUsuarioActual() != null;

        btnInscribirse.setDisable(!loggedIn);
        lblDebeIniciarSesion.setVisible(!loggedIn);
        if (loggedIn) {
            lblDebeIniciarSesion.setManaged(false);
        }

        if (!loggedIn || actividad == null || actividad.getId() == null) return;

        refrescarBotonInscripcion();

        btnInscribirse.setOnAction(e -> {
            long userId = Sesion.getIdUsuario();
            long actId = actividad.getId();

            boolean inscrito = inscripcionDAO.estaInscrito(userId, actId);

            if (inscrito) {
                inscripcionDAO.cancelar(userId, actId);
            } else {
                long inscritos = inscripcionDAO.contarInscritos(actId);
                if (inscritos >= actividad.getAforo()) {
                    return;
                }
                inscripcionDAO.inscribir(Sesion.getUsuarioActual(), actividad);
            }

            refrescarBotonInscripcion();
            actualizarPlazas();
        });
    }

    private void refrescarBotonInscripcion() {
        boolean inscrito = usuarioYaInscrito();
        btnInscribirse.setText(inscrito ? "Cancelar inscripción" : "Inscribirse");
    }

    private void actualizarMapaConActividad() {
        if (!mapaListo || actividad == null) return;

        if (actividad.getLatitud() == null || actividad.getLongitud() == null) {
            System.out.println("⚠️ Sin coords, Madrid");
            mapEngine.executeScript("planifyMap.setLocation(40.4168, -3.7038, 12);");
            return;
        }

<<<<<<< HEAD
        String latStr = String.format(Locale.US, "%.6f", actividad.getLatitud().doubleValue());
        String lngStr = String.format(Locale.US, "%.6f", actividad.getLongitud().doubleValue());

        String label = (actividad.getUbicacion() != null ? actividad.getUbicacion() : "Ubicación")
                .replace("'", "\\'").replace("\"", "\\\"");

        String script = String.format(
                "if (typeof window.updateMapLocation === 'function') {" +
                        "  window.updateMapLocation(%s, %s, '%s');" +
                        "} else { console.error('updateMapLocation no definida'); }",
                latStr, lngStr, label
        );

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    try {
                        webEngine.executeScript(script);
                        System.out.println("-> Coordenadas inyectadas después de 200ms: " + latStr + ", " + lngStr);
                    } catch (Exception e) {
                        System.err.println("Error inyectando script: " + e.getMessage());
                    }
                });
            }
        }, 200);
=======
        double lat = actividad.getLatitud().doubleValue();
        double lon = actividad.getLongitud().doubleValue();
        System.out.println("🎯 Mapa: " + lat + ", " + lon);

        try {
            String jsCall = "planifyMap.setLocation(" + lat + ", " + lon + ", 16);";
            mapEngine.executeScript(jsCall);
            mapaActualizado = true;
        } catch (Exception e) {
            System.err.println("❌ JS Error: " + e.getMessage());
        }
    }

    private void configurarInscripcionSegunSesion() {
        boolean loggedIn = Sesion.getUsuarioActual() != null;

        // ADMIN NO SE INSCRIBE (DE IVAN)
        if (Sesion.esAdmin()) {
            btnInscribirse.setVisible(false);
            btnInscribirse.setManaged(false);
            lblDebeIniciarSesion.setVisible(false);
            lblDebeIniciarSesion.setManaged(false);
            return;
        }

        btnInscribirse.setVisible(true);
        btnInscribirse.setManaged(true);
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
>>>>>>> origin/main
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/inicio.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) webViewMap.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("PlanifyPlus - Inicio");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
