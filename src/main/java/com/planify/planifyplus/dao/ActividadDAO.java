package com.planify.planifyplus.dao;

import com.planify.planifyplus.dto.ActividadDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.List;

public class ActividadDAO {

    private EntityManager em;

    public ActividadDAO() {
        em = Persistence.createEntityManagerFactory("planifyPU").createEntityManager();
    }

    // ============================================================
    //   MÉTODOS ORIGINALES (RESPETADOS)
    // ============================================================

    // Todas (por si la usas en algún sitio)
    public List<ActividadDTO> obtenerTodas() {
        return em.createQuery(
                "SELECT a FROM ActividadDTO a ORDER BY a.fechaHoraInicio ASC",
                ActividadDTO.class
        ).getResultList();
    }

    // SOLO actividades predeterminadas
    public List<ActividadDTO> obtenerPredeterminadas() {
        return em.createQuery(
                "SELECT a FROM ActividadDTO a " +
                        "WHERE a.predeterminada = true " +
                        "ORDER BY a.fechaHoraInicio ASC",
                ActividadDTO.class
        ).getResultList();
    }

    // SOLO actividades creadas por usuarios (no predeterminadas)
    public List<ActividadDTO> obtenerNoPredeterminadas() {
        return em.createQuery(
                "SELECT a FROM ActividadDTO a " +
                        "WHERE a.predeterminada = false " +
                        "ORDER BY a.fechaHoraInicio ASC",
                ActividadDTO.class
        ).getResultList();
    }

    public void guardar(ActividadDTO actividad) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (actividad.getId() == null) {
                em.persist(actividad);
            } else {
                em.merge(actividad);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
    }

    public void eliminarTodasPredeterminadas() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM ActividadDTO a WHERE a.predeterminada = true")
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
    }

    public long contar() {
        return em.createQuery("SELECT COUNT(a) FROM ActividadDTO a", Long.class)
                .getSingleResult();
    }

    public long contarPredeterminadas() {
        return em.createQuery(
                        "SELECT COUNT(a) FROM ActividadDTO a WHERE a.predeterminada = true",
                        Long.class
                )
                .getSingleResult();
    }

    public void cerrar() {
        if (em.isOpen()) {
            em.close();
        }
    }

    // ============================================================
    //               MÉTODOS NUEVOS
    // ============================================================

    /**
     * Elimina una actividad por su ID. Solo debe usarse por administradores.
     */
    public void eliminarPorId(Long id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ActividadDTO act = em.find(ActividadDTO.class, id);
            if (act != null) {
                em.remove(act);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Devuelve las actividades NO predeterminadas creadas por un usuario concreto.
     * Sirve para rellenar "Mis actividades creadas".
     */
    public List<ActividadDTO> obtenerCreadasPorUsuario(long idUsuario) {
        return em.createQuery(
                        "SELECT a FROM ActividadDTO a " +
                                "WHERE a.predeterminada = false " +
                                "AND a.creador.id = :idUsuario " +
                                "ORDER BY a.fechaHoraInicio ASC",
                        ActividadDTO.class
                )
                .setParameter("idUsuario", idUsuario)
                .getResultList();
    }

    // ================== NUEVO: DENUNCIAS ==================

    /**
     * Incremento en 1 el número de denuncias de una actividad.
     * Lo usaré cuando un usuario pulse "Denunciar actividad".
     */
    public void incrementarDenuncias(Long idActividad) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ActividadDTO act = em.find(ActividadDTO.class, idActividad);
            if (act != null) {
                int actuales = act.getNumDenuncias();
                act.setNumDenuncias(actuales + 1);
                em.merge(act);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Devuelve solo las actividades que tienen al menos 1 denuncia,
     * ordenadas de mayor a menor número de denuncias.
     * Esto lo usaré para rellenar "Actividades denunciadas" en la vista del admin.
     */
    public List<ActividadDTO> obtenerDenunciadasOrdenadas() {
        return em.createQuery(
                        "SELECT a FROM ActividadDTO a " +
                                "WHERE a.numDenuncias > 0 " +
                                "ORDER BY a.numDenuncias DESC, a.fechaHoraInicio ASC",
                        ActividadDTO.class
                )
                .getResultList();
    }
}




