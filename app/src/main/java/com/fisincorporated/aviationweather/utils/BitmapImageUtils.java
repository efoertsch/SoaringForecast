package com.fisincorporated.aviationweather.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fisincorporated.aviationweather.cache.BitmapCache;
import com.fisincorporated.aviationweather.common.BitmapImage;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class BitmapImageUtils {

    private BitmapCache bitmapCache;
    private OkHttpClient okHttpClient;

    public BitmapImageUtils(BitmapCache bitmapCache, OkHttpClient okHttpClient){
        this.bitmapCache = bitmapCache;
        this.okHttpClient = okHttpClient;
    }

    public BitmapImage getBitmapImage(final BitmapImage bitmapImage, final String url) {
        Bitmap bitmap = bitmapCache.get(url);
        if (bitmap != null) {
            bitmapImage.setBitmap(bitmap);
            return bitmapImage;
        }

        bitmap = download(url);
        if (bitmap != null) {
            bitmapImage.setBitmap(bitmap);
            bitmapCache.put(url, bitmap);
            return bitmapImage;
        }
        bitmapImage.setErrorOnLoad(true);
        return bitmapImage;

    }

    private Bitmap download(final String url) {
        Bitmap bitmap = null;
        //Timber.d("Calling to get: %s", url);
        Response response = null;
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            response = okHttpClient.newCall(request).execute();
            Timber.d("Content-Type for download: %s", response.header("Content-Type"));
            if (response.header("Content-Type").startsWith("image")) {
                InputStream inputStream = response.body().byteStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap != null) {
                    Timber.d("good bitmap ");
                    return bitmap;
                }
            }
        } catch (IOException e) {
            Timber.d("%s  IOException ", url);
            Timber.e(e.toString());
        } catch (NullPointerException npe) {
            Timber.d("%s  Null pointer exception on getting response byteStream", url);
            Timber.e(npe.toString());
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return bitmap;
    }
}
