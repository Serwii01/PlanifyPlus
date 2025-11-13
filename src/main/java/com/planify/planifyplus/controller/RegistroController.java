package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.UsuarioDAO;
import com.planify.planifyplus.dto.UsuarioDTO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

public class RegistroController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtContrasena;
    @FXML private ComboBox<String> cmbCiudad;
    @FXML private Button btnRegistrarse;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // Regex sencilla para email
    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    @FXML
    public void initialize() {
        // Lista de ciudades de ejemplo (ajustad a las vuestras si queréis)
        cmbCiudad.setItems(FXCollections.observableArrayList(
                List.of("Sevilla", "Madrid", "Barcelona", "Valencia", "Málaga", "Bilbao")
        ));
    }

    // Navegación
    @FXML
    private void onIrInicio() { go("/vistas/Inicio.fxml"); }

    @FXML
    private void onIrLogin() { go("/vistas/Login.fxml"); }

    // Registro
    @FXML
    private void onRegistrarse() {
        String nombre  = txtNombre.getText().trim();
        String email   = txtEmail.getText().trim();
        String pass    = txtContrasena.getText();
        String ciudad  = cmbCiudad.getValue(); // opcional

        // Validaciones mínimas de formulario
        if (nombre.isEmpty()) { error("El nombre es obligatorio."); return; }
        if (!EMAIL_REGEX.matcher(email).matches()) { error("Correo electrónico no válido."); return; }
        if (pass.length() < 6) { error("La contraseña debe tener al menos 6 caracteres."); return; }

        // Reglas de negocio para registro
        try {
            if (usuarioDAO.existeEmail(email)) {
                error("Ese correo ya está registrado.");
                return;
            }

            UsuarioDTO nuevo = new UsuarioDTO();
            nuevo.setNombre(nombre);
            nuevo.setEmail(email);
            nuevo.setContrasena(pass);              // (si activáis BCrypt, aquí poned el hash)
            nuevo.setCiudad(ciudad);
            nuevo.setEsAdmin(false);
            nuevo.setCreadoEn(LocalDateTime.now());

            usuarioDAO.crear(nuevo);

            info("Cuenta creada correctamente.");
            go("/vistas/Login.fxml");               // o /vistas/Inicio.fxml si preferís

        } catch (Exception e) {
            error("No se pudo registrar el usuario.\nDetalle: " + e.getMessage());
        }
    }

    // Helpers UI
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
