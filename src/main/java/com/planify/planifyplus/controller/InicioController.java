package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.ActividadDAO;
import com.planify.planifyplus.dao.InscripcionDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.util.Sesion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    private final ActividadDAO actividadDAO = new ActividadDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

    public void initialize() {
        logoImage.setImage(new Image(getClass().getResource("/img/descarga.png").toExternalForm()));
        cargarActividadesComunidad();

        boolean loggedIn = Sesion.getUsuarioActual() != null;
        updateUIForSession(loggedIn);

        if (loggedIn) {
            cargarActividadesUsuario();
        }

        configurarUIRol();

        VBox.setVgrow(scrollActividadesComunidad, Priority.ALWAYS);
        VBox.setVgrow(scrollActividadesUsuario, Priority.ALWAYS);
    }

    private void cargarActividadesComunidad() {
        contenedorComunidad.getChildren().clear();

        Long idUsuarioActual = null;
        if (Sesion.getUsuarioActual() != null) {
            idUsuarioActual = Sesion.getUsuarioActual().getId();
        }

        List<ActividadDTO> predeterminadas = actividadDAO.obtenerPredeterminadas();
        for (ActividadDTO act : predeterminadas) {
            contenedorComunidad.getChildren().add(crearCardActividad(act));
        }

        List<ActividadDTO> creadasUsuarios = actividadDAO.obtenerNoPredeterminadas();
        for (ActividadDTO act : creadasUsuarios) {
            if (idUsuarioActual != null &&
                    act.getCreador() != null &&
                    act.getCreador().getId() == idUsuarioActual) {
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

        if (esCreadorUsuario) {
            Button btnEditar = new Button("✏️");
            btnEditar.setStyle(
                    "-fx-background-color: #3B82F6;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 50%;" +
                            "-fx-font-size: 16;" +
                            "-fx-padding: 8 12 8 12;" +
                            "-fx-cursor: hand;"
            );
            btnEditar.setOnAction(e -> {
                e.consume();
                onEditarActividad(act);
            });

            Button btnEliminar = new Button("🗑️");
            btnEliminar.setStyle(
                    "-fx-background-color: #DC2626;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 50%;" +
                            "-fx-font-size: 16;" +
                            "-fx-padding: 8 12 8 12;" +
                            "-fx-cursor: hand;"
            );
            btnEliminar.setOnAction(e -> {
                e.consume();
                onEliminarActividadUsuario(act);
            });

            hBoton.getChildren().addAll(btnEditar, btnEliminar);

        } else {
            Button btnInscribir = new Button();
            btnInscribir.setStyle(
                    "-fx-background-color: #3B82F6;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 18;" +
                            "-fx-font-size: 15;" +
                            "-fx-padding: 6 22 6 22;"
            );

            if (!Sesion.haySesion()) {
                btnInscribir.setText("Inicia sesión");
                btnInscribir.setDisable(true);
            } else {
                boolean inscrito = inscripcionDAO.estaInscrito(Sesion.getIdUsuario(), act.getId());
                btnInscribir.setText(inscrito ? "Inscrito" : "Inscribirse");

                boolean lleno = inscritos >= act.getAforo();
                btnInscribir.setDisable(lleno && !inscrito);

                btnInscribir.setOnAction(e -> {
                    e.consume();
                    long userId = Sesion.getIdUsuario();
                    boolean ya = inscripcionDAO.estaInscrito(userId, act.getId());

                    if (ya) {
                        inscripcionDAO.cancelar(userId, act.getId());
                    } else {
                        long ahora = inscripcionDAO.contarInscritos(act.getId());
                        if (ahora >= act.getAforo()) return;
                        inscripcionDAO.inscribir(Sesion.getUsuarioActual(), act);
                    }

                    cargarActividadesComunidad();
                    if (Sesion.getUsuarioActual() != null) cargarActividadesUsuario();
                });
            }

            hBoton.getChildren().add(btnInscribir);
        }

        if (Sesion.esAdmin() && !esCreadorUsuario) {
            Button btnEliminarAdmin = new Button("Eliminar");
            btnEliminarAdmin.setStyle(
                    "-fx-background-color: #DC2626;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 18;" +
                            "-fx-font-size: 14;" +
                            "-fx-padding: 6 18 6 18;"
            );
            btnEliminarAdmin.setOnAction(e -> {
                e.consume();
                onEliminarActividad(act);
            });
            hBoton.getChildren().add(btnEliminarAdmin);
        }

        vbox.getChildren().addAll(hTituloTipo, lblDesc, lblCiudadAct, hFechaAforo, hBoton);
        vbox.setOnMouseClicked(e -> abrirDetalleActividad(act));

        return vbox;
    }

    private void onEditarActividad(ActividadDTO actividad) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/crearActividad.fxml"));
            Parent root = loader.load();

            CrearActividadController controller = loader.getController();
            controller.setActividadParaEditar(actividad);

            Stage stage = (Stage) logoImage.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarError("No se pudo abrir la pantalla de edición");
        }
    }

    private void onEliminarActividadUsuario(ActividadDTO act) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar actividad");
        alert.setHeaderText("¿Eliminar tu actividad?");
        alert.setContentText(
                "Esta acción no se puede deshacer.\n\n" +
                        "La actividad \"" + act.getTitulo() + "\" será eliminada."
        );

        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType btnEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(btnCancelar, btnEliminar);

        ButtonType resultado = alert.showAndWait().orElse(btnCancelar);
        if (resultado == btnEliminar) {
            actividadDAO.eliminarPorId(act.getId());
            cargarActividadesComunidad();
            cargarActividadesUsuario();

            Alert confirmacion = new Alert(Alert.AlertType.INFORMATION);
            confirmacion.setTitle("Actividad eliminada");
            confirmacion.setHeaderText(null);
            confirmacion.setContentText("La actividad \"" + act.getTitulo() + "\" ha sido eliminada correctamente.");
            confirmacion.showAndWait();
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void abrirDetalleActividad(ActividadDTO actividad) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/Actividad.fxml"));
            Parent root = loader.load();
            ActividadController controller = loader.getController();
            controller.setActividad(actividad);
            Stage stage = (Stage) logoImage.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String getTipoColor(String tipo) {
        return tipo.equals("DEPORTIVA") ? "#dbeafe"
                : tipo.equals("CULTURAL") ? "#e9d5ff"
                : "#bbf7d0";
    }

    @FXML private void handleRegister() { irAVista("registro.fxml"); }
    @FXML private void handleLogin() { irAVista("login.fxml"); }
    @FXML private void handleCrearActividad() { irAVista("crearActividad.fxml"); }
    @FXML private void handlePerfil() { irAVista("perfil.fxml"); }

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/" + fxml));
            Parent root = loader.load();
            Stage stage = (Stage) logoImage.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
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
            lblUser.setText(Sesion.getUsuarioActual().getNombre().substring(0, 1));
            lblCiudad.setText(Sesion.getUsuarioActual().getCiudad());
            cargarActividadesUsuario();
        } else {
            lblUser.setText("");
            lblCiudad.setText("");
            contenedorUsuario.getChildren().clear();
        }
    }

    public void onUsuarioLogueado() {
        updateUIForSession(true);
        configurarUIRol();
        cargarActividadesComunidad();
    }

    private void configurarUIRol() {
        boolean esAdmin = Sesion.esAdmin();
        if (lblAdminBadge != null) {
            lblAdminBadge.setVisible(esAdmin);
            lblAdminBadge.setManaged(esAdmin);
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
        alert.setContentText(
                "Esta acción no se puede deshacer.\n\n" +
                        "La actividad \"" + act.getTitulo() + "\" será eliminada."
        );

        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType btnEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(btnCancelar, btnEliminar);

        ButtonType resultado = alert.showAndWait().orElse(btnCancelar);
        if (resultado == btnEliminar) {
            actividadDAO.eliminarPorId(act.getId());
            cargarActividadesComunidad();
            if (Sesion.getUsuarioActual() != null) cargarActividadesUsuario();
        }
    }
}
