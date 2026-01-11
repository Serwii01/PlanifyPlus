package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.UsuarioDAO;
import com.planify.planifyplus.dto.UsuarioDTO;
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

public class ConfPerfilController {

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private ComboBox<String> cmbCiudad;

    @FXML
    private Button btnGuardarCambios;

    @FXML
    private Button btnCancelar;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

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

    @FXML
    private void onGuardarCambios(ActionEvent event) {
        UsuarioDTO usuario = Sesion.getUsuarioActual();
        if (usuario == null) {
            mostrarAlerta("Error", "No hay ningún usuario en sesión.", Alert.AlertType.ERROR);
            return;
        }

        String nombre = txtNombre.getText().trim();
        String email = txtEmail.getText().trim();
        String contrasena = txtContrasena.getText();
        String ciudad = cmbCiudad.getValue();

        if (nombre.isEmpty() || email.isEmpty() || contrasena.isEmpty()) {
            mostrarAlerta("Campos obligatorios",
                    "Nombre, correo y contraseña son obligatorios.",
                    Alert.AlertType.WARNING);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            mostrarAlerta("Correo no válido",
                    "Introduce un correo electrónico válido.",
                    Alert.AlertType.WARNING);
            return;
        }

        UsuarioDTO otro = usuarioDAO.buscarPorEmail(email);
        if (otro != null && otro.getId() != usuario.getId()) {
            mostrarAlerta("Correo ya en uso",
                    "Ese correo electrónico ya está registrado por otro usuario.",
                    Alert.AlertType.WARNING);
            return;
        }

        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setContrasena(contrasena);
        usuario.setCiudad(ciudad);

        try {
            usuarioDAO.actualizar(usuario);
            Sesion.actualizarUsuarioActual(usuario);

            mostrarAlerta("Éxito",
                    "Los cambios se han guardado correctamente.",
                    Alert.AlertType.INFORMATION);

            irAPerfil(event);

        } catch (RuntimeException ex) {
            ex.printStackTrace();
            mostrarAlerta("Error",
                    "Se ha producido un error al guardar en la base de datos.",
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onCancelar(ActionEvent event) {
        irAPerfil(event);
    }

    @FXML
    private void onIrInicio(ActionEvent event) {
        cambiarEscena("/vistas/Inicio.fxml", event);
    }

    private void irAPerfil(ActionEvent event) {
        cambiarEscena("/vistas/Perfil.fxml", event);
    }

    //  Método genérico: cambia escena + FULL SCREEN REAL
    private void cambiarEscena(String fxml, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            Scene scene = new Scene(root);

            //  si usas CSS global
            try {
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            } catch (Exception ignored) {}

            stage.setScene(scene);

            //  Pantalla completa real (no solo maximized)
            stage.setFullScreenExitHint("");
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

            stage.show();
            Platform.runLater(() -> stage.setFullScreen(true));

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error",
                    "No se ha podido abrir la nueva pantalla.",
                    Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
