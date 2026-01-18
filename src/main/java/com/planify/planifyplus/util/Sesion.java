package com.planify.planifyplus.util;

import com.planify.planifyplus.dto.UsuarioDTO;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * Clase utilitaria para gestionar la sesión del usuario en la aplicación.
 */
@Data
public class Sesion {

    /**
     * Usuario que tiene la sesión iniciada actualmente.
     */
    private static UsuarioDTO usuarioActual;

    /**
     * Conjunto de actividades denunciadas durante la sesión actual.
     * Se usa como caché para evitar denuncias duplicadas.
     */
    private static final Set<Long> actividadesDenunciadas = new HashSet<>();

    /**
     * Devuelve el usuario actual de la sesión.
     */
    public static UsuarioDTO getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Establece el usuario actual al iniciar sesión.
     * Limpia el estado asociado a la sesión anterior.
     */
    public static void setUsuarioActual(UsuarioDTO usuario) {
        usuarioActual = usuario;
        actividadesDenunciadas.clear();
    }

    /**
     * Cierra la sesión actual y limpia los datos temporales.
     */
    public static void cerrarSesion() {
        usuarioActual = null;
        actividadesDenunciadas.clear();
    }

    // ================== MÉTODOS DE ESTADO ==================

    /**
     * Indica si hay una sesión iniciada.
     */
    public static boolean haySesion() {
        return usuarioActual != null;
    }

    /**
     * Devuelve el id del usuario en sesión o -1 si no hay sesión.
     */
    public static long getIdUsuario() {
        return usuarioActual != null ? usuarioActual.getId() : -1;
    }

    /**
     * Indica si el usuario actual tiene rol de administrador.
     */
    public static boolean esAdmin() {
        return usuarioActual != null && usuarioActual.isEsAdmin();
    }

    /**
     * Actualiza los datos del usuario almacenado en sesión.
     * Se utiliza tras modificar el perfil.
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
            actividadesDenunciadas.clear();
        }
    }

    // ================== DENUNCIAS EN SESIÓN ==================

    /**
     * Comprueba si una actividad ya ha sido denunciada
     * durante la sesión actual.
     */
    public static boolean haDenunciadoActividad(Long idActividad) {
        if (idActividad == null) return false;
        return actividadesDenunciadas.contains(idActividad);
    }

    /**
     * Marca una actividad como denunciada en la sesión actual.
     */
    public static void marcarActividadDenunciada(Long idActividad) {
        if (idActividad == null) return;
        actividadesDenunciadas.add(idActividad);
    }

    /**
     * Marca varias actividades como denunciadas en la sesión actual.
     */
    public static void marcarActividadesDenunciadas(Set<Long> idsActividades) {
        if (idsActividades == null) return;
        actividadesDenunciadas.addAll(idsActividades);
    }
}
