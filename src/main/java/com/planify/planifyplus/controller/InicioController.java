package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.ActividadDAO;
import com.planify.planifyplus.dao.InscripcionDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.util.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador principal de la pantalla de inicio de la aplicación.
 * Gestiona la visualización de actividades, filtros de búsqueda,
 * navegación y gestión de sesión de usuario.
 */
public class InicioController {

    @FXML private ImageView logoImage;
    @FXML private TextField searchBar;
    @FXML private ComboBox<String> cmbDistancia;
    @FXML private Button btnRegister, btnLogin, btnLogout, btnCrearActividad, btnPerfil;
    @FXML private Label lblCiudad, lblUser, lblNoSesion;
    @FXML private VBox contenedorComunidad, contenedorUsuario;
    @FXML private ScrollPane scrollActividadesComunidad, scrollActividadesUsuario;
    @FXML private Label lblAdminBadge;
    @FXML private Label lblTituloPanelDerecho;
    @FXML private Button btnLegal;
    @FXML private VBox overlayLegal;
    @FXML private HBox hboxSinSesion;
    @FXML private HBox hboxConSesion;

    private final ActividadDAO actividadDAO = new ActividadDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

    // Lista completa de actividades sin filtros
    private List<ActividadDTO> todasActividadesComunidad = new ArrayList<>();

    /**
     * Inicializa el controlador.
     * Configura listeners, carga datos iniciales y ajusta la interfaz según la sesión.
     */
    public void initialize() {
        var img = getClass().getResource("/img/descarga.png");
        if (img != null) logoImage.setImage(new Image(img.toExternalForm()));

        // Listener del buscador
        searchBar.textProperty().addListener((obs, oldV, newV) -> aplicarFiltros());

        // Configurar ComboBox de distancia
        configurarComboBoxDistancia();

        cargarActividadesComunidad();

        boolean loggedIn = Sesion.getUsuarioActual() != null;
        updateUIForSession(loggedIn);

        if (loggedIn) cargarPanelDerechoSegunRol();

        configurarUIRol();

        VBox.setVgrow(scrollActividadesComunidad, Priority.ALWAYS);
        VBox.setVgrow(scrollActividadesUsuario, Priority.ALWAYS);
    }

