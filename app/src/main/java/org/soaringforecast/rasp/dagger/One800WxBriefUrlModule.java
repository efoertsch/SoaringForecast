package org.soaringforecast.rasp.dagger;

import android.content.Context;

import org.soaringforecast.rasp.R;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class One800WxBriefUrlModule {

    @Provides
    @Named("one_800wxbrief_server")
    public String getOne800WxBriefUrl(Context context){
        return context.getString(R.string.one_800wxbrief_url);
    }

}
