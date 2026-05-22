package com.aeroflow.service.dto;

import lombok.Data;

@Data
public class ConnectionThresholdTemplateModel {
    private String airportCode;
    private String transferType;
    private int minTransferTime;
    private int safetyBuffer;
    private int delayThreshold;
    private int totalTransferTime; // NOVO: Čist zbir za Drools šablon

    // Ručni konstruktor - zadržava isti broj parametara kao pri čitanju CSV-a
    public ConnectionThresholdTemplateModel(String airportCode, String transferType, int minTransferTime, int safetyBuffer, int delayThreshold) {
        this.airportCode = airportCode;
        this.transferType = transferType;
        this.minTransferTime = minTransferTime;
        this.safetyBuffer = safetyBuffer;
        this.delayThreshold = delayThreshold;
        this.totalTransferTime = minTransferTime + safetyBuffer; // Sabiramo bezbjedno u Javi!
    }
}