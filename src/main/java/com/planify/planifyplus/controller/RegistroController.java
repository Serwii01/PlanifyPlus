package com.planify.planifyplus.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class RegistroController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtContrasena;
    @FXML private ComboBox<String> cmbCiudad;
    @FXML private Button btnRegistrarse;

    // Regex sencilla para email
    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    @FXML
    public void initialize() {
        // Rellena combo de ciudades (ajústalo a lo que uséis)
        cmbCiudad.setItems(FXCollections.observableArrayList(
                List.of("Sevilla", "Madrid", "Barcelona", "Valencia", "Málaga", "Bilbao")
        ));
    }

    /* ======================
       Handlers de la vista
       ====================== */

    @FXML
    private void onRegistrarse() {
        String nombre  = txtNombre.getText().trim();
        String email   = txtEmail.getText().trim();
        String pass    = txtContrasena.getText();
        String ciudad  = cmbCiudad.getValue(); // opcional

        // Validación de formulario (sin persistir todavía)
        if (nombre.isEmpty()) { error("El nombre es obligatorio."); return; }
        if (!EMAIL_REGEX.matcher(email).matches()) { error("Correo electrónico no válido."); return; }
        if (pass.length() < 6) { error("La contraseña debe tener al menos 6 caracteres."); return; }

        // TODO (cuando toque): llamar a UsuarioService/UsuarioDAO para persistir
        // usuarioService.registrar(nombre, email, pass, ciudad);

        info("Validación correcta. (Pendiente de guardar en BD)");
        // Si queréis navegar tras el registro, descomenta una:
        // go("/vistas/Inicio.fxml");
        // go("/vistas/Login.fxml");
    }

    @FXML
    private void onIrInicio() {
        go("/vistas/Inicio.fxml");
    }

    @FXML
    private void onIrLogin() {
        go("/vistas/Login.fxml");
    }

    /* ======================
       Utilidades
       ====================== */

    private void go(String fxmlPath) {
        try {
            Stage stage = (Stage) btnRegistrarse.getScene().getWindow();
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
