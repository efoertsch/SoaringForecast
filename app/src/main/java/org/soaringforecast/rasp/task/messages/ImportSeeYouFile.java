package org.soaringforecast.rasp.task.messages;

import org.soaringforecast.rasp.turnpoints.json.TurnpointFile;

public class ImportSeeYouFile {

    private TurnpointFile turnpointFile;

    public ImportSeeYouFile(TurnpointFile turnpointFile) {
        this.turnpointFile  = turnpointFile;
    }

    public TurnpointFile getTurnpointFile() {
        return turnpointFile;
    }
}
