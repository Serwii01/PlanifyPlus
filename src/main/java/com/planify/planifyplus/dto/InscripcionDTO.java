package com.planify.planifyplus.dto;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

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

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioDTO usuario;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id", nullable = false)
    private ActividadDTO actividad;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;
}
