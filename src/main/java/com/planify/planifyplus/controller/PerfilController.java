package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.InscripcionDAO;
import com.planify.planifyplus.dao.UsuarioDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.UsuarioDTO;
import com.planify.planifyplus.util.Sesion;
import com.planify.planifyplus.util.ViewUtil;
import com.planify.planifyplus.util.WindowUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controlador de la pantalla de perfil.
 */
public class PerfilController {

    @FXML private Button btnHomePerfil;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblEmailUsuario;

    @FXML private ScrollPane scrollInscritas;
    @FXML private VBox vboxActividadesInscritas;
    @FXML private VBox vboxEmptyInscritas;
    @FXML private Label lblEmptyInscritas;
    @FXML private Button btnExplorarActividades;

    @FXML private Label lblMesAnio;
    @FXML private GridPane gridCalendario;
    @FXML private Button btnMesAnterior;
    @FXML private Button btnMesSiguiente;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

    private YearMonth mesActual = YearMonth.now();

    /**
     * Inicialización del controlador (JavaFX).
     */
    @FXML
    public void initialize() {
        if (vboxActividadesInscritas != null) {
            vboxActividadesInscritas.setFillWidth(true);
            vboxActividadesInscritas.setMaxWidth(Double.MAX_VALUE);
        }
        if (scrollInscritas != null) {
            scrollInscritas.setFitToWidth(true);
        }

        cargarDatosUsuario();
        cargarActividadesInscritasComoCards();
        cargarCalendario(mesActual);
    }

    /**
     * Carga datos básicos del usuario en sesión.
     */
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
     * Alterna el estado vacío del panel de inscripciones.
     *
     * @param visible true para mostrar el estado vacío
     * @param texto   texto a mostrar (opcional)
     */
    private void setEmptyInscritasVisible(boolean visible, String texto) {
        if (lblEmptyInscritas != null && texto != null) {
            lblEmptyInscritas.setText(texto);
        }

        if (vboxEmptyInscritas != null) {
            vboxEmptyInscritas.setVisible(visible);
            vboxEmptyInscritas.setManaged(visible);
        }

        if (scrollInscritas != null) {
            scrollInscritas.setVisible(!visible);
            scrollInscritas.setManaged(!visible);
        }
    }

    /**
     * Carga las actividades inscritas y las muestra como tarjetas.
     */
    private void cargarActividadesInscritasComoCards() {
        if (vboxActividadesInscritas != null) {
            vboxActividadesInscritas.getChildren().clear();
        }

        if (!Sesion.haySesion()) {
            setEmptyInscritasVisible(true, "Inicia sesión para ver tus inscripciones");
            if (btnExplorarActividades != null) btnExplorarActividades.setText("Ir a Inicio");
            return;
        } else {
            if (btnExplorarActividades != null) btnExplorarActividades.setText("Explorar Actividades");
        }

        List<ActividadDTO> actividades = inscripcionDAO.obtenerActividadesInscritas(Sesion.getIdUsuario());

        if (actividades == null || actividades.isEmpty()) {
            setEmptyInscritasVisible(true, "Aún no estás inscrito en ninguna actividad");
            return;
        }

        setEmptyInscritasVisible(false, null);

        for (ActividadDTO act : actividades) {
            Pane card = crearCardActividad(act);
            vboxActividadesInscritas.getChildren().add(card);
        }

        cargarCalendario(mesActual);
    }

