package org.soaringforecast.rasp.dagger;


import android.content.Context;

import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.retrofit.AviationWeatherGovApi;
import org.soaringforecast.rasp.retrofit.AviationWeatherGovRetrofit;
import org.soaringforecast.rasp.retrofit.ForecastServerRetrofit;
import org.soaringforecast.rasp.retrofit.JSONServerApi;
import org.soaringforecast.rasp.retrofit.JsonServerRetrofit;
import org.soaringforecast.rasp.retrofit.One800WxBriefApi;
import org.soaringforecast.rasp.retrofit.One800WxBriefServerRetrofit;
import org.soaringforecast.rasp.retrofit.SoaringForecastApi;
import org.soaringforecast.rasp.retrofit.UsgsApi;
import org.soaringforecast.rasp.retrofit.UsgsServerRetrofit;
import org.soaringforecast.rasp.utils.BitmapImageUtils;
import org.soaringforecast.rasp.utils.StringUtils;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class AppRepositoryModule  {

    @Provides
    @Singleton
    public AppRepository providesAppRepository(Context context
            , SoaringForecastApi soaringForecastApi
            , JSONServerApi jsonServerApi
            , AviationWeatherGovApi aviationWeatherGovApi
            , BitmapImageUtils bitmapImageUtils
            , @Named("forecast_server_url") String raspUrl
            , StringUtils stringUtils
            , AppPreferences appPreferences
            , UsgsApi usgsApi
            , One800WxBriefApi one800WxBriefApi) {
        return AppRepository.getAppRepository(context
                , soaringForecastApi
                , jsonServerApi
                , aviationWeatherGovApi
                , bitmapImageUtils
                , raspUrl
                , stringUtils
                , appPreferences
                , usgsApi
                ,one800WxBriefApi);
    }

    @Provides
    @Singleton
    public SoaringForecastApi getSoaringForecastApi(@Named("no_interceptor") OkHttpClient okHttpClient, @Named("forecast_server_url") String forecastServerUrl) {
        return new ForecastServerRetrofit(okHttpClient, forecastServerUrl).getRetrofit().create(SoaringForecastApi.class);
    }


    @Provides
    @Singleton
    // Apparently due to later version of Retrofit if you use "interceptor" you can get error:
    //java.lang.IllegalStateException: cannot make a new request because the previous response is still open: please call response.close()
    public JSONServerApi getJSONServerApi(@Named("no_interceptor") OkHttpClient okHttpClient, @Named("json_server_url") String jsonServerUrl) {
        return new JsonServerRetrofit(okHttpClient, jsonServerUrl).getRetrofit().create(JSONServerApi.class);
    }

    @Provides
    @Singleton
    public AviationWeatherGovApi providesAviationWeatherGovApi(@Named("no_interceptor") OkHttpClient okHttpClient, @Named("aviation_weather_gov_url") String aviationWeatherUrl) {
        return new AviationWeatherGovRetrofit(okHttpClient, aviationWeatherUrl).getRetrofit().create(AviationWeatherGovApi.class);
    }

    @Provides
    @Singleton
    public UsgsApi providesUsgsApi(@Named("no_interceptor") OkHttpClient okHttpClient, @Named("usgs_server") String usgsServerUrl) {
        return new UsgsServerRetrofit(okHttpClient, usgsServerUrl).getRetrofit().create(UsgsApi.class);
    }

    @Provides
    @Singleton
    public One800WxBriefApi providesOne800WxBriefApi(@Named("no_interceptor_no_cache") OkHttpClient okHttpClient, @Named("one_800wxbrief_server") String one800WxbriefUrl){
        return new One800WxBriefServerRetrofit(okHttpClient, one800WxbriefUrl).getRetrofit().create(One800WxBriefApi.class);
    }

}
