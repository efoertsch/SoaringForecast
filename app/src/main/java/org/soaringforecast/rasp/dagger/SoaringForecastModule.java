package org.soaringforecast.rasp.dagger;

import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.retrofit.SoaringForecastApi;
import org.soaringforecast.rasp.soaring.forecast.SoaringForecastDownloader;
import org.soaringforecast.rasp.utils.BitmapImageUtils;
import org.soaringforecast.rasp.utils.StringUtils;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class SoaringForecastModule extends ForecastServerModule {

    private static final String WINDY_HTML_FILENAME = "windy.html";
    private static final String APP_WINDY_URL = "file:///android_asset/" + WINDY_HTML_FILENAME;

    @Provides
    public SoaringForecastApi getSoaringForecastApi(@Named("interceptor") OkHttpClient okHttpClient, @Named("forecast_server_url") String forecastServerUrl) {
        return getForecastServerRetrofit(okHttpClient, forecastServerUrl).getRetrofit().create(SoaringForecastApi.class);
    }


    @Provides
    public SoaringForecastDownloader provideSoaringForecastDownloader(@Named("interceptor") OkHttpClient okHttpClient
            , BitmapImageUtils bitmapImageUtils, @Named("forecast_server_url") String raspUrl
            , StringUtils stringUtils, AppPreferences appPreferences) {
        return new SoaringForecastDownloader(getSoaringForecastApi(okHttpClient, raspUrl), bitmapImageUtils, raspUrl, stringUtils, appPreferences);
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
