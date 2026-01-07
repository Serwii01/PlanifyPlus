package com.planify.planifyplus.dto;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name="usuarios")
public class UsuarioDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Column(length = 255, nullable = false)
    private String contrasena;

    @Column(name = "es_admin", nullable = false)
    private boolean esAdmin;

    @Column(length = 100)
    private String ciudad;

    @Column(name="creado_en", nullable = false)
    private LocalDateTime creadoEn;

    // ================== NUEVO: DENUNCIAS (RELACIÓN) ==================

    /**
     * Aquí guardo las denuncias que ha hecho este usuario.
     * Lo necesito para que un usuario no pueda denunciar la misma actividad dos veces,
     * incluso aunque cierre y abra la app.
     */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DenunciaActividadDTO> denuncias = new ArrayList<>();
}
