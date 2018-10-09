package com.fisincorporated.aviationweather.dagger;


import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.task.TurnpointProcessor;

import dagger.Module;
import dagger.Provides;

@Module
public class TurnpointProcessorModule {

    @Provides
    TurnpointProcessor providesTurnpointProcessor(AppRepository appRepository){
        return new TurnpointProcessor(appRepository);
    }

}
