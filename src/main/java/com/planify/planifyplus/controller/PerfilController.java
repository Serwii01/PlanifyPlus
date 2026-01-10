package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.InscripcionDAO;
import com.planify.planifyplus.dao.UsuarioDAO;
import com.planify.planifyplus.dto.ActividadDTO;
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
import java.util.List;

public class PerfilController {

    @FXML private Button btnHomePerfil;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblEmailUsuario;

    @FXML private VBox vboxActividadesInscritas;
    @FXML private Button btnExplorarActividades;

    @FXML private Label lblMesAnio;
    @FXML private GridPane gridCalendario;
    @FXML private Button btnMesAnterior;
    @FXML private Button btnMesSiguiente;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

    private YearMonth mesActual = YearMonth.now();

    @FXML
    public void initialize() {
        cargarDatosUsuario();
        cargarActividadesInscritas();
        cargarCalendario(mesActual);
    }

    private void cargarDatosUsuario() {
        UsuarioDTO usuario = Sesion.getUsuarioActual();

        if (usuario == null) {
            lblNombreUsuario.setText("Invitado");
            lblEmailUsuario.setText("Inicia sesión para ver tu perfil");
            return;
        }

        lblNombreUsuario.setText(usuario.getNombre());
        lblEmailUsuario.setText(usuario.getEmail());
    }

    private void cargarActividadesInscritas() {
        vboxActividadesInscritas.getChildren().clear();

        if (!Sesion.haySesion()) {
            Label lbl = new Label("Inicia sesión para ver tus inscripciones.");
            vboxActividadesInscritas.getChildren().add(lbl);
            return;
        }

        List<ActividadDTO> actividades = inscripcionDAO.obtenerActividadesInscritas(Sesion.getIdUsuario());

        if (actividades.isEmpty()) {
            Label lbl = new Label("No estás inscrito en ninguna actividad todavía.");
            vboxActividadesInscritas.getChildren().add(lbl);
            return;
        }

        for (ActividadDTO act : actividades) {
            Label lbl = new Label("• " + act.getTitulo() + " (" + act.getFechaHoraInicio().toLocalDate() + ")");
            lbl.setStyle("-fx-font-size: 14;");
            vboxActividadesInscritas.getChildren().add(lbl);
        }
    }

    private void cargarCalendario(YearMonth mes) {
        String nombreMes = mes.getMonth().toString().toLowerCase();
        nombreMes = nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1);
        lblMesAnio.setText(nombreMes + " " + mes.getYear());

        gridCalendario.getChildren().clear();

        LocalDate primerDiaMes = mes.atDay(1);
        int diasMes = mes.lengthOfMonth();

        int offset = primerDiaMes.getDayOfWeek().getValue() - 1;

        int fila = 0;
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

            gridCalendario.add(lblDia, col, fila);

            col++;
            if (col > 6) {
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
        }
    }
}
