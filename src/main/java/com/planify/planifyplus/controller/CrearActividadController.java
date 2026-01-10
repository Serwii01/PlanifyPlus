package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.ActividadDAO;
import com.planify.planifyplus.dao.InscripcionDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.TipoActividad;
import com.planify.planifyplus.util.Sesion;
import com.planify.planifyplus.util.ViewUtil;
import com.planify.planifyplus.util.WindowUtil;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    @FXML private Button btnGuardar;
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
        System.out.println("🗺️ Cargando buscador desde: " + htmlUrl);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            System.out.println("📡 Estado del WebView: " + newState);

            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                System.out.println("✅ WebView cargado correctamente");

                javafx.application.Platform.runLater(() -> {
                    try {
                        JSObject window = (JSObject) webEngine.executeScript("window");
                        window.setMember("javaApp", new JavaScriptBridge());
                        System.out.println("✅ JavaScriptBridge registrado correctamente");

                        Object result = webEngine.executeScript("typeof javaApp");
                        System.out.println("🔍 Tipo de javaApp: " + result);

                        if (txtCiudad.getText() != null && !txtCiudad.getText().trim().isEmpty()) {
                            String jsCall = "if(typeof setCityContext === 'function') setCityContext('" +
                                    txtCiudad.getText().replace("'", "\\'") + "');";
                            webEngine.executeScript(jsCall);
                            System.out.println("📍 Contexto de ciudad establecido: " + txtCiudad.getText());
                        }

                        // NUEVO: Crear un polling para verificar si hay ubicación seleccionada
                        iniciarPollingUbicacion();

                    } catch (Exception e) {
                        System.err.println("❌ Error registrando JavaScriptBridge: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } else if (newState == javafx.concurrent.Worker.State.FAILED) {
                System.err.println("❌ Error cargando WebView buscador");
            }
        });

        webEngine.load(htmlUrl);

        txtCiudad.textProperty().addListener((obs, old, newVal) -> {
            if (webEngine != null && newVal != null && !newVal.trim().isEmpty()) {
                try {
                    String jsCall = "if(typeof setCityContext === 'function') setCityContext('" +
                            newVal.replace("'", "\\'") + "');";
                    webEngine.executeScript(jsCall);
                    System.out.println("📍 Contexto de ciudad actualizado: " + newVal);
                } catch (Exception e) {
                    System.err.println("⚠️ Error actualizando ciudad: " + e.getMessage());
                }
            }
        });
    }

    // NUEVO: Método para hacer polling de la ubicación seleccionada
    private javafx.animation.Timeline pollingTimeline;

    private void iniciarPollingUbicacion() {
        if (pollingTimeline != null) {
            pollingTimeline.stop();
        }

        pollingTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.millis(500),
                        event -> verificarUbicacionSeleccionada()
                )
        );
        pollingTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        pollingTimeline.play();
        System.out.println("🔄 Polling de ubicación iniciado");
    }

    private String ultimaUbicacion = "";

    private void verificarUbicacionSeleccionada() {
        try {
            // Obtener el texto del campo de ubicación dentro del WebView
            Object ubicacion = webEngine.executeScript(
                    "document.getElementById('locationText') ? document.getElementById('locationText').textContent : ''"
            );

            if (ubicacion != null && !ubicacion.toString().trim().isEmpty()) {
                String ubicacionStr = ubicacion.toString().trim();

                // Solo actualizar si cambió
                if (!ubicacionStr.equals(ultimaUbicacion)) {
                    ultimaUbicacion = ubicacionStr;

                    // Obtener coordenadas desde variables globales del JavaScript
                    Object latObj = webEngine.executeScript("window.selectedLat");
                    Object lonObj = webEngine.executeScript("window.selectedLon");

                    if (latObj != null && lonObj != null) {
                        double lat = Double.parseDouble(latObj.toString());
                        double lon = Double.parseDouble(lonObj.toString());

                        System.out.println("📍 Ubicación detectada por polling:");
                        System.out.println("   - Nombre: " + ubicacionStr);
                        System.out.println("   - Lat: " + lat);
                        System.out.println("   - Lon: " + lon);

                        javafx.application.Platform.runLater(() -> {
                            txtUbicacion.setText(ubicacionStr);
                            txtLatitud.setText(String.valueOf(lat));
                            txtLongitud.setText(String.valueOf(lon));
                            System.out.println("✅ Campos actualizados por polling");
                        });
                    }
                }
            }
        } catch (Exception e) {
            // Ignorar errores silenciosamente (es normal que falle cuando aún no hay selección)
        }
    }


    // Clase interna para comunicación JavaScript -> Java
    public class JavaScriptBridge {
        public void onLocationSelected(String displayName, double lat, double lon) {
            System.out.println("📍 onLocationSelected llamado desde JS:");
            System.out.println("   - Nombre: " + displayName);
            System.out.println("   - Lat: " + lat);
            System.out.println("   - Lon: " + lon);

            javafx.application.Platform.runLater(() -> {
                txtUbicacion.setText(displayName);
                txtLatitud.setText(String.valueOf(lat));
                txtLongitud.setText(String.valueOf(lon));
                System.out.println("✅ Campos actualizados en JavaFX");
            });
        }
    }

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

    @FXML
    private void handleGuardarActividad() {
        if (txtTitulo.getText().trim().isEmpty()) {
            mostrarError("El título es obligatorio");
            return;
        }
        if (txtDescripcion.getText().trim().isEmpty()) {
            mostrarError("La descripción es obligatoria");
            return;
        }
        if (dpFecha.getValue() == null) {
            mostrarError("La fecha es obligatoria");
            return;
        }
        if (txtHora.getText().trim().isEmpty()) {
            mostrarError("La hora es obligatoria");
            return;
        }
        if (cmbTipo.getValue() == null) {
            mostrarError("Debes seleccionar un tipo de actividad");
            return;
        }
        if (txtUbicacion.getText().trim().isEmpty()) {
            mostrarError("Debes seleccionar una ubicación del buscador");
            return;
        }
        if (txtCiudad.getText().trim().isEmpty()) {
            mostrarError("La ciudad es obligatoria");
            return;
        }
        if (txtAforo.getText().trim().isEmpty()) {
            mostrarError("El aforo es obligatorio");
            return;
        }

        try {
            String[] horaPartes = txtHora.getText().split(":");
            if (horaPartes.length != 2) {
                mostrarError("Formato de hora inválido. Usa HH:mm (ej. 18:30)");
                return;
            }

            int hora = Integer.parseInt(horaPartes[0]);
            int minutos = Integer.parseInt(horaPartes[1]);

            if (hora < 0 || hora > 23 || minutos < 0 || minutos > 59) {
                mostrarError("Hora o minutos inválidos");
                return;
            }

            LocalDateTime fechaHora = dpFecha.getValue().atTime(hora, minutos);

            int aforo = Integer.parseInt(txtAforo.getText());
            if (aforo <= 0) {
                mostrarError("El aforo debe ser mayor que 0");
                return;
            }

            boolean esEdicion = (actividadAEditar != null);

            ActividadDTO actividad;
            if (esEdicion) {
                actividad = actividadAEditar;
            } else {
                actividad = new ActividadDTO();
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

            if (!txtLatitud.getText().trim().isEmpty()) {
                actividad.setLatitud(new BigDecimal(txtLatitud.getText()));
            }
            if (!txtLongitud.getText().trim().isEmpty()) {
                actividad.setLongitud(new BigDecimal(txtLongitud.getText()));
            }

            actividadDAO.guardar(actividad);

            if (!esEdicion && Sesion.haySesion() && !Sesion.esAdmin()) {
                if (actividad.getId() != null) {
                    inscripcionDAO.inscribir(Sesion.getUsuarioActual(), actividad);
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText(esEdicion
                    ? "Actividad actualizada correctamente"
                    : "Actividad creada correctamente (ya estás inscrito)");
            alert.showAndWait();

            irAInicio();

        } catch (NumberFormatException e) {
            mostrarError("Formato de hora o aforo inválido");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al guardar: " + e.getMessage());
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
            mostrarError("No se pudo volver a la pantalla de inicio");
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
