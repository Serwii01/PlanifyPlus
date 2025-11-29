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

    // Todas (por si la usas en algún sitio)
    public List<ActividadDTO> obtenerTodas() {
        return em.createQuery(
                "SELECT a FROM ActividadDTO a ORDER BY a.fechaHoraInicio ASC",
                ActividadDTO.class
        ).getResultList();
    }

    // SOLO actividades predeterminadas (las del service)
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

    // Cuenta TODAS (si la necesitáis)
    public long contar() {
        return em.createQuery("SELECT COUNT(a) FROM ActividadDTO a", Long.class)
                .getSingleResult();
    }

    // >>> NUEVO: cuenta solo las predeterminadas
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
}
