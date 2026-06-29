package com.aeroflow.service.dto;

import lombok.Getter;
import lombok.Setter;
import com.aeroflow.model.Passenger;

@Setter
@Getter
public class BaggageAlertDTO {
    private String baggageTag;
    private String passengerId;
    private String name;
    private String status;
    private String nextFlight;
    private Passenger owner;
}