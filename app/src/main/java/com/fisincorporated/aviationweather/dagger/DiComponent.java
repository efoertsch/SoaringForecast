package com.fisincorporated.aviationweather.dagger;

import com.fisincorporated.aviationweather.app.WeatherApplication;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = {AppModule.class,  AndroidSupportInjectionModule.class, UIBuildersModule.class})
public interface DiComponent extends
        AndroidInjector<WeatherApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(WeatherApplication application);

        Builder appModule(AppModule appModule);

        DiComponent build();
    }

    void inject(WeatherApplication weatherApplication);




}

