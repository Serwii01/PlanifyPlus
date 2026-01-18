package com.planify.planifyplus.dao;

import com.planify.planifyplus.conexion.ConexionDB;
import com.planify.planifyplus.dto.UsuarioDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

/**
 * DAO para operaciones de usuarios.
 */
public class UsuarioDAO {

    /**
     * Comprueba si existe un email en la base de datos.
     *
     * @param email correo a comprobar
     * @return true si ya está registrado
     */
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

    /**
     * Inserta un usuario en base de datos.
     *
     * @param usuario usuario a persistir
     */
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

    /**
     * Busca un usuario por email.
     *
     * @param email email a buscar
     * @return usuario encontrado o null si no existe
     */
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
                return null;
            }

        } finally {
            em.close();
        }
    }

    /**
     * Valida credenciales y devuelve el usuario si coinciden.
     *
     * @param email correo
     * @param contrasena contraseña
     * @return usuario o null si no coincide
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
                return q.getSingleResult();
            } catch (NoResultException ex) {
                return null;
            }

        } finally {
            em.close();
        }
    }

    /**
     * Obtiene un usuario por id.
     *
     * @param id id del usuario
     * @return usuario o null si no existe
     */
    public UsuarioDTO obtenerPorId(long id) {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            return em.find(UsuarioDTO.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza un usuario existente.
     *
     * @param usuario usuario con cambios
     */
    public void actualizar(UsuarioDTO usuario) {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(usuario);
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}
