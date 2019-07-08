package org.soaringforecast.rasp.dagger;

import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.retrofit.GBSCJsonApi;
import org.soaringforecast.rasp.retrofit.SoaringForecastApi;
import org.soaringforecast.rasp.retrofit.SoaringForecastRetrofit;
import org.soaringforecast.rasp.soaring.forecast.SoaringForecastDownloader;
import org.soaringforecast.rasp.utils.BitmapImageUtils;
import org.soaringforecast.rasp.utils.StringUtils;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class SoaringForecastModule {

    private static final String WINDY_HTML_FILENAME = "windy.html";
    private static final String APP_WINDY_URL = "file:///android_asset/" + WINDY_HTML_FILENAME;
    private static SoaringForecastRetrofit soaringForecastRetrofit;

    @Provides
    public SoaringForecastApi providesSoaringForecastApi(@Named("interceptor") OkHttpClient okHttpClient, @Named("rasp_url") String raspUrl) {
        if (soaringForecastRetrofit == null){
            soaringForecastRetrofit =  new SoaringForecastRetrofit(okHttpClient, raspUrl);
        }
        return soaringForecastRetrofit.getRetrofit().create(SoaringForecastApi.class);
    }

    @Provides
    public GBSCJsonApi providesGBSCJsonApi(@Named("interceptor") OkHttpClient okHttpClient, @Named("rasp_url") String raspUrl) {
        if (soaringForecastRetrofit == null){
            soaringForecastRetrofit =  new SoaringForecastRetrofit(okHttpClient, raspUrl);
        }
        return soaringForecastRetrofit.getRetrofit().create(GBSCJsonApi.class);
    }

    @Provides
    public SoaringForecastDownloader provideSoaringForecastDownloader(@Named("interceptor") OkHttpClient okHttpClient
            , BitmapImageUtils bitmapImageUtils, @Named("rasp_url") String raspUrl
            , StringUtils stringUtils, AppPreferences appPreferences) {
        return new SoaringForecastDownloader(providesSoaringForecastApi(okHttpClient, raspUrl), bitmapImageUtils, raspUrl, stringUtils, appPreferences);
    }


    // Used for webview.loadUrl()
    @Provides
    @Named("appWindyUrl")
    public String getAppWindUrl() {
        return APP_WINDY_URL;
    }

    // Used for webview.loadData()
    @Provides
    @Named("windyHtmlFileName")
    public String getWindyHtmlFileName() {
        return WINDY_HTML_FILENAME;
    }


    @Provides
    public StringUtils getStringUtils() {
        return new StringUtils();
    }

}
