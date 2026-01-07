package com.planify.planifyplus.dao;

import com.planify.planifyplus.dto.DenunciaActividadDTO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.UsuarioDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;

public class DenunciaActividadDAO {

    private final EntityManager em;

    public DenunciaActividadDAO() {
        em = Persistence.createEntityManagerFactory("planifyPU").createEntityManager();
    }

    // Compruebo si ya existe una denuncia para (usuario, actividad)
    public boolean existeDenuncia(long idUsuario, long idActividad) {
        TypedQuery<Long> q = em.createQuery(
                "SELECT COUNT(d.id) FROM DenunciaActividadDTO d " +
                        "WHERE d.usuario.id = :u AND d.actividad.id = :a",
                Long.class
        );
        q.setParameter("u", idUsuario);
        q.setParameter("a", idActividad);
        return q.getSingleResult() > 0;
    }

    // Creo la denuncia en la tabla intermedia
    public void crearDenuncia(long idUsuario, long idActividad) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // Si ya existe, no hago nada
            if (existeDenuncia(idUsuario, idActividad)) {
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
        }
    }

    public void cerrar() {
        if (em.isOpen()) {
            em.close();
        }
    }
}
