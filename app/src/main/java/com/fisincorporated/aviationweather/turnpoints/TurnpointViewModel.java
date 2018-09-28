package com.fisincorporated.aviationweather.turnpoints;

import android.arch.lifecycle.ViewModel;
import android.view.View;

import com.fisincorporated.aviationweather.R;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

public class TurnpointViewModel extends ViewModel {

    TurnpointProcessor turnpointProcessor;

    @Inject
    TurnpointViewModel() {
    }

    public TurnpointViewModel(TurnpointProcessor turnpointProcessor){
        this.turnpointProcessor = turnpointProcessor;
    }


    public TurnpointViewModel setView(View view) {
        View bindingView = view.findViewById(R.id.turnpoint_imports_recycler_view);
        return this;
    }

    private List<File> getCupFiles(){
        return  turnpointProcessor.getCupFileList();

    }
}
