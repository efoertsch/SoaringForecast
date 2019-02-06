package org.soaringforecast.rasp.dagger;

import android.content.Context;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = {OkHttpClientModule.class
        , SoaringForecastModule.class
        , AppRepositoryModule.class})

public interface SoaringForecastComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder context(Context context);
        SoaringForecastComponent build();
    }

}
