package com.planify.planifyplus.dao;

import com.planify.planifyplus.conexion.ConexionDB;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.DenunciaActividadDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DAO de actividades (consultas y operaciones básicas de persistencia).
 */
public class ActividadDAO {

    // ============================================================
    //   CONSULTAS
    // ============================================================

    /**
     * Devuelve todas las actividades ordenadas por fecha.
     */
    public List<ActividadDTO> obtenerTodas() {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM ActividadDTO a ORDER BY a.fechaHoraInicio ASC",
                    ActividadDTO.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lista de actividades marcadas como predeterminadas.
     */
    public List<ActividadDTO> obtenerPredeterminadas() {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM ActividadDTO a " +
                            "WHERE a.predeterminada = true " +
                            "ORDER BY a.fechaHoraInicio ASC",
                    ActividadDTO.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lista de actividades creadas por usuarios (no predeterminadas).
     */
    public List<ActividadDTO> obtenerNoPredeterminadas() {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM ActividadDTO a " +
                            "WHERE a.predeterminada = false " +
                            "ORDER BY a.fechaHoraInicio ASC",
                    ActividadDTO.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Actividades creadas por un usuario concreto.
     *
     * @param idUsuario id del creador
     */
    public List<ActividadDTO> obtenerCreadasPorUsuario(long idUsuario) {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT a FROM ActividadDTO a " +
                                    "WHERE a.predeterminada = false " +
                                    "AND a.creador.id = :idUsuario " +
                                    "ORDER BY a.fechaHoraInicio ASC",
                            ActividadDTO.class
                    )
                    .setParameter("idUsuario", idUsuario)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Actividades con denuncias, ordenadas por número de denuncias.
     */
    public List<ActividadDTO> obtenerDenunciadasOrdenadas() {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM ActividadDTO a " +
                            "WHERE a.numDenuncias > 0 " +
                            "ORDER BY a.numDenuncias DESC, a.fechaHoraInicio ASC",
                    ActividadDTO.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Número total de actividades.
     */
    public long contar() {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            Long c = em.createQuery("SELECT COUNT(a) FROM ActividadDTO a", Long.class).getSingleResult();
            return c != null ? c : 0L;
        } finally {
            em.close();
        }
    }

    /**
     * Número de actividades predeterminadas.
     */
    public long contarPredeterminadas() {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            Long c = em.createQuery(
                    "SELECT COUNT(a) FROM ActividadDTO a WHERE a.predeterminada = true",
                    Long.class
            ).getSingleResult();
            return c != null ? c : 0L;
        } finally {
            em.close();
        }
    }

    // ============================================================
    //   ESCRITURAS
    // ============================================================

    /**
     * Guarda una actividad (persist si es nueva, merge si ya existe).
     *
     * @param actividad entidad a guardar
     */
    public void guardar(ActividadDTO actividad) {
        EntityManager em = ConexionDB.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            if (actividad.getId() == null) em.persist(actividad);
            else em.merge(actividad);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    /**
     * Elimina todas las actividades predeterminadas junto con sus relaciones (inscripciones/denuncias).
     */
    public void eliminarTodasPredeterminadas() {
        EntityManager em = ConexionDB.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            List<Long> idsPredeterminadas = em.createQuery(
                    "SELECT a.id FROM ActividadDTO a WHERE a.predeterminada = true",
                    Long.class
            ).getResultList();

            if (idsPredeterminadas.isEmpty()) {
                tx.commit();
                return;
            }

            em.createQuery(
                    "DELETE FROM InscripcionDTO i WHERE i.actividad.id IN :ids"
            ).setParameter("ids", idsPredeterminadas).executeUpdate();

            em.createQuery(
                    "DELETE FROM DenunciaActividadDTO d WHERE d.actividad.id IN :ids"
            ).setParameter("ids", idsPredeterminadas).executeUpdate();

            em.createQuery(
                    "DELETE FROM ActividadDTO a WHERE a.id IN :ids"
            ).setParameter("ids", idsPredeterminadas).executeUpdate();

            tx.commit();

            System.out.println("✅ Eliminadas " + idsPredeterminadas.size() + " actividades predeterminadas");

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    /**
     * Elimina una actividad por id, incluyendo relaciones dependientes.
     *
     * @param id id de la actividad
     */
    public void eliminarPorId(Long id) {
        if (id == null) return;

        EntityManager em = ConexionDB.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            em.createQuery("DELETE FROM DenunciaActividadDTO d WHERE d.actividad.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            em.createQuery("DELETE FROM InscripcionDTO i WHERE i.actividad.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            ActividadDTO act = em.find(ActividadDTO.class, id);
            if (act != null) em.remove(act);

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    /**
     * Incrementa el contador de denuncias de una actividad con bloqueo pesimista.
     *
     * @param idActividad id de la actividad
     */
    public void incrementarDenuncias(Long idActividad) {
        if (idActividad == null) return;

        EntityManager em = ConexionDB.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            ActividadDTO act = em.find(ActividadDTO.class, idActividad, LockModeType.PESSIMISTIC_WRITE);
            if (act != null) {
                act.setNumDenuncias(act.getNumDenuncias() + 1);
                em.merge(act);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    /**
     * Método mantenido por compatibilidad (no mantiene EntityManager abierto).
     */
    public void cerrar() {
        // NO-OP
    }

    // ============================================================
    //   Denuncia persistente (compatibilidad)
    // ============================================================

    /**
     * Comprueba si un usuario ya denunció una actividad.
     */
    public boolean usuarioYaDenuncio(long idUsuario, long idActividad) {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            TypedQuery<Long> q = em.createQuery(
                    "SELECT COUNT(d.id) FROM DenunciaActividadDTO d " +
                            "WHERE d.usuario.id = :u AND d.actividad.id = :a",
                    Long.class
            );
            q.setParameter("u", idUsuario);
            q.setParameter("a", idActividad);
            Long c = q.getSingleResult();
            return c != null && c > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Registra una denuncia si no existe y actualiza el contador de la actividad.
     */
    public void denunciarActividad(long idUsuario, long idActividad) {
        EntityManager em = ConexionDB.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Long yaExiste = em.createQuery(
                            "SELECT COUNT(d.id) FROM DenunciaActividadDTO d " +
                                    "WHERE d.usuario.id = :u AND d.actividad.id = :a",
                            Long.class
                    )
                    .setParameter("u", idUsuario)
                    .setParameter("a", idActividad)
                    .getSingleResult();

            if (yaExiste != null && yaExiste > 0) {
                tx.commit();
                return;
            }

            ActividadDTO act = em.find(ActividadDTO.class, idActividad, LockModeType.PESSIMISTIC_WRITE);
            if (act == null) {
                tx.rollback();
                return;
            }

            DenunciaActividadDTO denuncia = new DenunciaActividadDTO();
            denuncia.setUsuario(em.getReference(com.planify.planifyplus.dto.UsuarioDTO.class, idUsuario));
            denuncia.setActividad(act);
            denuncia.setFechaDenuncia(LocalDateTime.now());
            em.persist(denuncia);

            act.setNumDenuncias(act.getNumDenuncias() + 1);
            em.merge(act);

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
