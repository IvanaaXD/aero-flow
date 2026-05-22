package com.aeroflow.model;

import com.aeroflow.model.enums.BaggageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Baggage {
    private String baggageTag;
    private Passenger owner;
    private Flight assignedFlight;
    private BaggageStatus status;
}