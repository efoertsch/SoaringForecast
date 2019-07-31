package org.soaringforecast.rasp.soaring.forecast;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.retrofit.JSONServerApi;
import org.soaringforecast.rasp.soaring.json.SUARegion;
import org.soaringforecast.rasp.soaring.json.SUARegionFiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.regex.Pattern;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.exceptions.Exceptions;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class SUAHandler {

    private static SUAHandler suaHandler;
    private Context context;
    private JSONServerApi jsonServerApi;

    private SUAHandler() {
    }

    private SUAHandler(Context context, JSONServerApi jsonServerApi) {
        this.context = context;
        this.jsonServerApi = jsonServerApi;
    }

    public static SUAHandler getInstance(Context context, JSONServerApi jsonServerApi) {
        if (suaHandler == null) {
            suaHandler = new SUAHandler(context, jsonServerApi);
        }
        return suaHandler;
    }

    /**
     * The process to display an SUA for the region is
     * 1. See if SUA file already downloaded for region
     * 2. If so, pass on the GeoJson object
     * 3. In any case, get the lastest SUA info from the server
     * 4. If SUA file was available and the file name matches that of the
     * server, it means the file is still most current so stop here
     * 5. If SUA file not available OR the SUA file is no longer current (an updated file is on server)
     * a. download the new file
     * b. if successful download delete the old (if it existed)
     * c. Emit updated GeoJson object
     *
     * @param region
     */
    public Observable<JSONObject> displaySuaForRegion(String region) {
        return Observable.create(emitter -> {
            // return current stored file if there is one
            String oldSuaFilename = seeIfRegionSUAFileExists(region);
            if (oldSuaFilename != null) {
                Timber.d("Found existing SUA file for region: %1$s  filename: %2$s", region, oldSuaFilename);
                emitter.onNext(getSuaJSONObject(region, oldSuaFilename));
            } else {
                Timber.d("No SUA file found on device for region: %1$s ", region);
            }
            // get list of sua files for regions
            SUARegionFiles suaRegionFiles = jsonServerApi.getSUARegions().blockingGet();
            // get file name for region you are on
            if (suaRegionFiles != null) {
                String newSuaFilename = getSuaFilenameForRegion(suaRegionFiles, region);
                // if no new file found (some oops here) or no updated file just end
                if (newSuaFilename == null || newSuaFilename.equalsIgnoreCase(oldSuaFilename)) {
                    Timber.d("No server SUA file found OR current device file same as server version for region: %1$s ", region);
                } else {
                    // else download new file, delete old and emit new sua geojson object
                    Timber.d("Updated SUA file available for region: %1$s  updated file name: %2$s", region, newSuaFilename);
                    getDownloadSUACompleteable(region, newSuaFilename).blockingGet();
                    if (oldSuaFilename != null) {
                        deleteSUAFile(region, oldSuaFilename);
                    }
                    EventBus.getDefault().post(new SnackbarMessage(context.getString(R.string.downloading_new_sua)));
                    emitter.onNext(getSuaJSONObject(region, newSuaFilename));
                }
            } else {
                Timber.d("No server sua_regions file found ");
            }
            emitter.onComplete();
        });
    }

    private String getSuaFilenameForRegion(SUARegionFiles suaRegionFiles, String region) {
        for (SUARegion suaRegion : suaRegionFiles.getSuaRegionList()) {
            if (region.equalsIgnoreCase(suaRegion.getRegion())) {
                return suaRegion.getSuaFileName();
            }
        }
        return null;

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
                suaFilename = flists[0].getName().replaceFirst(region + '_', "");
            }
        }
        return suaFilename;
    }

    @NonNull
    private File getSuaFile(String regionName, String suaFilename) {
        String path = context.getFilesDir().getAbsolutePath() + "/" + getCompleteSuaFilename(regionName, suaFilename);
        return new File(path);
    }

    public void deleteSUAFile(String regionName, String suaFilename) {
        File suaFile = getSuaFile(regionName, suaFilename);
        if (suaFile.exists()) {
            Timber.d("Deleting existing %1$s file.", suaFilename);
            suaFile.delete();
        }
    }

    public void deleteAllSuaFilesOnDevice() {
        File fileDir = context.getFilesDir();
        final Pattern p = Pattern.compile("\\.geojson");
        File[] flists = fileDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                Timber.d("Found stored sua file with name %1$s", file.getName());
                return p.matcher(file.getName()).matches();
            }
        });

        for (int i = 0; i < flists.length; ++i) {
            flists[i].delete();
        }
    }

    public Completable getDownloadSUACompleteable(String regionName, String suaFileName) {
        return jsonServerApi.downloadSuaFile(suaFileName).flatMapCompletable(responseBody -> Completable.fromAction(() -> {
            try {
                writeSUAFileToDevice(regionName, suaFileName, responseBody.body());
                Timber.d("SUA file downloaded from server for region: %1$s  filename: %2$s", regionName, suaFileName);
            } catch (Throwable throwable) {
                throw Exceptions.propagate(throwable);
            }
        }));
    }

    /**
     * Download and save geojson sua file
     * To allow
     *
     * @param regionName  - e.g. "NewEngland"
     * @param suaFileName - sua geojson file on server
     */
    private void writeSUAFileToDevice(String regionName, String suaFileName, ResponseBody responseBody) {
        if (responseBody == null){
            EventBus.getDefault().post(new SnackbarMessage(context.getString(R.string.error_getting_sua_file_for_region, regionName), Snackbar.LENGTH_SHORT));
        }
        try {
            byte[] buffer = new byte[4096];
            int length;
            InputStream inputStream = responseBody.byteStream();
            OutputStream outputStream = context.openFileOutput(getCompleteSuaFilename(regionName, suaFileName)
                    , Context.MODE_PRIVATE);
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (Exception e) {
            Timber.e(e, "Error reading/writing sua geojson file");
            EventBus.getDefault().post(new SnackbarMessage(context.getString(R.string.error_getting_sua_file_for_region, regionName), Snackbar.LENGTH_SHORT));
        }
    }

    @NonNull
    private String getCompleteSuaFilename(String regionName, String suaFileName) {
        StringBuilder sb = new StringBuilder();
        return sb.append(regionName).append('_').append(suaFileName).toString();
    }


    public JSONObject getSuaJSONObject(String regionName, String suaFileName) throws IOException, JSONException {
        InputStream is = new FileInputStream(getSuaFile(regionName, suaFileName));
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder responseStrBuilder = new StringBuilder(500 * 1024);
        String inputStr;
        while ((inputStr = streamReader.readLine()) != null) {
            responseStrBuilder.append(inputStr);
        }
        streamReader.close();
        return new JSONObject(responseStrBuilder.toString());
    }



}
