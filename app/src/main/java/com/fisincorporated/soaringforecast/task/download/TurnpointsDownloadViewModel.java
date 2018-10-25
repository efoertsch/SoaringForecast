package com.fisincorporated.soaringforecast.task.download;

import android.arch.lifecycle.ViewModel;

import com.fisincorporated.soaringforecast.task.TurnpointProcessor;
import com.fisincorporated.soaringforecast.task.json.TurnpointFile;

import java.io.File;
import java.util.List;

public class TurnpointsDownloadViewModel extends ViewModel {

    TurnpointProcessor turnpointProcessor;

    public TurnpointsDownloadViewModel setTurnpointProcessor(TurnpointProcessor turnpointProcessor) {
        this.turnpointProcessor = turnpointProcessor;
        return this;
    }

    public List<File> getCupFiles() {
        return turnpointProcessor.getCupFileList();
    }

    public List<TurnpointFile> getTurnpointFiles() {
        return null;
    }

}
