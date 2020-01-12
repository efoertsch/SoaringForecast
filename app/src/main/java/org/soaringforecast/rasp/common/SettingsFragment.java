package org.soaringforecast.rasp.common;


import android.content.Context;

import javax.inject.Inject;

import androidx.preference.PreferenceFragmentCompat;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import dagger.android.support.AndroidSupportInjection;

public abstract class SettingsFragment extends PreferenceFragmentCompat  implements HasAndroidInjector {

    @Inject
    DispatchingAndroidInjector<Object> androidInjector;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return androidInjector;
    }



}
