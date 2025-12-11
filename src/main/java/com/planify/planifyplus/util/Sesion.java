package com.planify.planifyplus.util;

import com.planify.planifyplus.dto.UsuarioDTO;
import lombok.Data;

@Data
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

    /** Indica si hay un usuario con sesión iniciada. */
    public static boolean haySesion() {
        return usuarioActual != null;
    }

    /** Devuelve el id del usuario logueado o -1 si no hay sesión. */
    public static long getIdUsuario() {
        return usuarioActual != null ? usuarioActual.getId() : -1;
    }

    /** Devuelve si el usuario actual ES ADMIN. */
    public static boolean esAdmin() {
        return usuarioActual != null && usuarioActual.isEsAdmin();
    }

    /**
     * Actualiza los datos del usuario guardado en sesión después
     * de modificar el perfil.
     */
    public static void actualizarUsuarioActual(UsuarioDTO usuarioActualizado) {
        if (usuarioActual == null) {
            usuarioActual = usuarioActualizado;
            return;
        }

        if (usuarioActual.getId() == usuarioActualizado.getId()) {
            usuarioActual.setNombre(usuarioActualizado.getNombre());
            usuarioActual.setEmail(usuarioActualizado.getEmail());
            usuarioActual.setContrasena(usuarioActualizado.getContrasena());
            usuarioActual.setCiudad(usuarioActualizado.getCiudad());
            usuarioActual.setEsAdmin(usuarioActualizado.isEsAdmin());
        } else {
            usuarioActual = usuarioActualizado;
        }
    }
}
