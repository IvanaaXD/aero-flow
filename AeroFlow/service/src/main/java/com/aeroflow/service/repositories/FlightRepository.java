package com.aeroflow.service.repositories;

import com.aeroflow.model.Airport;
import com.aeroflow.model.Flight;
import com.aeroflow.model.enums.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, String> {
    // Pronalazi letove koji idu na istu destinaciju, poslije određenog vremena
    List<Flight> findByOriginAndDestinationAndScheduledDepartureAfterAndStatus(
            Airport origin,
            Airport destination,
            LocalDateTime time,
            FlightStatus status
    );
}