package com.aeroflow.model;

import com.aeroflow.model.enums.FlightStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import jakarta.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Flight {

    @Id
    private String flightNumber;

    private LocalDateTime scheduledDeparture;
    private LocalDateTime actualDeparture;
    private LocalDateTime scheduledArrival;
    private LocalDateTime actualArrival;
    private String gate;
    private FlightStatus status;
    private Double operatingCostPerMinute;

    @ManyToOne
    @JoinColumn(name = "origin_code")
    private Airport origin;

    @ManyToOne
    @JoinColumn(name = "destination_code")
    private Airport destination;
}