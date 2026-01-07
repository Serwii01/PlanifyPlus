package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.ActividadDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.TipoActividad;
import com.planify.planifyplus.util.Sesion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

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
    private ActividadDTO actividadAEditar = null;
    private WebEngine webEngine;

    @FXML
    public void initialize() {
        // Cargar tipos de actividad
        cmbTipo.getItems().addAll("DEPORTIVA", "CULTURAL", "TALLERES");

        // Inicializar WebView
        inicializarBuscadorMapa();
    }

    private void inicializarBuscadorMapa() {
        webEngine = webViewBuscador.getEngine();
        webEngine.setJavaScriptEnabled(true);

        // Cargar el HTML del buscador
        String htmlContent = getClass().getResource("/API/map-buscador-ubicacion.html").toExternalForm();
        webEngine.load(htmlContent);

        // Esperar a que se cargue para inyectar el puente Java
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaApp", new JavaScriptBridge());

                // Si hay ciudad en el contexto, pasarla al buscador
                if (txtCiudad.getText() != null && !txtCiudad.getText().trim().isEmpty()) {
                    webEngine.executeScript("setCityContext('" + txtCiudad.getText() + "')");
                }
            }
        });

        // Listener para actualizar el contexto de ciudad
        txtCiudad.textProperty().addListener((obs, old, newVal) -> {
            if (webEngine != null && newVal != null && !newVal.trim().isEmpty()) {
                webEngine.executeScript("setCityContext('" + newVal + "')");
            }
        });
    }

    /**
     * Puente JavaScript para recibir la ubicación seleccionada
     */
    public class JavaScriptBridge {
        public void onLocationSelected(String displayName, double lat, double lon) {
            javafx.application.Platform.runLater(() -> {
                txtUbicacion.setText(displayName);
                txtLatitud.setText(String.valueOf(lat));
                txtLongitud.setText(String.valueOf(lon));
                System.out.println("✓ Ubicación recibida: " + displayName + " (" + lat + ", " + lon + ")");
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

        // Cargar coordenadas si existen
        if (actividad.getLatitud() != null) {
            txtLatitud.setText(actividad.getLatitud().toString());
        }
        if (actividad.getLongitud() != null) {
            txtLongitud.setText(actividad.getLongitud().toString());
        }

        if (lblTituloPagina != null) {
            lblTituloPagina.setText("Editar Actividad");
        }
        if (btnGuardar != null) {
            btnGuardar.setText("Actualizar Actividad");
        }
    }

    @FXML
    private void handleGuardarActividad() {
        // Validaciones
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
            // Parsear hora
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

            ActividadDTO actividad;
            if (actividadAEditar != null) {
                actividad = actividadAEditar;
            } else {
                actividad = new ActividadDTO();
                actividad.setPredeterminada(false);
                actividad.setCreador(Sesion.getUsuarioActual());
                actividad.setCreadoEn(LocalDateTime.now());
            }

            // Actualizar campos
            actividad.setTitulo(txtTitulo.getText().trim());
            actividad.setDescripcion(txtDescripcion.getText().trim());
            actividad.setFechaHoraInicio(fechaHora);
            actividad.setTipo(TipoActividad.valueOf(cmbTipo.getValue()));
            actividad.setUbicacion(txtUbicacion.getText().trim());
            actividad.setCiudad(txtCiudad.getText().trim());
            actividad.setAforo(aforo);

            // Guardar coordenadas si existen
            if (!txtLatitud.getText().trim().isEmpty()) {
                actividad.setLatitud(new BigDecimal(txtLatitud.getText()));
            }
            if (!txtLongitud.getText().trim().isEmpty()) {
                actividad.setLongitud(new BigDecimal(txtLongitud.getText()));
            }

            actividadDAO.guardar(actividad);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText(actividadAEditar != null
                    ? "Actividad actualizada correctamente"
                    : "Actividad creada correctamente");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/inicio.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnGuardar.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
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
