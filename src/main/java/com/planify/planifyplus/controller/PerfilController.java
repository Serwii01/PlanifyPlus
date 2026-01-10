// src/main/java/com/planify/planifyplus/controller/PerfilController.java
package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.InscripcionDAO;
import com.planify.planifyplus.dao.UsuarioDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.UsuarioDTO;
import com.planify.planifyplus.util.Sesion;
import com.planify.planifyplus.util.ViewUtil;
import com.planify.planifyplus.util.WindowUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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

    // (no lo uso aquí, pero lo dejo por si ya lo estabais usando en otros cambios)
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

    private YearMonth mesActual = YearMonth.now();

    // cache del usuario
    private List<ActividadDTO> actividadesUsuario = List.of();
    private Set<LocalDate> fechasConActividad = Set.of();

    private final DateTimeFormatter fmtFechaCard =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

    @FXML
    public void initialize() {
        cargarDatosUsuario();
        cargarActividadesInscritasYFechas();   // <-- carga lista + set de fechas
        pintarActividadesInscritas();          // <-- tarjetas o estado vacío
        cargarCalendario(mesActual);           // <-- calendario con días azules
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

    /**
     * Carga las actividades inscritas del usuario (si hay sesión) y genera el set de LocalDate
     * para poder marcar el calendario.
     */
    private void cargarActividadesInscritasYFechas() {
        if (!Sesion.haySesion()) {
            actividadesUsuario = List.of();
            fechasConActividad = Set.of();
            return;
        }

        actividadesUsuario = inscripcionDAO.obtenerActividadesInscritas(Sesion.getIdUsuario());

        Set<LocalDate> fechas = new HashSet<>();
        for (ActividadDTO a : actividadesUsuario) {
            if (a != null && a.getFechaHoraInicio() != null) {
                fechas.add(a.getFechaHoraInicio().toLocalDate());
            }
        }
        fechasConActividad = fechas;
    }

    /**
     * Parte izquierda: si no hay actividades -> se ve como ahora (mensaje + botón).
     * Si hay actividades -> tarjetas.
     */
    private void pintarActividadesInscritas() {
        vboxActividadesInscritas.getChildren().clear();

        if (!Sesion.haySesion()) {
            // mensaje simple si no hay sesión
            vboxActividadesInscritas.setAlignment(Pos.CENTER);
            Label lbl = new Label("Inicia sesión para ver tus inscripciones.");
            lbl.setStyle("-fx-text-fill: #6B7280;");
            lbl.setFont(Font.font(16));
            vboxActividadesInscritas.getChildren().add(lbl);
            return;
        }

        if (actividadesUsuario == null || actividadesUsuario.isEmpty()) {
            // estado vacío: igual que el FXML (texto + botón explorar)
            renderEmptyState();
            return;
        }

        // modo lista de tarjetas
        vboxActividadesInscritas.setAlignment(Pos.TOP_LEFT);
        vboxActividadesInscritas.setSpacing(12);

        for (ActividadDTO act : actividadesUsuario) {
            vboxActividadesInscritas.getChildren().add(crearCardActividad(act));
        }
    }

    private void renderEmptyState() {
        vboxActividadesInscritas.setAlignment(Pos.CENTER);
        vboxActividadesInscritas.setSpacing(16);

        Label lbl = new Label("Aún no estás inscrito en ninguna actividad");
        lbl.setStyle("-fx-text-fill: #6B7280;");
        lbl.setFont(Font.font(16));

        Button btn = new Button("Explorar Actividades");
        btn.setPrefWidth(320);
        btn.setPrefHeight(46);
        btn.setStyle("""
                -fx-background-color: #4C8DF6;
                -fx-text-fill: white;
                -fx-background-radius: 999;
                -fx-cursor: hand;
                """);
        btn.setFont(Font.font(16));
        btn.setOnAction(e -> onExplorarActividades());

        vboxActividadesInscritas.getChildren().addAll(lbl, btn);
    }

    private VBox crearCardActividad(ActividadDTO act) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.setStyle("""
                -fx-background-color: #FFFFFF;
                -fx-background-radius: 16;
                -fx-border-color: #E5E7EB;
                -fx-border-radius: 16;
                """);
        card.setMaxWidth(Double.MAX_VALUE);

        String titulo = (act != null && act.getTitulo() != null) ? act.getTitulo() : "Actividad";
        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        String fechaStr = "";
        if (act != null && act.getFechaHoraInicio() != null) {
            fechaStr = act.getFechaHoraInicio().format(fmtFechaCard);
        }
        Label lblFecha = new Label(fechaStr);
        lblFecha.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");

        // Mini fila con “pill” opcional de ciudad (si existe)
        HBox extra = new HBox(8);
        extra.setAlignment(Pos.CENTER_LEFT);

        if (act != null && act.getCiudad() != null && !act.getCiudad().isBlank()) {
            Label pill = new Label(act.getCiudad());
            pill.setStyle("""
                    -fx-background-color: #EEF2FF;
                    -fx-text-fill: #3730A3;
                    -fx-padding: 3 10 3 10;
                    -fx-background-radius: 999;
                    -fx-font-size: 12px;
                    """);
            extra.getChildren().add(pill);
        } else {
            // para que no quede “vacío raro”
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            extra.getChildren().add(spacer);
        }

        card.getChildren().addAll(lblTitulo, lblFecha, extra);
        return card;
    }

    /**
     * Calendario: marca en azul (redondeado) los días donde el usuario tiene actividades.
     * NOTA: el GridPane del FXML tiene cabecera (Mo..Su) pero como aquí limpiamos el grid,
     * las volvemos a añadir siempre.
     */
    private void cargarCalendario(YearMonth mes) {
        // título mes/año bonito
        String nombreMes = mes.getMonth().toString().toLowerCase();
        nombreMes = nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1);
        lblMesAnio.setText(nombreMes + " " + mes.getYear());

        gridCalendario.getChildren().clear();

        // Cabecera días (fila 0)
        addHeaderDay("Mo", 0);
        addHeaderDay("Tu", 1);
        addHeaderDay("We", 2);
        addHeaderDay("Th", 3);
        addHeaderDay("Fr", 4);
        addHeaderDay("Sa", 5);
        addHeaderDay("Su", 6);

        LocalDate primerDiaMes = mes.atDay(1);
        int diasMes = mes.lengthOfMonth();

        // Lunes=1 ... Domingo=7
        int offset = primerDiaMes.getDayOfWeek().getValue() - 1;

        int fila = 1; // empezamos en fila 1 porque la fila 0 es cabecera
        int col = offset;

        for (int dia = 1; dia <= diasMes; dia++) {
            LocalDate fecha = mes.atDay(dia);

            Label lblDia = new Label(String.valueOf(dia));
            lblDia.setMinSize(32, 32);
            lblDia.setPrefSize(32, 32);
            lblDia.setAlignment(Pos.CENTER);

            boolean tieneActividad = fechasConActividad != null && fechasConActividad.contains(fecha);

            if (tieneActividad) {
                // día en azul (redondeado)
                lblDia.setStyle("""
                        -fx-background-color: #4C8DF6;
                        -fx-text-fill: white;
                        -fx-background-radius: 16;
                        -fx-font-size: 13px;
                        -fx-font-weight: bold;
                        """);
            } else {
                lblDia.setStyle("""
                        -fx-background-color: transparent;
                        -fx-text-fill: #111827;
                        -fx-background-radius: 16;
                        -fx-font-size: 13px;
                        """);
            }

            gridCalendario.add(lblDia, col, fila);

            col++;
            if (col > 6) {
                col = 0;
                fila++;
            }
        }
    }

    private void addHeaderDay(String text, int col) {
        Text t = new Text(text);
        t.setStyle("-fx-fill: #6B7280; -fx-font-size: 12px;");
        gridCalendario.add(t, col, 0);
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
    private void onIrInicio() { cambiarEscena("/vistas/Inicio.fxml"); }

    @FXML
    private void onIrConfPerfil() { cambiarEscena("/vistas/ConfPerfil.fxml"); }

    @FXML
    private void onExplorarActividades() { cambiarEscena("/vistas/Inicio.fxml"); }

    private void cambiarEscena(String rutaFXML) {
        try {
            Stage stage = (Stage) btnHomePerfil.getScene().getWindow();
            var root = ViewUtil.loadFXML(getClass(), rutaFXML);

            Scene scene = new Scene(root);
            var css = getClass().getResource("/css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            WindowUtil.forceMaximize(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
