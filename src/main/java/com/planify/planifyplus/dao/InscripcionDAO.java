package com.planify.planifyplus.dao;

import com.planify.planifyplus.conexion.ConexionDB;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.InscripcionDTO;
import com.planify.planifyplus.dto.UsuarioDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DAO para gestionar inscripciones a actividades.
 */
public class InscripcionDAO {

    /**
     * Comprueba si un usuario está inscrito en una actividad.
     *
     * @param usuarioId   id del usuario
     * @param actividadId id de la actividad
     * @return true si existe inscripción
     */
    public boolean estaInscrito(long usuarioId, long actividadId) {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(i) FROM InscripcionDTO i " +
                                    "WHERE i.usuario.id = :u AND i.actividad.id = :a",
                            Long.class
                    )
                    .setParameter("u", usuarioId)
                    .setParameter("a", actividadId)
                    .getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Crea la inscripción del usuario en la actividad (si no existía).
     *
     * @param usuario    usuario
     * @param actividad  actividad
     */
    public void inscribir(UsuarioDTO usuario, ActividadDTO actividad) {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            em.getTransaction().begin();

            Long count = em.createQuery(
                            "SELECT COUNT(i) FROM InscripcionDTO i " +
                                    "WHERE i.usuario.id = :u AND i.actividad.id = :a",
                            Long.class
                    )
                    .setParameter("u", usuario.getId())
                    .setParameter("a", actividad.getId())
                    .getSingleResult();

            if (count != null && count > 0) {
                em.getTransaction().commit();
                return;
            }

            InscripcionDTO ins = new InscripcionDTO();
            ins.setUsuario(em.getReference(UsuarioDTO.class, usuario.getId()));
            ins.setActividad(em.getReference(ActividadDTO.class, actividad.getId()));
            ins.setCreadaEn(LocalDateTime.now());

            em.persist(ins);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Cancela una inscripción (si existe).
     *
     * @param usuarioId   id del usuario
     * @param actividadId id de la actividad
     */
    public void cancelar(long usuarioId, long actividadId) {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            em.getTransaction().begin();

            try {
                InscripcionDTO ins = em.createQuery(
                                "SELECT i FROM InscripcionDTO i " +
                                        "WHERE i.usuario.id = :u AND i.actividad.id = :a",
                                InscripcionDTO.class
                        )
                        .setParameter("u", usuarioId)
                        .setParameter("a", actividadId)
                        .getSingleResult();

                em.remove(ins);
            } catch (NoResultException ignored) {
                // no existe
            }

            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Devuelve el número de inscritos en una actividad.
     *
     * @param actividadId id de la actividad
     * @return total de inscritos
     */
    public long contarInscritos(long actividadId) {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(i) FROM InscripcionDTO i WHERE i.actividad.id = :a",
                            Long.class
                    )
                    .setParameter("a", actividadId)
                    .getSingleResult();
            return count != null ? count : 0L;
        } finally {
            em.close();
        }
    }

    /**
     * Lista las actividades en las que está inscrito un usuario.
     *
     * @param usuarioId id del usuario
     * @return actividades inscritas
     */
    public List<ActividadDTO> obtenerActividadesInscritas(long usuarioId) {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT i.actividad FROM InscripcionDTO i " +
                                    "WHERE i.usuario.id = :u " +
                                    "ORDER BY i.actividad.fechaHoraInicio ASC",
                            ActividadDTO.class
                    )
                    .setParameter("u", usuarioId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
