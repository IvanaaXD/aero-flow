package com.aeroflow.service.controllers;

import com.aeroflow.model.*;
import com.aeroflow.model.enums.*;
import com.aeroflow.model.events.*;
import com.aeroflow.service.dto.BaggageAlertDTO;
import com.aeroflow.service.dto.ScenarioRequestDTO;
import com.aeroflow.service.repositories.PassengerRepository;
import com.aeroflow.service.services.AnalyticsService;
import com.aeroflow.service.services.DataInitializationService;
import com.aeroflow.service.services.DroolsEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/aeroflow")
public class AeroFlowController {

    private final DroolsEngineService droolsEngine;
    private final AnalyticsService analyticsService;
    private final DataInitializationService dataInitService;
    private final PassengerRepository passengerRepo;

    @Autowired
    public AeroFlowController(DroolsEngineService droolsEngine, AnalyticsService analyticsService, DataInitializationService dataInitializationService, PassengerRepository passengerRepo) {
        this.droolsEngine = droolsEngine;
        this.analyticsService = analyticsService;
        this.dataInitService = dataInitializationService;
        this.passengerRepo = passengerRepo;
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

            delayEvent.setNewEstimatedTime(LocalDateTime.now().plusMinutes(45));
            droolsEngine.insertEvent(delayEvent);
        }

        SecurityQueueEvent sqEvent = new SecurityQueueEvent();
        sqEvent.setTimestamp(new Date());
        sqEvent.setWaitTimeIncrease(250.0);
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

    @GetMapping("/analytics/baggage-alerts")
    public ResponseEntity<List<BaggageAlertDTO>> getBaggageAlerts() {
        List<BaggageAlertDTO> alerts = new ArrayList<>();
        for (Object obj : droolsEngine.getCepSession().getObjects()) {
            if (obj instanceof Baggage) {
                Baggage b = (Baggage) obj;
                
                if (b.getStatus() == BaggageStatus.SEPARATED || b.getStatus() == BaggageStatus.RE_ROUTED) {

                    BaggageAlertDTO dto = new BaggageAlertDTO();
                    dto.setBaggageTag(b.getBaggageTag());
                    dto.setStatus(b.getStatus().toString());
                    dto.setOwner(b.getOwner());
                    dto.setPassengerId(b.getOwner() != null ? b.getOwner().getPassengerId() : "N/A");
                    dto.setName(b.getOwner() != null ? b.getOwner().getName() : "Unknown");

                    if (b.getStatus() == BaggageStatus.RE_ROUTED && b.getAssignedFlight() != null) {
                        dto.setNextFlight(b.getAssignedFlight().getFlightNumber());
                    } else {
                        dto.setNextFlight("N/A");
                    }
                    alerts.add(dto);
                }
            }
        }
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/analytics/gate-anomalies")
    public ResponseEntity<List<PassengerScanEvent>> getGateAnomalies() {
        return ResponseEntity.ok(analyticsService.getTerminalAnomalies(droolsEngine.getCepSession()));
    }

    @PostMapping("/events/simulate-fault")
    public ResponseEntity<String> simulateFault(@RequestBody String party) {
        Fault fault = new Fault();
        fault.setParty("Airline_Fault");
        droolsEngine.insertFact(fault);

        return ResponseEntity.ok("Kvar avio-kompanije evidentiran.");
    }

    @GetMapping("/analytics/compensations")
    public ResponseEntity<List<Compensation>> getCompensations() {
        List<Compensation> compensations = new ArrayList<>();
        for (Object obj : droolsEngine.getCepSession().getObjects()) {
            if (obj instanceof Compensation) {
                compensations.add((Compensation) obj);
            }
        }
        return ResponseEntity.ok(compensations);
    }

    @PostMapping("/setup/day")
    public ResponseEntity<String> setupDay(@RequestBody ScenarioRequestDTO req) {
        // Sada možeš koristiti req.getPassengerId() u servisu
        dataInitService.seedDayLayoverScenario(req.getPassengerId(), req.getFlightNumber());
        return ResponseEntity.ok("Scenario: Dnevno čekanje učitan za " + req.getPassengerId());
    }

    @PostMapping("/setup/night")
    public ResponseEntity<String> setupNight(@RequestBody ScenarioRequestDTO req) {
        dataInitService.seedOvernightScenario(req.getPassengerId(), req.getFlightNumber());
        return ResponseEntity.ok("Scenario: Noćenje učitano za " + req.getPassengerId());
    }

    @PostMapping("/setup/group")
    public ResponseEntity<String> setupGroup(@RequestBody ScenarioRequestDTO req) {
        dataInitService.seedGroupHoldScenario();
        return ResponseEntity.ok("Scenario: Grupni let (" + req.getGroupSize() + " putnika) učitan.");
    }

    @GetMapping("/analytics/smart-hold-flights")
    public ResponseEntity<List<Flight>> getSmartHoldFlights() {
        List<Flight> delayedFlights = new ArrayList<>();
        for (Object obj : droolsEngine.getCepSession().getObjects()) {
            if (obj instanceof Flight) {
                Flight f = (Flight) obj;
                if (f.getStatus() == FlightStatus.DELAYED && f.getFlightNumber().equals("JU501")) {
                    delayedFlights.add(f);
                }
            }
        }
        return ResponseEntity.ok(delayedFlights);
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