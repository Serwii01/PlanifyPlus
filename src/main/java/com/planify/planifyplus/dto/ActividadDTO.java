package com.planify.planifyplus.dto;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "actividades")
public class ActividadDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150, nullable = false)
    private String titulo;

    @Lob
    @Column(nullable = false)
    private String descripcion;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoActividad tipo;

    @Column(length = 150, nullable = false)
    private String ubicacion;

    @Column(length = 100)
    private String ciudad;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitud;

    @Column(nullable = false)
    private Integer aforo;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "predeterminada", nullable = false)
    private boolean predeterminada = false;

    // ================= NUEVO: USUARIO CREADOR =================

    /**
     * Usuario que ha creado esta actividad.
     * Es nullable para que las actividades predeterminadas del servicio
     * sigan funcionando aunque no tengan creador asociado.
     */
    @ManyToOne
    @JoinColumn(name = "creado_por_id", nullable = true)
    private UsuarioDTO creador;
}
