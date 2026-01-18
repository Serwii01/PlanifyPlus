package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.UsuarioDAO;
import com.planify.planifyplus.dto.UsuarioDTO;
import com.planify.planifyplus.util.AlertUtil;
import com.planify.planifyplus.util.Sesion;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Controlador de configuración/edición del perfil del usuario.
 */
public class ConfPerfilController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtContrasena;
    @FXML private ComboBox<String> cmbCiudad;
    @FXML private Button btnGuardarCambios;
    @FXML private Button btnCancelar;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Inicializa la vista con los datos del usuario en sesión.
     */
    @FXML
    private void initialize() {
        cmbCiudad.getItems().setAll(
                "Madrid",
                "Barcelona",
                "Valencia",
                "Sevilla",
                "Bilbao",
                "Palma de Mallorca"
        );

        UsuarioDTO u = Sesion.getUsuarioActual();
        if (u != null) {
            txtNombre.setText(u.getNombre());
            txtEmail.setText(u.getEmail());
            txtContrasena.setText(u.getContrasena());
            if (u.getCiudad() != null && !u.getCiudad().isBlank()) {
                cmbCiudad.setValue(u.getCiudad());
            }
        }
    }

    /**
     * Guarda los cambios del perfil tras validar los campos.
     *
     * @param event evento del botón
     */
    @FXML
    private void onGuardarCambios(ActionEvent event) {
        UsuarioDTO usuario = Sesion.getUsuarioActual();
        if (usuario == null) {
            AlertUtil.error("Error", "No hay ningún usuario en sesión.");
            return;
        }

        String nombre = txtNombre.getText().trim();
        String email = txtEmail.getText().trim();
        String contrasena = txtContrasena.getText();
        String ciudad = cmbCiudad.getValue();

        if (nombre.isEmpty() || email.isEmpty() || contrasena.isEmpty() || ciudad == null || ciudad.isEmpty()) {
            AlertUtil.error("Campos obligatorios",
                    "Nombre, correo, contraseña y ciudad son obligatorios.");
            return;
        }

        if (nombre.length() < 2) {
            AlertUtil.error("Nombre inválido",
                    "El nombre debe tener al menos 2 caracteres.");
            return;
        }

        if (contrasena.length() < 6) {
            AlertUtil.error("Contraseña inválida",
                    "La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
        if (!emailPattern.matcher(email).matches()) {
            AlertUtil.error("Correo no válido",
                    "Introduce un correo electrónico válido (ej: usuario@dominio.com).");
            return;
        }

        UsuarioDTO otro = usuarioDAO.buscarPorEmail(email);
        if (otro != null && otro.getId() != usuario.getId()) {
            AlertUtil.error("Correo ya en uso",
                    "Ese correo electrónico ya está registrado por otro usuario.");
            return;
        }

        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setContrasena(contrasena);
        usuario.setCiudad(ciudad);

        try {
            usuarioDAO.actualizar(usuario);
            Sesion.actualizarUsuarioActual(usuario);

            AlertUtil.info("Éxito",
                    "Los cambios se han guardado correctamente.");

            irAPerfil(event);

        } catch (RuntimeException ex) {
            ex.printStackTrace();
            AlertUtil.error("Error",
                    "Se ha producido un error al guardar en la base de datos.");
        }
    }

    /**
     * Cancela la edición y vuelve al perfil.
     *
     * @param event evento del botón
     */
    @FXML
    private void onCancelar(ActionEvent event) {
        irAPerfil(event);
    }

    /**
     * Navega al inicio.
     *
     * @param event evento del botón
     */
    @FXML
    private void onIrInicio(ActionEvent event) {
        cambiarEscena("/vistas/Inicio.fxml", event);
    }

    /**
     * Abre la vista de perfil.
     *
     * @param event evento del botón
     */
    private void irAPerfil(ActionEvent event) {
        cambiarEscena("/vistas/Perfil.fxml", event);
    }

    /**
     * Cambia la escena actual al FXML indicado.
     *
     * @param fxml  ruta del FXML
     * @param event evento que origina el cambio
     */
    private void cambiarEscena(String fxml, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            Scene scene = new Scene(root);

            try {
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            } catch (Exception ignored) {}

            stage.setScene(scene);

            stage.setFullScreenExitHint("");
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

            stage.show();
            Platform.runLater(() -> stage.setFullScreen(true));

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.error("Error",
                    "No se ha podido abrir la nueva pantalla.");
        }
    }
}
