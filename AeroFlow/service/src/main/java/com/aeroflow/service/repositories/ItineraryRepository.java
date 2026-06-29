package com.aeroflow.service.repositories;

import com.aeroflow.model.Itinerary;
import com.aeroflow.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItineraryRepository extends JpaRepository<Itinerary, String> {
    Itinerary findByPassenger(Passenger p);
}