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
import com.planify.planifyplus.controller.ActividadController;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class InicioController {

    @FXML private ImageView logoImage;
    @FXML private TextField searchBar;
    @FXML private ComboBox<String> cmbDistancia;
    @FXML private Button btnRegister, btnLogin, btnLogout, btnCrearActividad, btnPerfil;
    @FXML private Label lblCiudad, lblUser, lblNoSesion;
    @FXML private VBox contenedorComunidad, contenedorUsuario;
    @FXML private ScrollPane scrollActividadesComunidad, scrollActividadesUsuario;

    // Badge ADMIN en la barra superior (añadido en el FXML)
    @FXML private Label lblAdminBadge;

    private final ActividadDAO actividadDAO = new ActividadDAO();




    public void initialize() {
        logoImage.setImage(new Image(getClass().getResource("/img/descarga.png").toExternalForm()));

        // Cargamos las actividades de la comunidad (predeterminadas + de otros usuarios)
        cargarActividadesComunidad();

        boolean loggedIn = Sesion.getUsuarioActual() != null;
        updateUIForSession(loggedIn);
        if (loggedIn) {
            cargarActividadesUsuario();
        }

        // Ajustar visibilidad según rol (admin / usuario normal)
        configurarUIRol();

        VBox.setVgrow(scrollActividadesComunidad, Priority.ALWAYS);
        VBox.setVgrow(scrollActividadesUsuario, Priority.ALWAYS);
    }

    // ============================================================
    //                CARGA DE ACTIVIDADES
    // ============================================================

    /**
     * Actividades de la comunidad:
     * - Siempre: TODAS las predeterminadas.
     * - Además: TODAS las NO predeterminadas creadas por otros usuarios.
     *   (si NO hay sesión, se muestran TODAS las no predeterminadas).
     */
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
            // Si hay sesión y el creador es el usuario actual -> NO la mostramos aquí
            // porque ya irá en "Mis actividades creadas".
            if (idUsuarioActual != null &&
                    act.getCreador() != null &&
                    act.getCreador().getId() == idUsuarioActual) {
                continue;
            }
            contenedorComunidad.getChildren().add(crearCardActividad(act));
        }
    }

    /**
     * Mis actividades creadas:
     * - Solo las NO predeterminadas cuyo creador es el usuario actual.
     */
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

    // ============================================================
    //                CREACIÓN DE LA CARD
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

        Button btnInscribir = new Button("Inscribirse");
        btnInscribir.setStyle(
                "-fx-background-color: #3B82F6;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-font-size: 15;" +
                        "-fx-padding: 6 22 6 22;"
        );
        hBoton.getChildren().add(btnInscribir);

        // Si es admin, añadimos botón rojo "Eliminar"
        if (Sesion.esAdmin()) {
            Button btnEliminar = new Button("Eliminar");
            btnEliminar.setStyle(
                    "-fx-background-color: #DC2626;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 18;" +
                            "-fx-font-size: 14;" +
                            "-fx-padding: 6 18 6 18;"
            );
            btnEliminar.setOnAction(e -> onEliminarActividad(act));
            hBoton.getChildren().add(btnEliminar);


             btnInscribir.setVisible(false);
             btnInscribir.setManaged(false);
        }

        vbox.getChildren().addAll(hTituloTipo, lblDesc, lblCiudadAct, hFechaAforo, hBoton);

        //si se clica en cualquier parte de la tarjeta se ejecuta el metodo de detalle
        vbox.setOnMouseClicked(e -> abrirDetalleActividad(act));

        return vbox;
    }

    //metodo para ir a activididad.fxml
    private void abrirDetalleActividad(ActividadDTO actividad) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/actividad.fxml"));
            Parent root = loader.load();

            // guarda la actividad sobre la que se clica
            ActividadController controller = loader.getController();
            controller.setActividad(actividad);

            // cambiar la escena en la misma ventana y con los estilos
            Stage stage = (Stage) logoImage.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //poner en mayúsculas
    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    //setea colores dependiendo el tipodeactividad
    private String getTipoColor(String tipo) {
        return tipo.equals("DEPORTIVA") ? "#dbeafe"
                : tipo.equals("CULTURAL") ? "#e9d5ff"
                : "#bbf7d0";
    }

    //navegar
    @FXML private void handleRegister() { irAVista("registro.fxml"); }
    @FXML private void handleLogin() { irAVista("login.fxml"); }
    @FXML private void handleCrearActividad() { irAVista("crearActividad.fxml"); }
    @FXML private void handlePerfil() { irAVista("perfil.fxml"); }

    @FXML
    private void handleLogout() {
        Sesion.cerrarSesion();
        updateUIForSession(false);
        configurarUIRol();
        cargarActividadesComunidad();   // reconstruir cards sin botones de admin
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

    //cambiar la interfaz si la sesion esta iniciada
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
        cargarActividadesComunidad(); // reconstruir cards ya con lógica según usuario
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

    // ===================== ELIMINAR ACTIVIDAD (ADMIN) =====================

    private void onEliminarActividad(ActividadDTO act) {
        if (!Sesion.esAdmin()) {
            return; // seguridad extra
        }

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
            if (Sesion.getUsuarioActual() != null) {
                cargarActividadesUsuario();
            }
        }
    }
}

