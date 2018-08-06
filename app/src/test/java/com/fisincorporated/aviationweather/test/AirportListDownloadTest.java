package com.fisincorporated.aviationweather.test;

import com.fisincorporated.aviationweather.airports.AirportListProcessor;
import com.fisincorporated.aviationweather.dagger.AppModule;
import com.fisincorporated.aviationweather.retrofit.LoggingInterceptor;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;

import static org.junit.Assert.assertTrue;


public class AirportListDownloadTest {

    private OkHttpClient okHttpClient = new AppModule().getOkHttpClient(new LoggingInterceptor());

    @Before
    public void createRetrofit() {
        okHttpClient = new AppModule().getOkHttpClient(new LoggingInterceptor());
    }

    @Test
    public void shouldGetAirportListTest() {
        Single<ResponseBody> observable = getDownloadAirportListObservable();
        TestObserver<ResponseBody> testObserver = new TestObserver<>();
        observable.subscribe(testObserver);
        testObserver.assertNoErrors();
    }

    @Test
    public void shouldSaveAirportListToFileTest() {
        new File("." + AirportListProcessor.AIRPORT_LIST_DIR).mkdir();
        File file = new File("." + AirportListProcessor.AIRPORT_LIST_DIR + "/" + AirportListProcessor.AIRPORT_LIST_FILENAME);

        Single<ResponseBody> observable = getDownloadAirportListObservable();

        AirportListProcessor.saveAirportListToFile(".", observable.blockingGet());

        assertTrue(file.exists());

    }

    private Single<ResponseBody> getDownloadAirportListObservable(){
        AirportListProcessor airportListProcessor = new AirportListProcessor();
        return  airportListProcessor.getDownloadAirportListObservable(okHttpClient);
    }
}
