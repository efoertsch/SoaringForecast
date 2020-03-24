package org.soaringforecast.rasp.airport.messages;

import org.soaringforecast.rasp.repository.Airport;

public class DeletedAirport {
    final private Airport airport;
    final private int index;


    public DeletedAirport(Airport airport, int index) {
        this.airport = airport;
        this.index = index;
    }

    public Airport getAirport() {
        return airport;
    }

    public int getIndex() {
        return index;
    }
}
