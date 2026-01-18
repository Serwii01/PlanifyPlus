package com.planify.planifyplus.dto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una actividad dentro de la aplicación.
 */
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

    /**
     * Dirección completa obtenida del geocoder.
     */
    @Column(length = 255, nullable = false)
    private String ubicacion;

    /**
     * Ciudad asociada a la actividad.
     */
    @Column(length = 100)
    private String ciudad;

    /**
     * Coordenadas geográficas para el mapa.
     */
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

    /**
     * Usuario que creó la actividad.
     * Puede ser null en actividades predeterminadas.
     */
    @ManyToOne
    @JoinColumn(name = "creado_por_id", nullable = true)
    private UsuarioDTO creador;

    /**
     * Contador de denuncias recibidas.
     */
    @Column(name = "num_denuncias", nullable = false)
    private int numDenuncias = 0;

    /**
     * Lista de denuncias asociadas a la actividad.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            mappedBy = "actividad",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<DenunciaActividadDTO> denuncias = new ArrayList<>();
}
