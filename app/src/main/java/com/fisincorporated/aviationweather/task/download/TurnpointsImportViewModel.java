package com.fisincorporated.aviationweather.task.download;

import android.arch.lifecycle.ViewModel;

import com.fisincorporated.aviationweather.task.TurnpointProcessor;

import java.io.File;
import java.util.List;

public class TurnpointsImportViewModel extends ViewModel {

    TurnpointProcessor turnpointProcessor;

    public TurnpointsImportViewModel setTurnpointProcessor(TurnpointProcessor turnpointProcessor) {
        this.turnpointProcessor = turnpointProcessor;
        return this;
    }

    public List<File> getCupFiles() {
        return turnpointProcessor.getCupFileList();
    }

}
