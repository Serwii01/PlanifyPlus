package com.planify.planifyplus.util;

import com.planify.planifyplus.dto.UsuarioDTO;

public class Sesion {

    // Usuario que está actualmente logueado
    private static UsuarioDTO usuarioActual;

    // Obtener usuario logueado
    public static UsuarioDTO getUsuarioActual() {
        return usuarioActual;
    }

    // Guardar usuario al iniciar sesión
    public static void setUsuarioActual(UsuarioDTO usuario) {
        usuarioActual = usuario;
    }

    // Cerrar sesión
    public static void cerrarSesion() {
        usuarioActual = null;
    }

    // ================== MÉTODOS EXTRA ==================

    /**
     * Indica si hay un usuario con sesión iniciada.
     */
    public static boolean haySesion() {
        return usuarioActual != null;
    }

    /**
     * Devuelve el id del usuario logueado o -1 si no hay sesión.
     */
    public static long getIdUsuario() {
        return usuarioActual != null ? usuarioActual.getId() : -1;
    }
}
