package com.aeroflow.model.events;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class SecurityQueueEvent extends Event {

    private Double waitTimeIncrease;

    public SecurityQueueEvent() {
        super();
    }

    public SecurityQueueEvent(Date timestamp, String source, Double waitTimeIncrease) {
        super(timestamp, source);
        this.waitTimeIncrease = waitTimeIncrease;
    }
}