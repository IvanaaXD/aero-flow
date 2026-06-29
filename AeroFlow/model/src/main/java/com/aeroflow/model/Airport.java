package com.aeroflow.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Airport {

    @Id
    private String code;
    private String name;

    @Transient
    private Map<String, Integer> mctMatrix;
}