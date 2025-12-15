package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.InscripcionDAO;
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

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

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

    private boolean mapaCargadoYListo = false;

    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

    @FXML
    public void initialize() {
        webEngine = webViewMapa.getEngine();
        webEngine.setJavaScriptEnabled(true);

        URL url = getClass().getResource("/API/map-crear-actividad.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
            System.out.println("Cargando mapa desde: " + url.toExternalForm());
        } else {
            System.err.println("No se encontró /API/map-crear-actividad.html");
        }

        btnVolver.setOnAction(e -> volverAInicio());

        webEngine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED) {
                webEngine.executeScript("window.onWebViewReady();");
                mapaCargadoYListo = true;

                if (actividad != null) {
                    actualizarMapaConActividad();
                }
            }
        });
    }

    public void setActividad(ActividadDTO actividad) {
        this.actividad = actividad;
        if (actividad == null) return;

        lblTitulo.setText(actividad.getTitulo());
        lblDescripcion.setText(actividad.getDescripcion());

        String tipoStr = actividad.getTipo().toString();
        lblTipo.setText(tipoStr.substring(0, 1).toUpperCase() + tipoStr.substring(1).toLowerCase());

        lblFecha.setText(actividad.getFechaHoraInicio().format(formatoFecha));
        lblHora.setText(actividad.getFechaHoraInicio().format(formatoHora));

        String ubicacion = actividad.getUbicacion() != null ? actividad.getUbicacion() : "";
        String ciudad = actividad.getCiudad() != null ? actividad.getCiudad() : "";
        lblUbicacionCaja.setText(ubicacion);
        lblCiudadCaja.setText(ciudad);

        actualizarPlazas();
        configurarInscripcionSegunSesion();

        if (mapaCargadoYListo) {
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
        if (actividad == null || actividad.getLatitud() == null || actividad.getLongitud() == null) {
            System.out.println("No hay coordenadas para esta actividad");
            return;
        }

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
    }

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
