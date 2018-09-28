package com.fisincorporated.aviationweather.dagger;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class ChannelIdModule {

    @Provides
    @Named("CHANNEL_ID")
    String provideChannelId() {
        return "Soaring Weather";
    }

}
