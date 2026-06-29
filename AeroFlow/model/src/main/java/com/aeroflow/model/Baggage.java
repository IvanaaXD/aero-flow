package com.aeroflow.model;

import com.aeroflow.model.enums.BaggageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Baggage {

    @Id
    private String baggageTag;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Passenger owner;

    @ManyToOne
    @JoinColumn(name = "flight_number")
    private Flight assignedFlight;

    @Enumerated(EnumType.STRING)
    private BaggageStatus status;
}