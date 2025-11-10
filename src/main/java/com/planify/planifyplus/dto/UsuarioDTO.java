package com.planify.planifyplus.dto;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;


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
}
