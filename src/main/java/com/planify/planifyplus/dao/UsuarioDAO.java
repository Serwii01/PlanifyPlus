package com.planify.planifyplus.dao;

import com.planify.planifyplus.conexion.ConexionDB;
import com.planify.planifyplus.dto.UsuarioDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class UsuarioDAO {


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


    public UsuarioDTO buscarPorEmail(String email) {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            TypedQuery<UsuarioDTO> q = em.createQuery(
                    "SELECT u FROM UsuarioDTO u WHERE LOWER(u.email) = LOWER(:e)",
                    UsuarioDTO.class
            );
            q.setParameter("e", email);

            try {
                return q.getSingleResult();
            } catch (NoResultException ex) {
                return null; // no existe
            }

        } finally {
            em.close();
        }
    }

    /**
     * Valida las credenciales: devuelve el usuario si email+contraseña son correctos.
     */
    public UsuarioDTO validarLogin(String email, String contrasena) {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            TypedQuery<UsuarioDTO> q = em.createQuery(
                    "SELECT u FROM UsuarioDTO u " +
                            "WHERE LOWER(u.email) = LOWER(:e) AND u.contrasena = :c",
                    UsuarioDTO.class
            );
            q.setParameter("e", email);
            q.setParameter("c", contrasena);

            try {
                return q.getSingleResult();  // credenciales válidas
            } catch (NoResultException ex) {
                return null; // incorrecto
            }

        } finally {
            em.close();
        }
    }
}