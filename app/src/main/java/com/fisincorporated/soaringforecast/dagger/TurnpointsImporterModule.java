package com.fisincorporated.soaringforecast.dagger;


import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.task.TurnpointsImporter;

import dagger.Module;
import dagger.Provides;

@Module
public class TurnpointsImporterModule {

    @Provides
    TurnpointsImporter providesTurnpointsImporter(AppRepository appRepository){
        return new TurnpointsImporter(appRepository);
    }

}
