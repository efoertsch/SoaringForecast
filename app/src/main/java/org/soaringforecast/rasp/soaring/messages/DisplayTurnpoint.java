package org.soaringforecast.rasp.soaring.messages;

import org.soaringforecast.rasp.repository.Turnpoint;

public class DisplayTurnpoint {

    private final Turnpoint turnpoint;

    public DisplayTurnpoint(Turnpoint turnpoint) {
        this.turnpoint = turnpoint;
    }

    public Turnpoint getTurnpoint() {
        return turnpoint;
    }
}
