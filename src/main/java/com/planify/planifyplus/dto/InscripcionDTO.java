package com.planify.planifyplus.dto;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad que representa la inscripción de un usuario en una actividad.
 */
@Data
@Entity
@Table(
        name = "inscripciones",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"usuario_id", "actividad_id"})
        }
)
public class InscripcionDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario inscrito en la actividad.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioDTO usuario;

    /**
     * Actividad en la que se ha inscrito el usuario.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id", nullable = false)
    private ActividadDTO actividad;

    /**
     * Fecha de creación de la inscripción.
     */
    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;
}
