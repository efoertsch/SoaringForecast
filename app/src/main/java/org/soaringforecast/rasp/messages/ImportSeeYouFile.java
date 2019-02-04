package org.soaringforecast.rasp.messages;

import org.soaringforecast.rasp.task.json.TurnpointFile;

public class ImportSeeYouFile {

    private TurnpointFile turnpointFile;

    public ImportSeeYouFile(TurnpointFile turnpointFile) {
        this.turnpointFile  = turnpointFile;
    }

    public TurnpointFile getTurnpointFile() {
        return turnpointFile;
    }
}
