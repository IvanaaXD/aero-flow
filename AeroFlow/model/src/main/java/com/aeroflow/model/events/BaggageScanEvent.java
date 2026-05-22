package com.aeroflow.model.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class BaggageScanEvent extends Event {
    private String baggageTag;
    private String location;

    public BaggageScanEvent() {
        super();
    }

    public BaggageScanEvent(Date timestamp, String source, String baggageTag, String location) {
        super(timestamp, source);
        this.baggageTag = baggageTag;
        this.location = location;
    }
}