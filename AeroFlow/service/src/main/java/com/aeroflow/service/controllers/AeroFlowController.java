package com.aeroflow.service.controllers;

import com.aeroflow.model.*;
import com.aeroflow.model.enums.*;
import com.aeroflow.model.events.*;
import com.aeroflow.service.services.AnalyticsService;
import com.aeroflow.service.services.DroolsEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/aeroflow")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AeroFlowController {

    private final DroolsEngineService droolsEngine;
    private final AnalyticsService analyticsService;

    @Autowired
    public AeroFlowController(DroolsEngineService droolsEngine, AnalyticsService analyticsService) {
        this.droolsEngine = droolsEngine;
        this.analyticsService = analyticsService;
    }

    // ========================================================================
    // 0. SETUP: Inteligentno namještanje radne memorije za okidanje svih pravila
    // ========================================================================
    @PostMapping("/setup")
    public ResponseEntity<String> setupTestData() {
        LocalDateTime now = LocalDateTime.now();
        Airport fra = new Airport("FRA", "Frankfurt", null);
        Airport beg = new Airport("BEG", "Belgrade", null);

        // --- ZA PRAVILO 1 (Rebooking - MCT ugrožen) ---
        Flight lh123 = new Flight("LH123", beg, fra, now.minusHours(2), null, now, null, "GATE_A1", FlightStatus.SCHEDULED, 50.0);
        Flight lh456 = new Flight("LH456", fra, beg, now.plusMinutes(40), null, now.plusHours(7), null, "GATE_B2", FlightStatus.SCHEDULED, 50.0);
        Passenger p1 = new Passenger("P-1000", "Marko Markovic", PassengerStatus.REGULAR);
        Itinerary it1 = new Itinerary("ITIN-1", p1, List.of(lh123, lh456), "FRA");

        droolsEngine.insertFact(lh123);
        droolsEngine.insertFact(lh456);
        droolsEngine.insertFact(p1);
        droolsEngine.insertFact(it1);

        // --- ZA PRAVILO 3 (Odvajanje prtljaga) ---
        Passenger p2 = new Passenger("P-1001", "Ana Anic", PassengerStatus.REGULAR);
        Baggage b1 = new Baggage("TAG-1001", p2, lh456, BaggageStatus.CHECKED_IN);

        droolsEngine.insertFact(p2);
        droolsEngine.insertFact(b1);

        // --- ZA PRAVILO 4 (Ekonomska optimizacija / Smart Hold) ---
        // JU500 stiže, a JU501 polijeće za samo 5 minuta.
        Flight ju500 = new Flight("JU500", fra, beg, now.minusHours(1), null, now, null, "GATE_C1", FlightStatus.SCHEDULED, 50.0);
        Flight ju501 = new Flight("JU501", beg, fra, now.plusMinutes(5), null, now.plusHours(1), null, "GATE_C2", FlightStatus.SCHEDULED, 50.0);

        droolsEngine.insertFact(ju500);
        droolsEngine.insertFact(ju501);

        // Generišemo 16 putnika da bismo zadovoljili uslov (intValue > 15) u DRL-u
        for (int i = 0; i < 16; i++) {
            Passenger groupPass = new Passenger("GRP-" + i, "Turista " + i, PassengerStatus.REGULAR);
            Itinerary groupItin = new Itinerary("ITIN-GRP-" + i, groupPass, List.of(ju500, ju501), "BEG");
            droolsEngine.insertFact(groupPass);
            droolsEngine.insertFact(groupItin);
        }

        // --- ZA PRAVILO 5 (Promjena Gejta) ---
        // Let OS789 polijeće za 30 minuta (manje od 35m kritičnog vremena u pravilu)
        Flight os789 = new Flight("OS789", beg, fra, now.plusMinutes(30), null, now.plusHours(2), null, "TERMINAL_2_G1", FlightStatus.SCHEDULED, 50.0);
        Passenger p3 = new Passenger("P-2002", "Jovan Jovanovic", PassengerStatus.REGULAR);
        Itinerary it3 = new Itinerary("ITIN-3", p3, List.of(os789), "BEG");

        droolsEngine.insertFact(os789);
        droolsEngine.insertFact(p3);
        droolsEngine.insertFact(it3);

        return ResponseEntity.ok("Sistem uspješno inicijalizovan. Spreman za simulacijuCEP događaja.");
    }

    // ========================================================================
    // PRAVILO 1 & 4: Prijava kašnjenja leta (FlightUpdateEvent)
    // ========================================================================
    @PostMapping("/events/flight-update")
    public ResponseEntity<String> reportFlightDelay(@RequestBody FlightUpdateRequestDTO request) {
        FlightUpdateEvent delayEvent = new FlightUpdateEvent();
        delayEvent.setTimestamp(new Date());
        delayEvent.setFlightNumber(request.getFlightNumber());
        delayEvent.setStatus(FlightStatus.valueOf(request.getStatus()));
        delayEvent.setNewEstimatedTime(LocalDateTime.now().plusMinutes(request.getDelayMinutes()));

        droolsEngine.insertEvent(delayEvent);
        return ResponseEntity.ok("Događaj o ažuriranju leta " + request.getFlightNumber() + " poslat.");
    }

    // ========================================================================
    // PRAVILO 2: Simulacija zagušenja aerodroma (Bottleneck)
    // ========================================================================
    @PostMapping("/events/simulate-bottleneck")
    public ResponseEntity<String> simulateBottleneck() {
        // Generišemo 11 brzih događaja kašnjenja za različite fiktivne letove
        for (int i = 0; i < 11; i++) {
            FlightUpdateEvent delayEvent = new FlightUpdateEvent();
            delayEvent.setTimestamp(new Date());
            delayEvent.setFlightNumber("FAKE-" + i);
            delayEvent.setStatus(FlightStatus.DELAYED);
            droolsEngine.insertEvent(delayEvent);
        }

        // Dodajemo događaj naglog rasta gužve na sigurnosnoj provjeri
        SecurityQueueEvent sqEvent = new SecurityQueueEvent();
        sqEvent.setTimestamp(new Date());
        sqEvent.setWaitTimeIncrease(250.0); // Preko praga od 200
        droolsEngine.insertEvent(sqEvent);

        return ResponseEntity.ok("Kaskadna kriza (Bottleneck) uspješno simulirana!");
    }

    // ========================================================================
    // PRAVILO 3: Prijava skeniranja putnika na gejtu (PassengerScanEvent)
    // ========================================================================
    @PostMapping("/events/passenger-scan")
    public ResponseEntity<String> reportPassengerScan(@RequestBody PassengerScanRequestDTO request) {
        PassengerScanEvent scanEvent = new PassengerScanEvent();
        scanEvent.setTimestamp(new Date());
        scanEvent.setPassengerId(request.getPassengerId());
        scanEvent.setLocation(request.getLocation());

        droolsEngine.insertEvent(scanEvent);
        return ResponseEntity.ok("Putnik " + request.getPassengerId() + " skeniran na " + request.getLocation());
    }

    // ========================================================================
    // PRAVILO 5: Simulacija nagle promjene gejta
    // ========================================================================
    @PostMapping("/events/gate-change-anomaly")
    public ResponseEntity<String> simulateGateChange(@RequestBody GateChangeRequestDTO request) {
        // 1. Događaj infrastrukturne promjene gejta
        GateChangedEvent gateEvent = new GateChangedEvent();
        gateEvent.setTimestamp(new Date());
        gateEvent.setFlightNumber(request.getFlightNumber());
        droolsEngine.insertEvent(gateEvent);

        // 2. Skeniranje putnika na pogrešnom terminalu
        PassengerScanEvent wrongScan = new PassengerScanEvent();
        wrongScan.setTimestamp(new Date());
        wrongScan.setPassengerId(request.getPassengerId());
        wrongScan.setLocation(request.getWrongTerminal());
        droolsEngine.insertEvent(wrongScan);

        return ResponseEntity.ok("Anomalija promjene gejta poslata za let " + request.getFlightNumber());
    }

    // ========================================================================
    // ANALITIKA: Endpoints za React Dashboard tabele
    // ========================================================================
    @GetMapping("/analytics/severely-delayed-passengers")
    public ResponseEntity<List<Passenger>> getSeverelyDelayedPassengers() {
        List<Passenger> endangered = analyticsService.getEndangeredTransitPassengers(droolsEngine.getCepSession());
        return ResponseEntity.ok(endangered);
    }

    @GetMapping("/analytics/gate-hold-savings")
    public ResponseEntity<Double> getGateHoldSavings() {
        double savings = analyticsService.calculateGateHoldSavings(droolsEngine.getCepSession());
        return ResponseEntity.ok(savings);
    }


    // ========================================================================
    // INNER DTO KLASE ZA PRIJEM PODATAKA IZ REACT-A
    // ========================================================================

    public static class FlightUpdateRequestDTO {
        private String flightNumber;
        private String status;
        private int delayMinutes;

        public String getFlightNumber() { return flightNumber; }
        public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getDelayMinutes() { return delayMinutes; }
        public void setDelayMinutes(int delayMinutes) { this.delayMinutes = delayMinutes; }
    }

    public static class PassengerScanRequestDTO {
        private String passengerId;
        private String location;

        public String getPassengerId() { return passengerId; }
        public void setPassengerId(String passengerId) { this.passengerId = passengerId; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    public static class GateChangeRequestDTO {
        private String flightNumber;
        private String passengerId;
        private String wrongTerminal;

        public String getFlightNumber() { return flightNumber; }
        public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
        public String getPassengerId() { return passengerId; }
        public void setPassengerId(String passengerId) { this.passengerId = passengerId; }
        public String getWrongTerminal() { return wrongTerminal; }
        public void setWrongTerminal(String wrongTerminal) { this.wrongTerminal = wrongTerminal; }
    }

    @GetMapping("/logs")
    public ResponseEntity<List<String>> getLogs() {
        return ResponseEntity.ok(droolsEngine.getBackendLogs());
    }
}