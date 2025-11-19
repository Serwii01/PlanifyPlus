package com.planify.planifyplus.dto;
import lombok.Data;
import jakarta.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name="inscripciones")
public class IncripcionDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "actividad_id", nullable = false)
    private long actividad_id;

    @Column(name = "creadaEn")
    private Timestamp creadaEn;
}
