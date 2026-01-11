package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.ActividadDAO;
import com.planify.planifyplus.dao.InscripcionDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.TipoActividad;
import com.planify.planifyplus.util.AlertUtil;
import com.planify.planifyplus.util.Sesion;
import com.planify.planifyplus.util.ViewUtil;
import com.planify.planifyplus.util.WindowUtil;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CrearActividadController {

    @FXML private TextField txtTitulo;
    @FXML private TextArea txtDescripcion;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtHora;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtUbicacion;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtAforo;
    @FXML private TextField txtLatitud;
    @FXML private TextField txtLongitud;
    @FXML private WebView webViewBuscador;
    @FXML private javafx.scene.control.Button btnGuardar;
    @FXML private Label lblTituloPagina;

    private final ActividadDAO actividadDAO = new ActividadDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

    private ActividadDTO actividadAEditar = null;
    private WebEngine webEngine;

    @FXML
    public void initialize() {
        cmbTipo.getItems().addAll("DEPORTIVA", "CULTURAL", "TALLER");
        inicializarBuscadorMapa();
    }

    private void inicializarBuscadorMapa() {
        webEngine = webViewBuscador.getEngine();
        webEngine.setJavaScriptEnabled(true);

        String htmlUrl = getClass().getResource("/API/map-buscador-ubicacion.html").toExternalForm();
        webEngine.load(htmlUrl);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaApp", new JavaScriptBridge());
                    iniciarPollingUbicacion();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        txtCiudad.textProperty().addListener((obs, old, newVal) -> {
            if (webEngine != null && newVal != null && !newVal.trim().isEmpty()) {
                try {
                    webEngine.executeScript(
                            "if(typeof setCityContext === 'function') setCityContext('" +
                                    newVal.replace("'", "\\'") + "');"
                    );
                } catch (Exception ignored) {}
            }
        });
    }

    // ========== POLLING UBICACIÓN ==========
    private Timeline pollingTimeline;
    private String ultimaUbicacion = "";

    private void iniciarPollingUbicacion() {
        if (pollingTimeline != null) pollingTimeline.stop();

        pollingTimeline = new Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.millis(500),
                        event -> verificarUbicacionSeleccionada()
                )
        );
        pollingTimeline.setCycleCount(Timeline.INDEFINITE);
        pollingTimeline.play();
    }

    private void verificarUbicacionSeleccionada() {
        try {
            Object ubicacion = webEngine.executeScript(
                    "document.getElementById('locationText') ? document.getElementById('locationText').textContent : ''"
            );

            if (ubicacion != null && !ubicacion.toString().trim().isEmpty()) {
                String ubicacionStr = ubicacion.toString().trim();
                if (!ubicacionStr.equals(ultimaUbicacion)) {
                    ultimaUbicacion = ubicacionStr;

                    Object latObj = webEngine.executeScript("window.selectedLat");
                    Object lonObj = webEngine.executeScript("window.selectedLon");

                    if (latObj != null && lonObj != null) {
                        double lat = Double.parseDouble(latObj.toString());
                        double lon = Double.parseDouble(lonObj.toString());

                        javafx.application.Platform.runLater(() -> {
                            txtUbicacion.setText(ubicacionStr);
                            txtLatitud.setText(String.valueOf(lat));
                            txtLongitud.setText(String.valueOf(lon));
                        });
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    // ========== JS BRIDGE ==========
    public class JavaScriptBridge {
        public void onLocationSelected(String displayName, double lat, double lon) {
            javafx.application.Platform.runLater(() -> {
                txtUbicacion.setText(displayName);
                txtLatitud.setText(String.valueOf(lat));
                txtLongitud.setText(String.valueOf(lon));
            });
        }
    }

    // ========== EDICIÓN ==========
    public void setActividadParaEditar(ActividadDTO actividad) {
        this.actividadAEditar = actividad;

        txtTitulo.setText(actividad.getTitulo());
        txtDescripcion.setText(actividad.getDescripcion());
        dpFecha.setValue(actividad.getFechaHoraInicio().toLocalDate());

        LocalTime hora = actividad.getFechaHoraInicio().toLocalTime();
        txtHora.setText(String.format("%02d:%02d", hora.getHour(), hora.getMinute()));

        cmbTipo.setValue(actividad.getTipo().toString());
        txtUbicacion.setText(actividad.getUbicacion());
        txtCiudad.setText(actividad.getCiudad());
        txtAforo.setText(String.valueOf(actividad.getAforo()));

        if (actividad.getLatitud() != null) txtLatitud.setText(actividad.getLatitud().toString());
        if (actividad.getLongitud() != null) txtLongitud.setText(actividad.getLongitud().toString());

        if (lblTituloPagina != null) lblTituloPagina.setText("Editar Actividad");
        if (btnGuardar != null) btnGuardar.setText("Actualizar Actividad");
    }

    // ========== GUARDAR ==========
    @FXML
    private void handleGuardarActividad() {
        if (txtTitulo.getText().trim().isEmpty()) { AlertUtil.error("Campo obligatorio", "El título es obligatorio."); return; }
        if (txtDescripcion.getText().trim().isEmpty()) { AlertUtil.error("Campo obligatorio", "La descripción es obligatoria."); return; }
        if (dpFecha.getValue() == null) { AlertUtil.error("Campo obligatorio", "La fecha es obligatoria."); return; }
        if (txtHora.getText().trim().isEmpty()) { AlertUtil.error("Campo obligatorio", "La hora es obligatoria."); return; }
        if (cmbTipo.getValue() == null) { AlertUtil.error("Campo obligatorio", "Debes seleccionar un tipo de actividad."); return; }
        if (txtUbicacion.getText().trim().isEmpty()) { AlertUtil.error("Campo obligatorio", "Debes seleccionar una ubicación del buscador."); return; }
        if (txtCiudad.getText().trim().isEmpty()) { AlertUtil.error("Campo obligatorio", "La ciudad es obligatoria."); return; }
        if (txtAforo.getText().trim().isEmpty()) { AlertUtil.error("Campo obligatorio", "El aforo es obligatorio."); return; }

        try {
            String[] horaPartes = txtHora.getText().split(":");
            if (horaPartes.length != 2) { AlertUtil.error("Hora inválida", "Formato de hora inválido. Usa HH:mm (ej. 18:30)."); return; }

            int hora = Integer.parseInt(horaPartes[0]);
            int minutos = Integer.parseInt(horaPartes[1]);

            if (hora < 0 || hora > 23 || minutos < 0 || minutos > 59) {
                AlertUtil.error("Hora inválida", "Hora o minutos inválidos.");
                return;
            }

            LocalDateTime fechaHora = dpFecha.getValue().atTime(hora, minutos);

            // ✅ NO PERMITIR FECHAS PASADAS O IGUAL A AHORA
            if (!fechaHora.isAfter(LocalDateTime.now())) {
                AlertUtil.error("Fecha inválida", "La actividad debe tener una fecha y hora futura.");
                return;
            }

            int aforo = Integer.parseInt(txtAforo.getText());
            if (aforo <= 0) {
                AlertUtil.error("Aforo inválido", "El aforo debe ser mayor que 0.");
                return;
            }

            boolean esEdicion = (actividadAEditar != null);

            ActividadDTO actividad = esEdicion ? actividadAEditar : new ActividadDTO();

            if (!esEdicion) {
                actividad.setPredeterminada(false);
                actividad.setCreador(Sesion.getUsuarioActual());
                actividad.setCreadoEn(LocalDateTime.now());
            }

            actividad.setTitulo(txtTitulo.getText().trim());
            actividad.setDescripcion(txtDescripcion.getText().trim());
            actividad.setFechaHoraInicio(fechaHora);
            actividad.setTipo(TipoActividad.valueOf(cmbTipo.getValue()));
            actividad.setUbicacion(txtUbicacion.getText().trim());
            actividad.setCiudad(txtCiudad.getText().trim());
            actividad.setAforo(aforo);

            if (!txtLatitud.getText().trim().isEmpty())
                actividad.setLatitud(new BigDecimal(txtLatitud.getText()));
            if (!txtLongitud.getText().trim().isEmpty())
                actividad.setLongitud(new BigDecimal(txtLongitud.getText()));

            actividadDAO.guardar(actividad);

            if (!esEdicion && Sesion.haySesion() && !Sesion.esAdmin()) {
                inscripcionDAO.inscribir(Sesion.getUsuarioActual(), actividad);
            }

            AlertUtil.info(
                    "Éxito",
                    esEdicion
                            ? "Actividad actualizada correctamente."
                            : "Actividad creada correctamente.\n\n(Ya estás inscrito)"
            );

            irAInicio();

        } catch (NumberFormatException nfe) {
            AlertUtil.error("Formato inválido", "Formato de hora o aforo inválido.");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.error("Error", "Error al guardar la actividad.\n\nDetalle: " + e.getMessage());
        }
    }

    @FXML
    private void handleVolver() {
        irAInicio();
    }

    private void irAInicio() {
        try {
            Stage stage = (Stage) btnGuardar.getScene().getWindow();
            Parent root = ViewUtil.loadFXML(getClass(), "/vistas/Inicio.fxml");
            Scene scene = new Scene(root);

            var css = getClass().getResource("/css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            WindowUtil.forceMaximize(stage);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.error("Error", "No se pudo volver a la pantalla de inicio.");
        }
    }
}
