package com.aeroflow.model.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class PassengerScanEvent extends Event {
    private String passengerId;
    private String location;
    private String gate;

    public PassengerScanEvent() {
        super();
    }

    public PassengerScanEvent(Date timestamp, String source, String passengerId, String location, String gate) {
        super(timestamp, source);
        this.passengerId = passengerId;
        this.location = location;
        this.gate = gate;
    }
}