    /**
     * Configura el ComboBox para el filtrado por distancia.
     * Establece las opciones y el formato de las celdas.
     */
    private void configurarComboBoxDistancia() {
        List<String> opciones = List.of(
                "Todas las distancias",
                "Menos de 5 km",
                "Menos de 10 km",
                "Menos de 20 km",
                "Menos de 50 km"
        );
        cmbDistancia.setItems(FXCollections.observableArrayList(opciones));
        cmbDistancia.setValue("Todas las distancias");
        cmbDistancia.setOnAction(event -> aplicarFiltros());

        // ALTURA AUMENTADA A 42
        cmbDistancia.setPrefWidth(200);
        cmbDistancia.setMinWidth(200);
        cmbDistancia.setMaxWidth(200);
        cmbDistancia.setPrefHeight(42);
        cmbDistancia.setMinHeight(42);
        cmbDistancia.setMaxHeight(42);

        cmbDistancia.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    // Padding interno para centrar verticalmente
                    setStyle("-fx-text-fill: #1F2937; " +
                            "-fx-font-size: 13px; " +
                            "-fx-alignment: CENTER_LEFT; " +
                            "-fx-padding: 2 0 0 0;");
                }
            }
        });

        cmbDistancia.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #1F2937; -fx-font-size: 13px; -fx-padding: 8 12;");
                }
            }
        });
    }


    /**
     * Carga las actividades de la comunidad desde la base de datos.
     * Filtra las actividades propias del usuario y aplica los filtros de búsqueda y distancia.
     */
    private void cargarActividadesComunidad() {
        Long idUsuarioActual = (Sesion.getUsuarioActual() != null)
                ? Sesion.getUsuarioActual().getId() : null;

        // Construir lista completa en memoria
        todasActividadesComunidad = new ArrayList<>();

        List<ActividadDTO> predeterminadas = actividadDAO.obtenerPredeterminadas();
        todasActividadesComunidad.addAll(predeterminadas);

        List<ActividadDTO> creadasUsuarios = actividadDAO.obtenerNoPredeterminadas();
        for (ActividadDTO act : creadasUsuarios) {
            if (idUsuarioActual != null && act.getCreador() != null
                    && act.getCreador().getId() == idUsuarioActual) {
                continue; // las suyas van al panel derecho
            }
            todasActividadesComunidad.add(act);
        }

        // Ordenar por fecha
        todasActividadesComunidad.sort(Comparator.comparing(ActividadDTO::getFechaHoraInicio));

        // Aplicar filtros (búsqueda + distancia)
        aplicarFiltros();
    }

    /**
     * Este metodo es el que hace que funcionen los filtros de la pantalla principal.
     * Básicamente coge todas las actividades que tenemos cargadas y va mirando una a una
     * si cumple con lo que ha puesto el usuario en el buscador o con la distancia seleccionada.
     * <p>
     * Pasos que sigue:
     * 1. Limpia la pantalla para no duplicar cosas.
     * 2. Mira si el texto de la actividad coincide con lo que se busca.
     * 3. Si hay un usuario conectado, calcula la distancia para ver si está cerca.
     * 4. Al final pinta solo las tarjetas que han pasado todas las pruebas.
     */
    private void aplicarFiltros() {
        // Primero borramos todo lo que hay en el contenedor para empezar de cero
        contenedorComunidad.getChildren().clear();
        
        // Si no hay actividades cargadas, no hacemos nada y salimos
        if (todasActividadesComunidad == null || todasActividadesComunidad.isEmpty()) return;

        // FILTRO 1: Búsqueda por texto
        // Cogemos lo que ha escrito el usuario, quitamos espacios y lo pasamos a minúsculas
        String query = searchBar.getText();
        if (query == null) query = "";
        String q = query.toLowerCase().trim();

        // Usamos un stream para filtrar la lista
        List<ActividadDTO> actividadesFiltradas = todasActividadesComunidad.stream()
                .filter(act -> {
                    // Si no ha escrito nada, esta actividad vale
                    if (q.isEmpty()) return true;
                    
                    // Comprobamos si el texto buscado está en el título, descripción o ciudad
                    return act.getTitulo().toLowerCase().contains(q)
                            || act.getDescripcion().toLowerCase().contains(q)
                            || (act.getCiudad() != null && act.getCiudad().toLowerCase().contains(q))
                            || (act.getUbicacion() != null && act.getUbicacion().toLowerCase().contains(q))
                            || (act.getTipo() != null && act.getTipo().toString().toLowerCase().contains(q))    ;
                })
                .collect(Collectors.toList());

        // FILTRO 2: Distancia (esto solo funciona si el usuario ha iniciado sesión)
        String filtroDistancia = cmbDistancia.getValue();
        if (Sesion.getUsuarioActual() != null
                && filtroDistancia != null
                && !filtroDistancia.equals("Todas las distancias")) {

            // Cogemos las coordenadas del usuario actual
            double latUsuario = Sesion.getUsuarioActual().getLatitud();
            double lonUsuario = Sesion.getUsuarioActual().getLongitud();
            
            // Vemos cuántos km máximo quiere buscar
            double distanciaMaxima = extraerDistanciaMaxima(filtroDistancia);

            // Volvemos a filtrar la lista que ya teníamos filtrada por texto
            actividadesFiltradas = actividadesFiltradas.stream()
                    .filter(act -> {
                        // Si la actividad no tiene coordenadas (como las predeterminadas), la mostramos igual
                        if (act.getLatitud() == null || act.getLongitud() == null) {
                            return true;
                        }

                        double latActividad = act.getLatitud().doubleValue();
                        double lonActividad = act.getLongitud().doubleValue();

                        // Calculamos la distancia real usando la fórmula
                        double distancia = DistanciaUtil.calcularDistancia(
                                latUsuario, lonUsuario, latActividad, lonActividad
                        );
                        
                        // Si está dentro del radio, nos la quedamos
                        return distancia <= distanciaMaxima;
                    })
                    .collect(Collectors.toList());
        }

        // Por último, recorremos la lista final y creamos una tarjeta para cada actividad
        for (ActividadDTO act : actividadesFiltradas) {
            contenedorComunidad.getChildren().add(crearCardActividad(act));
        }
    }

    /**
     * Convierte la selección del filtro de distancia en un valor numérico.
     *
     * @param filtro Texto seleccionado en el ComboBox.
     * @return Distancia máxima en kilómetros.
     */
    private double extraerDistanciaMaxima(String filtro) {
        switch (filtro) {
            case "Menos de 5 km": return 5.0;
            case "Menos de 10 km": return 10.0;
            case "Menos de 20 km": return 20.0;
            case "Menos de 50 km": return 50.0;
            default: return Double.MAX_VALUE;
        }
    }

    /**
     * Carga y muestra las actividades creadas por el usuario actual en el panel derecho.
     */
    private void cargarActividadesUsuario() {
        contenedorUsuario.getChildren().clear();
        if (Sesion.getUsuarioActual() == null) return;

        long idUsuario = Sesion.getUsuarioActual().getId();
        List<ActividadDTO> actividades = actividadDAO.obtenerCreadasPorUsuario(idUsuario);
        for (ActividadDTO act : actividades) {
            contenedorUsuario.getChildren().add(crearCardActividad(act));
        }
    }

    /**
     * Carga y muestra las actividades denunciadas en el panel derecho (solo para administradores).
     */
    private void cargarActividadesDenunciadas() {
        contenedorUsuario.getChildren().clear();
        List<ActividadDTO> denunciadas = actividadDAO.obtenerDenunciadasOrdenadas();

        if (denunciadas.isEmpty()) {
            Label lbl = new Label("No hay actividades denunciadas.");
            lbl.setStyle("-fx-text-fill: #6B7280; -fx-padding: 12;");
            contenedorUsuario.getChildren().add(lbl);
            return;
        }

        for (ActividadDTO act : denunciadas) {
            contenedorUsuario.getChildren().add(crearCardActividad(act));
        }
    }

    /**
     * Configura el contenido del panel derecho basándose en el rol del usuario (Admin o Usuario normal).
     */
    private void cargarPanelDerechoSegunRol() {
        if (!Sesion.haySesion()) {
            contenedorUsuario.getChildren().clear();
            return;
        }
        if (Sesion.esAdmin()) cargarActividadesDenunciadas();
        else cargarActividadesUsuario();
    }

    /**
     * Crea un componente visual (tarjeta) para representar una actividad.
     *
     * @param act DTO de la actividad.
     * @return Panel (Pane) que contiene la información visual de la actividad.
     */
    private Pane crearCardActividad(ActividadDTO act) {
        VBox vbox = new VBox(12);
        vbox.setStyle(
                "-fx-padding: 18 18 18 18;" +
                        "-fx-background-color: #FFFFFF;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-color: #ececec;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);"
        );

        HBox hTituloTipo = new HBox(8);
        Label lblTitulo = new Label(act.getTitulo());
        lblTitulo.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        lblTitulo.setWrapText(true);
        Label lblTipo = new Label(capitalize(act.getTipo().toString().toLowerCase()));
        lblTipo.setStyle(
                "-fx-background-color: " + getTipoColor(act.getTipo().toString()) + ";" +
                        "-fx-font-size: 13;" +
                        "-fx-padding: 4 14 4 14;" +
                        "-fx-background-radius: 11;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #226;"
        );
        HBox.setHgrow(lblTitulo, Priority.ALWAYS);
        hTituloTipo.getChildren().addAll(lblTitulo, lblTipo);

        Label lblDesc = new Label(act.getDescripcion());
        lblDesc.setStyle("-fx-text-fill: #4B4B4B; -fx-font-size: 14;");

        Label lblCiudadAct = new Label("📍 " + act.getCiudad() +
                (act.getUbicacion() != null && !act.getUbicacion().isEmpty()
                        ? " · " + act.getUbicacion()
                        : ""));
        lblCiudadAct.setStyle("-fx-text-fill: #1663e3; -fx-font-size: 15;");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String fechaFormateada = act.getFechaHoraInicio().format(formatter);
        long inscritos = inscripcionDAO.contarInscritos(act.getId());

        Label lblFecha = new Label(fechaFormateada);
        lblFecha.setStyle("-fx-font-size: 15; -fx-text-fill: #222;");
        Label lblAforo = new Label(inscritos + " / " + act.getAforo() + " inscritos");

        HBox hBoton = new HBox(8);
        hBoton.setAlignment(Pos.CENTER_RIGHT);

        boolean esCreadorUsuario = Sesion.getUsuarioActual() != null &&
                act.getCreador() != null &&
                act.getCreador().getId() == Sesion.getUsuarioActual().getId();

        boolean esAdmin = Sesion.esAdmin();

        if (esCreadorUsuario) {

            Button btnEditar = new Button("Editar");
            btnEditar.setStyle(
                    "-fx-background-color: #3B82F6;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 18;" +
                            "-fx-font-size: 14;" +
                            "-fx-padding: 6 18 6 18;" +
                            "-fx-cursor: hand;"
            );
            btnEditar.setOnAction(e -> {
                e.consume();
                onEditarActividad(act);
            });
            btnEditar.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

            Button btnEliminar = new Button("Eliminar");
            btnEliminar.setStyle(
                    "-fx-background-color: #DC2626;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 18;" +
                            "-fx-font-size: 14;" +
                            "-fx-padding: 6 18 6 18;" +
                            "-fx-cursor: hand;"
            );
            btnEliminar.setOnAction(e -> {
                e.consume();
                onEliminarActividadUsuario(act);
            });
            btnEliminar.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

            hBoton.getChildren().addAll(btnEditar, btnEliminar);

            HBox.setHgrow(lblFecha, Priority.ALWAYS);
            HBox hFechaAforo = new HBox(10);
            hFechaAforo.getChildren().addAll(lblFecha, lblAforo);

            vbox.getChildren().addAll(hTituloTipo, lblDesc, lblCiudadAct, hFechaAforo, hBoton);

        } else if (esAdmin) {

            Button btnEliminarAdmin = new Button("Eliminar");
            btnEliminarAdmin.setStyle(
                    "-fx-background-color: #DC2626;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 18;" +
                            "-fx-font-size: 14;" +
                            "-fx-padding: 6 18 6 18;" +
                            "-fx-cursor: hand;"
            );
            btnEliminarAdmin.setOnAction(e -> {
                e.consume();
                onEliminarActividad(act);
            });
            btnEliminarAdmin.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

            hBoton.getChildren().add(btnEliminarAdmin);

            if (act.getNumDenuncias() > 0) {
                Label lblDenuncias = new Label(act.getNumDenuncias() + " denuncia" +
                        (act.getNumDenuncias() != 1 ? "s" : ""));
                lblDenuncias.setStyle(
                        "-fx-text-fill: #DC2626;" +
                                "-fx-font-size: 15;" +
                                "-fx-font-weight: bold;"
                );

                HBox.setHgrow(lblFecha, Priority.ALWAYS);
                HBox hFechaAforoDenuncias = new HBox(10);
                hFechaAforoDenuncias.getChildren().addAll(lblFecha, lblAforo, lblDenuncias);

                vbox.getChildren().addAll(hTituloTipo, lblDesc, lblCiudadAct, hFechaAforoDenuncias, hBoton);
            } else {
                HBox.setHgrow(lblFecha, Priority.ALWAYS);
                HBox hFechaAforo = new HBox(10);
                hFechaAforo.getChildren().addAll(lblFecha, lblAforo);

                vbox.getChildren().addAll(hTituloTipo, lblDesc, lblCiudadAct, hFechaAforo, hBoton);
            }

        } else {

            HBox.setHgrow(lblFecha, Priority.ALWAYS);
            HBox hFechaAforo = new HBox(10);
            hFechaAforo.getChildren().addAll(lblFecha, lblAforo);

            Button btnInscribir = new Button("Inscribirse");
            btnInscribir.setStyle(
                    "-fx-background-color: #3B82F6;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-radius: 18;" +
                            "-fx-font-size: 15;" +
                            "-fx-padding: 6 22 6 22;"
            );
            btnInscribir.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

            if (!Sesion.haySesion()) {
                btnInscribir.setText("Inicia sesión");
                btnInscribir.setDisable(false);
                btnInscribir.setOnAction(e -> {
                    e.consume();
                    irAVista("Login.fxml");
                });
            } else {
                boolean inscrito = inscripcionDAO.estaInscrito(Sesion.getIdUsuario(), act.getId());
                btnInscribir.setText(inscrito ? "Inscrito" : "Inscribirse");

                boolean lleno = inscritos >= act.getAforo();
                btnInscribir.setDisable(lleno && !inscrito);

                btnInscribir.setOnAction(e -> {
                    e.consume();
                    long userId = Sesion.getIdUsuario();
                    boolean ya = inscripcionDAO.estaInscrito(userId, act.getId());

                    if (ya) inscripcionDAO.cancelar(userId, act.getId());
                    else {
                        long ahora = inscripcionDAO.contarInscritos(act.getId());
                        if (ahora >= act.getAforo()) return;
                        inscripcionDAO.inscribir(Sesion.getUsuarioActual(), act);
                    }

                    cargarActividadesComunidad();
                    cargarPanelDerechoSegunRol();
                });
            }

            hBoton.getChildren().add(btnInscribir);
            vbox.getChildren().addAll(hTituloTipo, lblDesc, lblCiudadAct, hFechaAforo, hBoton);
        }

        vbox.setOnMouseClicked(e -> abrirDetalleActividad(act));

        return vbox;
    }

    /**
     * Abre la vista de edición para una actividad específica.
     *
     * @param actividad La actividad a editar.
     */
    private void onEditarActividad(ActividadDTO actividad) {
        try {
            var loader = ViewUtil.loaderFXML(getClass(), "/vistas/crearActividad.fxml");
            Parent root = loader.load();

            CrearActividadController controller = loader.getController();
            controller.setActividadParaEditar(actividad);

            Stage stage = (Stage) logoImage.getScene().getWindow();
            Scene scene = new Scene(root);

            var css = getClass().getResource("/css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            WindowUtil.forceMaximize(stage);

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarError("No se pudo abrir la pantalla de edición");
        }
    }

    /**
     * Maneja la eliminación de una actividad por parte del usuario creador.
     * Solicita confirmación antes de eliminar.
     *
     * @param act La actividad a eliminar.
     */
    private void onEliminarActividadUsuario(ActividadDTO act) {
        boolean confirmar = AlertUtil.confirm(
                "Eliminar actividad",
                "Esta acción no se puede deshacer.\n\nLa actividad \"" + act.getTitulo() + "\" será eliminada."
        );

        if (!confirmar) return;

        try {
            actividadDAO.eliminarPorId(act.getId());
            cargarActividadesComunidad();
            cargarPanelDerechoSegunRol();

            AlertUtil.info(
                    "Actividad eliminada",
                    "La actividad \"" + act.getTitulo() + "\" ha sido eliminada correctamente."
            );
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo eliminar la actividad.");
        }
    }

    /**
     * Abre la vista de detalle de una actividad.
     *
     * @param actividad La actividad a visualizar.
     */
    private void abrirDetalleActividad(ActividadDTO actividad) {
        try {
            var loader = ViewUtil.loaderFXML(getClass(), "/vistas/Actividad.fxml");
            Parent root = loader.load();

            ActividadController controller = loader.getController();
            controller.setActividad(actividad);

            Stage stage = (Stage) logoImage.getScene().getWindow();
            Scene scene = new Scene(root);

            var css = getClass().getResource("/css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            WindowUtil.forceMaximize(stage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Capitaliza la primera letra de una cadena.
     *
     * @param str Cadena de entrada.
     * @return Cadena con la primera letra en mayúscula.
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Obtiene el color de fondo asociado a un tipo de actividad.
     *
     * @param tipo Tipo de actividad (String).
     * @return Código de color hexadecimal.
     */
    private String getTipoColor(String tipo) {
        return tipo.equals("DEPORTIVA") ? "#dbeafe"
                : tipo.equals("CULTURAL") ? "#e9d5ff"
                : "#bbf7d0";
    }

    /**
     * Maneja el evento de clic en el botón de registro.
     * Redirige a la vista de registro.
     */
    @FXML private void handleRegister() { irAVista("Registro.fxml"); }

    /**
     * Maneja el evento de clic en el botón de inicio de sesión.
     * Redirige a la vista de login.
     */
    @FXML private void handleLogin() { irAVista("Login.fxml"); }

    /**
     * Maneja el evento de clic en el botón de crear actividad.
     * Redirige a la vista de creación de actividad.
     */
    @FXML private void handleCrearActividad() { irAVista("crearActividad.fxml"); }

    /**
     * Maneja el evento de clic en el botón de perfil.
     * Redirige a la vista de perfil de usuario.
     */
    @FXML private void handlePerfil() { irAVista("Perfil.fxml"); }

    /**
     * Maneja el evento de cierre de sesión.
     * Limpia la sesión actual y actualiza la interfaz.
     */
    @FXML
    private void handleLogout() {
        Sesion.cerrarSesion();
        updateUIForSession(false);
        configurarUIRol();
        cargarActividadesComunidad();
        contenedorUsuario.getChildren().clear();
    }

    /**
     * Navega a una vista FXML específica.
     *
     * @param fxml Nombre del archivo FXML (sin ruta, solo nombre).
     */
    private void irAVista(String fxml) {
        try {
            Stage stage = (Stage) logoImage.getScene().getWindow();
            Parent root = ViewUtil.loadFXML(getClass(), "/vistas/" + fxml);

            Scene scene = new Scene(root);
            var css = getClass().getResource("/css/styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            WindowUtil.forceMaximize(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Actualiza la visibilidad de los elementos de la interfaz según el estado de la sesión.
     *
     * @param loggedIn true si el usuario ha iniciado sesión, false en caso contrario.
     */
    public void updateUIForSession(boolean loggedIn) {
        btnRegister.setVisible(!loggedIn);
        btnLogin.setVisible(!loggedIn);
        btnPerfil.setVisible(loggedIn);
        btnPerfil.setText("Mi Perfil");
        btnLogout.setVisible(loggedIn);
        btnLogout.setText("Cerrar sesión");
        btnCrearActividad.setVisible(loggedIn);
        btnCrearActividad.setText("Crear Actividad");
        lblCiudad.setVisible(loggedIn);
        lblUser.setVisible(loggedIn);
        scrollActividadesUsuario.setVisible(loggedIn);
        lblNoSesion.setVisible(!loggedIn);
        hboxSinSesion.setVisible(!loggedIn);
        hboxSinSesion.setManaged(!loggedIn);

        hboxConSesion.setVisible(loggedIn);
        hboxConSesion.setManaged(loggedIn);

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

        cargarPanelDerechoSegunRol();
    }

    /**
     * Callback ejecutado cuando un usuario inicia sesión exitosamente.
     * Refresca toda la interfaz y carga los datos correspondientes.
     */
    public void onUsuarioLogueado() {
        updateUIForSession(true);
        configurarUIRol();
        cargarActividadesComunidad();
        cargarPanelDerechoSegunRol();
    }

    /**
     * Configura elementos específicos de la interfaz según el rol del usuario (Admin vs Usuario).
     * Oculta o muestra controles exclusivos de administración.
     */
    private void configurarUIRol() {
        //verifica que el usuario sea administrador mediante la clase sesion
        boolean esAdmin = Sesion.esAdmin();

        //muestra el icono de administrador
        if (lblAdminBadge != null) {
            lblAdminBadge.setVisible(esAdmin);
            lblAdminBadge.setManaged(esAdmin);
        }
        //cambia el titulo del panel derecho de mis actividades creadas a actividades denunciadas si es admin
        if (lblTituloPanelDerecho != null) {
            lblTituloPanelDerecho.setText(esAdmin ? "Actividades denunciadas" : "Mis Actividades Creadas");
        }

        //muestra el boton de crear actividades
        if (btnCrearActividad != null) {
            boolean mostrar = Sesion.haySesion() && !esAdmin;
            btnCrearActividad.setVisible(mostrar);
            btnCrearActividad.setManaged(mostrar);
        }


        if (cmbDistancia != null) {
            cmbDistancia.setVisible(!esAdmin);
            cmbDistancia.setManaged(!esAdmin);
        }

        if (lblCiudad != null) {
            if (esAdmin) {
                lblCiudad.setVisible(false);
                lblCiudad.setManaged(false);
            } else {
                lblCiudad.setManaged(true);
            }
        }

        if (btnPerfil != null) {
            boolean mostrarPerfil = Sesion.haySesion() && !esAdmin;
            btnPerfil.setVisible(mostrarPerfil);
            btnPerfil.setManaged(mostrarPerfil);
        }
    }

    /**
     * Maneja la eliminación de una actividad por parte de un administrador.
     *
     * @param act La actividad a eliminar.
     */
    private void onEliminarActividad(ActividadDTO act) {
        if (!Sesion.esAdmin()) return;

        boolean confirmar = AlertUtil.confirm(
                "Eliminar actividad",
                "Esta acción no se puede deshacer.\n\nLa actividad \"" + act.getTitulo() + "\" será eliminada."
        );
        //si se cancela la alerta aborta el proceso
        if (!confirmar) return;


        try {
            actividadDAO.eliminarPorId(act.getId());
            cargarActividadesComunidad();
            cargarPanelDerechoSegunRol();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo eliminar la actividad.");
        }
    }

    /**
     * Muestra una ventana modal con el aviso legal y términos de uso.
     */
    @FXML
    private void mostrarLegal() {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initOwner(btnLegal.getScene().getWindow());
        modalStage.setTitle("Aviso Legal");

        VBox contenido = new VBox(20);
        contenido.setStyle(
                "-fx-background-color: white;" +
                        "-fx-padding: 40;" +
                        "-fx-alignment: center;"
        );
        contenido.setPrefWidth(600);
        contenido.setPrefHeight(500);

        Label titulo = new Label("Aviso Legal - PlanifyPlus");
        titulo.setStyle(
                "-fx-font-size: 22;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #1563e2;" +
                        "-fx-padding: 0 0 20 0;"
        );

        ScrollPane scrollLegal = new ScrollPane();
        scrollLegal.setFitToWidth(true);
        scrollLegal.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollLegal, Priority.ALWAYS);

        VBox textos = new VBox(15);
        textos.setStyle("-fx-padding: 20;");

        Label texto1 = new Label("PlanifyPlus es una plataforma para organizar actividades comunitarias.");
        texto1.setWrapText(true);
        texto1.setStyle("-fx-font-size: 15; -fx-text-fill: #333;");

        Label texto2 = new Label("Condiciones de uso:\n" +
                "• Solo mayores de 18 años\n" +
                "• Respeta las normas de convivencia\n" +
                "• Las actividades son responsabilidad del organizador\n" +
                "• Prohibido contenido ilegal o spam");
        texto2.setWrapText(true);
        texto2.setStyle("-fx-font-size: 15; -fx-text-fill: #333;");

        Label texto3 = new Label("Datos personales: Cumplimos RGPD. Consulta Política de Privacidad.");
        texto3.setWrapText(true);
        texto3.setStyle("-fx-font-size: 15; -fx-text-fill: #333;");

        Label texto4 = new Label("Versión 1.0 - © 2026 PlanifyPlus. Todos los derechos reservados.");
        texto4.setWrapText(true);
        texto4.setStyle("-fx-font-size: 13; -fx-text-fill: #6B7280;");

        textos.getChildren().addAll(texto1, texto2, texto3, texto4);
        scrollLegal.setContent(textos);

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setStyle(
                "-fx-background-color: #3B82F6;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-size: 15;" +
                        "-fx-padding: 12 30 12 30;"
        );
        btnCerrar.setOnAction(e -> modalStage.close());

        HBox botones = new HBox(btnCerrar);
        botones.setAlignment(Pos.CENTER);

        contenido.getChildren().addAll(titulo, scrollLegal, botones);

        Scene scene = new Scene(contenido);
        modalStage.setScene(scene);
        modalStage.setResizable(false);
        modalStage.showAndWait();
    }

    /**
     * Muestra una alerta de error con el mensaje especificado.
     *
     * @param mensaje Mensaje de error a mostrar.
     */
    private void mostrarError(String mensaje) {
        AlertUtil.error("Error", mensaje);
    }
}
