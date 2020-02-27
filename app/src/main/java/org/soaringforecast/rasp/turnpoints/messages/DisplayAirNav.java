package org.soaringforecast.rasp.turnpoints.messages;

import org.soaringforecast.rasp.repository.Turnpoint;

public class DisplayAirNav {

    final Turnpoint turnpoint;

    public DisplayAirNav(Turnpoint turnpoint) {
        this.turnpoint = turnpoint;
    }

    public Turnpoint getTurnpoint() {
        return turnpoint;
    }


}
