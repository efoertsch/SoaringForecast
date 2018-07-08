package com.fisincorporated.aviationweather.app;


import com.fisincorporated.aviationweather.dagger.AppModule;
import com.fisincorporated.aviationweather.dagger.DaggerDiComponent;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import okhttp3.Interceptor;
import retrofit.MockInterceptor;

//https://artemzin.com/blog/how-to-mock-dependencies-in-unit-integration-and-functional-tests-dagger-robolectric-instrumentation/
public class WeatherApplicationTest extends WeatherApplication {


    public static final String AIRPORT_PREFS_TEST = "SHARED_AIRPORT_FUNCTIONAL_TEST";


    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        AppModule appModule = new AppModule(){
            @Override
            public String providesAppSharedPreferencesName() {
                return AIRPORT_PREFS_TEST;
            }

            @Override
            public Interceptor getInterceptor() {
                return new MockInterceptor();
            }
        };
        component = DaggerDiComponent.builder().application(this).appModule(appModule).build();

        component.inject(this);
        return component;
    }

}
