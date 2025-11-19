package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.UsuarioDAO;
import com.planify.planifyplus.dto.UsuarioDTO;
import com.planify.planifyplus.util.Sesion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;

public class PerfilController {

    // --- UI del header ---
    @FXML private Button btnHomePerfil;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblEmailUsuario;

    // --- Actividades inscritas ---
    @FXML private VBox vboxActividadesInscritas;
    @FXML private Button btnExplorarActividades;

    // --- Calendario ---
    @FXML private Label lblMesAnio;
    @FXML private GridPane gridCalendario;
    @FXML private Button btnMesAnterior;
    @FXML private Button btnMesSiguiente;

    // DAO por si lo necesitáis (aunque ahora mismo no lo usamos directamente)
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // Mes que se está mostrando en el calendario
    private YearMonth mesActual = YearMonth.now();

    // ================== INICIALIZACIÓN ==================

    @FXML
    public void initialize() {
        // 1) Cargar datos del usuario en el header
        cargarDatosUsuario();

        // 2) Inicializar calendario del mes actual
        cargarCalendario(mesActual);

        // 3) De momento, el VBox de actividades se queda con el mensaje por defecto
        //    (cuando tengáis inscripciones, aquí se actualizará).
    }

    private void cargarDatosUsuario() {
        UsuarioDTO usuario = Sesion.getUsuarioActual();

        if (usuario == null) {
            lblNombreUsuario.setText("Invitado");
            lblEmailUsuario.setText("Inicia sesión para ver tu perfil");
            return;
        }

        // Si quisieras recargar desde BD:
        // usuario = usuarioDAO.obtenerPorId(usuario.getId());

        lblNombreUsuario.setText(usuario.getNombre());
        lblEmailUsuario.setText(usuario.getEmail());
    }

    // ================== CALENDARIO (LUNES–DOMINGO) ==================

    private void cargarCalendario(YearMonth mes) {
        // Ejemplo: "October 2025" -> lo formateamos a "October 2025" pero podrías traducirlo si quieres
        String nombreMes = mes.getMonth().toString().toLowerCase(); // "october"
        nombreMes = nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1);
        lblMesAnio.setText(nombreMes + " " + mes.getYear());

        // Limpiar cuadrícula
        gridCalendario.getChildren().clear();

        LocalDate primerDiaMes = mes.atDay(1);
        int diasMes = mes.lengthOfMonth();

        // Queremos LUNES como primera columna.
        // DayOfWeek: MONDAY=1 ... SUNDAY=7 -> offset = 0..6 (lunes..domingo)
        int offset = primerDiaMes.getDayOfWeek().getValue() - 1; // Lunes=0, Martes=1, ..., Domingo=6

        int fila = 0;  // primera fila para días
        int col = offset;

        for (int dia = 1; dia <= diasMes; dia++) {
            Label lblDia = new Label(String.valueOf(dia));
            lblDia.setMinSize(32, 32);
            lblDia.setPrefSize(32, 32);
            lblDia.setStyle(
                    "-fx-alignment: center; " +
                            "-fx-background-radius: 16; " +
                            "-fx-font-size: 13px;"
            );

            // TODO: si el usuario tiene actividades este día,
            // aquí podrías cambiar el estilo para resaltarlo:
            // lblDia.setStyle(lblDia.getStyle()
            //      + "-fx-background-color: #4C8DF6; -fx-text-fill: white;");

            gridCalendario.add(lblDia, col, fila);

            col++;
            if (col > 6) { // columnas 0..6 (Lunes..Domingo)
                col = 0;
                fila++;
            }
        }
    }

    @FXML
    private void onMesAnterior() {
        mesActual = mesActual.minusMonths(1);
        cargarCalendario(mesActual);
    }

    @FXML
    private void onMesSiguiente() {
        mesActual = mesActual.plusMonths(1);
        cargarCalendario(mesActual);
    }

    // ================== NAVEGACIÓN ==================

    @FXML
    private void onIrInicio() {
        cambiarEscena("/vistas/Inicio.fxml");
    }

    @FXML
    private void onIrConfPerfil() {
        cambiarEscena("/vistas/ConfPerfil.fxml");
    }

    @FXML
    private void onExplorarActividades() {
        // De momento lo mandamos al main / listado de actividades
        cambiarEscena("/vistas/Inicio.fxml");
    }

    private void cambiarEscena(String rutaFXML) {
        try {
            Stage stage = (Stage) btnHomePerfil.getScene().getWindow();
            Scene nuevaScene = new Scene(FXMLLoader.load(getClass().getResource(rutaFXML)));
            stage.setScene(nuevaScene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            // Aquí podrías mostrar un Alert si quieres
        }
    }
}
