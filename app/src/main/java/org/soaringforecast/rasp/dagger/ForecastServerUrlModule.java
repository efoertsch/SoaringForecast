package org.soaringforecast.rasp.dagger;

import android.content.Context;

import org.soaringforecast.rasp.R;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class ForecastServerUrlModule {

    @Provides
    @Named("forecast_server_url")
    public String getRaspUrl(Context context){
        return context.getString(R.string.forecast_server_url);
    }


}
