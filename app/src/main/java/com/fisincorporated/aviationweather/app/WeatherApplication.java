package com.fisincorporated.aviationweather.app;

import android.app.Application;

import com.fisincorporated.aviationweather.dagger.AppModule;
import com.fisincorporated.aviationweather.dagger.DaggerDiComponent;
import com.fisincorporated.aviationweather.dagger.DiComponent;

public class WeatherApplication extends Application {

    DiComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        createDaggerInjections();

    }

    private void createDaggerInjections() {
        component = DaggerDiComponent.builder()
                .appModule(new AppModule(this))
                .build();
        component.inject(this);
    }

    public DiComponent getComponent() {
        return component;
    }

}
