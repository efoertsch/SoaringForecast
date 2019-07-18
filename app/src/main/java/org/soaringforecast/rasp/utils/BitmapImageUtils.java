package org.soaringforecast.rasp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.widget.ImageView;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.cache.BitmapCache;
import org.soaringforecast.rasp.common.BitmapImage;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

// TODO separate cache logic from other bitmap stuff
//TODO use retrofit @STREAM to download bitmaps and clean up how url assembled
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

    public Bitmap download(final String url) {
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
//                 Set<String > names =  response.headers().names();
//                 for (String name: names){
//                     Timber.d("Response.headers().name: %1$s, value: %2$s", name, response.header(name));
//                 }

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

    public static Bitmap getBitmapFromVectorDrawable(Context context, @DrawableRes int drawableId) {
        Drawable drawable = getDrawableFromVectorDrawable(context, drawableId);

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static Drawable getDrawableFromVectorDrawable(Context context,@DrawableRes int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }
        return drawable;
    }

    public static Bitmap drawTextOnDrawable(Context context, Drawable drawable, String text) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        float scale = context.getResources().getDisplayMetrics().density;
        Bitmap.Config bitmapConfig = bitmap.getConfig();

        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        bitmap = bitmap.copy(bitmapConfig, true);
        canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        /* SET FONT COLOR (e.g. WHITE -> rgb(255,255,255)) */
        paint.setColor(Color.rgb(0, 0, 0));
        /* SET FONT SIZE (e.g. 15) */
        paint.setTextSize((int) (12 * scale));
        /* SET SHADOW WIDTH, POSITION AND COLOR (e.g. BLACK) */
        //paint.setShadowLayer(1f, 0f, 1f, Color.BLACK);

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() + bounds.height()) / 2;
        canvas.drawText(text, x, y, paint);

        return bitmap;
    }

    public static Bitmap drawTextOnBitmap(Context context,Bitmap bitmap, String text) {
        Bitmap newCopyBitmap;
        Canvas canvas ;

        float scale = context.getResources().getDisplayMetrics().density;
        Bitmap.Config bitmapConfig = bitmap.getConfig();

        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        newCopyBitmap = bitmap.copy(bitmapConfig, true);
        canvas = new Canvas(newCopyBitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        /* SET FONT COLOR (e.g. WHITE -> rgb(255,255,255)) */
        paint.setColor(Color.rgb(0, 0, 0));
        /* SET FONT SIZE (e.g. 15) */
        paint.setTextSize((int) (12 * scale));
        /* SET SHADOW WIDTH, POSITION AND COLOR (e.g. BLACK) */
        //paint.setShadowLayer(1f, 0f, 1f, Color.BLACK);

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() + bounds.height()) / 2;
        canvas.drawText(text, x, y, paint);

        return newCopyBitmap;
    }

}
