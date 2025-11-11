package com.planify.planifyplus.conexion;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class ConexionDB {
    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("planifyPU");

    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }

    public static void close() {
        if (EMF != null && EMF.isOpen()) EMF.close();
    }
}