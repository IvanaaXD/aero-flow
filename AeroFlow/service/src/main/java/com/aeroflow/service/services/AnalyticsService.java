package com.aeroflow.service.services;

import com.aeroflow.model.*;
import com.aeroflow.model.events.PassengerScanEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsService {

    // 1. Ugroženi putnici u tranzitu (Koristi DRL: "GetEndangeredTransitPassengers")
    public List<Passenger> getEndangeredTransitPassengers(KieSession session) {
        List<Passenger> passengers = new ArrayList<>();
        QueryResults results = session.getQueryResults("GetEndangeredTransitPassengers");

        for (QueryResultsRow row : results) {
            // U DRL-u smo bindovali "$passenger"
            Passenger p = (Passenger) row.get("$passenger");
            passengers.add(p);
        }
        return passengers;
    }

    // 2. Analiza hronično problematičnih ruta
    public boolean isRouteChronicallyProblematic(KieSession session, String destinationCode) {
        // U DRL-u smo bindovali "$disruption"
        QueryResults results = session.getQueryResults("GetSevereDelaysByDestination", destinationCode);
        return results.size() > 5;
    }

    // 3. Finansijski uticaj kaskadnih grešaka
    public double calculateFinancialImpact(KieSession session, String faultParty) {
        double totalCost = 0.0;
        QueryResults results = session.getQueryResults("GetCompensationsByFaultParty", faultParty);

        for (QueryResultsRow row : results) {
            // U DRL-u smo bindovali "$comp"
            Compensation comp = (Compensation) row.get("$comp");
            if (comp.getValue() != null) {
                totalCost += comp.getValue();
            }
        }
        return totalCost;
    }

    // 4. Kritične rute za transfer prtljaga
    public boolean hasBaggageTransferIssues(KieSession session, String flightNumber) {
        // U DRL-u smo bindovali "$baggage"
        QueryResults results = session.getQueryResults("GetSeparatedBaggageByFlight", flightNumber);
        return results.size() > 15;
    }

    // 5. Izvještaj o isplativosti zadržavanja letova
    public double calculateGateHoldSavings(KieSession session) {
        double totalSaved = 0.0;
        QueryResults results = session.getQueryResults("GetSmartHoldFlights");

        for (QueryResultsRow row : results) {
            // U DRL-u smo bindovali "$flight"
            Flight f = (Flight) row.get("$flight");
            double penalZaKasnjenje = f.getOperatingCostPerMinute() != null ? f.getOperatingCostPerMinute() * 15 : 0;
            totalSaved += (1500 - penalZaKasnjenje);
        }
        return totalSaved;
    }

    // 6. Analiza infrastrukturnih grešaka terminala
    public List<PassengerScanEvent> getTerminalAnomalies(KieSession session) {
        List<PassengerScanEvent> anomalies = new ArrayList<>();
        QueryResults results = session.getQueryResults("GetTerminalAnomalies");

        for (QueryResultsRow row : results) {
            // U DRL-u smo bindovali "$pScan"
            PassengerScanEvent pScan = (PassengerScanEvent) row.get("$pScan");
            anomalies.add(pScan);
        }
        return anomalies;
    }

    // 7. Efikasnost kriznog bafera
    public int getDisruptionsDuringWeatherEvent(KieSession session, String weatherCondition) {
        QueryResults results = session.getQueryResults("GetDisruptionsDuringWeatherEvent", weatherCondition);
        return results.size();
    }
}