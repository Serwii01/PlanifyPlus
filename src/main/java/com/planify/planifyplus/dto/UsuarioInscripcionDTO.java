package com.planify.planifyplus.dto;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Entidad auxiliar que representa una inscripción de usuario.
 * Se utiliza para almacenar información básica sobre el momento
 * en el que un usuario se inscribe en una actividad.
 */
@Data
@Entity
@Table(name = "usuariosInscripciones")
public class UsuarioInscripcionDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * Identificador de la inscripción asociada.
     */
    @Column(name = "inscripcionID", nullable = false)
    private long inscripcion_id;

    /**
     * Fecha y hora en la que se realizó la inscripción.
     */
    @Column(name = "inscritoEn", nullable = false)
    private Timestamp inscritoEn;
}
