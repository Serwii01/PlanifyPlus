package com.planify.planifyplus.dto;
import lombok.Data;
import jakarta.persistence.*;
import java.sql.Timestamp;

@Data
<<<<<<< HEAD
@Entity
@Table(name = "usuariosInscripciones")
=======

@Table(name = "usuarioInscripciones")
>>>>>>> ivan
public class UsuarioInscripcionDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="inscripcionID")
    private long inscripcion_id;

    @Column(name="inscritoEn")
    private Timestamp inscritoEn;
}
