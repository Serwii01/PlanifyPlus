package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.ActividadDAO;
import com.planify.planifyplus.dao.InscripcionDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.TipoActividad;
import com.planify.planifyplus.dto.UsuarioDTO;
import com.planify.planifyplus.util.Sesion;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CrearActividadControllerTest {

    private CrearActividadController controller;

    @Mock
    private ActividadDAO actividadDAOMock;

    @Mock
    private InscripcionDAO inscripcionDAOMock;

    private TextField txtTitulo;
    private TextArea txtDescripcion;
    private DatePicker dpFecha;
    private TextField txtHora;
    private ComboBox<String> cmbTipo;
    private TextField txtUbicacion;
    private TextField txtCiudad;
    private TextField txtAforo;
    private TextField txtLatitud;
    private TextField txtLongitud;
    private WebView webViewBuscador;
    private Button btnGuardar;
    private Label lblTituloPagina;

    private MockedStatic<Sesion> sesionMock;
    private UsuarioDTO usuarioTest;

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

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                controller = new CrearActividadController();

                txtTitulo = new TextField();
                txtDescripcion = new TextArea();
                dpFecha = new DatePicker();
                txtHora = new TextField();
                cmbTipo = new ComboBox<>();
                txtUbicacion = new TextField();
                txtCiudad = new TextField();
                txtAforo = new TextField();
                txtLatitud = new TextField();
                txtLongitud = new TextField();
                webViewBuscador = new WebView();
                btnGuardar = new Button();
                lblTituloPagina = new Label();

                setPrivateField(controller, "txtTitulo", txtTitulo);
                setPrivateField(controller, "txtDescripcion", txtDescripcion);
                setPrivateField(controller, "dpFecha", dpFecha);
                setPrivateField(controller, "txtHora", txtHora);
                setPrivateField(controller, "cmbTipo", cmbTipo);
                setPrivateField(controller, "txtUbicacion", txtUbicacion);
                setPrivateField(controller, "txtCiudad", txtCiudad);
                setPrivateField(controller, "txtAforo", txtAforo);
                setPrivateField(controller, "txtLatitud", txtLatitud);
                setPrivateField(controller, "txtLongitud", txtLongitud);
                setPrivateField(controller, "webViewBuscador", webViewBuscador);
                setPrivateField(controller, "btnGuardar", btnGuardar);
                setPrivateField(controller, "lblTituloPagina", lblTituloPagina);
                setPrivateField(controller, "actividadDAO", actividadDAOMock);
                setPrivateField(controller, "inscripcionDAO", inscripcionDAOMock);

                // Inicializar ComboBox con los tipos
                cmbTipo.getItems().addAll("DEPORTIVA", "CULTURAL", "TALLER");

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
    void testInitializeComboBoxTipos() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                assertEquals(3, cmbTipo.getItems().size());
                assertTrue(cmbTipo.getItems().contains("DEPORTIVA"));
                assertTrue(cmbTipo.getItems().contains("CULTURAL"));
                assertTrue(cmbTipo.getItems().contains("TALLER"));
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testSetActividadParaEditarCargaDatos() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ActividadDTO actividad = new ActividadDTO();
                actividad.setId(1L);
                actividad.setTitulo("Actividad Test");
                actividad.setDescripcion("Descripción de prueba");
                actividad.setFechaHoraInicio(LocalDateTime.of(2026, 3, 15, 18, 30));
                actividad.setTipo(TipoActividad.DEPORTIVA);
                actividad.setUbicacion("Plaza España");
                actividad.setCiudad("Sevilla");
                actividad.setAforo(20);
                actividad.setLatitud(new BigDecimal("37.3771"));
                actividad.setLongitud(new BigDecimal("-5.9868"));

                controller.setActividadParaEditar(actividad);

                assertEquals("Actividad Test", txtTitulo.getText());
                assertEquals("Descripción de prueba", txtDescripcion.getText());
                assertEquals(LocalDate.of(2026, 3, 15), dpFecha.getValue());
                assertEquals("18:30", txtHora.getText());
                assertEquals("DEPORTIVA", cmbTipo.getValue());
                assertEquals("Plaza España", txtUbicacion.getText());
                assertEquals("Sevilla", txtCiudad.getText());
                assertEquals("20", txtAforo.getText());
                assertEquals("37.3771", txtLatitud.getText());
                assertEquals("-5.9868", txtLongitud.getText());
                assertEquals("Editar Actividad", lblTituloPagina.getText());
                assertEquals("Actualizar", btnGuardar.getText());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testSetActividadParaEditarSinCoordenadas() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ActividadDTO actividad = new ActividadDTO();
                actividad.setId(1L);
                actividad.setTitulo("Sin Coordenadas");
                actividad.setDescripcion("Test");
                actividad.setFechaHoraInicio(LocalDateTime.of(2026, 3, 15, 18, 30));
                actividad.setTipo(TipoActividad.CULTURAL);
                actividad.setUbicacion("Sin ubicación");
                actividad.setCiudad("Madrid");
                actividad.setAforo(10);

                controller.setActividadParaEditar(actividad);

                assertEquals("", txtLatitud.getText());
                assertEquals("", txtLongitud.getText());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testValidacionTituloVacio() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::haySesion).thenReturn(true);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                txtTitulo.setText("");
                txtDescripcion.setText("Descripción");
                dpFecha.setValue(LocalDate.now().plusDays(1));
                txtHora.setText("18:30");
                cmbTipo.setValue("DEPORTIVA");
                txtUbicacion.setText("Plaza España");
                txtCiudad.setText("Sevilla");
                txtAforo.setText("20");

                // Esta llamada debería mostrar error (verificamos que no se llama a guardar)
                controller.handleGuardarActividad();

                verify(actividadDAOMock, never()).guardar(any());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testValidacionDescripcionVacia() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::haySesion).thenReturn(true);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                txtTitulo.setText("Título");
                txtDescripcion.setText("");
                dpFecha.setValue(LocalDate.now().plusDays(1));
                txtHora.setText("18:30");
                cmbTipo.setValue("DEPORTIVA");
                txtUbicacion.setText("Plaza España");
                txtCiudad.setText("Sevilla");
                txtAforo.setText("20");

                controller.handleGuardarActividad();

                verify(actividadDAOMock, never()).guardar(any());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testValidacionFechaVacia() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::haySesion).thenReturn(true);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                txtTitulo.setText("Título");
                txtDescripcion.setText("Descripción");
                dpFecha.setValue(null);
                txtHora.setText("18:30");
                cmbTipo.setValue("DEPORTIVA");
                txtUbicacion.setText("Plaza España");
                txtCiudad.setText("Sevilla");
                txtAforo.setText("20");

                controller.handleGuardarActividad();

                verify(actividadDAOMock, never()).guardar(any());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testValidacionHoraInvalidaFormato() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::haySesion).thenReturn(true);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                txtTitulo.setText("Título");
                txtDescripcion.setText("Descripción");
                dpFecha.setValue(LocalDate.now().plusDays(1));
                txtHora.setText("25:70"); // Hora inválida
                cmbTipo.setValue("DEPORTIVA");
                txtUbicacion.setText("Plaza España");
                txtCiudad.setText("Sevilla");
                txtAforo.setText("20");

                controller.handleGuardarActividad();

                verify(actividadDAOMock, never()).guardar(any());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testValidacionFechaPasada() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::haySesion).thenReturn(true);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                txtTitulo.setText("Título");
                txtDescripcion.setText("Descripción");
                dpFecha.setValue(LocalDate.now().minusDays(1)); // Fecha pasada
                txtHora.setText("18:30");
                cmbTipo.setValue("DEPORTIVA");
                txtUbicacion.setText("Plaza España");
                txtCiudad.setText("Sevilla");
                txtAforo.setText("20");

                controller.handleGuardarActividad();

                verify(actividadDAOMock, never()).guardar(any());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testValidacionAforoCero() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::haySesion).thenReturn(true);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                txtTitulo.setText("Título");
                txtDescripcion.setText("Descripción");
                dpFecha.setValue(LocalDate.now().plusDays(1));
                txtHora.setText("18:30");
                cmbTipo.setValue("DEPORTIVA");
                txtUbicacion.setText("Plaza España");
                txtCiudad.setText("Sevilla");
                txtAforo.setText("0"); // Aforo inválido

                controller.handleGuardarActividad();

                verify(actividadDAOMock, never()).guardar(any());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testGuardarActividadNuevaUsuarioNormal() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::haySesion).thenReturn(true);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                txtTitulo.setText("Actividad Nueva");
                txtDescripcion.setText("Descripción completa");
                dpFecha.setValue(LocalDate.now().plusDays(5));
                txtHora.setText("18:30");
                cmbTipo.setValue("DEPORTIVA");
                txtUbicacion.setText("Plaza España");
                txtCiudad.setText("Sevilla");
                txtAforo.setText("20");
                txtLatitud.setText("37.3771");
                txtLongitud.setText("-5.9868");

                controller.handleGuardarActividad();

                verify(actividadDAOMock, times(1)).guardar(any(ActividadDTO.class));
                verify(inscripcionDAOMock, times(1)).inscribir(eq(usuarioTest), any(ActividadDTO.class));
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testGuardarActividadNuevaAdmin() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::haySesion).thenReturn(true);
        sesionMock.when(Sesion::esAdmin).thenReturn(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                txtTitulo.setText("Actividad Nueva");
                txtDescripcion.setText("Descripción completa");
                dpFecha.setValue(LocalDate.now().plusDays(5));
                txtHora.setText("18:30");
                cmbTipo.setValue("CULTURAL");
                txtUbicacion.setText("Museo");
                txtCiudad.setText("Madrid");
                txtAforo.setText("50");

                controller.handleGuardarActividad();

                verify(actividadDAOMock, times(1)).guardar(any(ActividadDTO.class));
                // Admin NO se inscribe automáticamente
                verify(inscripcionDAOMock, never()).inscribir(any(), any());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testActualizarActividadExistente() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::haySesion).thenReturn(true);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ActividadDTO actividadExistente = new ActividadDTO();
                actividadExistente.setId(5L);
                actividadExistente.setTitulo("Actividad Original");
                actividadExistente.setDescripcion("Descripción original");
                actividadExistente.setFechaHoraInicio(LocalDateTime.of(2026, 3, 15, 18, 30));
                actividadExistente.setTipo(TipoActividad.DEPORTIVA);
                actividadExistente.setUbicacion("Plaza España");
                actividadExistente.setCiudad("Sevilla");
                actividadExistente.setAforo(20);

                controller.setActividadParaEditar(actividadExistente);

                txtTitulo.setText("Actividad Modificada");
                txtDescripcion.setText("Nueva descripción");
                txtAforo.setText("30");

                controller.handleGuardarActividad();

                verify(actividadDAOMock, times(1)).guardar(any(ActividadDTO.class));
                // En edición NO se inscribe automáticamente
                verify(inscripcionDAOMock, never()).inscribir(any(), any());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testJavaScriptBridgeOnLocationSelected() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                CrearActividadController.JavaScriptBridge bridge = controller.new JavaScriptBridge();

                bridge.onLocationSelected("Catedral de Sevilla", 37.3860, -5.9926);

                // Esperar un poco para que Platform.runLater interno se ejecute
                Thread.sleep(100);

                assertEquals("Catedral de Sevilla", txtUbicacion.getText());
                assertEquals("37.386", txtLatitud.getText());
                assertEquals("-5.9926", txtLongitud.getText());
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testGuardarActividadSinCoordenadas() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);
        sesionMock.when(Sesion::haySesion).thenReturn(true);
        sesionMock.when(Sesion::esAdmin).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                txtTitulo.setText("Actividad sin coordenadas");
                txtDescripcion.setText("Descripción");
                dpFecha.setValue(LocalDate.now().plusDays(5));
                txtHora.setText("18:30");
                cmbTipo.setValue("TALLER");
                txtUbicacion.setText("Sala 5");
                txtCiudad.setText("Barcelona");
                txtAforo.setText("15");
                txtLatitud.setText("");
                txtLongitud.setText("");

                controller.handleGuardarActividad();

                verify(actividadDAOMock, times(1)).guardar(any(ActividadDTO.class));
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }
}
