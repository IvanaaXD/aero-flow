package com.aeroflow.service.services;

import com.aeroflow.model.events.Event;
import com.aeroflow.service.dto.ConnectionThresholdTemplateModel;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.ResourceType;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DroolsEngineService {

    private final KieContainer kieContainer;
    private KieSession cepSession;

    // Thread-safe lista za čuvanje logova
    private final List<String> backendLogs = Collections.synchronizedList(new ArrayList<>());

    public List<String> getBackendLogs() {
        return new ArrayList<>(backendLogs);
    }

    public void log(String message) {
        String entry = java.time.LocalTime.now().toString().substring(0, 8) + " " + message;
        backendLogs.add(entry);
        if (backendLogs.size() > 100) backendLogs.remove(0); // Održavamo buffer
        System.out.println(entry);
    }

    @Autowired
    public DroolsEngineService(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    @PostConstruct
    public void initializeSession() {
        log("==== Inicijalizacija AeroFlow Ujedinjenog Drools Engine-a ====");
        compileAndLoadTemplate();
    }

    public void compileAndLoadTemplate() {
        List<ConnectionThresholdTemplateModel> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                this.getClass().getResourceAsStream("/data/european_hubs_thresholds.csv")))) {

            String line;
            br.readLine(); // Preskačemo header

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 5) continue;
                data.add(new ConnectionThresholdTemplateModel(
                        values[0].trim(),
                        values[1].trim(),
                        Integer.parseInt(values[2].trim()),
                        Integer.parseInt(values[3].trim()),
                        Integer.parseInt(values[4].trim())
                ));
            }
        } catch (Exception e) {
            log("ERROR: Greška prilikom čitanja CSV fajla: " + e.getMessage());
        }

        StringBuilder drlBuilder = new StringBuilder();
        drlBuilder.append("package templates;\n\n")
                .append("import com.aeroflow.model.*;\n")
                .append("import com.aeroflow.model.events.*;\n")
                .append("import com.aeroflow.model.enums.*;\n")
                .append("import java.time.temporal.ChronoUnit;\n\n");

        for (ConnectionThresholdTemplateModel row : data) {
            drlBuilder.append("rule \"Dynamic_Rebooking_").append(row.getAirportCode()).append("_").append(row.getTransferType()).append("\"\n")
                    .append("when\n")
                    .append("    $flight1 : Flight(status == FlightStatus.DELAYED, destination.code == \"").append(row.getAirportCode()).append("\", actualArrival != null, scheduledArrival != null)\n")
                    .append("    eval(ChronoUnit.MINUTES.between($flight1.getScheduledArrival(), $flight1.getActualArrival()) > ").append(row.getDelayThreshold()).append(")\n")
                    .append("    $itinerary : Itinerary($passenger : passenger, $flights : flights)\n")
                    .append("    Flight(this == $flight1) from $flights\n")
                    .append("    $flight2 : Flight(origin.code == \"").append(row.getAirportCode()).append("\", status == FlightStatus.SCHEDULED, $scheduledDep : scheduledDeparture) from $flights\n")
                    .append("    eval(ChronoUnit.MINUTES.between($flight1.getActualArrival(), $scheduledDep) < ").append(row.getTotalTransferTime()).append(")\n")
                    .append("then\n")
                    .append("    $passenger.setCurrentStatus(PassengerStatus.SEVERELY_DELAYED);\n")
                    .append("    update($passenger);\n")
                    .append("end\n\n");
        }

        String generatedDrl = drlBuilder.toString();
        log("==== 1. USPJEŠNO GENERISANA PRAVILA IZ CSV PODATAKA ====");

        KieHelper kieHelper = new KieHelper();
        KieServices ks = KieServices.Factory.get();

        try {
            kieHelper.addResource(ks.getResources().newClassPathResource("cep/aeroflow-cep.drl"), ResourceType.DRL);
            kieHelper.addResource(ks.getResources().newClassPathResource("forward/forward-chaining.drl"), ResourceType.DRL);
            kieHelper.addResource(ks.getResources().newClassPathResource("queries/analytical-queries.drl"), ResourceType.DRL);
        } catch (Exception e) {
            log("WARN: Upozorenje pri učitavanju statičkih DRL fajlova: " + e.getMessage());
        }

        kieHelper.addContent(generatedDrl, ResourceType.DRL);

        KieBaseConfiguration config = ks.newKieBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);

        this.cepSession = kieHelper.build(config).newKieSession();
        this.cepSession.setGlobal("logService", this);

        log("==== 2. KONAČNA SESIJA POKRENUTA U STREAM MODU ====");
    }

    public void insertFact(Object fact) {
        cepSession.insert(fact);
    }

    public void insertEvent(Event event) {
        cepSession.insert(event);
        log("EVENT: Registrovan " + event.getClass().getSimpleName());
        cepSession.fireAllRules();
    }

    public KieSession getCepSession() {
        return this.cepSession;
    }

    @PreDestroy
    public void disposeSession() {
        if (this.cepSession != null) {
            this.cepSession.dispose();
            log("==== Sesija zatvorena. ====");
        }
    }
}