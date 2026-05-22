package com.aeroflow.model.events;

import com.aeroflow.model.enums.FlightStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class FlightUpdateEvent extends Event {
    private String flightNumber;
    private LocalDateTime newEstimatedTime;
    private FlightStatus status;

    public FlightUpdateEvent() {
        super();
    }

    public FlightUpdateEvent(Date timestamp, String source, String flightNumber, LocalDateTime newEstimatedTime, FlightStatus status) {
        super(timestamp, source);
        this.flightNumber = flightNumber;
        this.newEstimatedTime = newEstimatedTime;
        this.status = status;
    }
}