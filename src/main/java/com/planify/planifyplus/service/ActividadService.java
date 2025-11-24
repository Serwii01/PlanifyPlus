package com.planify.planifyplus.service;

import com.planify.planifyplus.dao.ActividadDAO;
import com.planify.planifyplus.dto.ActividadDTO;
import com.planify.planifyplus.dto.TipoActividad;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ActividadService {

    private ActividadDAO actividadDAO = new ActividadDAO();

    public void inicializarActividadesPredeterminadas() {
        if (actividadDAO.contar() == 0) {
            crearActividadPredeterminada("Partido de fútbol", "Liga amistosa en campo grande. ¡Forma tu equipo!", TipoActividad.DEPORTIVA,
                    "Polideportivo San Isidro", "Sevilla", 37.3886, -5.9823, 22, 1);

            crearActividadPredeterminada("Clase de yoga", "Sesión de yoga relajante al atardecer", TipoActividad.DEPORTIVA,
                    "Parque de María Luisa", "Sevilla", 37.3687, -5.9877, 25, 2);

            crearActividadPredeterminada("Taller de cerámica", "Aprende a moldear y pintar cerámica", TipoActividad.TALLER,
                    "Centro Cultural Triana", "Sevilla", 37.3826, -6.0039, 15, 3);

            crearActividadPredeterminada("Cine fórum", "Proyección y debate de película independiente", TipoActividad.CULTURAL,
                    "Sala Avenida", "Sevilla", 37.3891, -5.9845, 40, 4);

            crearActividadPredeterminada("Ruta senderismo", "Excursión por la Sierra Norte", TipoActividad.DEPORTIVA,
                    "Inicio en Estación de tren", "Constantina", 37.8687, -5.6191, 18, 5);

            crearActividadPredeterminada("Curso de cocina italiana", "Elabora pasta y pizza desde cero", TipoActividad.TALLER,
                    "Aula Gastronómica", "Camas", 37.4107, -6.0338, 12, 6);

            crearActividadPredeterminada("Tarde de juegos de mesa", "Juegos clásicos y actuales", TipoActividad.CULTURAL,
                    "Biblioteca Pública", "Dos Hermanas", 37.2828, -5.9205, 20, 7);

            crearActividadPredeterminada("Clase de Zumba", "Ven a moverte al ritmo latino", TipoActividad.DEPORTIVA,
                    "Gimnasio Central", "Alcalá de Guadaíra", 37.3372, -5.8449, 30, 8);

            crearActividadPredeterminada("Taller de fotografía", "Descubre los secretos del retrato urbano", TipoActividad.TALLER,
                    "Casa de la Juventud", "Sevilla", 37.3925, -5.9840, 15, 9);

            crearActividadPredeterminada("Recital de poesía", "Lectura en vivo de poetas locales", TipoActividad.CULTURAL,
                    "Café Literario", "Sevilla", 37.3861, -5.9920, 35, 10);
        }
    }

    private void crearActividadPredeterminada(String titulo, String descripcion, TipoActividad tipo, String ubicacion, String ciudad, double latitud, double longitud, int aforo, int diasDesdeAhora) {
        ActividadDTO act = new ActividadDTO();
        act.setTitulo(titulo != null ? titulo : "Sin Título");
        act.setDescripcion(descripcion != null ? descripcion : "Sin descripción");
        act.setTipo(tipo != null ? tipo : TipoActividad.TALLER);
        act.setFechaHoraInicio(LocalDateTime.now().plusDays(diasDesdeAhora));
        act.setUbicacion(ubicacion != null ? ubicacion : "Ubicación desconocida");
        act.setCiudad(ciudad != null ? ciudad : "Ciudad ejemplo");
        act.setLatitud(BigDecimal.valueOf(latitud));
        act.setLongitud(BigDecimal.valueOf(longitud));
        act.setAforo(aforo > 0 ? aforo : 10);
        act.setCreadoEn(LocalDateTime.now());
        act.setPredeterminada(true);
        actividadDAO.guardar(act);
    }

    public void limpiarActividadesPredeterminadas() {
        actividadDAO.eliminarTodasPredeterminadas();
    }

    public void cerrar() {
        actividadDAO.cerrar();
    }
}
