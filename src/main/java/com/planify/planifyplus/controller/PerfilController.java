package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.InscripcionDAO;
import com.planify.planifyplus.dao.UsuarioDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.UsuarioDTO;
import com.planify.planifyplus.util.Sesion;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    // Formatos bonitos
    private final DateTimeFormatter formatoFechaCard =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM, HH:mm", new Locale("es", "ES"));

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
            lbl.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13;");
            vboxActividadesInscritas.getChildren().add(lbl);
            return;
        }

        List<ActividadDTO> actividades = inscripcionDAO.obtenerActividadesInscritas(Sesion.getIdUsuario());

        if (actividades == null || actividades.isEmpty()) {
            Label lbl = new Label("No estás inscrito en ninguna actividad todavía.");
            lbl.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13;");
            vboxActividadesInscritas.getChildren().add(lbl);
            return;
        }

        for (ActividadDTO act : actividades) {
            vboxActividadesInscritas.getChildren().add(crearTarjetaActividad(act));
        }
    }

    private VBox crearTarjetaActividad(ActividadDTO act) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-radius: 14;"
        );

        Label titulo = new Label(act.getTitulo() != null ? act.getTitulo() : "Actividad");
        titulo.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #111827;");
        titulo.setWrapText(true);

        String fechaTxt = (act.getFechaHoraInicio() != null)
                ? act.getFechaHoraInicio().format(formatoFechaCard)
                : "Fecha no disponible";

        Label fecha = new Label("📅 " + fechaTxt);
        fecha.setStyle("-fx-font-size: 12.5; -fx-text-fill: #4b5563;");

        String ubicacionTxt = (act.getUbicacion() != null && !act.getUbicacion().isBlank())
                ? act.getUbicacion()
                : "Sin ubicación";

        String ciudadTxt = (act.getCiudad() != null && !act.getCiudad().isBlank())
                ? act.getCiudad()
                : "Sin ciudad";

        Label lugar = new Label("📍 " + ubicacionTxt + " · " + ciudadTxt);
        lugar.setStyle("-fx-font-size: 12.5; -fx-text-fill: #4b5563;");
        lugar.setWrapText(true);

        HBox acciones = new HBox(10);
        Button btnVer = new Button("Ver");
        btnVer.setStyle(
                "-fx-background-color: #3b82f6;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 6 14 6 14;"
        );
        btnVer.setOnAction(e -> abrirActividad(act));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        acciones.getChildren().addAll(spacer, btnVer);

        card.getChildren().addAll(titulo, fecha, lugar, acciones);
        return card;
    }

    private void abrirActividad(ActividadDTO actividad) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/actividad.fxml"));
            Parent root = loader.load();

            // IMPORTANTE: setActividad existe en tu ActividadController
            ActividadController controller = loader.getController();
            controller.setActividad(actividad);

            Stage stage = (Stage) btnHomePerfil.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void cargarCalendario(YearMonth mes) {
        // Título del mes
        String nombreMes = mes.getMonth().toString().toLowerCase();
        nombreMes = nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1);
        lblMesAnio.setText(nombreMes + " " + mes.getYear());

        gridCalendario.getChildren().clear();

        // Fechas que hay que pintar en azul (solo del mes visible)
        Set<LocalDate> fechasMarcadas = obtenerFechasInscritasDelMes(mes);

        LocalDate primerDiaMes = mes.atDay(1);
        int diasMes = mes.lengthOfMonth();

        // 0 = lunes, 6 = domingo (tu calendario está así)
        int offset = primerDiaMes.getDayOfWeek().getValue() - 1;

        int fila = 0;
        int col = offset;

        for (int dia = 1; dia <= diasMes; dia++) {
            LocalDate fecha = mes.atDay(dia);

            Label lblDia = new Label(String.valueOf(dia));
            lblDia.setMinSize(32, 32);
            lblDia.setPrefSize(32, 32);

            // Estilo base
            String styleBase =
                    "-fx-alignment: center;" +
                            "-fx-background-radius: 16;" +
                            "-fx-font-size: 13px;" +
                            "-fx-text-fill: #111827;";

            // Si el usuario tiene actividad ese día => azul
            if (fechasMarcadas.contains(fecha)) {
                lblDia.setStyle(styleBase +
                        "-fx-background-color: #3b82f6;" +
                        "-fx-text-fill: white;");
            } else {
                lblDia.setStyle(styleBase);
            }

            gridCalendario.add(lblDia, col, fila);

            col++;
            if (col > 6) {
                col = 0;
                fila++;
            }
        }
    }

    private Set<LocalDate> obtenerFechasInscritasDelMes(YearMonth mes) {
        Set<LocalDate> fechas = new HashSet<>();

        if (!Sesion.haySesion()) return fechas;

        List<ActividadDTO> actividades = inscripcionDAO.obtenerActividadesInscritas(Sesion.getIdUsuario());
        if (actividades == null || actividades.isEmpty()) return fechas;

        for (ActividadDTO act : actividades) {
            if (act.getFechaHoraInicio() == null) continue;

            LocalDate fecha = act.getFechaHoraInicio().toLocalDate();
            if (fecha.getYear() == mes.getYear() && fecha.getMonth() == mes.getMonth()) {
                fechas.add(fecha);
            }
        }

        return fechas;
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
