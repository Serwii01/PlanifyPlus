package com.planify.planifyplus.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import javax.swing.text.html.ImageView;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField userLogin;          // <-- coincide con fx:id del FXML

    @FXML
    private PasswordField contrasenaLogin; // <-- coincide con fx:id del FXML

    @FXML
    private Button iniciarSesionBoton;     // <-- coincide con fx:id del FXML

    @FXML
    private Hyperlink botonNoTenerCuenta;   // <-- coincide con fx:id del FXML
    @FXML
    private ImageView homeCasa;   // <-- coincide con fx:id del FXML

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicialización opcional
    }

    @FXML
    private void onIniciarSesion(ActionEvent e) {
        System.out.println("Login con usuario = " + userLogin.getText());
    }
}
