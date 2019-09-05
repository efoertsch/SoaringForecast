package org.soaringforecast.rasp.dagger;

import android.content.Context;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = {
        AppRepositoryModule.class})

public interface StartupComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder context(Context context);

        StartupComponent build();
    }

}

