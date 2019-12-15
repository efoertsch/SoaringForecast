package org.soaringforecast.rasp.dagger;

import android.content.Context;

import org.soaringforecast.rasp.R;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class AviationWeatherGovModule {

    @Provides
    @Named("aviation_weather_gov_url")
    public String getAviationWeatherGovUrl(Context context) {
        return context.getString(R.string.aviation_weather_gov_url);
    }

}
