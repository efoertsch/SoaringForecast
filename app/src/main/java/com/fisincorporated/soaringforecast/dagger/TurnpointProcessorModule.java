package com.fisincorporated.soaringforecast.dagger;


import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.task.TurnpointProcessor;

import dagger.Module;
import dagger.Provides;

@Module
public class TurnpointProcessorModule {

    @Provides
    TurnpointProcessor providesTurnpointProcessor(AppRepository appRepository){
        return new TurnpointProcessor(appRepository);
    }

}
