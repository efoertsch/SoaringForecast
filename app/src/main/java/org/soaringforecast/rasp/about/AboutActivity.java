package org.soaringforecast.rasp.about;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.MasterActivity;

public class AboutActivity extends MasterActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityTitle(R.string.activity_about);
    }

    protected Fragment createFragment(){
        return new AboutFragment();
    }
}
