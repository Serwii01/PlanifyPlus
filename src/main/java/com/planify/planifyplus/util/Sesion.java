package com.planify.planifyplus.util;

import com.planify.planifyplus.dto.UsuarioDTO;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Sesion {

    // Usuario que está actualmente logueado
    private static UsuarioDTO usuarioActual;

    // En este conjunto guardo los IDs de actividades que ya he denunciado en esta sesión
    private static final Set<Long> actividadesDenunciadas = new HashSet<>();

    // Obtener usuario logueado
    public static UsuarioDTO getUsuarioActual() {
        return usuarioActual;
    }

    // Guardar usuario al iniciar sesión
    public static void setUsuarioActual(UsuarioDTO usuario) {
        usuarioActual = usuario;

        // Cuando cambia el usuario, limpio el "cache" de denuncias de esta sesión
        actividadesDenunciadas.clear();
    }

    // Cerrar sesión
    public static void cerrarSesion() {
        usuarioActual = null;
        // Cuando cierro sesión, vacío también las actividades denunciadas
        actividadesDenunciadas.clear();
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

            // Si por lo que sea me cambian el usuario actual,
            // limpio también el cache de denuncias
            actividadesDenunciadas.clear();
        }
    }

    // ================== DENUNCIAS EN ESTA SESIÓN ==================

    /**
     * Con este método compruebo si ya he denunciado esta actividad
     * en la sesión actual.
     */
    public static boolean haDenunciadoActividad(Long idActividad) {
        if (idActividad == null) {
            return false;
        }
        return actividadesDenunciadas.contains(idActividad);
    }

    /**
     * Con este método marco que he denunciado una actividad
     * en la sesión actual.
     */
    public static void marcarActividadDenunciada(Long idActividad) {
        if (idActividad == null) {
            return;
        }
        actividadesDenunciadas.add(idActividad);
    }

    /**
     * Con este método marco una lista de actividades como denunciadas
     * (me servirá cuando cargue desde BD las denuncias del usuario).
     */
    public static void marcarActividadesDenunciadas(Set<Long> idsActividades) {
        if (idsActividades == null) {
            return;
        }
        actividadesDenunciadas.addAll(idsActividades);
    }
}
