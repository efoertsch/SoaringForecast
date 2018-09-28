package com.fisincorporated.aviationweather.turnpoints;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.common.MasterActivity;

import javax.inject.Inject;


public class TurnpointsImportActivity extends MasterActivity {

    @Inject
    TurnpointsImportFragment turnpointsImportFragment;

    @Override
    protected Fragment createFragment() {
        return  turnpointsImportFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityTitle(R.string.import_turnpoints);

    }

}
