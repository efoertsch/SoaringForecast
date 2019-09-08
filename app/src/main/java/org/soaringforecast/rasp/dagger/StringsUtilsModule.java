package org.soaringforecast.rasp.dagger;

import org.soaringforecast.rasp.utils.StringUtils;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class StringsUtilsModule {

    @Provides
    @Singleton
    public StringUtils provideStringUtils() {
        return new StringUtils();
    }


}
