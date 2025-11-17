package com.planify.planifyplus.dao;

import com.planify.planifyplus.dto.ActividadDTO;
import jakarta.persistence.EntityManager;
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
}
