package org.soaringforecast.rasp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.widget.ImageView;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.cache.BitmapCache;
import org.soaringforecast.rasp.common.BitmapImage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class BitmapImageUtils {

    private BitmapCache bitmapCache;
    private OkHttpClient okHttpClient;

    public BitmapImageUtils(BitmapCache bitmapCache, OkHttpClient okHttpClient) {
        this.bitmapCache = bitmapCache;
        this.okHttpClient = okHttpClient;
    }

    public BitmapImage getBitmapImage(final BitmapImage bitmapImage, final String baseUrl, final String bitmapUrl) {
        Bitmap bitmap = bitmapCache.get(bitmapUrl);
        if (bitmap != null) {
            bitmapImage.setBitmap(bitmap);
            return bitmapImage;
        }

        bitmap = download(baseUrl + bitmapUrl);
        if (bitmap != null) {
            bitmapImage.setBitmap(bitmap);
            bitmapCache.put(bitmapUrl, bitmap);
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
            } else {
                 Set<String > names =  response.headers().names();
                 for (String name: names){
                     Timber.d("Response.headers().name: %1$s, value: %2$s", name, response.header(name));
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

    public void clearAllImages() {
        bitmapCache.clearCache();
    }

    public static void getForecastDrawable(String forecastCategory, ImageView imageView) {
        switch (forecastCategory) {
            case "thermal":
                imageView.setImageResource(R.drawable.ic_thermal);
                imageView.setBackgroundColor(imageView.getContext().getResources().getColor(R.color.colorPrimary));
                return;
            case "wind":
                imageView.setImageResource(R.drawable.ic_wind);
                imageView.setBackgroundColor(imageView.getContext().getResources().getColor(R.color.colorPrimary));
                return;
            case "cloud":
                imageView.setImageResource( R.drawable.ic_cloud_white_24dp);
                imageView.setBackgroundColor(imageView.getContext().getResources().getColor(R.color.colorPrimary));
                return;
            case "wave":
                imageView.setImageResource(R.drawable.ic_wave);
                imageView.setBackgroundColor(imageView.getContext().getResources().getColor(R.color.colorPrimary));
                return;
        }

    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
