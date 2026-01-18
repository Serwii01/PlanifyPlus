// src/main/java/com/planify/planifyplus/controller/RegistroController.java
package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.UsuarioDAO;
import com.planify.planifyplus.dto.UsuarioDTO;
import com.planify.planifyplus.util.AlertUtil;
import com.planify.planifyplus.util.ViewUtil;
import com.planify.planifyplus.util.WindowUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Controlador de la pantalla de registro.
 */
public class RegistroController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtContrasena;
    @FXML private ComboBox<String> cmbCiudad;
    @FXML private Button btnRegistrarse;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /** Patrón simple para validar emails. */
    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    /**
     * Inicializa el formulario.
     */
    @FXML
    public void initialize() {
        cmbCiudad.setItems(FXCollections.observableArrayList(
                List.of("Sevilla", "Madrid", "Barcelona", "Valencia", "Málaga", "Bilbao")
        ));
    }

    /**
     * Vuelve a la pantalla de inicio.
     */
    @FXML
    private void onIrInicio() {
        go("/vistas/Inicio.fxml");
    }

    /**
     * Abre la pantalla de login.
     */
    @FXML
    private void onIrLogin() {
        go("/vistas/Login.fxml");
    }

    /**
     * Valida el formulario y crea el usuario.
     */
    @FXML
    private void onRegistrarse() {
        String nombre  = txtNombre.getText().trim();
        String email   = txtEmail.getText().trim();
        String pass    = txtContrasena.getText();
        String ciudad  = cmbCiudad.getValue();

        if (nombre.isEmpty()) {
            AlertUtil.error("Campo obligatorio", "El nombre es obligatorio.");
            return;
        }

        if (!EMAIL_REGEX.matcher(email).matches()) {
            AlertUtil.error("Correo no válido", "Introduce un correo electrónico válido.");
            return;
        }

        if (pass.length() < 6) {
            AlertUtil.error("Contraseña no válida", "La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        try {
            if (usuarioDAO.existeEmail(email)) {
                AlertUtil.error("Correo ya registrado", "Ese correo ya está registrado.");
                return;
            }

            UsuarioDTO nuevo = new UsuarioDTO();
            nuevo.setNombre(nombre);
            nuevo.setEmail(email);
            nuevo.setContrasena(pass);
            nuevo.setCiudad(ciudad);
            nuevo.setEsAdmin(false);
            nuevo.setCreadoEn(LocalDateTime.now());

            usuarioDAO.crear(nuevo);

            AlertUtil.info("Registro completado", "Cuenta creada correctamente.");
            go("/vistas/Login.fxml");

        } catch (Exception e) {
            AlertUtil.error(
                    "Error en el registro",
                    "No se pudo registrar el usuario.\n\nDetalle: " + e.getMessage()
            );
        }
    }

    /**
     * Cambia la escena al FXML indicado.
     *
     * @param fxmlPath ruta del FXML
     */
    private void go(String fxmlPath) {
        try {
            Stage stage = (Stage) btnRegistrarse.getScene().getWindow();
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