    /**
     * Crea una tarjeta de actividad para el listado del perfil.
     *
     * @param act actividad
     * @return nodo listo para pintar
     */
    private Pane crearCardActividad(ActividadDTO act) {
        VBox vbox = new VBox(12);
        vbox.setMaxWidth(Double.MAX_VALUE);

        vbox.setStyle(
                "-fx-padding: 18 18 18 18;" +
                        "-fx-background-color: #FFF;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-color: #ececec;"
        );

        HBox hTituloTipo = new HBox(8);
        Label lblTitulo = new Label(act.getTitulo());
        lblTitulo.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label lblTipo = new Label(capitalize(act.getTipo().toString().toLowerCase()));
        lblTipo.setStyle(
                "-fx-background-color: " + getTipoColor(act.getTipo().toString()) + ";" +
                        "-fx-font-size: 13;" +
                        "-fx-padding: 4 14 4 14;" +
                        "-fx-background-radius: 11;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #226;"
        );

        HBox.setHgrow(lblTitulo, Priority.ALWAYS);
        hTituloTipo.getChildren().addAll(lblTitulo, lblTipo);

        Label lblDesc = new Label(act.getDescripcion());
        lblDesc.setStyle("-fx-text-fill: #4B4B4B; -fx-font-size: 14;");

        Label lblCiudadAct = new Label("📍 " + act.getCiudad() +
                (act.getUbicacion() != null && !act.getUbicacion().isEmpty()
                        ? " · " + act.getUbicacion()
                        : ""));
        lblCiudadAct.setStyle("-fx-text-fill: #1663e3; -fx-font-size: 15;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String fechaFormateada = act.getFechaHoraInicio().format(formatter);

        long inscritos = inscripcionDAO.contarInscritos(act.getId());

        HBox hFechaAforo = new HBox(10);
        Label lblFecha = new Label(fechaFormateada);
        lblFecha.setStyle("-fx-font-size: 15; -fx-text-fill: #222;");
        Label lblAforo = new Label(inscritos + " / " + act.getAforo() + " inscritos");
        HBox.setHgrow(lblFecha, Priority.ALWAYS);
        hFechaAforo.getChildren().addAll(lblFecha, lblAforo);

        HBox hBoton = new HBox(8);
        hBoton.setAlignment(Pos.CENTER_RIGHT);

        Button btnInscribir = new Button();
        btnInscribir.setStyle(
                "-fx-background-color: #3B82F6;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-font-size: 15;" +
                        "-fx-padding: 6 22 6 22;"
        );

        boolean inscrito = inscripcionDAO.estaInscrito(Sesion.getIdUsuario(), act.getId());
        btnInscribir.setText(inscrito ? "Inscrito" : "Inscribirse");

        boolean lleno = inscritos >= act.getAforo();
        btnInscribir.setDisable(lleno && !inscrito);

        btnInscribir.setOnAction(e -> {
            e.consume();
            long userId = Sesion.getIdUsuario();
            boolean ya = inscripcionDAO.estaInscrito(userId, act.getId());

            if (ya) inscripcionDAO.cancelar(userId, act.getId());
            else {
                long ahora = inscripcionDAO.contarInscritos(act.getId());
                if (ahora >= act.getAforo()) return;
                inscripcionDAO.inscribir(Sesion.getUsuarioActual(), act);
            }

            cargarActividadesInscritasComoCards();
        });

        hBoton.getChildren().add(btnInscribir);

        vbox.getChildren().addAll(hTituloTipo, lblDesc, lblCiudadAct, hFechaAforo, hBoton);
        vbox.setOnMouseClicked(e -> abrirDetalleActividad(act));

        return vbox;
    }

    /**
     * Devuelve los días del mes en los que el usuario tiene alguna actividad.
     *
     * @param mes mes a comprobar
     * @return conjunto de días (1..31)
     */
    private Set<Integer> getDiasConActividadesEnMes(YearMonth mes) {
        Set<Integer> dias = new HashSet<>();

        if (!Sesion.haySesion()) return dias;

        List<ActividadDTO> actividades = inscripcionDAO.obtenerActividadesInscritas(Sesion.getIdUsuario());
        if (actividades == null) return dias;

        for (ActividadDTO act : actividades) {
            if (act.getFechaHoraInicio() == null) continue;
            LocalDate d = act.getFechaHoraInicio().toLocalDate();
            if (d.getYear() == mes.getYear() && d.getMonthValue() == mes.getMonthValue()) {
                dias.add(d.getDayOfMonth());
            }
        }

        return dias;
    }

    /**
     * Pinta el calendario del mes indicado y marca los días con actividad.
     *
     * @param mes mes a mostrar
     */
    private void cargarCalendario(YearMonth mes) {
        String nombreMes = mes.getMonth().toString().toLowerCase();
        nombreMes = nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1);
        lblMesAnio.setText(nombreMes + " " + mes.getYear());

        gridCalendario.getChildren().removeIf(n -> {
            Integer r = GridPane.getRowIndex(n);
            return r != null && r > 0;
        });

        Set<Integer> diasConActividades = getDiasConActividadesEnMes(mes);

        LocalDate primerDiaMes = mes.atDay(1);
        int diasMes = mes.lengthOfMonth();

        int offset = primerDiaMes.getDayOfWeek().getValue() - 1;

        int fila = 1;
        int col = offset;

        for (int dia = 1; dia <= diasMes; dia++) {
            Label lblDia = new Label(String.valueOf(dia));
            lblDia.setMinSize(32, 32);
            lblDia.setPrefSize(32, 32);

            boolean tieneActividad = diasConActividades.contains(dia);

            if (tieneActividad) {
                lblDia.setStyle(
                        "-fx-alignment: center;" +
                                "-fx-background-color: #4C8DF6;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 16;" +
                                "-fx-font-size: 13px;" +
                                "-fx-font-weight: bold;"
                );
            } else {
                lblDia.setStyle(
                        "-fx-alignment: center;" +
                                "-fx-background-radius: 16;" +
                                "-fx-font-size: 13px;"
                );
            }

            gridCalendario.add(lblDia, col, fila);

            col++;
            if (col > 6) {
                col = 0;
                fila++;
            }
        }
    }

    /**
     * Abre la vista de detalle de una actividad.
     */
    private void abrirDetalleActividad(ActividadDTO actividad) {
        try {
            var loader = ViewUtil.loaderFXML(getClass(), "/vistas/Actividad.fxml");
            Parent root = loader.load();

            ActividadController controller = loader.getController();
            controller.setActividad(actividad);

            Stage stage = (Stage) btnHomePerfil.getScene().getWindow();
            Scene scene = new Scene(root);

            var css = getClass().getResource("/css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            WindowUtil.forceMaximize(stage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Muestra el mes anterior en el calendario.
     */
    @FXML
    private void onMesAnterior() {
        mesActual = mesActual.minusMonths(1);
        cargarCalendario(mesActual);
    }

    /**
     * Muestra el mes siguiente en el calendario.
     */
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

    /**
     * Cambia la escena a la vista indicada.
     */
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

    /**
     * Capitaliza la primera letra.
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Color asociado al tipo de actividad.
     */
    private String getTipoColor(String tipo) {
        return "DEPORTIVA".equals(tipo) ? "#dbeafe"
                : "CULTURAL".equals(tipo) ? "#e9d5ff"
                : "#bbf7d0";
    }
}
