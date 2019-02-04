package org.soaringforecast.rasp.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import org.soaringforecast.rasp.BuildConfig;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

// This wraps Jake Warton DiskLruCache and is set to only handle bitmaps
// From https://stackoverflow.com/questions/10185898/using-disklrucache-in-android-4-0-does-not-provide-for-opencache-method
// with some mods to allow PNGs to be stored along with JPEGS
// Keys must be [a-z0-9_-]{1,64} to make cache key requirements
public class DiskLruImageCache {

    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;
    private static final String TAG = "DiskLruImageCache";
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static final int MAX_KEY_LENGTH = 64;

    private DiskLruCache mDiskCache;
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
    private int mCompressQuality = 70;

    //
    private static final String regex = "[^A-za-z0-9_-]+";
    private static final String regexReplaceChar = "-";
    private static final Pattern regexPattern = Pattern.compile(regex);

    DiskLruImageCache(Context context, String uniqueName, int diskCacheSize, int quality) {
        try {
            final File diskCacheDir = getDiskCacheDir(context, uniqueName);
            mDiskCache = DiskLruCache.open(diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize);
            mCompressQuality = quality;


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor)
            throws IOException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(editor.newOutputStream(0), IO_BUFFER_SIZE);
            return bitmap.compress(mCompressFormat, mCompressQuality, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Environment.isExternalStorageRemovable() ?
                        context.getExternalCacheDir().getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    public void put(String key, Bitmap data) {
        DiskLruCache.Editor editor = null;
        try {
            editor = mDiskCache.edit(getCacheKey(key));
            if (editor == null) {
                return;
            }

            if (writeBitmapToFile(data, editor)) {
                mDiskCache.flush();
                editor.commit();
                if (BuildConfig.DEBUG) {
                    Timber.d("image put on disk cache %s", getCacheKey(key));
                }
            } else {
                editor.abort();
                if (BuildConfig.DEBUG) {
                    Timber.d("ERROR on: image put on disk cache %s", getCacheKey(key));
                }
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Timber.d("ERROR on: image put on disk cache %s", getCacheKey(key));
            }
            try {
                if (editor != null) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }
        }
    }

    public Bitmap getBitmap(String key) {

        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = null;
        try {

            snapshot = mDiskCache.get(getCacheKey(key));
            if (snapshot == null) {
                return null;
            }
            final InputStream in = snapshot.getInputStream(0);
            if (in != null) {
                final BufferedInputStream buffIn =
                        new BufferedInputStream(in, IO_BUFFER_SIZE);
                bitmap = BitmapFactory.decodeStream(buffIn);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }

        if (BuildConfig.DEBUG) {
            Timber.d(bitmap == null ? "" : "image read from disk %s",  getCacheKey(key));
        }

        return bitmap;

    }

    public boolean containsKey(String key) {

        boolean contained = false;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get(getCacheKey(key));
            contained = snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }

        return contained;

    }

    public void clearCache() {
        if (BuildConfig.DEBUG) {
            Timber.d("disk cache CLEARED");
        }
        try {
            mDiskCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getCacheFolder() {
        return mDiskCache.getDirectory();
    }

    public String getCacheKey(String key){
        Matcher m = regexPattern.matcher(key);
        if (key.length() <= MAX_KEY_LENGTH) {
            return m.replaceAll(regexReplaceChar).toLowerCase();
        } else {
            return  m.replaceAll(regexReplaceChar).toLowerCase().substring(key.length() - MAX_KEY_LENGTH);
        }
    }

}