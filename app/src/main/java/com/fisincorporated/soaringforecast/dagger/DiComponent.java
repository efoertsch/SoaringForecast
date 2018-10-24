package com.fisincorporated.soaringforecast.dagger;

import com.fisincorporated.soaringforecast.app.SoaringWeatherApplication;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = {AppModule.class
        , AndroidSupportInjectionModule.class
        , UIBuildersModule.class
        , AppRepositoryModule.class
        , ChannelIdModule.class
        })

public interface DiComponent extends
        AndroidInjector<SoaringWeatherApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(SoaringWeatherApplication application);

        Builder appModule(AppModule appModule);

        DiComponent build();
    }

    void inject(SoaringWeatherApplication soaringWeatherApplication);




}

