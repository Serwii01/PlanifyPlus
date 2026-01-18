package com.planify.planifyplus.dto;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad que representa una denuncia de una actividad por un usuario.
 */
@Data
@Entity
@Table(
        name = "denuncias_actividades",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"usuario_id", "actividad_id"})
        }
)
public class DenunciaActividadDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario que realiza la denuncia.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioDTO usuario;

    /**
     * Actividad denunciada.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "actividad_id", nullable = false)
    private ActividadDTO actividad;

    /**
     * Fecha y hora en la que se registró la denuncia.
     */
    @Column(name = "fecha_denuncia", nullable = false)
    private LocalDateTime fechaDenuncia;
}
