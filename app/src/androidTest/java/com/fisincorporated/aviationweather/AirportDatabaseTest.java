package com.fisincorporated.aviationweather;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fisincorporated.aviationweather.airport.AirportListDownloader;
import com.fisincorporated.aviationweather.repository.Airport;
import com.fisincorporated.aviationweather.repository.AppDatabase;
import com.fisincorporated.aviationweather.repository.AppRepository;

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
    private AppRepository appRepository;
    private AppDatabase appDatabase;
    private OkHttpClient okHttpClient = new OkHttpClient();


    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        appRepository = AppRepository.getAppRepository(context);

    }

    @After
    public void closeDb() throws IOException {
        appDatabase.close();
    }

    @Test
    public void saveOneAirportToDB() {
        Airport airport = Airport.getNewAirport();
        airport.setIdent("3B3");
        airport.setName("Sterling Airport");
        airport.setMunicipality("Sterling");
        airport.setState("MA");
        appRepository.insertAirport(airport);
        airport = appRepository.getAirport("3B3").blockingGet();
        assertNotNull(airport);
        List<Airport> airports = appRepository.findAirports("3B3").blockingGet();
        assert (airports.size() == 1);
        airports = appRepository.findAirports( "Sterling Airport").blockingGet();
        assert (airports.size() == 1);

        airports = appRepository.findAirports("Sterling").blockingGet();
        assert (airports.size() == 1);

    }

    @Test
    public void downloadAirportFileAndSaveToDB() {
        AirportListDownloader airportListDownloader = new AirportListDownloader(okHttpClient, appRepository);
        airportListDownloader.downloadAirportsToDB(okHttpClient).blockingAwait();
        assert(appRepository.getCountOfAirports().blockingGet() > 500);
    }
}
