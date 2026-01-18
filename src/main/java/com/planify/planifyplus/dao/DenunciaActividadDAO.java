package com.planify.planifyplus.dao;

import com.planify.planifyplus.conexion.ConexionDB;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.DenunciaActividadDTO;
import com.planify.planifyplus.dto.UsuarioDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;

/**
 * DAO para gestionar denuncias de actividades.
 */
public class DenunciaActividadDAO {

    /**
     * Comprueba si ya existe una denuncia para el par (usuario, actividad).
     *
     * @param idUsuario   id del usuario
     * @param idActividad id de la actividad
     * @return true si ya existe, false en caso contrario
     */
    public boolean existeDenuncia(long idUsuario, long idActividad) {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            TypedQuery<Long> q = em.createQuery(
                    "SELECT COUNT(d.id) FROM DenunciaActividadDTO d " +
                            "WHERE d.usuario.id = :u AND d.actividad.id = :a",
                    Long.class
            );
            q.setParameter("u", idUsuario);
            q.setParameter("a", idActividad);
            Long count = q.getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Crea la denuncia si no existía previamente.
     *
     * @param idUsuario   id del usuario
     * @param idActividad id de la actividad
     */
    public void crearDenuncia(long idUsuario, long idActividad) {
        EntityManager em = ConexionDB.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            TypedQuery<Long> q = em.createQuery(
                    "SELECT COUNT(d.id) FROM DenunciaActividadDTO d " +
                            "WHERE d.usuario.id = :u AND d.actividad.id = :a",
                    Long.class
            );
            q.setParameter("u", idUsuario);
            q.setParameter("a", idActividad);

            Long ya = q.getSingleResult();
            if (ya != null && ya > 0) {
                tx.commit();
                return;
            }

            UsuarioDTO usuarioRef = em.getReference(UsuarioDTO.class, idUsuario);
            ActividadDTO actividadRef = em.getReference(ActividadDTO.class, idActividad);

            DenunciaActividadDTO denuncia = new DenunciaActividadDTO();
            denuncia.setUsuario(usuarioRef);
            denuncia.setActividad(actividadRef);
            denuncia.setFechaDenuncia(LocalDateTime.now());

            em.persist(denuncia);

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    /**
     * Método mantenido por compatibilidad (no mantiene recursos abiertos).
     */
    public void cerrar() {
        // NO-OP
    }
}
