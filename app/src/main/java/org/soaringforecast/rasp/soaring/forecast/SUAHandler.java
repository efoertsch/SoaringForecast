package org.soaringforecast.rasp.soaring.forecast;

import android.content.Context;
import android.support.annotation.NonNull;

import org.soaringforecast.rasp.retrofit.JSONServerApi;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import io.reactivex.Completable;
import io.reactivex.exceptions.Exceptions;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import timber.log.Timber;

public class SUAHandler {


    private static SUAHandler suaHandler;
    private Retrofit retrofit;
    private Context context;

    private SUAHandler() {
    }

    private SUAHandler(Context context, Retrofit retrofit) {
        this.context = context;
        this.retrofit = retrofit;
    }

    public static SUAHandler getInstance(Context context, Retrofit retrofit) {
        if (suaHandler == null) {
            suaHandler = new SUAHandler(context, retrofit);
        }
        return suaHandler;
    }

    /**
     * The process to display an SUA for the region is
     * 1. See if SUA file already downloaded for region
     * 2. If so, pass on that file is available for display
     * 3. In any case, get the lastest SUA info from the server
     * 4. If SUA file was available and the file name matches that of the
     * server, it means the file is still most current
     * 5. If SUA file not available OR if the file was downloaded but is not the
     * same as no longer current
     * a. download the new file
     * b. if successful download delete the old (if it existed)
     * c. Send up flare that new SUA file available for display
     * 6. If any errors along the way display msg to that effect
     *
     * @param region
     */
    public void displaySuaForRegion(String region) {

    }

    /**
     * SUA files will be stored with a filename associated to the region, e.g. "NewEngland_sterling7_sua.geojson"
     * So only 1 SUA file per region.
     * This method finds out is the specified regions SUA has been download.
     *
     * @param region
     * @return The name of the SUA file (if it exists) that was previously downloaded and saved
     */
    public String seeIfRegionSUAFileExists(String region) {
        String suaFilename = null;
        File fileDir = context.getFilesDir();
        if (fileDir.exists() && fileDir.isDirectory()) {
            final Pattern p = Pattern.compile(region + "_.*\\.geojson");
            File[] flists = fileDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    Timber.d("Found stored sua file with name %1$s", file.getName());
                    return p.matcher(file.getName()).matches();
                }
            });

            // Should only be 0 or 1
            if (flists.length > 0) {
                suaFilename = flists[0].getName().replaceFirst(region, "");
            }
        }
        return suaFilename;
    }

    @NonNull
    private File getFile(String suaFilename) {
        String path = context.getFilesDir().getAbsolutePath() + "/" + suaFilename;
        return new File(path);
    }

    public void deleteSUAFile(String suaFilename) {
        File suaFile = getFile(suaFilename);
        if (suaFile.exists()){
            Timber.d("Deleting existing %1$s file.", suaFilename);
            suaFile.delete();
        }
    }


    public Completable getDownloadSUACompleteable(String regionName, String suaFileName) {
        JSONServerApi jsonServerApi = retrofit.create(JSONServerApi.class);
        return jsonServerApi.downloadSuaFile(suaFileName).flatMapCompletable(responseBody -> {
            return Completable.fromAction(() -> {
                try {
                    writeSUAFileToDevice(regionName, suaFileName, responseBody.body());
                } catch (Throwable throwable) {
                    throw Exceptions.propagate(throwable);
                }
            });
        });
    }

    /**
     * Download and save geojson sua file
     * To allow
     *
     * @param regionName  - e.g. "NewEngland"
     * @param suaFileName - sua geojson file on server
     */
    public void writeSUAFileToDevice(String regionName, String suaFileName, ResponseBody responseBody) {
        try {
            byte[] buffer = new byte[4096];
            int length;
            InputStream inputStream = responseBody.byteStream();
            OutputStream outputStream = context.openFileOutput(regionName + "_" + suaFileName
                    , Context.MODE_PRIVATE);
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (Exception e) {
            Timber.e(e, "Error reading/writing sua geojson file");
        }
    }

    public void readSuaFile(String regionName, String suaFileName) {

    }
}
