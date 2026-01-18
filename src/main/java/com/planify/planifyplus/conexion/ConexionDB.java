package com.planify.planifyplus.conexion;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Clase utilitaria para la gestión de la conexión JPA.
 */
public class ConexionDB {

    /**
     * EntityManagerFactory asociada a la unidad de persistencia.
     */
    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("planifyPU");

    /**
     * Devuelve un EntityManager para trabajar con la base de datos.
     *
     * @return EntityManager activo
     */
    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }

    /**
     * Cierra la factoría de EntityManager.
     */
    public static void close() {
        if (EMF != null && EMF.isOpen()) {
            EMF.close();
        }
    }
}
