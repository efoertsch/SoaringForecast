package org.soaringforecast.rasp.dagger;

import android.content.Context;

import org.soaringforecast.rasp.R;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class UsgsServerUrlModule {

    @Provides
    @Named("usgs_server")
    public String getUsgsUrl(Context context){
        return context.getString(R.string.usgs_server_url);
    }

}
