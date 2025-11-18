package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.ActividadDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.List;

public class InicioController {

    @FXML
    private ImageView logoImage;
    @FXML
    private TextField searchBar;
    @FXML
    private Button btnRegister, btnLogin, btnLogout, btnCrearActividad, btnPerfil;
    @FXML
    private Label lblCiudad, lblUser, lblNoSesion;
    @FXML
    private VBox contenedorComunidad, contenedorUsuario;
    @FXML
    private ScrollPane scrollActividadesComunidad, scrollActividadesUsuario;

    private final ActividadDAO actividadDAO = new ActividadDAO();

    public void initialize() {
        logoImage.setImage(new Image(getClass().getResource("/imagenes/descarga.png").toExternalForm()));
        cargarActividadesComunidad();
        updateUIForSession(Sesion.getUsuarioActual() != null);
    }

    private void cargarActividadesComunidad() {
        contenedorComunidad.getChildren().clear();
        List<ActividadDTO> actividades = actividadDAO.obtenerTodas();
        for (ActividadDTO act : actividades) {
            contenedorComunidad.getChildren().add(
                    crearCardActividad(act)
            );
        }
    }

    private Pane crearCardActividad(ActividadDTO act) {
        VBox vbox = new VBox(5);
        vbox.setStyle("-fx-padding: 14; -fx-background-color: #FFF; -fx-border-radius: 14; -fx-background-radius: 14; -fx-border-width: 1; -fx-border-color: #e2e8f0;");
        Label lblTitulo = new Label(act.getTitulo());
        lblTitulo.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        Label lblTipo = new Label(act.getTipo().toString());
        lblTipo.setStyle("-fx-background-color: " + getTipoColor(act.getTipo().toString()) + "; -fx-font-size: 13; -fx-padding: 4 18 4 18; -fx-background-radius: 11; -fx-font-weight: bold;");
        Label lblDesc = new Label(act.getDescripcion());
        lblDesc.setStyle("-fx-text-fill: #4B4B4B; -fx-font-size: 14;");
        Label lblCiudad = new Label("📍 " + act.getCiudad() + " · " + act.getUbicacion());
        lblCiudad.setStyle("-fx-text-fill: #1663e3; -fx-font-size: 14;");
        Label lblFecha = new Label(String.valueOf(act.getFechaHoraInicio()));
        lblFecha.setStyle("-fx-font-size: 14; -fx-text-fill: #222;");
        Label lblAforo = new Label("Aforo: " + act.getAforo() + " personas");
        lblAforo.setStyle("-fx-font-size: 13; -fx-text-fill: #666;");
        Button btnInscribir = new Button("Inscribirse");
        btnInscribir.setStyle("-fx-background-color: #93C5FD; -fx-text-fill: #222; -fx-background-radius: 20; -fx-padding: 5 28 5 28;");
        vbox.getChildren().addAll(lblTitulo, lblTipo, lblDesc, lblCiudad, lblFecha, lblAforo, btnInscribir);
        return vbox;
    }

    private String getTipoColor(String tipo) {
        return tipo.equals("DEPORTIVA") ? "#dbeafe"
                : tipo.equals("CULTURAL") ? "#e9d5ff"
                : "#bbf7d0";
    }

    @FXML
    private void handleRegister() {
        irAVista("registro.fxml");
    }

    @FXML
    private void handleLogin() {
        irAVista("login.fxml");
    }

    @FXML
    private void handleLogout() {
        Sesion.cerrarSesion();
        updateUIForSession(false);
    }

    @FXML
    private void handleCrearActividad() {
        irAVista("crearActividad.fxml");
    }

    @FXML
    private void handlePerfil() {
        irAVista("perfil.fxml");
    }

    private void irAVista(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/" + fxml));
            Parent root = loader.load();
            Stage stage = (Stage) logoImage.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateUIForSession(boolean loggedIn) {
        btnRegister.setVisible(!loggedIn);
        btnLogin.setVisible(!loggedIn);
        lblCiudad.setVisible(loggedIn);
        lblUser.setVisible(loggedIn);
        btnPerfil.setVisible(loggedIn);
        btnLogout.setVisible(loggedIn);
        btnCrearActividad.setVisible(loggedIn);
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
    }

    // Este método debe llamarse tras login exitoso, por ejemplo:
    public void onUsuarioLogueado() {
        updateUIForSession(true);
    }
}
