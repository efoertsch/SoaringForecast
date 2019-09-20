package org.soaringforecast.rasp.airport.messages;

import org.soaringforecast.rasp.repository.Airport;

import java.util.List;

public final class AirportOrderEvent {
    private final List<Airport> airports;

    public AirportOrderEvent(List<Airport> airports) {
        this.airports = airports;
    }

    public List<Airport> getAirports() {
        return airports;
    }
}
