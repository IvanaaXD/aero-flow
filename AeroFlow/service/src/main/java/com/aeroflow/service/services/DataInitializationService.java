package com.aeroflow.service.services;

import com.aeroflow.model.*;
import com.aeroflow.model.enums.*;
import com.aeroflow.service.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DataInitializationService {
    private final AirportRepository airportRepo;
    private final FlightRepository flightRepo;
    private final PassengerRepository passengerRepo;
    private final ItineraryRepository itineraryRepo;
    private final BaggageRepository baggageRepo;
    private final DroolsEngineService droolsEngine;

    public DataInitializationService(AirportRepository airportRepo, FlightRepository flightRepo, PassengerRepository passengerRepo, ItineraryRepository itineraryRepo, BaggageRepository baggageRepo, DroolsEngineService droolsEngine) {
        this.airportRepo = airportRepo;
        this.flightRepo = flightRepo;
        this.passengerRepo = passengerRepo;
        this.itineraryRepo = itineraryRepo;
        this.baggageRepo = baggageRepo;
        this.droolsEngine = droolsEngine;
    }

    public void clearAll() {
        baggageRepo.deleteAll();
        itineraryRepo.deleteAll();
        flightRepo.deleteAll();
        passengerRepo.deleteAll();
        airportRepo.deleteAll();

        // 2. OČISTI DROOLS
        droolsEngine.clearSession();

        droolsEngine.clearLogs();
        System.out.println("✅ Svi podaci i relacije su uspješno uneseni.");
    }
    @Transactional
    public void seedDayLayoverScenario(String passengerId, String flightNumber) {
        clearAll(); // Očisti sesiju prije svakog scenarija
        LocalDateTime now = LocalDateTime.now();
        Airport fra = airportRepo.save(new Airport("FRA", "Frankfurt", null));
        Airport beg = airportRepo.save(new Airport("BEG", "Belgrade", null));

        // Prvi let koristi proslijeđeni broj (flightNumber)
        Flight f1 = flightRepo.save(new Flight(flightNumber, now.minusHours(5), now.minusHours(2), now.minusHours(2), now.minusHours(2), "A1", FlightStatus.DELAYED, 50.0, beg, fra));
        // Drugi let je vezani let (pomoćni)
        Flight f2 = flightRepo.save(new Flight("LH-NEXT", now.plusHours(8), null, now.plusHours(8), null, "B2", FlightStatus.SCHEDULED, 50.0, fra, beg));

        Passenger p = passengerRepo.save(new Passenger(passengerId, "Test Putnik (" + passengerId + ")", PassengerStatus.SEVERELY_DELAYED));
        itineraryRepo.save(new Itinerary("ITIN-" + passengerId, p, List.of(f1, f2), "FRA"));

        droolsEngine.insertFact(f1);
        droolsEngine.insertFact(f2);
        droolsEngine.insertFact(p);

        System.out.println("✅ Scenario DAY učitan za putnika: " + passengerId);
    }

    @Transactional
    public void seedOvernightScenario(String passengerId, String flightNumber) {
        clearAll();
        LocalDateTime now = LocalDateTime.now();
        Airport fra = airportRepo.save(new Airport("FRA", "Frankfurt", null));
        Airport beg = airportRepo.save(new Airport("BEG", "Belgrade", null));

        // Prvi let je danas (kasni), drugi je sutra
        Flight f1 = flightRepo.save(new Flight(flightNumber, now.minusHours(2), now, now, now, "A1", FlightStatus.DELAYED, 50.0, beg, fra));
        Flight f2 = flightRepo.save(new Flight("LH-TOMORROW", now.plusDays(1), null, now.plusDays(1), null, "B2", FlightStatus.SCHEDULED, 50.0, fra, beg));

        Passenger p = passengerRepo.save(new Passenger(passengerId, "Test Putnik (" + passengerId + ")", PassengerStatus.SEVERELY_DELAYED));
        itineraryRepo.save(new Itinerary("ITIN-" + passengerId, p, List.of(f1, f2), "FRA"));

        droolsEngine.insertFact(f1);
        droolsEngine.insertFact(f2);
        droolsEngine.insertFact(p);

        System.out.println("✅ Scenario NIGHT učitan za putnika: " + passengerId);
    }

    @Transactional
    public void seedGroupHoldScenario() {
        clearAll();

        LocalDateTime now = LocalDateTime.now();

        // 1. Aerodromi
        Airport fra = airportRepo.save(new Airport("FRA", "Frankfurt", null));
        Airport beg = airportRepo.save(new Airport("BEG", "Belgrade", null));

        // 2. Letovi
        Flight lh123 = flightRepo.save(new Flight("LH123", now.minusHours(2), null, now, null, "GATE_A1", FlightStatus.SCHEDULED, 50.0, beg, fra));
        Flight lh456 = flightRepo.save(new Flight("LH456", now.plusMinutes(40), null, now.plusHours(7), null, "GATE_B2", FlightStatus.SCHEDULED, 50.0, fra, beg));
        Flight ju500 = flightRepo.save(new Flight("JU500", now.minusHours(1), null, now, null, "GATE_C1", FlightStatus.SCHEDULED, 50.0, fra, beg));
        Flight ju501 = flightRepo.save(new Flight("JU501", now.plusMinutes(5), null, now.plusHours(1), null, "GATE_C2", FlightStatus.SCHEDULED, 50.0, beg, fra));
        Flight os789 = flightRepo.save(new Flight("OS789", now.plusMinutes(30), null, now.plusHours(2), null, "TERMINAL_2_G1", FlightStatus.SCHEDULED, 50.0, beg, fra));
        Flight rescueFlight = flightRepo.save(new Flight("RES-999", now.plusHours(8), null, now.plusHours(10), null, "GATE_Z9", FlightStatus.SCHEDULED, 50.0, fra, beg));

        droolsEngine.insertFact(lh123); droolsEngine.insertFact(lh456);
        droolsEngine.insertFact(ju500); droolsEngine.insertFact(ju501);
        droolsEngine.insertFact(os789);
        droolsEngine.insertFact(rescueFlight);

        // 3. Putnici, Itinereri i Prtljag (SVE MORA BITI INSERT-OVANO)

        // P-1000
        Passenger p1 = passengerRepo.save(new Passenger("P-1000", "Marko Markovic", PassengerStatus.REGULAR));
        Itinerary itin1 = itineraryRepo.save(new Itinerary("ITIN-1", p1, List.of(lh123, lh456), "FRA"));
        droolsEngine.insertFact(p1);
        droolsEngine.insertFact(itin1); // <--- DODATO

        // P-1001
        Passenger p2 = passengerRepo.save(new Passenger("P-1001", "Ana Anic", PassengerStatus.REGULAR));
        Baggage bag1 = baggageRepo.save(new Baggage("TAG-1001", p2, lh456, BaggageStatus.CHECKED_IN));
        droolsEngine.insertFact(p2);
        droolsEngine.insertFact(bag1); // <--- DODATO

        // P-2002
        Passenger p3 = passengerRepo.save(new Passenger("P-2002", "Jovan Jovanovic", PassengerStatus.REGULAR));
        Itinerary itin3 = itineraryRepo.save(new Itinerary("ITIN-3", p3, List.of(os789), "BEG"));
        droolsEngine.insertFact(p3);
        droolsEngine.insertFact(itin3); // <--- DODATO

        // Grupni putnici
        for (int i = 0; i < 16; i++) {
            Passenger groupPass = passengerRepo.save(new Passenger("GRP-" + i, "Turista " + i, PassengerStatus.REGULAR));
            Itinerary groupItin = itineraryRepo.save(new Itinerary("ITIN-GRP-" + i, groupPass, List.of(ju500, ju501), "FRA"));
            droolsEngine.insertFact(groupPass);
            droolsEngine.insertFact(groupItin);
        }

        System.out.println("✅ Svi podaci i relacije su uspješno uneseni u Drools memoriju.");
    }
}