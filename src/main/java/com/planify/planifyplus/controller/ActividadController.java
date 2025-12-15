package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.ActividadDAO;
import com.planify.planifyplus.dao.DenunciaActividadDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.UsuarioDTO;
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

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy");
    private final DateTimeFormatter formatoHora =
            DateTimeFormatter.ofPattern("HH:mm");

    private boolean mapaActualizado = false;

    private final ActividadDAO actividadDAO = new ActividadDAO();
    private final DenunciaActividadDAO denunciaDAO = new DenunciaActividadDAO();

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
        configurarInscripcionSegunSesion();
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

        lblPlazas.setText("1 / " + actividad.getAforo() + " personas inscritas");

        webEngine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED && !mapaActualizado) {
                actualizarMapaConActividad();
            }
        });

        configurarInscripcionSegunSesion();
        configurarBotonDenunciarSegunSesionYActividad();
    }

    private void actualizarMapaConActividad() {
        if (actividad == null || actividad.getLatitud() == null || actividad.getLongitud() == null) {
            System.out.println("No hay coordenadas para esta actividad");
            return;
        }

        double lat = actividad.getLatitud().doubleValue();
        double lng = actividad.getLongitud().doubleValue();
        String label = actividad.getUbicacion() != null ? actividad.getUbicacion() : "Ubicación";

        label = label.replace("'", "\\'").replace("\"", "\\\"");

        String script = String.format(
                "if (typeof window.updateMapLocation === 'function') {" +
                        "  window.updateMapLocation(%f, %f, '%s');" +
                        "} else { console.error('updateMapLocation no definida'); }",
                lat, lng, label
        );

        try {
            webEngine.executeScript(script);
            mapaActualizado = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configurarInscripcionSegunSesion() {
        boolean loggedIn = Sesion.getUsuarioActual() != null;

        // Admin: no se inscribe
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
        lblDebeIniciarSesion.setManaged(!loggedIn);

        btnInscribirse.setOnAction(e -> {
            if (!loggedIn) return;
            btnInscribirse.setText("Inscrito");
            btnInscribirse.setDisable(true);
        });
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

        // Admin no denuncia
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

        // Persistente (BD). Si existe -> "Ya denunciada" incluso tras reiniciar
        boolean yaDenunciadaBD = denunciaDAO.existeDenuncia(idUsuario, idActividad);

        if (yaDenunciadaBD) {
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
        if (usuario == null || actividad == null || actividad.getId() == null) return;
        if (Sesion.esAdmin()) return;

        long idUsuario = usuario.getId();
        long idActividad = actividad.getId();

        if (denunciaDAO.existeDenuncia(idUsuario, idActividad)) {
            btnDenunciar.setDisable(true);
            btnDenunciar.setText("Ya denunciada");
            return;
        }

        // 1) Guardar denuncia persistente
        denunciaDAO.crearDenuncia(idUsuario, idActividad);

        // 2) Incrementar contador en actividad (para ordenar en admin)
        actividadDAO.incrementarDenuncias(idActividad);

        btnDenunciar.setDisable(true);
        btnDenunciar.setText("Denuncia enviada");
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
