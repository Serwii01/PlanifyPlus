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

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Controlador de la vista de detalle de una actividad.
 */
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

    /** Actividad mostrada actualmente. */
    private ActividadDTO actividad;

    /** Motor JS del WebView para el mapa. */
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

    /**
     * Inicialización del controlador (JavaFX).
     */
    @FXML
    public void initialize() {
        btnVolver.setOnAction(e -> volverAInicio());
        Platform.runLater(this::initMap);
    }

    /**
     * Carga el HTML del mapa y deja el WebView listo para recibir coordenadas.
     */
    private void initMap() {
        if (webViewMap == null) return;

        mapEngine = webViewMap.getEngine();

        try {
            URL htmlUrl = getClass().getResource("/API/map-crear-actividad.html");

            if (htmlUrl != null) {
                mapEngine.load(htmlUrl.toExternalForm());
                System.out.println("✅ Cargando mapa desde: " + htmlUrl);
            } else {
                System.err.println("error");
                htmlUrl = getClass().getResource("/map-crear-actividad.html");
                if (htmlUrl != null) {
                    mapEngine.load(htmlUrl.toExternalForm());
                }
            }
        } catch (Exception e) {
            System.err.println("error cargando mapa: " + e.getMessage());
            e.printStackTrace();
        }

        mapEngine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED) {
                System.out.println("webView cargado");
                mapaListo = true;

                Platform.runLater(() -> {
                    try {
                        mapEngine.executeScript("if(typeof window.onWebViewReady === 'function') window.onWebViewReady();");
                        System.out.println("✅ onWebViewReady() ejecutado");
                    } catch (Exception e) {
                        System.err.println("⚠️ onWebViewReady no disponible: " + e.getMessage());
                    }
                });

                actualizarMapaConActividadConReintento();
            } else if (state == Worker.State.FAILED) {
                System.err.println("❌ Error cargando WebView");
            }
        });
    }

    /**
     * Carga la actividad en pantalla y refresca estado de botones/mapa.
     *
     * @param actividad actividad a mostrar
     */
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

        actualizarEstadoInscripcion();

        configurarBotonDenunciarSegunSesionYActividad();
        actualizarMapaConActividadConReintento();
    }

    /**
     * Ajusta el botón de inscripción según sesión, rol y aforo.
     */
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

    /**
     * Actualiza el mapa con la ubicación de la actividad, reintentando si el JS aún no está listo.
     */
    private void actualizarMapaConActividadConReintento() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::actualizarMapaConActividadConReintento);
            return;
        }

        if (actividad == null || mapEngine == null || !mapaListo || mapaActualizado) return;

        boolean existeFuncion = false;
        try {
            Object res = mapEngine.executeScript("typeof window.updateMapLocation");
            existeFuncion = "function".equals(res);
            System.out.println("🔍 Verificando updateMapLocation: " + res);
        } catch (Exception e) {
            System.err.println("⚠️ Error verificando función: " + e.getMessage());
        }

        if (!existeFuncion) {
            if (intentosMapa < MAX_INTENTOS_MAPA) {
                reintentarMapa();
            }
            return;
        }

        try {
            if (actividad.getLatitud() == null || actividad.getLongitud() == null) {
                mapEngine.executeScript("window.updateMapLocation(40.4168, -3.7038, 'Ubicación no especificada');");
                System.out.println("📍 Mostrando ubicación por defecto");
            } else {
                double lat = actividad.getLatitud().doubleValue();
                double lon = actividad.getLongitud().doubleValue();
                String label = actividad.getUbicacion() != null ? actividad.getUbicacion() : "Ubicación de la actividad";

                String jsCall = String.format(Locale.US,
                        "window.updateMapLocation(%f, %f, '%s');",
                        lat, lon, label.replace("'", "\\'"));

                mapEngine.executeScript(jsCall);
                System.out.println("✅ Mapa actualizado: lat=" + lat + ", lon=" + lon);
            }

            mapaActualizado = true;

        } catch (Exception e) {
            System.err.println("❌ Error actualizando mapa: " + e.getMessage());
            e.printStackTrace();
            reintentarMapa();
        }
    }

    /**
     * Programa un reintento corto para la actualización del mapa.
     */
    private void reintentarMapa() {
        if (intentosMapa >= MAX_INTENTOS_MAPA) return;
        intentosMapa++;

        PauseTransition pt = new PauseTransition(Duration.millis(180));
        pt.setOnFinished(e -> actualizarMapaConActividadConReintento());
        pt.play();
    }

    /**
     * Configura el botón de denuncia según sesión, rol y si ya existe denuncia.
     */
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

    /**
     * Registra la denuncia y actualiza el estado del botón.
     */
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

    /**
     * Vuelve a la pantalla de inicio.
     */
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
