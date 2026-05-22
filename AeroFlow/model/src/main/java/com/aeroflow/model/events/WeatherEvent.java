package com.aeroflow.model.events;

import com.aeroflow.model.enums.WeatherSeverity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class WeatherEvent extends Event {
    private String airportCode;
    private WeatherSeverity severity;
    private String conditionType;

    public WeatherEvent() {
        super();
    }

    public WeatherEvent(Date timestamp, String source, String airportCode, WeatherSeverity severity, String conditionType) {
        super(timestamp, source);
        this.airportCode = airportCode;
        this.severity = severity;
        this.conditionType = conditionType;
    }
}