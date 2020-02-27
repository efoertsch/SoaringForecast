package org.soaringforecast.rasp.soaring.messages;

import org.soaringforecast.rasp.repository.Turnpoint;

public class DisplayTurnpointSatelliteView {

    private final Turnpoint turnpoint;

    public DisplayTurnpointSatelliteView(Turnpoint turnpoint) {
        this.turnpoint = turnpoint;
    }

    public Turnpoint getTurnpoint() {
        return turnpoint;
    }
}
