package org.soaringforecast.rasp.turnpoints.messages;

import org.soaringforecast.rasp.repository.Turnpoint;

public class EditTurnpoint {
    final Turnpoint turnpoint;

    public EditTurnpoint(Turnpoint turnpoint){
        this.turnpoint = turnpoint;
    }

    public Turnpoint getTurnpoint() {
        return turnpoint;
    }
}
