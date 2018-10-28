package com.fisincorporated.soaringforecast.messages;

import com.fisincorporated.soaringforecast.task.json.TurnpointFile;

public class ImportSeeYouFile {

    private TurnpointFile turnpointFile;

    public ImportSeeYouFile(TurnpointFile turnpointFile) {
        this.turnpointFile  = turnpointFile;
    }

    public TurnpointFile getTurnpointFile() {
        return turnpointFile;
    }
}
