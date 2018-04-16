package com.fisincorporated.aviationweather.app;

import com.fisincorporated.aviationweather.BuildConfig;
import com.fisincorporated.aviationweather.dagger.AppModule;
import com.fisincorporated.aviationweather.dagger.DaggerDiComponent;
import com.fisincorporated.aviationweather.dagger.DiComponent;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import timber.log.Timber;

public class WeatherApplication extends DaggerApplication {

    protected DiComponent component;

    // Ensure that defaults set prior to doing anything
    @Inject
    AppPreferences appPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
       // createDaggerInjections();
        initTimber();
    }

    private void initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            // Update if adding crash reporting
            Timber.plant(new Timber.DebugTree());
        }

    }

//    protected void createDaggerInjections() {
//        component = DaggerDiComponent.builder()
//                .appModule(new AppModule(this))
//                .build();
//        component.inject(this);
//    }

    public DiComponent getComponent() {
        return component;
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        component = DaggerDiComponent.builder().application(this).appModule(new AppModule(this)).build();
        component.inject(this);
        return component;
    }
}
