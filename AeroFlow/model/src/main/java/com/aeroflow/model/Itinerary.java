package com.aeroflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Itinerary {
    private String itineraryId;
    private Passenger passenger;
    private List<Flight> flights;
    private String finalDestination;
}