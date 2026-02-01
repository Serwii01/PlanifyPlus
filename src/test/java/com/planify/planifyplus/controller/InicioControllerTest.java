package com.planify.planifyplus.controller;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class InicioControllerTest {

    private static Object callPrivate(Object target, String method, Class<?>[] types, Object... args) {
        try {
            Method m = target.getClass().getDeclaredMethod(method, types);
            m.setAccessible(true);
            return m.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void extraerDistanciaMaxima_5km() {
        InicioController c = new InicioController();
        double d = (double) callPrivate(c, "extraerDistanciaMaxima", new Class[]{String.class}, "Menos de 5 km");
        assertEquals(5.0, d);
    }

    @Test
    void extraerDistanciaMaxima_10km() {
        InicioController c = new InicioController();
        double d = (double) callPrivate(c, "extraerDistanciaMaxima", new Class[]{String.class}, "Menos de 10 km");
        assertEquals(10.0, d);
    }

    @Test
    void extraerDistanciaMaxima_20km() {
        InicioController c = new InicioController();
        double d = (double) callPrivate(c, "extraerDistanciaMaxima", new Class[]{String.class}, "Menos de 20 km");
        assertEquals(20.0, d);
    }

    @Test
    void extraerDistanciaMaxima_50km() {
        InicioController c = new InicioController();
        double d = (double) callPrivate(c, "extraerDistanciaMaxima", new Class[]{String.class}, "Menos de 50 km");
        assertEquals(50.0, d);
    }

    @Test
    void extraerDistanciaMaxima_default() {
        InicioController c = new InicioController();
        double d = (double) callPrivate(c, "extraerDistanciaMaxima", new Class[]{String.class}, "Todas las distancias");
        assertEquals(Double.MAX_VALUE, d);
    }

    @Test
    void capitalize_basico() {
        InicioController c = new InicioController();
        String r = (String) callPrivate(c, "capitalize", new Class[]{String.class}, "hola");
        assertEquals("Hola", r);
    }

    @Test
    void getTipoColor_deportiva() {
        InicioController c = new InicioController();
        String r = (String) callPrivate(c, "getTipoColor", new Class[]{String.class}, "DEPORTIVA");
        assertEquals("#dbeafe", r);
    }

    @Test
    void getTipoColor_cultural() {
        InicioController c = new InicioController();
        String r = (String) callPrivate(c, "getTipoColor", new Class[]{String.class}, "CULTURAL");
        assertEquals("#e9d5ff", r);
    }

    @Test
    void getTipoColor_otro() {
        InicioController c = new InicioController();
        String r = (String) callPrivate(c, "getTipoColor", new Class[]{String.class}, "SOCIAL");
        assertEquals("#bbf7d0", r);
    }
}
