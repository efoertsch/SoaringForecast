package org.soaringforecast.rasp.common.recycleradapter;

import org.greenrobot.eventbus.EventBus;

import dagger.android.support.DaggerFragment;

public class MasterFragment extends DaggerFragment {

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

}
