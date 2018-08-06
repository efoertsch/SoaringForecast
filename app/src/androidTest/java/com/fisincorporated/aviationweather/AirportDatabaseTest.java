package com.fisincorporated.aviationweather;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fisincorporated.aviationweather.airports.AirportListProcessor;
import com.fisincorporated.aviationweather.repository.Airport;
import com.fisincorporated.aviationweather.repository.AirportDao;
import com.fisincorporated.aviationweather.repository.AppDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class AirportDatabaseTest {
    private AirportDao airportDao;
    private AppDatabase appDatabase;
    private OkHttpClient okHttpClient = new OkHttpClient();


    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        airportDao = appDatabase.getAirportDao();
    }

    @After
    public void closeDb() throws IOException {
        appDatabase.close();
    }

    @Test
    public void saveOneAirportToDB() {
        MutableLiveData<Airport> airport = new MutableLiveData<>();
        airport.setValue(Airport.getNewAirport());
        airport.getValue().setIdent("3B3");
        airport.getValue().setName("Sterling Airport");
        airport.getValue().setMunicipality("Sterling");
        airport.getValue().setState("MA");
        airportDao.insertAirport(airport.getValue());
        airport.setValue(airportDao.getAirportByIdent("3B3").getValue());
        assertNotNull(airport);
        LiveData<List<Airport>> airports = airportDao.findAirports("3B3", "xxx", "xxxx");
        assert (airports.getValue().size() == 1);
        airports = airportDao.findAirports("xxx", "Sterling Airport", "xxxx");
        assert (airports.getValue().size() == 1);

        airports = airportDao.findAirports("xxx", "xxx", "Sterling");
        assert (airports.getValue().size() == 1);

    }

    @Test
    public void downloadAirportFileAndSaveToDB() {
        airportDao = appDatabase.getAirportDao();
        AirportListProcessor airportListProcessor = new AirportListProcessor(okHttpClient, airportDao);
        airportListProcessor.downloadAirportsToDB(okHttpClient).blockingAwait();
        assert(airportDao.getCountOfAirports() > 500);
    }
}
