package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.ActividadDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.TipoActividad;
import com.planify.planifyplus.dto.UsuarioDTO;
import com.planify.planifyplus.util.Sesion;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
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
    @FXML private ComboBox<String> cmbCategoria; // Cultural / Taller / Deportiva
    @FXML private TextField txtCiudad;
    @FXML private TextField txtLugar;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtHora;
    @FXML private Spinner<Integer> spnPlazas;
    @FXML private TextArea txtDescripcion;

    private final ActividadDAO actividadDAO = new ActividadDAO();

    @FXML
    private void initialize() {

        // Logo
        logoImage.setImage(new Image(
                getClass().getResource("/img/descarga.png").toExternalForm()
        ));

        // Categorías reales de tu Enum
        cmbCategoria.getItems().addAll("CULTURAL", "TALLER", "DEPORTIVA");

        // Spinner aforo
        SpinnerValueFactory<Integer> vf =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 10);
        spnPlazas.setValueFactory(vf);

        // Rellenar información del usuario logueado
        UsuarioDTO u = Sesion.getUsuarioActual();
        if (u != null) {
            lblCiudad.setText(u.getCiudad());
            lblUser.setText(u.getNombre().substring(0, 1).toUpperCase());
        } else {
            // Si no hay sesión, al inicio
            irA("/vistas/Inicio.fxml");
        }
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

        // -------- VALIDACIONES --------
        String titulo = txtTitulo.getText().trim();
        if (titulo.isEmpty()) { error("El título es obligatorio."); return; }

        String tipoStr = cmbCategoria.getValue();
        if (tipoStr == null) { error("Selecciona una categoría."); return; }
        TipoActividad tipo = TipoActividad.valueOf(tipoStr);

        String ciudad = txtCiudad.getText().trim();
        if (ciudad.isEmpty()) { error("La ciudad es obligatoria."); return; }

        String lugar = txtLugar.getText().trim();
        if (lugar.isEmpty()) { error("El lugar/ubicación es obligatorio."); return; }

        LocalDate fecha = dpFecha.getValue();
        if (fecha == null) { error("Selecciona una fecha."); return; }

        String horaStr = txtHora.getText().trim();
        if (horaStr.isEmpty()) { error("Indica una hora (formato HH:mm)."); return; }

        LocalTime hora;
        try {
            hora = LocalTime.parse(horaStr); // formato 13:30
        } catch (Exception e) {
            error("Formato de hora incorrecto. Usa HH:mm (ej: 17:30).");
            return;
        }

        LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);

        int aforo = spnPlazas.getValue();

        String descripcion = txtDescripcion.getText().trim();
        if (descripcion.isEmpty()) { error("La descripción es obligatoria."); return; }


        // -------- CREAR OBJETO ACTIVIDAD --------
        ActividadDTO actividad = new ActividadDTO();
        actividad.setTitulo(titulo);
        actividad.setDescripcion(descripcion);
        actividad.setFechaHoraInicio(fechaHora);
        actividad.setTipo(tipo);
        actividad.setUbicacion(lugar);
        actividad.setCiudad(ciudad);
        actividad.setAforo(aforo);
        actividad.setCreadoEn(LocalDateTime.now());

        // Coordenadas opcionales → null (tu DTO lo permite)
        actividad.setLatitud(null);
        actividad.setLongitud(null);
        actividad.setPredeterminada(false);

        // -------- GUARDAR EN BD --------
        actividadDAO.guardar(actividad);

        info("Actividad creada correctamente.");

        // Volver al inicio
        irA("/vistas/Inicio.fxml");
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
        }
    }

    private void error(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
