package com.planify.planifyplus.dto;

import com.planify.planifyplus.util.DistanciaUtil;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa a un usuario de la aplicación.
 */
@Data
@Entity
@Table(name = "usuarios")
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

    /**
     * Coordenadas asociadas a la ciudad del usuario.
     */
    @Column(nullable = false)
    private double latitud = 0.0;

    @Column(nullable = false)
    private double longitud = 0.0;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    /**
     * Denuncias realizadas por el usuario.
     */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DenunciaActividadDTO> denuncias = new ArrayList<>();

    /**
     * Asigna la ciudad y actualiza automáticamente las coordenadas.
     *
     * @param ciudad ciudad del usuario
     */
    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
        if (ciudad != null && !ciudad.isEmpty()) {
            double[] coords = DistanciaUtil.getCoordenadasCiudad(ciudad);
            this.latitud = coords[0];
            this.longitud = coords[1];
        }
    }
}
