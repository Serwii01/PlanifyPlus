package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.UsuarioDAO;
import com.planify.planifyplus.dto.UsuarioDTO;
import com.planify.planifyplus.util.Sesion;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ConfPerfilControllerTest {

    private ConfPerfilController controller;

    @Mock
    private UsuarioDAO usuarioDAOMock;

    private TextField txtNombre;
    private TextField txtEmail;
    private PasswordField txtContrasena;
    private ComboBox<String> cmbCiudad;
    private Button btnGuardarCambios;
    private Button btnCancelar;

    private MockedStatic<Sesion> sesionMock;
    private Scene mockScene;
    private Stage mockStage;

    @BeforeAll
    static void initToolkit() {
        new JFXPanel();
    }

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        sesionMock = mockStatic(Sesion.class);

        // SOLUCIÓN 1: Crear mocks de Scene/Stage FUERA de Platform.runLater
        mockScene = mock(Scene.class);
        mockStage = mock(Stage.class);
        when(mockScene.getWindow()).thenReturn(mockStage);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller = new ConfPerfilController();

                // Inicializar componentes JavaFX
                txtNombre = new TextField();
                txtEmail = new TextField();
                txtContrasena = new PasswordField();
                cmbCiudad = new ComboBox<>();
                btnGuardarCambios = new Button();
                btnCancelar = new Button();

                // SOLUCIÓN 2: Configurar Scene DESPUÉS de crear los botones
                btnGuardarCambios.setUserData(mockScene); // Workaround para testing
                btnCancelar.setUserData(mockScene);

                // Inyectar campos privados
                setPrivateField(controller, "txtNombre", txtNombre);
                setPrivateField(controller, "txtEmail", txtEmail);
                setPrivateField(controller, "txtContrasena", txtContrasena);
                setPrivateField(controller, "cmbCiudad", cmbCiudad);
                setPrivateField(controller, "btnGuardarCambios", btnGuardarCambios);
                setPrivateField(controller, "btnCancelar", btnCancelar);
                setPrivateField(controller, "usuarioDAO", usuarioDAOMock);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @AfterEach
    void tearDown() {
        if (sesionMock != null) {
            sesionMock.close();
        }
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testInitializeConUsuario() throws Exception {
        // SOLUCIÓN 3: Configurar mock FUERA de Platform.runLater
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setNombre("Usuario Test");
        usuario.setEmail("test@test.com");
        usuario.setContrasena("password123");
        usuario.setCiudad("Sevilla");

        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuario);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.initialize();

                // Pequeño delay para asegurar que se complete la UI
                Thread.sleep(100);

                assertEquals("Usuario Test", txtNombre.getText());
                assertEquals("test@test.com", txtEmail.getText());
                assertEquals("password123", txtContrasena.getText());
                assertEquals("Sevilla", cmbCiudad.getValue());
                assertTrue(cmbCiudad.getItems().contains("Madrid"));
                assertTrue(cmbCiudad.getItems().contains("Barcelona"));
            } catch (Exception e) {
                fail("Error en el test: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testInitializeSinUsuario() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(null);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.initialize();

            assertTrue(txtNombre.getText().isEmpty());
            assertTrue(txtEmail.getText().isEmpty());
            assertTrue(txtContrasena.getText().isEmpty());
            assertTrue(cmbCiudad.getItems().contains("Madrid"));

            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnGuardarCambiosSinSesion() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(null);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller.onGuardarCambios(new ActionEvent());

            verify(usuarioDAOMock, never()).actualizar(any());

            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnGuardarCambiosCamposVacios() throws Exception {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(1L);
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuario);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            txtNombre.setText("");
            txtEmail.setText("");
            txtContrasena.setText("");
            cmbCiudad.setValue(null);

            controller.onGuardarCambios(new ActionEvent());

            verify(usuarioDAOMock, never()).actualizar(any());

            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnGuardarCambiosNombreCorto() throws Exception {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(1L);
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuario);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            txtNombre.setText("A");
            txtEmail.setText("test@test.com");
            txtContrasena.setText("password123");
            cmbCiudad.setValue("Madrid");

            controller.onGuardarCambios(new ActionEvent());

            verify(usuarioDAOMock, never()).actualizar(any());

            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnGuardarCambiosContrasenaCorta() throws Exception {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(1L);
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuario);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            txtNombre.setText("Usuario");
            txtEmail.setText("test@test.com");
            txtContrasena.setText("123");
            cmbCiudad.setValue("Madrid");

            controller.onGuardarCambios(new ActionEvent());

            verify(usuarioDAOMock, never()).actualizar(any());

            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnGuardarCambiosEmailInvalido() throws Exception {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(1L);
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuario);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            txtNombre.setText("Usuario");
            txtEmail.setText("emailinvalido");
            txtContrasena.setText("password123");
            cmbCiudad.setValue("Madrid");

            controller.onGuardarCambios(new ActionEvent());

            verify(usuarioDAOMock, never()).actualizar(any());

            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnGuardarCambiosEmailYaEnUso() throws Exception {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(1L);

        // SOLUCIÓN 4: Configurar mocks ANTES del Platform.runLater
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuario);

        UsuarioDTO otroUsuario = new UsuarioDTO();
        otroUsuario.setId(2L);
        when(usuarioDAOMock.buscarPorEmail("otro@test.com")).thenReturn(otroUsuario);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            txtNombre.setText("Usuario");
            txtEmail.setText("otro@test.com");
            txtContrasena.setText("password123");
            cmbCiudad.setValue("Madrid");

            controller.onGuardarCambios(new ActionEvent());

            verify(usuarioDAOMock, never()).actualizar(any());

            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnGuardarCambiosExitoso() throws Exception {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(1L);
        usuario.setNombre("Nombre Viejo");
        usuario.setEmail("viejo@test.com");
        usuario.setContrasena("oldpass");
        usuario.setCiudad("Valencia");

        // SOLUCIÓN 5: TODOS los mocks configurados ANTES
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuario);
        when(usuarioDAOMock.buscarPorEmail(anyString())).thenReturn(null);
        doNothing().when(usuarioDAOMock).actualizar(any());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            // Mock Scene/Stage para navegación
            when(btnGuardarCambios.getScene()).thenReturn(mockScene);

            txtNombre.setText("Nombre Nuevo");
            txtEmail.setText("nuevo@test.com");
            txtContrasena.setText("newpass123");
            cmbCiudad.setValue("Sevilla");

            assertDoesNotThrow(() -> controller.onGuardarCambios(new ActionEvent()));

            verify(usuarioDAOMock, times(1)).actualizar(argThat(u ->
                    u.getNombre().equals("Nombre Nuevo") &&
                            u.getEmail().equals("nuevo@test.com") &&
                            u.getContrasena().equals("newpass123") &&
                            u.getCiudad().equals("Sevilla")
            ));

            sesionMock.verify(() -> Sesion.actualizarUsuarioActual(any(UsuarioDTO.class)), times(1));

            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnCancelar() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            when(btnCancelar.getScene()).thenReturn(mockScene);
            assertDoesNotThrow(() -> controller.onCancelar(new ActionEvent()));
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnIrInicio() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            when(btnGuardarCambios.getScene()).thenReturn(mockScene);
            assertDoesNotThrow(() -> controller.onIrInicio(new ActionEvent()));
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
}
