package org.soaringforecast.rasp.dagger;

import android.content.Context;

import org.soaringforecast.rasp.cache.BitmapCache;
import org.soaringforecast.rasp.utils.BitmapImageUtils;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class BitmapImageModule {

    private BitmapCache bitmapCache;

    @Provides
    @Singleton
    public BitmapImageUtils provideBitmapImageUtils(Context appContext, @Named("no_interceptor") OkHttpClient okHttpClient) {
        return new BitmapImageUtils(getBitmapCache(appContext), okHttpClient);
    }

    @Provides
    @Singleton
    public BitmapCache getBitmapCache(Context appContext) {
        if (bitmapCache == null) {
            bitmapCache = BitmapCache.getInstance(appContext);
        }
        return bitmapCache;
    }

}
