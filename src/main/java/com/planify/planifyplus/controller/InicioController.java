package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.ActividadDAO;
import com.planify.planifyplus.dao.InscripcionDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.util.Sesion;
import com.planify.planifyplus.util.ViewUtil;
import com.planify.planifyplus.util.WindowUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class InicioController {

    @FXML private ImageView logoImage;
    @FXML private TextField searchBar;
    @FXML private ComboBox cmbDistancia;
    @FXML private Button btnRegister, btnLogin, btnLogout, btnCrearActividad, btnPerfil;
    @FXML private Label lblCiudad, lblUser, lblNoSesion;
    @FXML private VBox contenedorComunidad, contenedorUsuario;
    @FXML private ScrollPane scrollActividadesComunidad, scrollActividadesUsuario;
    @FXML private Label lblAdminBadge;
    @FXML private Label lblTituloPanelDerecho;

    private final ActividadDAO actividadDAO = new ActividadDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

    public void initialize() {
        var img = getClass().getResource("/img/descarga.png");
        if (img != null) logoImage.setImage(new Image(img.toExternalForm()));

        cargarActividadesComunidad();

        boolean loggedIn = Sesion.getUsuarioActual() != null;
        updateUIForSession(loggedIn);

        if (loggedIn) cargarPanelDerechoSegunRol();

        configurarUIRol();

        VBox.setVgrow(scrollActividadesComunidad, Priority.ALWAYS);
        VBox.setVgrow(scrollActividadesUsuario, Priority.ALWAYS);
    }

    private void cargarActividadesComunidad() {
        contenedorComunidad.getChildren().clear();

        Long idUsuarioActual = (Sesion.getUsuarioActual() != null) ? Sesion.getUsuarioActual().getId() : null;

        List<ActividadDTO> predeterminadas = actividadDAO.obtenerPredeterminadas();
        for (ActividadDTO act : predeterminadas) {
            contenedorComunidad.getChildren().add(crearCardActividad(act));
        }

        List<ActividadDTO> creadasUsuarios = actividadDAO.obtenerNoPredeterminadas();
        for (ActividadDTO act : creadasUsuarios) {
            if (idUsuarioActual != null && act.getCreador() != null && act.getCreador().getId() == idUsuarioActual) {
                continue;
            }
            contenedorComunidad.getChildren().add(crearCardActividad(act));
        }
    }

    private void cargarActividadesUsuario() {
        contenedorUsuario.getChildren().clear();
        if (Sesion.getUsuarioActual() == null) return;

        long idUsuario = Sesion.getUsuarioActual().getId();
        List<ActividadDTO> actividades = actividadDAO.obtenerCreadasPorUsuario(idUsuario);
        for (ActividadDTO act : actividades) {
            contenedorUsuario.getChildren().add(crearCardActividad(act));
        }
    }

    private void cargarActividadesDenunciadas() {
        contenedorUsuario.getChildren().clear();
        List<ActividadDTO> denunciadas = actividadDAO.obtenerDenunciadasOrdenadas();

        // Opcional: mensaje si no hay
        if (denunciadas.isEmpty()) {
            Label lbl = new Label("No hay actividades denunciadas.");
            lbl.setStyle("-fx-text-fill: #6B7280; -fx-padding: 12;");
            contenedorUsuario.getChildren().add(lbl);
            return;
        }

        for (ActividadDTO act : denunciadas) {
            contenedorUsuario.getChildren().add(crearCardActividad(act));
        }
    }

    private void cargarPanelDerechoSegunRol() {
        if (!Sesion.haySesion()) {
            contenedorUsuario.getChildren().clear();
            return;
        }
        if (Sesion.esAdmin()) cargarActividadesDenunciadas();
        else cargarActividadesUsuario();
    }

    private Pane crearCardActividad(ActividadDTO act) {
        VBox vbox = new VBox(12);
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

        boolean esCreadorUsuario = Sesion.getUsuarioActual() != null &&
                act.getCreador() != null &&
                act.getCreador().getId() == Sesion.getUsuarioActual().getId();

        boolean esAdmin = Sesion.esAdmin();

        if (esCreadorUsuario) {

            Button btnEditar = new Button("Editar");
            btnEditar.setStyle(
                    "-fx-background-color: #3B82F6;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 18;" +
                            "-fx-font-size: 14;" +
                            "-fx-padding: 6 18 6 18;" +
                            "-fx-cursor: hand;"
            );
            btnEditar.setOnAction(e -> {
                e.consume();
                onEditarActividad(act);
            });
            btnEditar.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

            Button btnEliminar = new Button("Eliminar");
            btnEliminar.setStyle(
                    "-fx-background-color: #DC2626;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 18;" +
                            "-fx-font-size: 14;" +
                            "-fx-padding: 6 18 6 18;" +
                            "-fx-cursor: hand;"
            );
            btnEliminar.setOnAction(e -> {
                e.consume();
                onEliminarActividadUsuario(act);
            });
            btnEliminar.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

            hBoton.getChildren().addAll(btnEditar, btnEliminar);

        } else if (esAdmin) {

            Button btnEliminarAdmin = new Button("Eliminar");
            btnEliminarAdmin.setStyle(
                    "-fx-background-color: #DC2626;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 18;" +
                            "-fx-font-size: 14;" +
                            "-fx-padding: 6 18 6 18;" +
                            "-fx-cursor: hand;"
            );
            btnEliminarAdmin.setOnAction(e -> {
                e.consume();
                onEliminarActividad(act);
            });
            btnEliminarAdmin.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

            hBoton.getChildren().add(btnEliminarAdmin);

        } else {

            Button btnInscribir = new Button("Inscribirse");
            btnInscribir.setStyle(
                    "-fx-background-color: #3B82F6;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 18;" +
                            "-fx-font-size: 15;" +
                            "-fx-padding: 6 22 6 22;"
            );
            btnInscribir.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

            if (!Sesion.haySesion()) {
                btnInscribir.setText("Inicia sesión");
                btnInscribir.setDisable(false);
                btnInscribir.setOnAction(e -> {
                    e.consume();
                    irAVista("Login.fxml");
                });
            } else {
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

                    cargarActividadesComunidad();
                    cargarPanelDerechoSegunRol();
                });
            }

            hBoton.getChildren().add(btnInscribir);
        }

        vbox.getChildren().addAll(hTituloTipo, lblDesc, lblCiudadAct, hFechaAforo, hBoton);

        // ✅ IMPORTANTE: el click de abrir detalle solo si no has pulsado un botón (ya lo consumimos arriba)
        vbox.setOnMouseClicked(e -> abrirDetalleActividad(act));

        return vbox;
    }

    private void onEditarActividad(ActividadDTO actividad) {
        try {
            var loader = ViewUtil.loaderFXML(getClass(), "/vistas/crearActividad.fxml");
            Parent root = loader.load();

            CrearActividadController controller = loader.getController();
            controller.setActividadParaEditar(actividad);

            Stage stage = (Stage) logoImage.getScene().getWindow();
            Scene scene = new Scene(root);

            var css = getClass().getResource("/css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            WindowUtil.forceMaximize(stage);

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarError("No se pudo abrir la pantalla de edición");
        }
    }

    private void onEliminarActividadUsuario(ActividadDTO act) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar actividad");
        alert.setHeaderText("¿Eliminar tu actividad?");
        alert.setContentText("Esta acción no se puede deshacer.\n\nLa actividad \"" + act.getTitulo() + "\" será eliminada.");

        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType btnEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(btnCancelar, btnEliminar);

        ButtonType resultado = alert.showAndWait().orElse(btnCancelar);
        if (resultado == btnEliminar) {
            actividadDAO.eliminarPorId(act.getId());
            cargarActividadesComunidad();
            cargarPanelDerechoSegunRol();

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Actividad eliminada");
            ok.setHeaderText(null);
            ok.setContentText("La actividad \"" + act.getTitulo() + "\" ha sido eliminada correctamente.");
            ok.showAndWait();
        }
    }

    private void abrirDetalleActividad(ActividadDTO actividad) {
        try {
            var loader = ViewUtil.loaderFXML(getClass(), "/vistas/Actividad.fxml");
            Parent root = loader.load();

            ActividadController controller = loader.getController();
            controller.setActividad(actividad);

            Stage stage = (Stage) logoImage.getScene().getWindow();
            Scene scene = new Scene(root);

            var css = getClass().getResource("/css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            WindowUtil.forceMaximize(stage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String capitalize(String str) { return str.substring(0, 1).toUpperCase() + str.substring(1); }

    private String getTipoColor(String tipo) {
        return tipo.equals("DEPORTIVA") ? "#dbeafe"
                : tipo.equals("CULTURAL") ? "#e9d5ff"
                : "#bbf7d0";
    }

    @FXML private void handleRegister() { irAVista("Registro.fxml"); }
    @FXML private void handleLogin() { irAVista("Login.fxml"); }
    @FXML private void handleCrearActividad() { irAVista("crearActividad.fxml"); }
    @FXML private void handlePerfil() { irAVista("Perfil.fxml"); }

    @FXML
    private void handleLogout() {
        Sesion.cerrarSesion();
        updateUIForSession(false);
        configurarUIRol();
        cargarActividadesComunidad();
        contenedorUsuario.getChildren().clear();
    }

    private void irAVista(String fxml) {
        try {
            Stage stage = (Stage) logoImage.getScene().getWindow();
            Parent root = ViewUtil.loadFXML(getClass(), "/vistas/" + fxml);

            Scene scene = new Scene(root);
            var css = getClass().getResource("/css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            WindowUtil.forceMaximize(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateUIForSession(boolean loggedIn) {
        btnRegister.setVisible(!loggedIn);
        btnLogin.setVisible(!loggedIn);
        btnPerfil.setVisible(loggedIn);
        btnPerfil.setText("Mi Perfil");
        btnLogout.setVisible(loggedIn);
        btnLogout.setText("Cerrar sesión");
        btnCrearActividad.setVisible(loggedIn);
        btnCrearActividad.setText("Crear Actividad");
        lblCiudad.setVisible(loggedIn);
        lblUser.setVisible(loggedIn);
        scrollActividadesUsuario.setVisible(loggedIn);
        lblNoSesion.setVisible(!loggedIn);

        if (loggedIn && Sesion.getUsuarioActual() != null) {
            lblUser.setText(Sesion.getUsuarioActual().getNombre());
            lblCiudad.setText(Sesion.getUsuarioActual().getCiudad());
        } else {
            lblUser.setText("");
            lblCiudad.setText("");
            contenedorUsuario.getChildren().clear();
        }

        cargarPanelDerechoSegunRol();
    }

    public void onUsuarioLogueado() {
        updateUIForSession(true);
        configurarUIRol();
        cargarActividadesComunidad();
        cargarPanelDerechoSegunRol();
    }

    private void configurarUIRol() {
        boolean esAdmin = Sesion.esAdmin();
        if (lblAdminBadge != null) {
            lblAdminBadge.setVisible(esAdmin);
            lblAdminBadge.setManaged(esAdmin);
        }

        if (lblTituloPanelDerecho != null) {
            lblTituloPanelDerecho.setText(esAdmin ? "Actividades denunciadas" : "Mis Actividades Creadas");
        }

        if (btnCrearActividad != null) {
            btnCrearActividad.setVisible(Sesion.haySesion() && !esAdmin);
            btnCrearActividad.setManaged(Sesion.haySesion() && !esAdmin);
        }

        if (cmbDistancia != null) {
            cmbDistancia.setVisible(!esAdmin);
            cmbDistancia.setManaged(!esAdmin);
        }

        if (lblCiudad != null) {
            if (esAdmin) {
                lblCiudad.setVisible(false);
                lblCiudad.setManaged(false);
            } else {
                lblCiudad.setManaged(true);
            }
        }
    }

    private void onEliminarActividad(ActividadDTO act) {
        if (!Sesion.esAdmin()) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar actividad");
        alert.setHeaderText("¿Eliminar actividad?");
        alert.setContentText("Esta acción no se puede deshacer.\n\nLa actividad \"" + act.getTitulo() + "\" será eliminada.");

        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType btnEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(btnCancelar, btnEliminar);

        ButtonType resultado = alert.showAndWait().orElse(btnCancelar);
        if (resultado == btnEliminar) {
            actividadDAO.eliminarPorId(act.getId());
            cargarActividadesComunidad();
            cargarPanelDerechoSegunRol();
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
