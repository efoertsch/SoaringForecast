package com.fisincorporated.aviationweather.dagger;


import android.content.Context;

import com.fisincorporated.aviationweather.repository.AppRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppRepositoryModule {

    @Provides
    @Singleton
    public AppRepository getAppRepository(Context context) {
        return  AppRepository.getAppRepository(context);
    }

}
