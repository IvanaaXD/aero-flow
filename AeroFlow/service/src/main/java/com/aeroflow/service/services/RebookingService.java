package com.aeroflow.service.services;

import com.aeroflow.model.Flight;
import com.aeroflow.model.Itinerary;
import com.aeroflow.model.Passenger;
import com.aeroflow.model.enums.FlightStatus;
import com.aeroflow.model.enums.PassengerStatus;
import com.aeroflow.service.repositories.FlightRepository;
import com.aeroflow.service.repositories.ItineraryRepository;
import com.aeroflow.service.repositories.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RebookingService {
    @Autowired
    private FlightRepository flightRepo;
    @Autowired private ItineraryRepository itineraryRepo;
    @Autowired private PassengerRepository passengerRepo;

    @Transactional
    public void rebookPassenger(Passenger p) {

        Itinerary itinerary = itineraryRepo.findByPassenger(p);

        Flight missedFlight = itinerary.getFlights().get(1);

        List<Flight> alternatives = flightRepo.findByOriginAndDestinationAndScheduledDepartureAfterAndStatus(
                missedFlight.getOrigin(),
                missedFlight.getDestination(),
                LocalDateTime.now().plusHours(1),
                FlightStatus.SCHEDULED
        );

        if (!alternatives.isEmpty()) {
            Flight newFlight = alternatives.get(0);

            List<Flight> updatedFlights = new ArrayList<>(itinerary.getFlights());
            updatedFlights.set(1, newFlight);
            itinerary.setFlights(updatedFlights);
            itineraryRepo.save(itinerary);

            p.setCurrentStatus(PassengerStatus.REBOOKED);
            passengerRepo.save(p);

            System.out.println(">>> SMS POSLAT: Poštovani " + p.getName() +
                    ", vaš let je prebukiran na " + newFlight.getFlightNumber() +
                    ". Novi polazak: " + newFlight.getScheduledDeparture());
        }
    }
}