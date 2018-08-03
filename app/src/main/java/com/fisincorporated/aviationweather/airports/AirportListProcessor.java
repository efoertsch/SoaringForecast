package com.fisincorporated.aviationweather.airports;

import android.os.AsyncTask;

import com.fisincorporated.aviationweather.retrofit.AirportListDownloadApi;
import com.fisincorporated.aviationweather.retrofit.AirportListRetrofit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import timber.log.Timber;


/**
 * Download the airport list
 * Only process US airports
 * Read file and process in Realm database
 */
public class AirportListProcessor {

    private final OkHttpClient okHttpClient;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public static final String AIRPORT_LIST_DIR = "/airportlist";
    public static final String AIRPORT_LIST_FILENAME = "airportlist.csv";

    private String path;
    private ResponseBody responseBody;
    private AirportDao airportDao;
    Disposable disposable;


    public AirportListProcessor(OkHttpClient okHttpClient, AirportDao airportDao) {
        this.okHttpClient = okHttpClient;
        this.airportDao = airportDao;

    }

    public void doAirpportListDownloadAndStore(){
       disposable = downloadAirportsToDB(okHttpClient).subscribeWith(new DisposableCompletableObserver() {
           @Override
           public void onComplete() {
               // all done
           }

           @Override
           public void onError(Throwable e) {
                Timber.d(e);
           }
       });
    }

    public void cancelAirportListDownloadAndStore(){
        disposable.dispose();
    }

    public Completable downloadAirportsToDB(OkHttpClient okHttpClient) {
        return getDownloadAirportListObservable(okHttpClient).flatMapCompletable(responseBody ->
                saveAirportListToDB(responseBody));
    }

    public Single<ResponseBody> getDownloadAirportListObservable(OkHttpClient okHttpClient) {
        Retrofit retrofit = new AirportListRetrofit(okHttpClient).getRetrofit();
        AirportListDownloadApi downloadService = retrofit.create(AirportListDownloadApi.class);
        Single<ResponseBody> observable = downloadService.downloadAirportList();
        return observable;
    }


    public Completable saveAirportListToDB(ResponseBody responseBody) {
        return new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver s) {
                try {
                    int linesRead = 0;
                    BufferedReader reader = null;
                    String airportLine;
                    int usAirports = 0;

                    try {
                        Timber.d("File Size=" + responseBody.contentLength());
                        reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));

                        airportLine = reader.readLine();
                        while (airportLine != null) {
                            linesRead++;
                            if (linesRead == 2 || airportLine.contains("US-")) {
                                storeAirportInDB(airportLine);
                                usAirports++;
                            }
                            airportLine = reader.readLine();
                        }
                        Timber.d("Lines read: %1$d   Lines written %2$d", linesRead, usAirports);
                        Timber.d("File saved successfully!");
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                        Timber.d("Failed to save the file!");
                        return;
                    } finally {
                        if (reader != null) reader.close();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Timber.d("Failed to save the file!");
                    return;
                }
            }
        };
    }

    private void storeAirportInDB(String airportLine) {
        airportDao.insertAirport(Airport.createAirport(airportLine));
    }


    // For Android path should be something like "/data/data/" + packageName
    public void saveAirportListFile(Single<ResponseBody> single, String path) {
        compositeDisposable.add(single.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(responseBody -> airportDownloadAsync(path, responseBody).execute(),
                        throwable -> {
                            Timber.d("Error: %s ", throwable.getMessage());
                            //TODO - put error on bus
                            throwable.printStackTrace();
                        }));
    }

    private static AsyncTask<Void, ResponseBody, Void> airportDownloadAsync(String path,
                                                                            ResponseBody responseBody) {
        return new AsyncTask<Void, ResponseBody, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if ((responseBody != null)) {
                    saveAirportListToFile(path, responseBody);
                }
                return null;
            }
        };
    }

    // For Android path should be something like "/data/data/" + packageName
    // Set to only save US airports
    public static void saveAirportListToFile(String path, ResponseBody responseBody) {
        try {
            int linesRead = 0;
            int linesWritten = 0;
            BufferedReader reader = null;
            BufferedWriter writer = null;
            String airportLine;
            createAirportFileDirectory(path);
            File destinationFile = getAirportFilePathAndName(path);

            try {
                Timber.d("File Size=" + responseBody.contentLength());
                reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destinationFile)));

                airportLine = reader.readLine();
                while (airportLine != null) {
                    linesRead++;
                    if (linesRead == 1 || airportLine.contains("US-")) {
                        writer.write(airportLine);
                        linesWritten++;
                    }
                    airportLine = reader.readLine();
                }
                Timber.d("Lines read: %1$d   Lines written %2$d", linesRead, linesWritten);

                writer.flush();

                Timber.d("File saved successfully!");
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Timber.d("Failed to save the file!");
                return;
            } finally {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Timber.d("Failed to save the file!");
            return;
        }
    }

    private static void createAirportFileDirectory(String path) {
        new File(path + AIRPORT_LIST_DIR).mkdir();
    }

    private static File getAirportFilePathAndName(String path) {
        return new File(path + AIRPORT_LIST_DIR + "/" + AIRPORT_LIST_FILENAME);
    }

}
