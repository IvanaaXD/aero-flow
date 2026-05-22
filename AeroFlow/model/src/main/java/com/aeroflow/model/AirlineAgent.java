package com.aeroflow.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AirlineAgent extends User {
    public AirlineAgent() {
        super();
    }
}