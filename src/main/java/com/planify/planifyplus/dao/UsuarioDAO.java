package com.planify.planifyplus.dao;

import com.planify.planifyplus.conexion.ConexionDB;
import com.planify.planifyplus.dto.UsuarioDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class UsuarioDAO {

    /** Comprueba si existe ya un usuario con ese email (case-insensitive). */
    public boolean existeEmail(String email) {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            TypedQuery<Long> q = em.createQuery(
                    "SELECT COUNT(u.id) FROM UsuarioDTO u WHERE LOWER(u.email) = LOWER(:e)",
                    Long.class
            );
            q.setParameter("e", email);
            return q.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    /** Inserta el usuario en BD (asume que ya has validado reglas en el controller). */
    public void crear(UsuarioDTO usuario) {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(usuario);
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    /*
    Juanka te dejo esto comentado para que tu pongas aquí tus métodos del login
    */
}

