package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.ActividadDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.TipoActividad;
import com.planify.planifyplus.dto.UsuarioDTO;
import com.planify.planifyplus.util.Sesion;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CrearActividadController {

    // ---- BARRA SUPERIOR ----
    @FXML private Button btnHome;
    @FXML private ImageView logoImage;
    @FXML private Label lblCiudad;
    @FXML private Label lblUser;
    @FXML private Button btnPerfil;
    @FXML private Button btnLogout;

    // ---- FORMULARIO ----
    @FXML private TextField txtTitulo;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField txtCiudad;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtHora;
    @FXML private Spinner<Integer> spnPlazas;
    @FXML private TextArea txtDescripcion;

    // NUEVO: WebView para el buscador de ubicaciones
    @FXML private WebView webViewBuscador;

    private final ActividadDAO actividadDAO = new ActividadDAO();
    private WebEngine webEngine;

    // Variables para guardar las coordenadas seleccionadas - INICIALIZADAS EXPLÍCITAMENTE
    private volatile BigDecimal latitudSeleccionada = null;
    private volatile BigDecimal longitudSeleccionada = null;
    private volatile String ubicacionSeleccionada = null;

    @FXML
    private void initialize() {
        // Logo
        logoImage.setImage(new Image(
                getClass().getResource("/img/descarga.png").toExternalForm()
        ));

        // Categorías
        cmbCategoria.getItems().addAll("CULTURAL", "TALLER", "DEPORTIVA");

        // Spinner aforo
        SpinnerValueFactory<Integer> vf =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 10);
        spnPlazas.setValueFactory(vf);

        // Inicializar WebView para el buscador
        inicializarBuscadorUbicacion();

        // Rellenar información del usuario logueado
        UsuarioDTO u = Sesion.getUsuarioActual();
        if (u != null) {
            lblCiudad.setText(u.getCiudad());
            lblUser.setText(u.getNombre().substring(0, 1).toUpperCase());
        } else {
            irA("/vistas/Inicio.fxml");
        }
    }

    private void inicializarBuscadorUbicacion() {
        webEngine = webViewBuscador.getEngine();
        webEngine.setJavaScriptEnabled(true);

        // Cargar el HTML del buscador
        URL url = getClass().getResource("/API/map-buscador-ubicacion.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
            System.out.println("✅ Cargando buscador desde: " + url.toExternalForm());

            // Cuando cargue, establecer el bridge con Java
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    try {
                        // Crear un bridge entre JavaScript y Java
                        JSObject window = (JSObject) webEngine.executeScript("window");
                        window.setMember("javaApp", this);

                        // Pasar ciudad del usuario
                        UsuarioDTO u = Sesion.getUsuarioActual();
                        if (u != null && u.getCiudad() != null) {
                            String script = String.format("setCityContext('%s');", u.getCiudad());
                            webEngine.executeScript(script);
                        }

                        System.out.println("✅ Bridge JavaScript-Java establecido correctamente");
                    } catch (Exception e) {
                        System.err.println("❌ Error al establecer bridge: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else if (newState == Worker.State.FAILED) {
                    System.err.println("❌ Falló la carga del buscador");
                }
            });
        } else {
            System.err.println("❌ No se pudo cargar el archivo map-buscador-ubicacion.html");
        }
    }

    // MÉTODO PÚBLICO llamado desde JavaScript - sin clase interna
    public void onLocationSelected(String displayName, double lat, double lng) {
        // Usar Platform.runLater para ejecutar en el hilo de JavaFX
        Platform.runLater(() -> {
            // Guardar las coordenadas seleccionadas
            this.latitudSeleccionada = BigDecimal.valueOf(lat);
            this.longitudSeleccionada = BigDecimal.valueOf(lng);
            this.ubicacionSeleccionada = displayName;

            System.out.println("✅ Ubicación guardada en Java:");
            System.out.println("   Nombre: " + displayName);
            System.out.println("   Lat: " + lat);
            System.out.println("   Lng: " + lng);
            System.out.println("   Variables actualizadas: lat=" + latitudSeleccionada + ", lng=" + longitudSeleccionada);
        });
    }

    // ======================== NAVEGACIÓN ========================
    @FXML
    private void onIrInicio() { irA("/vistas/Inicio.fxml"); }

    @FXML
    private void onIrPerfil() { irA("/vistas/Perfil.fxml"); }

    @FXML
    private void onLogout() {
        Sesion.cerrarSesion();
        irA("/vistas/Inicio.fxml");
    }

    // ======================== GUARDAR ACTIVIDAD ========================
    @FXML
    private void onGuardarActividad() {
        System.out.println("🔍 Verificando ubicación:");
        System.out.println("   ubicacionSeleccionada: " + ubicacionSeleccionada);
        System.out.println("   latitudSeleccionada: " + latitudSeleccionada);
        System.out.println("   longitudSeleccionada: " + longitudSeleccionada);

        // -------- VALIDACIONES --------
        String titulo = txtTitulo.getText().trim();
        if (titulo.isEmpty()) { error("El título es obligatorio."); return; }

        String tipoStr = cmbCategoria.getValue();
        if (tipoStr == null) { error("Selecciona una categoría."); return; }
        TipoActividad tipo = TipoActividad.valueOf(tipoStr);

        String ciudad = txtCiudad.getText().trim();
        if (ciudad.isEmpty()) { error("La ciudad es obligatoria."); return; }

        // VALIDAR QUE SE HAYA SELECCIONADO UNA UBICACIÓN
        if (ubicacionSeleccionada == null || ubicacionSeleccionada.isEmpty()) {
            error("Debes seleccionar una ubicación del buscador.");
            return;
        }

        if (latitudSeleccionada == null || longitudSeleccionada == null) {
            error("Error: Las coordenadas no se guardaron correctamente. Intenta seleccionar la ubicación de nuevo.");
            return;
        }

        LocalDate fecha = dpFecha.getValue();
        if (fecha == null) { error("Selecciona una fecha."); return; }

        String horaStr = txtHora.getText().trim();
        if (horaStr.isEmpty()) { error("Indica una hora (formato HH:mm)."); return; }

        LocalTime hora;
        try {
            hora = LocalTime.parse(horaStr);
        } catch (Exception e) {
            error("Formato de hora incorrecto. Usa HH:mm (ej: 17:30).");
            return;
        }

        LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);
        int aforo = spnPlazas.getValue();

        String descripcion = txtDescripcion.getText().trim();
        if (descripcion.isEmpty()) { error("La descripción es obligatoria."); return; }

        UsuarioDTO creador = Sesion.getUsuarioActual();
        if (creador == null) {
            error("Debes iniciar sesión para crear una actividad.");
            irA("/vistas/login.fxml");
            return;
        }

        // -------- CREAR OBJETO ACTIVIDAD --------
        ActividadDTO actividad = new ActividadDTO();
        actividad.setTitulo(titulo);
        actividad.setDescripcion(descripcion);
        actividad.setFechaHoraInicio(fechaHora);
        actividad.setTipo(tipo);
        actividad.setUbicacion(ubicacionSeleccionada);
        actividad.setCiudad(ciudad);
        actividad.setAforo(aforo);
        actividad.setCreadoEn(LocalDateTime.now());
        actividad.setPredeterminada(false);

        // GUARDAR LAS COORDENADAS
        actividad.setLatitud(latitudSeleccionada);
        actividad.setLongitud(longitudSeleccionada);

        actividad.setCreador(creador);

        // -------- GUARDAR EN BD --------
        try {
            actividadDAO.guardar(actividad);
            System.out.println("✅ Actividad guardada en BD con coordenadas: " + latitudSeleccionada + ", " + longitudSeleccionada);
            info("✅ Actividad creada correctamente.\n\n📍 Ubicación: " + ubicacionSeleccionada);
            irA("/vistas/Inicio.fxml");
        } catch (Exception e) {
            error("Error al guardar la actividad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ======================== CANCELAR ========================
    @FXML
    private void onCancelar() {
        irA("/vistas/Inicio.fxml");
    }

    // ======================== HELPERS ========================
    private void irA(String ruta) {
        try {
            Stage stage = (Stage) btnHome.getScene().getWindow();
            Scene sc = new Scene(FXMLLoader.load(getClass().getResource(ruta)));
            stage.setScene(sc);
            stage.centerOnScreen();
        } catch (IOException e) {
            error("No se pudo abrir: " + ruta);
            e.printStackTrace();
        }
    }

    private void error(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle("Error");
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle("Éxito");
        a.setHeaderText(null);
        a.showAndWait();
    }
}
