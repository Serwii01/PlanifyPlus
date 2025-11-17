package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.UsuarioDAO;
import com.planify.planifyplus.dto.UsuarioDTO;
import com.planify.planifyplus.util.Sesion;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField userLogin;

    @FXML
    private PasswordField contrasenaLogin;

    @FXML
    private Button iniciarSesionBoton;

    @FXML
    private Hyperlink botonNoTenerCuenta;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    // ====== ACCIONES ======

    @FXML
    private void onIniciarSesion(ActionEvent e) {
        String email = userLogin.getText().trim();
        String pass  = contrasenaLogin.getText();

        if (email.isEmpty()) {
            error("Introduce el correo electrónico.");
            return;
        }
        if (pass.isEmpty()) {
            error("Introduce la contraseña.");
            return;
        }

        try {
            UsuarioDTO usuario = usuarioDAO.buscarPorEmail(email);

            if (usuario == null) {
                error("No existe ninguna cuenta con ese correo.");
                return;
            }


            if (!usuario.getContrasena().equals(pass)) {
                error("Contraseña incorrecta.");
                return;
            }

            //  login correcto → guardamos en la sesión global
            Sesion.setUsuarioActual(usuario);

            info("¡Bienvenido, " + usuario.getNombre() + "!");

            // Ir a la pantalla principal
            go("/vistas/Inicio.fxml");

        } catch (Exception ex) {
            error("No se pudo iniciar sesión.\nDetalle: " + ex.getMessage());
        }
    }

    @FXML
    private void onIrRegistro(ActionEvent e) {
        go("/vistas/Registro.fxml");
    }

    @FXML
    private void onIrInicio(ActionEvent e) {
        go("/vistas/Inicio.fxml");
    }

    // ====== HELPERS ======

    private void go(String fxmlPath) {
        try {
            Stage stage = (Stage) iniciarSesionBoton.getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource(fxmlPath))));
            stage.centerOnScreen();
        } catch (IOException e) {
            error("No se pudo abrir la pantalla: " + fxmlPath);
        }
    }

    private void error(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}