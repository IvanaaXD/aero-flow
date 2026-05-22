package com.aeroflow.service.dto;

import lombok.Data;

@Data
public class FlightDelayRequestDTO {
    private String flightNumber;
    private int delayMinutes;
}