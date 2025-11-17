package com.planify.planifyplus.util;

import com.planify.planifyplus.dto.UsuarioDTO;

public class Sesion {

    // Usuario esta actualmente logueado
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
}