package org.soaringforecast.rasp.dagger;

import org.soaringforecast.rasp.retrofit.JSONServerApi;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class RegionTurnpointsModule extends ForecastServerModule {
    
    @Provides
    public JSONServerApi providesRegionTurnpointsJsonApi(@Named("interceptor") OkHttpClient okHttpClient, @Named("rasp_url") String raspUrl) {
        return getForecastServerRetrofit(okHttpClient, raspUrl).getRetrofit().create(JSONServerApi.class);
    }

}
