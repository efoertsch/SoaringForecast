package org.soaringforecast.rasp.dagger;

import org.soaringforecast.rasp.soaring.forecast.TurnpointBitmapUtils;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class TurnpointBitmapUtilsModule {

    @Provides
    @Singleton
    public TurnpointBitmapUtils providesTurnpointBitmapUtilsModule() {
        return TurnpointBitmapUtils.getInstance();
    }
}
