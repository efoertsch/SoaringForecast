package org.soaringforecast.rasp.dagger;

import android.app.Application;

import org.soaringforecast.rasp.app.SoaringWeatherApplication;
import org.soaringforecast.rasp.soaring.AppPreferencesModule;

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
        , BitmapImageModule.class
        , OkHttpClientModule.class
        , StringsUtilsModule.class
        , AppPreferencesModule.class
        , ForecastServerUrlModule.class
        , JsonServerUrlModule.class
        , TurnpointBitmapUtilsModule.class
        , AviationWeatherGovModule.class
        , UsgsServerUrlModule.class
        , One800WxBriefUrlModule.class
})

public interface ApplicationComponent extends
        AndroidInjector<SoaringWeatherApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        Builder appModule(AppModule appModule);

        ApplicationComponent build();
    }

    void inject(SoaringWeatherApplication soaringWeatherApplication);

}

