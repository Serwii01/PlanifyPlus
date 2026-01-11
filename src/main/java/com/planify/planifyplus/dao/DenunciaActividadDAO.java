package com.planify.planifyplus.dao;

import com.planify.planifyplus.conexion.ConexionDB;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.DenunciaActividadDTO;
import com.planify.planifyplus.dto.UsuarioDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;

public class DenunciaActividadDAO {

    // Compruebo si ya existe una denuncia para (usuario, actividad)
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

    // Creo la denuncia en la tabla intermedia
    public void crearDenuncia(long idUsuario, long idActividad) {
        EntityManager em = ConexionDB.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Si ya existe, no hago nada
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

    // Compatibilidad: antes cerraba el EM fijo; ahora no hace falta.
    public void cerrar() {
        // NO-OP
    }
}
