package com.planify.planifyplus.dto;
import lombok.Data;
import jakarta.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "usuariosInscripciones")
public class UsuarioInscripcionDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="inscripcionID")
    private long inscripcion_id;

    @Column(name="inscritoEn")
    private Timestamp inscritoEn;
}
