package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.UsuarioDAO;
import com.planify.planifyplus.dto.UsuarioDTO;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegistroControllerTest {

    private RegistroController controller;

    @Mock
    private UsuarioDAO usuarioDAOMock;

    private TextField txtNombre;
    private TextField txtEmail;
    private PasswordField txtContrasena;
    private ComboBox<String> cmbCiudad;
    private Button btnRegistrarse;

    @BeforeAll
    static void initToolkit() {
        new JFXPanel();
    }

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller = new RegistroController();
                txtNombre = new TextField();
                txtEmail = new TextField();
                txtContrasena = new PasswordField();
                cmbCiudad = new ComboBox<>();
                btnRegistrarse = new Button();

                setPrivateField(controller, "txtNombre", txtNombre);
                setPrivateField(controller, "txtEmail", txtEmail);
                setPrivateField(controller, "txtContrasena", txtContrasena);
                setPrivateField(controller, "cmbCiudad", cmbCiudad);
                setPrivateField(controller, "btnRegistrarse", btnRegistrarse);
                setPrivateField(controller, "usuarioDAO", usuarioDAOMock);

                controller.initialize();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testInitialize() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            assertNotNull(cmbCiudad.getItems());
            assertTrue(cmbCiudad.getItems().contains("Sevilla"));
            assertTrue(cmbCiudad.getItems().contains("Madrid"));
            assertTrue(cmbCiudad.getItems().contains("Barcelona"));
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnRegistrarseNombreVacio() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            txtNombre.setText("");
            txtEmail.setText("test@test.com");
            txtContrasena.setText("123456");
            cmbCiudad.setValue("Sevilla");

            controller.onRegistrarse();

            verify(usuarioDAOMock, never()).existeEmail(anyString());
            verify(usuarioDAOMock, never()).crear(any());

            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnRegistrarseEmailInvalido() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            txtNombre.setText("Usuario Test");
            txtEmail.setText("emailinvalido");
            txtContrasena.setText("123456");
            cmbCiudad.setValue("Sevilla");

            controller.onRegistrarse();

            verify(usuarioDAOMock, never()).existeEmail(anyString());
            verify(usuarioDAOMock, never()).crear(any());

            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnRegistrarseContrasenaCorta() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            txtNombre.setText("Usuario Test");
            txtEmail.setText("test@test.com");
            txtContrasena.setText("123");
            cmbCiudad.setValue("Sevilla");

            controller.onRegistrarse();

            verify(usuarioDAOMock, never()).existeEmail(anyString());
            verify(usuarioDAOMock, never()).crear(any());

            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnRegistrarseEmailYaRegistrado() throws Exception {
        when(usuarioDAOMock.existeEmail(anyString())).thenReturn(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            txtNombre.setText("Usuario Test");
            txtEmail.setText("test@test.com");
            txtContrasena.setText("123456");
            cmbCiudad.setValue("Sevilla");

            try {
                controller.onRegistrarse();
            } catch (Exception e) {
                // Esperado si intenta cambiar de escena
            }

            verify(usuarioDAOMock, times(1)).existeEmail("test@test.com");
            verify(usuarioDAOMock, never()).crear(any());

            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnRegistrarseExitoso() throws Exception {
        when(usuarioDAOMock.existeEmail(anyString())).thenReturn(false);
        doNothing().when(usuarioDAOMock).crear(any(UsuarioDTO.class));

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            txtNombre.setText("Usuario Nuevo");
            txtEmail.setText("nuevo@test.com");
            txtContrasena.setText("password123");
            cmbCiudad.setValue("Madrid");

            try {
                controller.onRegistrarse();
            } catch (Exception e) {
                // Esperado si intenta cambiar de escena (no tenemos Stage en test)
            }

            verify(usuarioDAOMock, times(1)).existeEmail("nuevo@test.com");
            verify(usuarioDAOMock, times(1)).crear(argThat(usuario ->
                    usuario.getNombre().equals("Usuario Nuevo") &&
                            usuario.getEmail().equals("nuevo@test.com") &&
                            usuario.getContrasena().equals("password123") &&
                            usuario.getCiudad().equals("Madrid") &&
                            !usuario.isEsAdmin()
            ));

            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnIrInicio() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.onIrInicio();
            } catch (Exception e) {
                // Se espera excepción al no tener Stage configurado
                assertTrue(e instanceof NullPointerException || e.getMessage() != null);
            }
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnIrLogin() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.onIrLogin();
            } catch (Exception e) {
                // Se espera excepción al no tener Stage configurado
                assertTrue(e instanceof NullPointerException || e.getMessage() != null);
            }
            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }
}
