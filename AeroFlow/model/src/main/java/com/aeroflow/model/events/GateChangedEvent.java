package com.aeroflow.model.events;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class GateChangedEvent extends Event {

    private String flightNumber;

    public GateChangedEvent() {
        super();
    }

    public GateChangedEvent(Date timestamp, String source, String flightNumber) {
        super(timestamp, source);
        this.flightNumber = flightNumber;
    }

}