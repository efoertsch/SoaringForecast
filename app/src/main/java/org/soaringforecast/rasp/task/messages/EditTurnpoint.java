package org.soaringforecast.rasp.task.messages;

public class EditTurnpoint {
    final long turnpointId;

    public EditTurnpoint(long turnpointId){
        this.turnpointId = turnpointId;
    }

    public long getTurnpointId() {
        return turnpointId;
    }
}
