package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.ActividadDAO;
import com.planify.planifyplus.dao.DenunciaActividadDAO;
import com.planify.planifyplus.dao.InscripcionDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.TipoActividad;
import com.planify.planifyplus.dto.UsuarioDTO;
import com.planify.planifyplus.util.Sesion;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActividadControllerTest {

    private ActividadController controller;

    @Mock
    private ActividadDAO actividadDAOMock;

    @Mock
    private DenunciaActividadDAO denunciaDAOMock;

    @Mock
    private InscripcionDAO inscripcionDAOMock;

    private WebView webViewMap;
    private Label lblTipo;
    private Label lblTitulo;
    private Label lblDescripcion;
    private Label lblFecha;
    private Label lblHora;
    private Label lblUbicacionCaja;
    private Label lblCiudadCaja;
    private Label lblPlazas;
    private Label lblDebeIniciarSesion;
    private Button btnInscribirse;
    private Button btnDenunciar;
    private Button btnVolver;

    private MockedStatic<Sesion> sesionMock;
    private UsuarioDTO usuarioTest;
    private ActividadDTO actividadTest;

    @BeforeAll
    static void initToolkit() {
        new JFXPanel();
    }

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        sesionMock = mockStatic(Sesion.class);

        usuarioTest = new UsuarioDTO();
        usuarioTest.setId(1L);
        usuarioTest.setNombre("Test User");
        usuarioTest.setEmail("test@test.com");

        actividadTest = new ActividadDTO();
        actividadTest.setId(1L);
        actividadTest.setTitulo("Actividad Test");
        actividadTest.setDescripcion("Descripción de prueba");
        actividadTest.setTipo(TipoActividad.DEPORTIVA);
        actividadTest.setFechaHoraInicio(LocalDateTime.of(2026, 3, 15, 18, 30));
        actividadTest.setUbicacion("Plaza España");
        actividadTest.setCiudad("Sevilla");
        actividadTest.setAforo(20);
        actividadTest.setLatitud(new BigDecimal("37.3771"));
        actividadTest.setLongitud(new BigDecimal("-5.9868"));

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller = new ActividadController();

                webViewMap = new WebView();
                lblTipo = new Label();
                lblTitulo = new Label();
                lblDescripcion = new Label();
                lblFecha = new Label();
                lblHora = new Label();
                lblUbicacionCaja = new Label();
                lblCiudadCaja = new Label();
                lblPlazas = new Label();
                lblDebeIniciarSesion = new Label();
                btnInscribirse = new Button();
                btnDenunciar = new Button();
                btnVolver = new Button();

                setPrivateField(controller, "webViewMap", webViewMap);
                setPrivateField(controller, "lblTipo", lblTipo);
                setPrivateField(controller, "lblTitulo", lblTitulo);
                setPrivateField(controller, "lblDescripcion", lblDescripcion);
                setPrivateField(controller, "lblFecha", lblFecha);
                setPrivateField(controller, "lblHora", lblHora);
                setPrivateField(controller, "lblUbicacionCaja", lblUbicacionCaja);
                setPrivateField(controller, "lblCiudadCaja", lblCiudadCaja);
                setPrivateField(controller, "lblPlazas", lblPlazas);
                setPrivateField(controller, "lblDebeIniciarSesion", lblDebeIniciarSesion);
                setPrivateField(controller, "btnInscribirse", btnInscribirse);
                setPrivateField(controller, "btnDenunciar", btnDenunciar);
                setPrivateField(controller, "btnVolver", btnVolver);
                setPrivateField(controller, "actividadDAO", actividadDAOMock);
                setPrivateField(controller, "denunciaDAO", denunciaDAOMock);
                setPrivateField(controller, "inscripcionDAO", inscripcionDAOMock);

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
    void testSetActividadCargaDatos() throws Exception {
        // Configurar mocks ANTES del Platform.runLater
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(null);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);
        when(inscripcionDAOMock.contarInscritos(1L)).thenReturn(5L);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setActividad(actividadTest);

                assertEquals("Actividad Test", lblTitulo.getText());
                assertEquals("Descripción de prueba", lblDescripcion.getText());
                assertEquals("Deportiva", lblTipo.getText());
                assertEquals("Plaza España", lblUbicacionCaja.getText());
                assertEquals("Sevilla", lblCiudadCaja.getText());
                assertEquals("5 / 20 personas inscritas", lblPlazas.getText());
                assertTrue(lblFecha.getText().contains("15"));
                assertEquals("18:30", lblHora.getText());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testSetActividadSinUbicacion() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(null);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);
        when(inscripcionDAOMock.contarInscritos(2L)).thenReturn(0L);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ActividadDTO actSinUbicacion = new ActividadDTO();
                actSinUbicacion.setId(2L);
                actSinUbicacion.setTitulo("Sin Ubicación");
                actSinUbicacion.setDescripcion("Test");
                actSinUbicacion.setTipo(TipoActividad.CULTURAL);
                actSinUbicacion.setFechaHoraInicio(LocalDateTime.now().plusDays(1));
                actSinUbicacion.setAforo(10);

                controller.setActividad(actSinUbicacion);

                assertEquals("Sin ubicación", lblUbicacionCaja.getText());
                assertEquals("Sin ciudad", lblCiudadCaja.getText());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testBotonInscribirseSinSesion() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(null);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);
        when(inscripcionDAOMock.contarInscritos(1L)).thenReturn(0L);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setActividad(actividadTest);

                assertTrue(btnInscribirse.isVisible());
                assertTrue(btnInscribirse.isDisabled());
                assertEquals("Inscribirse a esta actividad", btnInscribirse.getText());
                assertTrue(lblDebeIniciarSesion.isVisible());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testBotonInscribirseUsuarioYaInscrito() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);
        when(inscripcionDAOMock.contarInscritos(1L)).thenReturn(5L);
        when(inscripcionDAOMock.estaInscrito(1L, 1L)).thenReturn(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setActividad(actividadTest);

                assertTrue(btnInscribirse.isVisible());
                assertTrue(btnInscribirse.isDisabled());
                assertEquals("Inscrito ✓", btnInscribirse.getText());
                assertFalse(lblDebeIniciarSesion.isVisible());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testBotonInscribirseUsuarioNoInscrito() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);
        when(inscripcionDAOMock.contarInscritos(1L)).thenReturn(5L);
        when(inscripcionDAOMock.estaInscrito(1L, 1L)).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setActividad(actividadTest);

                assertTrue(btnInscribirse.isVisible());
                assertFalse(btnInscribirse.isDisabled());
                assertEquals("Inscribirse a esta actividad", btnInscribirse.getText());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testBotonInscribirseAforoCompleto() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);
        when(inscripcionDAOMock.contarInscritos(1L)).thenReturn(20L);
        when(inscripcionDAOMock.estaInscrito(1L, 1L)).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setActividad(actividadTest);

                assertTrue(btnInscribirse.isDisabled());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testBotonInscribirseAdminOculto() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::esAdmin).thenReturn(true);
        when(inscripcionDAOMock.contarInscritos(1L)).thenReturn(0L);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setActividad(actividadTest);

                assertFalse(btnInscribirse.isVisible());
                assertFalse(lblDebeIniciarSesion.isVisible());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testBotonDenunciarSinSesion() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(null);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);
        when(inscripcionDAOMock.contarInscritos(1L)).thenReturn(0L);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setActividad(actividadTest);

                assertTrue(btnDenunciar.isVisible());
                assertTrue(btnDenunciar.isDisabled());
                assertEquals("Inicia sesión para denunciar", btnDenunciar.getText());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testBotonDenunciarAdminOculto() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::esAdmin).thenReturn(true);
        when(inscripcionDAOMock.contarInscritos(1L)).thenReturn(0L);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setActividad(actividadTest);

                assertFalse(btnDenunciar.isVisible());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testBotonDenunciarYaDenunciada() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);
        when(inscripcionDAOMock.contarInscritos(1L)).thenReturn(0L);
        when(denunciaDAOMock.existeDenuncia(1L, 1L)).thenReturn(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setActividad(actividadTest);

                assertTrue(btnDenunciar.isVisible());
                assertTrue(btnDenunciar.isDisabled());
                assertEquals("Ya denunciada", btnDenunciar.getText());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testBotonDenunciarDisponible() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);
        when(inscripcionDAOMock.contarInscritos(1L)).thenReturn(0L);
        when(denunciaDAOMock.existeDenuncia(1L, 1L)).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setActividad(actividadTest);

                assertTrue(btnDenunciar.isVisible());
                assertFalse(btnDenunciar.isDisabled());
                assertEquals("Denunciar actividad", btnDenunciar.getText());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testManejarDenuncia() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);
        when(inscripcionDAOMock.contarInscritos(1L)).thenReturn(0L);
        when(denunciaDAOMock.existeDenuncia(1L, 1L)).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller.setActividad(actividadTest);

                Method method = ActividadController.class.getDeclaredMethod("manejarDenuncia");
                method.setAccessible(true);
                method.invoke(controller);

                verify(denunciaDAOMock).crearDenuncia(1L, 1L);
                verify(actividadDAOMock).incrementarDenuncias(1L);
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testSetActividadNull() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                assertDoesNotThrow(() -> controller.setActividad(null));
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }
}
