package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.ActividadDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.util.Sesion;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    @FXML private Label lblTituloPanelDerecho; // Label del título "Mis actividades creadas"

    private final ActividadDAO actividadDAO = new ActividadDAO();

    public void initialize() {
        logoImage.setImage(new Image(getClass().getResource("/img/descarga.png").toExternalForm()));
        cargarActividadesComunidad();

        boolean loggedIn = Sesion.getUsuarioActual() != null;
        updateUIForSession(loggedIn);
        configurarUIRol();

        VBox.setVgrow(scrollActividadesComunidad, Priority.ALWAYS);
        VBox.setVgrow(scrollActividadesUsuario, Priority.ALWAYS);
    }

    // ============================================================
    // CARGA DE ACTIVIDADES
    // ============================================================

    //metodo que coloca las actividades en el contenedor de la izquierda
    private void cargarActividadesComunidad() {
        contenedorComunidad.getChildren().clear();
        Long idUsuarioActual = null;
        if (Sesion.getUsuarioActual() != null) {
            idUsuarioActual = Sesion.getUsuarioActual().getId();
        }

        // 1) Predeterminadas
        List<ActividadDTO> predeterminadas = actividadDAO.obtenerPredeterminadas();
        for (ActividadDTO act : predeterminadas) {
            contenedorComunidad.getChildren().add(crearCardActividad(act));
        }

        // 2) No predeterminadas (creadas por usuarios)
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
        if (Sesion.getUsuarioActual() == null) {
            return;
        }

        long idUsuario = Sesion.getUsuarioActual().getId();
        List<ActividadDTO> actividades = actividadDAO.obtenerCreadasPorUsuario(idUsuario);
        for (ActividadDTO act : actividades) {
            contenedorUsuario.getChildren().add(crearCardActividad(act));
        }
    }

    // actividades denunciadas para el admin
    private void cargarActividadesDenunciadas() {
        contenedorUsuario.getChildren().clear();
        List<ActividadDTO> denunciadas = actividadDAO.obtenerDenunciadasOrdenadas();
        for (ActividadDTO act : denunciadas) {
            contenedorUsuario.getChildren().add(crearCardActividad(act));
        }
    }

    // decide qué cargar en el panel derecho según el rol
    private void cargarPanelDerechoSegunRol() {
        if (!Sesion.haySesion()) {
            contenedorUsuario.getChildren().clear();
            return;
        }

        if (Sesion.esAdmin()) {
            cargarActividadesDenunciadas();
        } else {
            cargarActividadesUsuario();
        }
    }

    // ============================================================
    // CREACIÓN DE LA CARD CON BOTONES DE EDITAR Y ELIMINAR
    // ============================================================

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

        HBox hFechaAforo = new HBox(10);
        Label lblFecha = new Label(fechaFormateada);
        lblFecha.setStyle("-fx-font-size: 15; -fx-text-fill: #222;");
        Label lblAforo = new Label("1 / " + act.getAforo() + " inscritos");
        HBox.setHgrow(lblFecha, Priority.ALWAYS);
        hFechaAforo.getChildren().addAll(lblFecha, lblAforo);

        HBox hBoton = new HBox(8);
        hBoton.setAlignment(Pos.CENTER_RIGHT);

        boolean esCreadorUsuario = Sesion.getUsuarioActual() != null &&
                act.getCreador() != null &&
                act.getCreador().getId() == Sesion.getUsuarioActual().getId();

        boolean esAdmin = Sesion.esAdmin();

        // Si el usuario es el creador, mostrar botones de editar y eliminar
        if (esCreadorUsuario) {
            // Botón Editar con icono ✏️
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

            // Botón Eliminar con icono 🗑️
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

        } else if (esAdmin) {
            // Admin: solo botón Eliminar (sin Inscribirse)
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
            hBoton.getChildren().add(btnEliminarAdmin);

        } else {
            // Botón Inscribirse para actividades de otros usuarios (usuario normal)
            Button btnInscribir = new Button("Inscribirse");
            btnInscribir.setStyle(
                    "-fx-background-color: #3B82F6;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 18;" +
                            "-fx-font-size: 15;" +
                            "-fx-padding: 6 22 6 22;"
            );
            hBoton.getChildren().add(btnInscribir);
        }

        vbox.getChildren().addAll(hTituloTipo, lblDesc, lblCiudadAct, hFechaAforo, hBoton);
        vbox.setOnMouseClicked(e -> abrirDetalleActividad(act));

        return vbox;
    }

    // ============================================================
    // MÉTODOS PARA EDITAR Y ELIMINAR ACTIVIDADES
    // ============================================================

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
            cargarPanelDerechoSegunRol();

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

    //abrir la vista de la actividad en detalle
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

    //metodo para poner en mayusculas
    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    //setear color segun tipo de actividad
    private String getTipoColor(String tipo) {
        return tipo.equals("DEPORTIVA") ? "#dbeafe"
                : tipo.equals("CULTURAL") ? "#e9d5ff"
                : "#bbf7d0";
    }

    // navegacion
    @FXML private void handleRegister() { irAVista("registro.fxml"); }
    @FXML private void handleLogin() { irAVista("login.fxml"); }
    @FXML private void handleCrearActividad() { irAVista("crearActividad.fxml"); }
    @FXML private void handlePerfil() { irAVista("perfil.fxml"); }

    //metodo para cerrar sesion
    @FXML
    private void handleLogout() {
        Sesion.cerrarSesion();
        updateUIForSession(false);
        configurarUIRol();
        cargarActividadesComunidad();
        contenedorUsuario.getChildren().clear();
    }

    //metodo para cambiar de vista
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

    //cuando hay sesion activa cambia la interfaz
    public void updateUIForSession(boolean loggedIn) {
        //cambia valores de la vista
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

        //para poner el nombre y la ciudad del usuario
        if (loggedIn && Sesion.getUsuarioActual() != null) {
            lblUser.setText(Sesion.getUsuarioActual().getNombre().substring(0, 1));
            lblCiudad.setText(Sesion.getUsuarioActual().getCiudad());
        } else {
            lblUser.setText("");
            lblCiudad.setText("");
            contenedorUsuario.getChildren().clear();
        }

        // cargo el panel derecho según el rol
        cargarPanelDerechoSegunRol();
    }

    //cuando el usuario se logea la interfaz cambia
    public void onUsuarioLogueado() {
        updateUIForSession(true);
        configurarUIRol();
        cargarActividadesComunidad();
        cargarPanelDerechoSegunRol();
    }

    // Cambiar la interfaz segun el rol
    private void configurarUIRol() {
        //si es adminstrador pone el icono
        boolean esAdmin = Sesion.esAdmin();
        if (lblAdminBadge != null) {
            lblAdminBadge.setVisible(esAdmin);
            lblAdminBadge.setManaged(esAdmin);
        }

        // cambio el título del panel derecho
        if (lblTituloPanelDerecho != null) {
            if (esAdmin) {
                lblTituloPanelDerecho.setText("Actividades denunciadas");
            } else {
                lblTituloPanelDerecho.setText("Mis Actividades Creadas");
            }
        }

        // oculto el boton de crear actividad para admins
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

    //eliminar actividad siendo administrador
    private void onEliminarActividad(ActividadDTO act) {
        //comprueba si es administrador primero
        if (!Sesion.esAdmin()) {
            return;
        }

        //crea una alerta de confirmación de eliminación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar actividad");
        alert.setHeaderText("¿Eliminar actividad?");
        alert.setContentText(
                "Esta acción no se puede deshacer.\n\n" +
                        "La actividad \"" + act.getTitulo() + "\" será eliminada."
        );
        //crea dos botones, para confirmar o eliminar
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType btnEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        //le setea los botones a la alerta
        alert.getButtonTypes().setAll(btnCancelar, btnEliminar);

        ButtonType resultado = alert.showAndWait().orElse(btnCancelar);
        //si se elimina la actividad
        if (resultado == btnEliminar) {
            //llama al metodo eliminar por id (actividad DAO)
            actividadDAO.eliminarPorId(act.getId());
            //despues vuelve a cargar las actividades de la comunidad
            cargarActividadesComunidad();
            //y recarga el panel derecho según el rol
            cargarPanelDerechoSegunRol();
        }
    }
}
