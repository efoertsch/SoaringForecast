package com.fisincorporated.aviationweather.app;

import android.app.Application;

import com.fisincorporated.aviationweather.dagger.AppModule;
import com.fisincorporated.aviationweather.dagger.DaggerDiComponent;
import com.fisincorporated.aviationweather.dagger.DiComponent;

import javax.inject.Inject;

public class WeatherApplication extends Application {

    protected DiComponent component;

    // Ensure that defaults set prior to doing anything
    @Inject
    AppPreferences appPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        createDaggerInjections();
    }

    protected void createDaggerInjections() {
        component = DaggerDiComponent.builder()
                .appModule(new AppModule(this))
                .build();
        component.inject(this);
    }

    public DiComponent getComponent() {
        return component;
    }

}
