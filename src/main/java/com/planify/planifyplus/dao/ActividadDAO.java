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

    public List<ActividadDTO> obtenerTodas() {
        return em.createQuery("SELECT a FROM ActividadDTO a ORDER BY a.fechaHoraInicio ASC", ActividadDTO.class).getResultList();
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

    // Método para comprobar si hay actividades existentes
    public long contar() {
        return em.createQuery("SELECT COUNT(a) FROM ActividadDTO a", Long.class)
                .getSingleResult();
    }

    public void cerrar() {
        if (em.isOpen()) {
            em.close();
        }
    }
}
