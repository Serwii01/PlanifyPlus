package com.planify.planifyplus.dto;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="inscripciones")
public class IncripcionDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


}
