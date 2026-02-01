package com.planify.planifyplus.controller;

import com.planify.planifyplus.dao.InscripcionDAO;
import com.planify.planifyplus.dao.UsuarioDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.TipoActividad;
import com.planify.planifyplus.dto.UsuarioDTO;
import com.planify.planifyplus.util.Sesion;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PerfilControllerTest {

    private PerfilController controller;

    @Mock
    private UsuarioDAO usuarioDAOMock;

    @Mock
    private InscripcionDAO inscripcionDAOMock;

    private Button btnHomePerfil;
    private Label lblNombreUsuario;
    private Label lblEmailUsuario;
    private ScrollPane scrollInscritas;
    private VBox vboxActividadesInscritas;
    private VBox vboxEmptyInscritas;
    private Label lblEmptyInscritas;
    private Button btnExplorarActividades;
    private Label lblMesAnio;
    private GridPane gridCalendario;
    private Button btnMesAnterior;
    private Button btnMesSiguiente;

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
                controller = new PerfilController();

                btnHomePerfil = new Button();
                lblNombreUsuario = new Label();
                lblEmailUsuario = new Label();
                scrollInscritas = new ScrollPane();
                vboxActividadesInscritas = new VBox();
                vboxEmptyInscritas = new VBox();
                lblEmptyInscritas = new Label();
                btnExplorarActividades = new Button();
                lblMesAnio = new Label();
                gridCalendario = new GridPane();
                btnMesAnterior = new Button();
                btnMesSiguiente = new Button();

                // Configurar GridPane con cabeceras de días
                String[] dias = {"L", "M", "X", "J", "V", "S", "D"};
                for (int i = 0; i < dias.length; i++) {
                    Label lblDia = new Label(dias[i]);
                    gridCalendario.add(lblDia, i, 0);
                }

                setPrivateField(controller, "btnHomePerfil", btnHomePerfil);
                setPrivateField(controller, "lblNombreUsuario", lblNombreUsuario);
                setPrivateField(controller, "lblEmailUsuario", lblEmailUsuario);
                setPrivateField(controller, "scrollInscritas", scrollInscritas);
                setPrivateField(controller, "vboxActividadesInscritas", vboxActividadesInscritas);
                setPrivateField(controller, "vboxEmptyInscritas", vboxEmptyInscritas);
                setPrivateField(controller, "lblEmptyInscritas", lblEmptyInscritas);
                setPrivateField(controller, "btnExplorarActividades", btnExplorarActividades);
                setPrivateField(controller, "lblMesAnio", lblMesAnio);
                setPrivateField(controller, "gridCalendario", gridCalendario);
                setPrivateField(controller, "btnMesAnterior", btnMesAnterior);
                setPrivateField(controller, "btnMesSiguiente", btnMesSiguiente);
                setPrivateField(controller, "usuarioDAO", usuarioDAOMock);
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

    private Object invokePrivateMethod(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    @Test
    void testCargarDatosUsuarioConSesion() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                invokePrivateMethod(controller, "cargarDatosUsuario", new Class[]{});

                assertEquals("Test User", lblNombreUsuario.getText());
                assertEquals("test@test.com", lblEmailUsuario.getText());
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCargarDatosUsuarioSinSesion() throws Exception {
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(null);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                invokePrivateMethod(controller, "cargarDatosUsuario", new Class[]{});

                assertEquals("Invitado", lblNombreUsuario.getText());
                assertEquals("Inicia sesión para ver tu perfil", lblEmailUsuario.getText());
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testSetEmptyInscritasVisibleTrue() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                invokePrivateMethod(controller, "setEmptyInscritasVisible",
                        new Class[]{boolean.class, String.class}, true, "Sin actividades");

                assertEquals("Sin actividades", lblEmptyInscritas.getText());
                assertTrue(vboxEmptyInscritas.isVisible());
                assertTrue(vboxEmptyInscritas.isManaged());
                assertFalse(scrollInscritas.isVisible());
                assertFalse(scrollInscritas.isManaged());
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testSetEmptyInscritasVisibleFalse() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                invokePrivateMethod(controller, "setEmptyInscritasVisible",
                        new Class[]{boolean.class, String.class}, false, null);

                assertFalse(vboxEmptyInscritas.isVisible());
                assertFalse(vboxEmptyInscritas.isManaged());
                assertTrue(scrollInscritas.isVisible());
                assertTrue(scrollInscritas.isManaged());
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCargarActividadesInscritasSinSesion() throws Exception {
        sesionMock.when(Sesion::haySesion).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                invokePrivateMethod(controller, "cargarActividadesInscritasComoCards", new Class[]{});

                assertEquals("Inicia sesión para ver tus inscripciones", lblEmptyInscritas.getText());
                assertEquals("Ir a Inicio", btnExplorarActividades.getText());
                assertTrue(vboxEmptyInscritas.isVisible());
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCargarActividadesInscritasListaVacia() throws Exception {
        sesionMock.when(Sesion::haySesion).thenReturn(true);
        sesionMock.when(Sesion::getIdUsuario).thenReturn(1L);
        when(inscripcionDAOMock.obtenerActividadesInscritas(1L)).thenReturn(new ArrayList<>());

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                invokePrivateMethod(controller, "cargarActividadesInscritasComoCards", new Class[]{});

                assertEquals("Aún no estás inscrito en ninguna actividad", lblEmptyInscritas.getText());
                assertEquals("Explorar Actividades", btnExplorarActividades.getText());
                assertTrue(vboxEmptyInscritas.isVisible());
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCargarActividadesInscritasConActividades() throws Exception {
        sesionMock.when(Sesion::haySesion).thenReturn(true);
        sesionMock.when(Sesion::getIdUsuario).thenReturn(1L);
        sesionMock.when(Sesion::getUsuarioActual).thenReturn(usuarioTest);

        List<ActividadDTO> actividades = new ArrayList<>();
        ActividadDTO act1 = new ActividadDTO();
        act1.setId(1L);
        act1.setTitulo("Actividad 1");
        act1.setDescripcion("Descripción 1");
        act1.setTipo(TipoActividad.DEPORTIVA);
        act1.setCiudad("Sevilla");
        act1.setUbicacion("Plaza España");
        act1.setAforo(20);
        act1.setFechaHoraInicio(LocalDateTime.now().plusDays(5));
        actividades.add(act1);

        when(inscripcionDAOMock.obtenerActividadesInscritas(1L)).thenReturn(actividades);
        when(inscripcionDAOMock.contarInscritos(1L)).thenReturn(5L);
        when(inscripcionDAOMock.estaInscrito(1L, 1L)).thenReturn(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                invokePrivateMethod(controller, "cargarActividadesInscritasComoCards", new Class[]{});

                assertFalse(vboxEmptyInscritas.isVisible());
                assertEquals(1, vboxActividadesInscritas.getChildren().size());
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testGetDiasConActividadesEnMesSinSesion() throws Exception {
        sesionMock.when(Sesion::haySesion).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                @SuppressWarnings("unchecked")
                Set<Integer> dias = (Set<Integer>) invokePrivateMethod(controller,
                        "getDiasConActividadesEnMes", new Class[]{YearMonth.class}, YearMonth.now());

                assertTrue(dias.isEmpty());
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testGetDiasConActividadesEnMesConActividades() throws Exception {
        sesionMock.when(Sesion::haySesion).thenReturn(true);
        sesionMock.when(Sesion::getIdUsuario).thenReturn(1L);

        YearMonth marzo2026 = YearMonth.of(2026, 3);

        List<ActividadDTO> actividades = new ArrayList<>();

        ActividadDTO act1 = new ActividadDTO();
        act1.setId(1L);
        act1.setFechaHoraInicio(LocalDateTime.of(2026, 3, 15, 18, 30));
        actividades.add(act1);

        ActividadDTO act2 = new ActividadDTO();
        act2.setId(2L);
        act2.setFechaHoraInicio(LocalDateTime.of(2026, 3, 20, 19, 0));
        actividades.add(act2);

        ActividadDTO act3 = new ActividadDTO();
        act3.setId(3L);
        act3.setFechaHoraInicio(LocalDateTime.of(2026, 4, 10, 10, 0)); // Otro mes
        actividades.add(act3);

        when(inscripcionDAOMock.obtenerActividadesInscritas(1L)).thenReturn(actividades);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                @SuppressWarnings("unchecked")
                Set<Integer> dias = (Set<Integer>) invokePrivateMethod(controller,
                        "getDiasConActividadesEnMes", new Class[]{YearMonth.class}, marzo2026);

                assertEquals(2, dias.size());
                assertTrue(dias.contains(15));
                assertTrue(dias.contains(20));
                assertFalse(dias.contains(10)); // Día de otro mes
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCargarCalendario() throws Exception {
        sesionMock.when(Sesion::haySesion).thenReturn(false);

        YearMonth marzo2026 = YearMonth.of(2026, 3);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                invokePrivateMethod(controller, "cargarCalendario",
                        new Class[]{YearMonth.class}, marzo2026);

                assertTrue(lblMesAnio.getText().contains("Marzo"));
                assertTrue(lblMesAnio.getText().contains("2026"));

                // Verificar que se han añadido días al grid (31 días de marzo + 7 cabeceras)
                assertTrue(gridCalendario.getChildren().size() > 7);
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnMesAnterior() throws Exception {
        sesionMock.when(Sesion::haySesion).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                YearMonth mesInicial = YearMonth.now();
                setPrivateField(controller, "mesActual", mesInicial);

                controller.onMesAnterior();

                Field mesActualField = controller.getClass().getDeclaredField("mesActual");
                mesActualField.setAccessible(true);
                YearMonth mesActual = (YearMonth) mesActualField.get(controller);

                assertEquals(mesInicial.minusMonths(1), mesActual);
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testOnMesSiguiente() throws Exception {
        sesionMock.when(Sesion::haySesion).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                YearMonth mesInicial = YearMonth.now();
                setPrivateField(controller, "mesActual", mesInicial);

                controller.onMesSiguiente();

                Field mesActualField = controller.getClass().getDeclaredField("mesActual");
                mesActualField.setAccessible(true);
                YearMonth mesActual = (YearMonth) mesActualField.get(controller);

                assertEquals(mesInicial.plusMonths(1), mesActual);
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCapitalize() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                String result1 = (String) invokePrivateMethod(controller, "capitalize",
                        new Class[]{String.class}, "deportiva");
                assertEquals("Deportiva", result1);

                String result2 = (String) invokePrivateMethod(controller, "capitalize",
                        new Class[]{String.class}, "");
                assertEquals("", result2);

                String result3 = (String) invokePrivateMethod(controller, "capitalize",
                        new Class[]{String.class}, (Object) null);
                assertNull(result3);
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testGetTipoColor() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                String colorDeportiva = (String) invokePrivateMethod(controller, "getTipoColor",
                        new Class[]{String.class}, "DEPORTIVA");
                assertEquals("#dbeafe", colorDeportiva);

                String colorCultural = (String) invokePrivateMethod(controller, "getTipoColor",
                        new Class[]{String.class}, "CULTURAL");
                assertEquals("#e9d5ff", colorCultural);

                String colorTaller = (String) invokePrivateMethod(controller, "getTipoColor",
                        new Class[]{String.class}, "TALLER");
                assertEquals("#bbf7d0", colorTaller);
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCrearCardActividadConInscripcion() throws Exception {
        sesionMock.when(Sesion::getIdUsuario).thenReturn(1L);
        when(inscripcionDAOMock.contarInscritos(1L)).thenReturn(10L);
        when(inscripcionDAOMock.estaInscrito(1L, 1L)).thenReturn(true);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ActividadDTO actividad = new ActividadDTO();
                actividad.setId(1L);
                actividad.setTitulo("Fútbol");
                actividad.setDescripcion("Partido amistoso");
                actividad.setTipo(TipoActividad.DEPORTIVA);
                actividad.setCiudad("Sevilla");
                actividad.setUbicacion("Estadio");
                actividad.setAforo(20);
                actividad.setFechaHoraInicio(LocalDateTime.of(2026, 3, 15, 18, 30));

                Object card = invokePrivateMethod(controller, "crearCardActividad",
                        new Class[]{ActividadDTO.class}, actividad);

                assertNotNull(card);
                assertTrue(card instanceof VBox);
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCrearCardActividadAforoCompleto() throws Exception {
        sesionMock.when(Sesion::getIdUsuario).thenReturn(1L);
        when(inscripcionDAOMock.contarInscritos(1L)).thenReturn(20L); // Aforo completo
        when(inscripcionDAOMock.estaInscrito(1L, 1L)).thenReturn(false);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                ActividadDTO actividad = new ActividadDTO();
                actividad.setId(1L);
                actividad.setTitulo("Taller completo");
                actividad.setDescripcion("Ya no quedan plazas");
                actividad.setTipo(TipoActividad.TALLER);
                actividad.setCiudad("Madrid");
                actividad.setAforo(20);
                actividad.setFechaHoraInicio(LocalDateTime.of(2026, 3, 15, 18, 30));

                Object card = invokePrivateMethod(controller, "crearCardActividad",
                        new Class[]{ActividadDTO.class}, actividad);

                assertNotNull(card);
                assertTrue(card instanceof VBox);
            } catch (Exception e) {
                fail("Error: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }
}
