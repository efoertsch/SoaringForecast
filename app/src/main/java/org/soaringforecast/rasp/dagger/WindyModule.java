package org.soaringforecast.rasp.dagger;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class WindyModule extends ForecastServerModule {

    private static final String WINDY_HTML_FILENAME = "windy.html";
    private static final String APP_WINDY_URL = "file:///android_asset/" + WINDY_HTML_FILENAME;


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



}
