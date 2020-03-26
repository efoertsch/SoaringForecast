package org.soaringforecast.rasp.turnpoints.messages;

import org.soaringforecast.rasp.repository.Turnpoint;

public class DeletedTurnpoint {
    private final Turnpoint turnpoint;

    public DeletedTurnpoint(Turnpoint turnpoint) {
        this.turnpoint = turnpoint;
    }

    public Turnpoint getTurnpoint() {
        return turnpoint;
    }
}
