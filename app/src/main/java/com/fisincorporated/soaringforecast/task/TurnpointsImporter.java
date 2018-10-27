package com.fisincorporated.soaringforecast.task;

import android.os.Environment;

import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.repository.Turnpoint;
import com.fisincorporated.soaringforecast.retrofit.TurnpointFileApi;
import com.fisincorporated.soaringforecast.retrofit.TurnpointFileRetrofit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import timber.log.Timber;

public class TurnpointsImporter {

    private AppRepository appRepository;
    private OkHttpClient okHttpClient;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    @Inject
    public TurnpointsImporter(AppRepository appRepository) {
        this.appRepository = appRepository;
    }

    public TurnpointsImporter setOkHttpClient(OkHttpClient okHttpClient){
        this.okHttpClient = okHttpClient;
        return this;
    }


    public Completable importTurnpointFileCompletable(final String fileName) {
        return new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver s) {
                try {
                    importTurnpointsFromDownloadDirectory(fileName);
                    s.onComplete();
                } catch (IOException e) {
                    s.onError(e);
                }
            }
        };
    }

    /**
     * @param fileName SeeYou cup file name in device Download directory
     * @return success if import went OK
     * @throws IOException
     */
    public boolean importTurnpointsFromDownloadDirectory(final String fileName) throws IOException {
        BufferedReader reader = null;
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String cupFilePath = path.getAbsoluteFile() + "/" + fileName;
        try {
            Timber.d("Turnpoint file: %1$s", fileName);
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(cupFilePath)));
            saveTurnpoints(reader);
            return true;
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException ignored) {
            }
        }
    }

    public Single<Integer> importTurnpointsFromUrl(String url) {
        return getDownloadTurnpointsObservable(okHttpClient, url)
                .flatMap(responseBody -> saveTurnpointsFromUrl(responseBody));
    }

    public Single<ResponseBody> getDownloadTurnpointsObservable(OkHttpClient okHttpClient, String url) {
        Retrofit retrofit = new TurnpointFileRetrofit(okHttpClient).getRetrofit();
        TurnpointFileApi downloadService = retrofit.create(TurnpointFileApi.class);
        return downloadService.getCupFile(url);
    }

    /**
     *
     * @param responseBody
     * @return
     * @throws IOException
     */
    public Single<Integer> saveTurnpointsFromUrl(ResponseBody responseBody) {
        return new Single<Integer>() {
            @Override
            protected void subscribeActual(SingleObserver s) {
                BufferedReader reader = null;
                try {
                    Timber.d("File Size= %1$s", responseBody.contentLength());
                    reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
                    int numberTurnpoints = saveTurnpoints(reader);
                    s.onSuccess(numberTurnpoints);
                } catch (IOException e) {
                    e.printStackTrace();
                    Timber.d("Failed to save the file!");
                    s.onError(e);
                } finally {
                    if (reader != null) try {
                        reader.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        };
    }

    private int saveTurnpoints(BufferedReader reader) throws IOException {
        int linesRead = 0;
        int numberTurnpoints = 0;
        String turnpointLine;
        Turnpoint turnpoint;
        turnpointLine = reader.readLine();
        while (turnpointLine != null && !turnpointLine.isEmpty()) {
            linesRead++;
            if (linesRead > 1) {
                turnpoint = Turnpoint.createTurnpointFromCSVDetail(turnpointLine);
                if (turnpoint != null) {
                    appRepository.insertTurnpoint(turnpoint);
                }
                numberTurnpoints++;
            }
            turnpointLine = reader.readLine();
        }
        Timber.d("Lines read: %1$d  Lines written %2$d", linesRead, numberTurnpoints);
        Timber.d("File saved to DB successfully!");
        return numberTurnpoints;
    }

}
