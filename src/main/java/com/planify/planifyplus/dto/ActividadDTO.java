package com.planify.planifyplus.dto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // Dirección formateada devuelta por el geocoder (ej. "Palacio de San Telmo, Sevilla, España")
    @Column(length = 255, nullable = false)
    private String ubicacion;

    // Ciudad extraída del resultado del geocoder (opcional)
    @Column(length = 100)
    private String ciudad;

    // Coordenadas para Leaflet (6 decimales de precisión)
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

    // ================= NUEVO: CONTADOR DE DENUNCIAS =================

    /**
     * Número total de denuncias recibidas.
     * Se usa para ordenar las actividades denunciadas en el admin.
     */
    @Column(name = "num_denuncias", nullable = false)
    private int numDenuncias = 0;

    // ================= NUEVO: RELACIÓN CON DENUNCIAS =================

    /**
     * Denuncias asociadas a esta actividad.
     * Me sirve para saber qué usuarios la han denunciado
     * y evitar denuncias duplicadas.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DenunciaActividadDTO> denuncias = new ArrayList<>();
}
