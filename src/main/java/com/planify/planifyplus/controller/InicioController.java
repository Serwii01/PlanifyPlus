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
    @FXML private ComboBox<String> cmbDistancia;
    @FXML private Button btnRegister, btnLogin, btnLogout, btnCrearActividad, btnPerfil;
    @FXML private Label lblCiudad, lblUser, lblNoSesion;
    @FXML private VBox contenedorComunidad, contenedorUsuario;
    @FXML private ScrollPane scrollActividadesComunidad, scrollActividadesUsuario;

    private final ActividadDAO actividadDAO = new ActividadDAO();

    public void initialize() {
        logoImage.setImage(new Image(getClass().getResource("/img/descarga.png").toExternalForm()));

        cargarActividadesComunidad();

        boolean loggedIn = Sesion.getUsuarioActual() != null;
        updateUIForSession(loggedIn);
        if (loggedIn) {
            cargarActividadesUsuario();
        }

        VBox.setVgrow(scrollActividadesComunidad, Priority.ALWAYS);
        VBox.setVgrow(scrollActividadesUsuario, Priority.ALWAYS);
    }

    private void cargarActividadesComunidad() {
        contenedorComunidad.getChildren().clear();
        List<ActividadDTO> actividades = actividadDAO.obtenerPredeterminadas();
        for (ActividadDTO act : actividades) {
            contenedorComunidad.getChildren().add(crearCardActividad(act));
        }
    }

    private void cargarActividadesUsuario() {
        contenedorUsuario.getChildren().clear();

        if (Sesion.getUsuarioActual() == null) {
            return;
        }

        List<ActividadDTO> actividades = actividadDAO.obtenerNoPredeterminadas();
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

        Label lblCiudad = new Label("📍 " + act.getCiudad() +
                (act.getUbicacion() != null && !act.getUbicacion().isEmpty()
                        ? " · " + act.getUbicacion()
                        : ""));
        lblCiudad.setStyle("-fx-text-fill: #1663e3; -fx-font-size: 15;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String fechaFormateada = act.getFechaHoraInicio().format(formatter);
        HBox hFechaAforo = new HBox(10);
        Label lblFecha = new Label(fechaFormateada);
        lblFecha.setStyle("-fx-font-size: 15; -fx-text-fill: #222;");
        Label lblAforo = new Label("1 / " + act.getAforo() + " inscritos");
        HBox.setHgrow(lblFecha, Priority.ALWAYS);
        hFechaAforo.getChildren().addAll(lblFecha, lblAforo);

        HBox hBoton = new HBox();
        Button btnInscribir = new Button("Inscribirse");
        btnInscribir.setStyle(
                "-fx-background-color: #3B82F6;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-font-size: 15;" +
                        "-fx-padding: 6 22 6 22;"
        );
        hBoton.getChildren().add(btnInscribir);
        hBoton.setAlignment(Pos.CENTER_RIGHT);

        vbox.getChildren().addAll(hTituloTipo, lblDesc, lblCiudad, hFechaAforo, hBoton);

        return vbox;
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

    @FXML
    private void handleLogout() {
        Sesion.cerrarSesion();
        updateUIForSession(false);
    }

    @FXML private void handleCrearActividad() { irAVista("crearActividad.fxml"); }
    @FXML private void handlePerfil() { irAVista("perfil.fxml"); }

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
    }
}
