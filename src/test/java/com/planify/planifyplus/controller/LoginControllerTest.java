package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.UsuarioDAO;
import com.planify.planifyplus.dto.UsuarioDTO;
import com.planify.planifyplus.util.AlertUtil;
import com.planify.planifyplus.util.Sesion;
import com.planify.planifyplus.util.ViewUtil;
import com.planify.planifyplus.util.WindowUtil;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;

import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoginControllerTest {

    @BeforeAll
    static void initJavaFx() throws Exception {
        // Arranca JavaFX toolkit para tests (fiable en IntelliJ/Windows)
        new JFXPanel();
        runFxAndWait(() -> {});
    }

    @AfterEach
    void cleanup() {
        Sesion.cerrarSesion();
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static void runFxAndWait(ThrowingRunnable r) throws Exception {
        if (Platform.isFxApplicationThread()) {
            r.run();
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        final Exception[] ex = new Exception[1];

        Platform.runLater(() -> {
            try {
                r.run();
            } catch (Exception e) {
                ex[0] = e;
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX thread no respondió a tiempo");
        if (ex[0] != null) throw ex[0];
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static void setUsuarioDao(LoginController controller, UsuarioDAO mockDao) throws Exception {
        Field f = LoginController.class.getDeclaredField("usuarioDAO");
        f.setAccessible(true);
        f.set(controller, mockDao);
    }

    private static void callOnIniciarSesion(LoginController controller) throws Exception {
        Method m = LoginController.class.getDeclaredMethod("onIniciarSesion", ActionEvent.class);
        m.setAccessible(true);
        m.invoke(controller, new ActionEvent());
    }

    private static LoginController createControllerWithScene() throws Exception {
        LoginController c = new LoginController();

        TextField email = new TextField();
        PasswordField pass = new PasswordField();
        Button btn = new Button("Login");

        // Stage real para que go() no falle en getScene().getWindow()
        Stage stage = new Stage();
        Pane root = new Pane(btn, email, pass);
        stage.setScene(new Scene(root, 300, 200));
        stage.show();

        setField(c, "userLogin", email);
        setField(c, "contrasenaLogin", pass);
        setField(c, "iniciarSesionBoton", btn);

        return c;
    }

    @Test
    void emailVacio_muestraError_yNoConsultaDao() throws Exception {
        runFxAndWait(() -> {
            LoginController controller = createControllerWithScene();
            UsuarioDAO dao = mock(UsuarioDAO.class);
            setUsuarioDao(controller, dao);

            try (MockedStatic<AlertUtil> alerts = mockStatic(AlertUtil.class)) {
                ((TextField) getPrivate(controller, "userLogin")).setText("   ");
                ((PasswordField) getPrivate(controller, "contrasenaLogin")).setText("1234");

                callOnIniciarSesion(controller);

                alerts.verify(() -> AlertUtil.error("Campo obligatorio", "Introduce el correo electrónico."));
                verifyNoInteractions(dao);
            }
        });
    }

    @Test
    void passVacia_muestraError() throws Exception {
        runFxAndWait(() -> {
            LoginController controller = createControllerWithScene();
            UsuarioDAO dao = mock(UsuarioDAO.class);
            setUsuarioDao(controller, dao);

            try (MockedStatic<AlertUtil> alerts = mockStatic(AlertUtil.class)) {
                ((TextField) getPrivate(controller, "userLogin")).setText("test@mail.com");
                ((PasswordField) getPrivate(controller, "contrasenaLogin")).setText("");

                callOnIniciarSesion(controller);

                alerts.verify(() -> AlertUtil.error("Campo obligatorio", "Introduce la contraseña."));
                verifyNoInteractions(dao);
            }
        });
    }

    @Test
    void usuarioNoExiste_muestraCuentaNoEncontrada() throws Exception {
        runFxAndWait(() -> {
            LoginController controller = createControllerWithScene();
            UsuarioDAO dao = mock(UsuarioDAO.class);
            when(dao.buscarPorEmail("test@mail.com")).thenReturn(null);
            setUsuarioDao(controller, dao);

            try (MockedStatic<AlertUtil> alerts = mockStatic(AlertUtil.class)) {
                ((TextField) getPrivate(controller, "userLogin")).setText("test@mail.com");
                ((PasswordField) getPrivate(controller, "contrasenaLogin")).setText("1234");

                callOnIniciarSesion(controller);

                alerts.verify(() -> AlertUtil.error("Cuenta no encontrada", "No existe ninguna cuenta con ese correo."));
                verify(dao).buscarPorEmail("test@mail.com");
            }
        });
    }

    @Test
    void passIncorrecta_muestraInicioIncorrecto() throws Exception {
        runFxAndWait(() -> {
            LoginController controller = createControllerWithScene();
            UsuarioDAO dao = mock(UsuarioDAO.class);

            UsuarioDTO u = new UsuarioDTO();
            u.setNombre("Juan");
            u.setContrasena("correcta");

            when(dao.buscarPorEmail("test@mail.com")).thenReturn(u);
            setUsuarioDao(controller, dao);

            try (MockedStatic<AlertUtil> alerts = mockStatic(AlertUtil.class)) {
                ((TextField) getPrivate(controller, "userLogin")).setText("test@mail.com");
                ((PasswordField) getPrivate(controller, "contrasenaLogin")).setText("mala");

                callOnIniciarSesion(controller);

                alerts.verify(() -> AlertUtil.error(
                        "Inicio de sesión incorrecto",
                        "La contraseña que has introducido no es correcta."
                ));
            }
        });
    }

    @Test
    void loginCorrecto_seteaSesion_muestraBienvenida_yEvitaBD() throws Exception {
        runFxAndWait(() -> {
            LoginController controller = createControllerWithScene();
            UsuarioDAO dao = mock(UsuarioDAO.class);

            UsuarioDTO u = new UsuarioDTO();
            u.setNombre("Juan");
            u.setEmail("test@mail.com");
            u.setContrasena("1234");

            when(dao.buscarPorEmail("test@mail.com")).thenReturn(u);
            setUsuarioDao(controller, dao);

            // ✅ mocks estáticos EN EL HILO FX para que funcionen
            try (MockedStatic<AlertUtil> alerts = mockStatic(AlertUtil.class);
                 MockedStatic<ViewUtil> views = mockStatic(ViewUtil.class);
                 MockedStatic<WindowUtil> wins = mockStatic(WindowUtil.class)) {

                // ✅ esto evita que cargue Inicio.fxml y que dispare Hibernate/MySQL
                views.when(() -> ViewUtil.loadFXML(any(), anyString())).thenReturn(new Pane());

                ((TextField) getPrivate(controller, "userLogin")).setText("test@mail.com");
                ((PasswordField) getPrivate(controller, "contrasenaLogin")).setText("1234");

                callOnIniciarSesion(controller);

                // Sesión se setea de verdad
                Assertions.assertEquals(u, Sesion.getUsuarioActual());

                alerts.verify(() -> AlertUtil.info(eq("Bienvenido"), contains("Hola, " + u.getNombre())));
                views.verify(() -> ViewUtil.loadFXML(any(), eq("/vistas/Inicio.fxml")));
                wins.verify(() -> WindowUtil.forceMaximize(any(Stage.class)));
            }
        });
    }

    private static Object getPrivate(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(target);
    }
}
