package com.aeroflow.model;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class AirportStaff extends User {
    public AirportStaff() {
        super();
    }
}