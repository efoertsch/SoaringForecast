package com.fisincorporated.soaringforecast.airport;

import com.fisincorporated.soaringforecast.repository.Airport;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.retrofit.AirportListDownloadApi;
import com.fisincorporated.soaringforecast.retrofit.AirportListRetrofit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import timber.log.Timber;


/**
 * Download the airport list
 * Only process US airports
 * Read file and process in Room database
 */
public class AirportListDownloader {

    private OkHttpClient okHttpClient;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private AppRepository appRepository;
    private Disposable disposable;

    @Inject
    public AirportListDownloader(OkHttpClient okHttpClient, AppRepository appRepository) {
        this.okHttpClient = okHttpClient;
        this.appRepository = appRepository;
    }

    public void cancelAirportListDownloadAndStore() {
        disposable.dispose();
    }

    public Completable downloadAirportsToDB(OkHttpClient okHttpClient) {
        return getDownloadAirportListObservable(okHttpClient).flatMapCompletable(responseBody ->
                saveAirportListToDB(responseBody));
    }

    public Single<ResponseBody> getDownloadAirportListObservable(OkHttpClient okHttpClient) {
        Retrofit retrofit = new AirportListRetrofit(okHttpClient).getRetrofit();
        AirportListDownloadApi downloadService = retrofit.create(AirportListDownloadApi.class);
        return downloadService.downloadAirportList();
    }


    private Completable saveAirportListToDB(ResponseBody responseBody) {
        return new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver s) {
                Airport airport;
                String airportLine;
                int linesRead = 0;
                int usAirports = 0;
                BufferedReader reader = null;
                try {
                    Timber.d("File Size= %1$s" , responseBody.contentLength());
                    reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
                    airportLine = reader.readLine();
                    while (airportLine != null && !airportLine.isEmpty()) {
                        linesRead++;
                        if (linesRead == 2 || airportLine.contains("US-")) {
                            airport = Airport.createAirportFromCSVDetail(airportLine);
                            if (airport != null && airport.getIdent() != null) {
                                appRepository.insertAirport(airport);
                            }
                            usAirports++;
                        }
                        airportLine = reader.readLine();
                    }
                    Timber.d("Lines read: %1$d   Lines written %2$d", linesRead, usAirports);
                    Timber.d("File saved to DB successfully!");
                    s.onComplete();
                } catch (IOException e) {
                    e.printStackTrace();
                    Timber.d("Failed to save the file!");
                    s.onError(e);
                } finally {
                    if (reader != null) try {reader.close();} catch (IOException ignored){}
                }
            }
        };
    }

}
