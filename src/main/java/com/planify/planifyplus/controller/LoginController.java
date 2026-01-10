// src/main/java/com/planify/planifyplus/controller/LoginController.java
package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.UsuarioDAO;
import com.planify.planifyplus.dto.UsuarioDTO;
import com.planify.planifyplus.util.AlertUtil;
import com.planify.planifyplus.util.Sesion;
import com.planify.planifyplus.util.ViewUtil;
import com.planify.planifyplus.util.WindowUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField userLogin;
    @FXML private PasswordField contrasenaLogin;
    @FXML private Button iniciarSesionBoton;
    @FXML private Hyperlink botonNoTenerCuenta;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    private void onIniciarSesion(ActionEvent e) {
        String email = userLogin.getText().trim();
        String pass  = contrasenaLogin.getText();

        if (email.isEmpty()) {
            AlertUtil.error("Campo obligatorio", "Introduce el correo electrónico.");
            return;
        }
        if (pass.isEmpty()) {
            AlertUtil.error("Campo obligatorio", "Introduce la contraseña.");
            return;
        }

        try {
            UsuarioDTO usuario = usuarioDAO.buscarPorEmail(email);

            if (usuario == null) {
                AlertUtil.error("Cuenta no encontrada", "No existe ninguna cuenta con ese correo.");
                return;
            }

            if (!usuario.getContrasena().equals(pass)) {
                AlertUtil.error("Inicio de sesión incorrecto", "La contraseña que has introducido no es correcta.");
                return;
            }

            Sesion.setUsuarioActual(usuario);

            AlertUtil.info(
                    "Bienvenido",
                    "Hola, " + usuario.getNombre() + "\n\n¡Bienvenido a PlanifyPlus!"
            );

            go("/vistas/Inicio.fxml");

        } catch (Exception ex) {
            AlertUtil.error("Error", "No se pudo iniciar sesión.\nDetalle: " + ex.getMessage());
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

    private void go(String fxmlPath) {
        try {
            Stage stage = (Stage) iniciarSesionBoton.getScene().getWindow();
            var root = ViewUtil.loadFXML(getClass(), fxmlPath);
            Scene scene = new Scene(root);

            var css = getClass().getResource("/css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            WindowUtil.forceMaximize(stage);
        } catch (Exception e) {
            AlertUtil.error("Error", "No se pudo abrir la pantalla: " + fxmlPath);
        }
    }
}
