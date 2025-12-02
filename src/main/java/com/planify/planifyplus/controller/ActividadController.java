package com.planify.planifyplus.controller;

import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.util.Sesion;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;

public class ActividadController {

    @FXML private WebView webViewMapa;

    @FXML private Label lblTipo;
    @FXML private Label lblTitulo;
    @FXML private Label lblDescripcion;

    @FXML private Label lblFecha;
    @FXML private Label lblHora;
    @FXML private Label lblUbicacionCaja;
    @FXML private Label lblCiudadCaja;


    @FXML private Label lblPlazas;
    @FXML private Label lblDebeIniciarSesion;

    @FXML private Button btnInscribirse;
    @FXML private Button btnDenunciar;
    @FXML private Button btnVolver;

    private ActividadDTO actividad;
    private WebEngine webEngine;

    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy");
    private final DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        //para la API
        webEngine = webViewMapa.getEngine();

        // hmtl de la API
        URL url = getClass().getResource("/API/map-crear-actividad.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
        }
        //para volver al inicio
        btnVolver.setOnAction(e -> volverAInicio());
        configurarInscripcionSegunSesion();
    }

    public void setActividad(ActividadDTO actividad) {
        this.actividad = actividad;
        if (actividad == null) return;

        lblTitulo.setText(actividad.getTitulo());
        lblDescripcion.setText(actividad.getDescripcion());

        // Tipo con color similar a Inicio
        String tipoStr = actividad.getTipo().toString();
        lblTipo.setText(
                tipoStr.substring(0, 1).toUpperCase() + tipoStr.substring(1).toLowerCase()
        );

        // Fecha y hora
        lblFecha.setText(actividad.getFechaHoraInicio().format(formatoFecha));
        lblHora.setText(actividad.getFechaHoraInicio().format(formatoHora));

        // Ubicación / ciudad
        String ubicacion = actividad.getUbicacion() != null ? actividad.getUbicacion() : "";
        String ciudad = actividad.getCiudad() != null ? actividad.getCiudad() : "";

        lblUbicacionCaja.setText(ubicacion);
        lblCiudadCaja.setText(ciudad);

        // Plazas (de momento 1 inscrito fijo como en el mockup)
        lblPlazas.setText("1 / " + actividad.getAforo() + " personas inscritas");

        // Cuando el HTML del mapa haya cargado, luego podrás pasarle lat/lng
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED && actividad.getLatitud() != null) {
                double lat = actividad.getLatitud().doubleValue();
                double lng = actividad.getLongitud().doubleValue();
                String label = actividad.getUbicacion() != null ? actividad.getUbicacion() : "Aquí";

            }
        });
    }

    private void configurarInscripcionSegunSesion() {
        //detecta si el user esta logeado
        boolean loggedIn = Sesion.getUsuarioActual() != null;

        //visibilidad de los botones dependiendo de la sesion
        btnInscribirse.setDisable(!loggedIn);
        lblDebeIniciarSesion.setVisible(!loggedIn);

        if (loggedIn) {
            lblDebeIniciarSesion.setManaged(false);
        }

        //simple estetica, sin terminar de implementar
        btnInscribirse.setOnAction(e -> {
            if (!loggedIn) return;
            btnInscribirse.setText("Inscrito");
            btnInscribirse.setDisable(true);
        });
    }
    //metodo para volver a inicio
    private void volverAInicio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/inicio.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) webViewMapa.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
