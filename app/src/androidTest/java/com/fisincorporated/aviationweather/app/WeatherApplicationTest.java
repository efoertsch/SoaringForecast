package com.fisincorporated.aviationweather.app;


import com.fisincorporated.aviationweather.dagger.AppModule;
import com.fisincorporated.aviationweather.dagger.DaggerDiComponent;
import com.fisincorporated.aviationweather.retrofit.AppRetrofit;

import retrofit.MockInterceptor;
import retrofit2.Retrofit;

//https://artemzin.com/blog/how-to-mock-dependencies-in-unit-integration-and-functional-tests-dagger-robolectric-instrumentation/
public class WeatherApplicationTest extends WeatherApplication {

    @Override
    protected void createDaggerInjections() {
        component = DaggerDiComponent.builder()
                .appModule(new AppModule(this) {
                    @Override
                    public String providesAppSharedPreferencesName() {
                        return "SHARED_AIRPORT_FUNCTIONAL_TEST";
                    }

                    public Retrofit provideAppRetrofit() {
                        return new AppRetrofit(new MockInterceptor()).getRetrofit();
                    }
                })
                .build();
        component.inject(this);
    }
}
