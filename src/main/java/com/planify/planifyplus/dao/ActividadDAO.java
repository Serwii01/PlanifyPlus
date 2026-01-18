package com.planify.planifyplus.dao;

import com.planify.planifyplus.conexion.ConexionDB;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.DenunciaActividadDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

public class ActividadDAO {

    // ============================================================
    //   CONSULTAS
    // ============================================================

    public List<ActividadDTO> obtenerTodas() {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM ActividadDTO a ORDER BY a.fechaHoraInicio ASC",
                    ActividadDTO.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<ActividadDTO> obtenerPredeterminadas() {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM ActividadDTO a " +
                            "WHERE a.predeterminada = true " +
                            "ORDER BY a.fechaHoraInicio ASC",
                    ActividadDTO.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<ActividadDTO> obtenerNoPredeterminadas() {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM ActividadDTO a " +
                            "WHERE a.predeterminada = false " +
                            "ORDER BY a.fechaHoraInicio ASC",
                    ActividadDTO.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public List<ActividadDTO> obtenerCreadasPorUsuario(long idUsuario) {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT a FROM ActividadDTO a " +
                                    "WHERE a.predeterminada = false " +
                                    "AND a.creador.id = :idUsuario " +
                                    "ORDER BY a.fechaHoraInicio ASC",
                            ActividadDTO.class
                    )
                    .setParameter("idUsuario", idUsuario)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<ActividadDTO> obtenerDenunciadasOrdenadas() {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM ActividadDTO a " +
                            "WHERE a.numDenuncias > 0 " +
                            "ORDER BY a.numDenuncias DESC, a.fechaHoraInicio ASC",
                    ActividadDTO.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public long contar() {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            Long c = em.createQuery("SELECT COUNT(a) FROM ActividadDTO a", Long.class).getSingleResult();
            return c != null ? c : 0L;
        } finally {
            em.close();
        }
    }

    public long contarPredeterminadas() {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            Long c = em.createQuery(
                    "SELECT COUNT(a) FROM ActividadDTO a WHERE a.predeterminada = true",
                    Long.class
            ).getSingleResult();
            return c != null ? c : 0L;
        } finally {
            em.close();
        }
    }

    // ============================================================
    //   ESCRITURAS
    // ============================================================

    public void guardar(ActividadDTO actividad) {
        EntityManager em = ConexionDB.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            if (actividad.getId() == null) em.persist(actividad);
            else em.merge(actividad);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void eliminarTodasPredeterminadas() {
        EntityManager em = ConexionDB.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            List<Long> idsPredeterminadas = em.createQuery(
                    "SELECT a.id FROM ActividadDTO a WHERE a.predeterminada = true",
                    Long.class
            ).getResultList();

            // Si no hay actividades predeterminadas, no hacer nada
            if (idsPredeterminadas.isEmpty()) {
                tx.commit();
                return;
            }

            // 1. Eliminar inscripciones usando los IDs
            em.createQuery(
                    "DELETE FROM InscripcionDTO i WHERE i.actividad.id IN :ids"
            ).setParameter("ids", idsPredeterminadas).executeUpdate();

            // 2. Eliminar denuncias usando los IDs
            em.createQuery(
                    "DELETE FROM DenunciaActividadDTO d WHERE d.actividad.id IN :ids"
            ).setParameter("ids", idsPredeterminadas).executeUpdate();

            // 3. Eliminar las actividades
            em.createQuery(
                    "DELETE FROM ActividadDTO a WHERE a.id IN :ids"
            ).setParameter("ids", idsPredeterminadas).executeUpdate();

            tx.commit();

            System.out.println("✅ Eliminadas " + idsPredeterminadas.size() + " actividades predeterminadas");

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }




    public void eliminarPorId(Long id) {
        if (id == null) return;

        EntityManager em = ConexionDB.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // 1) Borrar denuncias asociadas
            em.createQuery("DELETE FROM DenunciaActividadDTO d WHERE d.actividad.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            // 2) Borrar inscripciones asociadas
            em.createQuery("DELETE FROM InscripcionDTO i WHERE i.actividad.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            // 3) Borrar actividad
            ActividadDTO act = em.find(ActividadDTO.class, id);
            if (act != null) em.remove(act);

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void incrementarDenuncias(Long idActividad) {
        if (idActividad == null) return;

        EntityManager em = ConexionDB.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            ActividadDTO act = em.find(ActividadDTO.class, idActividad, LockModeType.PESSIMISTIC_WRITE);
            if (act != null) {
                act.setNumDenuncias(act.getNumDenuncias() + 1);
                em.merge(act);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // Mantengo el método por compatibilidad con lo que tengáis llamado
    public void cerrar() {
        // NO-OP (ya no mantenemos EM abierto)
    }

    // ============================================================
    //   Denuncia persistente (por si lo sigues usando)
    // ============================================================

    public boolean usuarioYaDenuncio(long idUsuario, long idActividad) {
        EntityManager em = ConexionDB.getEntityManager();
        try {
            TypedQuery<Long> q = em.createQuery(
                    "SELECT COUNT(d.id) FROM DenunciaActividadDTO d " +
                            "WHERE d.usuario.id = :u AND d.actividad.id = :a",
                    Long.class
            );
            q.setParameter("u", idUsuario);
            q.setParameter("a", idActividad);
            Long c = q.getSingleResult();
            return c != null && c > 0;
        } finally {
            em.close();
        }
    }

    public void denunciarActividad(long idUsuario, long idActividad) {
        EntityManager em = ConexionDB.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Long yaExiste = em.createQuery(
                            "SELECT COUNT(d.id) FROM DenunciaActividadDTO d " +
                                    "WHERE d.usuario.id = :u AND d.actividad.id = :a",
                            Long.class
                    )
                    .setParameter("u", idUsuario)
                    .setParameter("a", idActividad)
                    .getSingleResult();

            if (yaExiste != null && yaExiste > 0) {
                tx.commit();
                return;
            }

            ActividadDTO act = em.find(ActividadDTO.class, idActividad, LockModeType.PESSIMISTIC_WRITE);
            if (act == null) {
                tx.rollback();
                return;
            }

            DenunciaActividadDTO denuncia = new DenunciaActividadDTO();
            denuncia.setUsuario(em.getReference(com.planify.planifyplus.dto.UsuarioDTO.class, idUsuario));
            denuncia.setActividad(act);
            denuncia.setFechaDenuncia(LocalDateTime.now());
            em.persist(denuncia);

            act.setNumDenuncias(act.getNumDenuncias() + 1);
            em.merge(act);

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
