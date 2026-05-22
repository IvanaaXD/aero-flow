package com.aeroflow.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AirportStaff extends User {
    public AirportStaff() {
        super();
    }
}