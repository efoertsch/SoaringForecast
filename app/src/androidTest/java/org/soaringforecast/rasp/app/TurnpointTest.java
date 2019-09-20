package org.soaringforecast.rasp.app;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.maps.model.LatLng;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.soaringforecast.rasp.repository.AppDatabase;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Turnpoint;

import java.util.List;

import timber.log.Timber;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TurnpointTest {
    private AppRepository appRepository;
    private AppDatabase appDatabase;
    private Turnpoint turnpoint;

    private String sterling = "\"Sterling\",\"3B3\",US,4225.500N,07147.470W,459ft,5,160,3086ft,122.900,\"Home Field, Finish Point, Turn Point, 3B3, RW width: 40, CTAF: 122.9, Fuel: 100LL\"";
    private String sterlingUpdate = "\"Sterling\",\"3B3\",UK,4225.501N,07147.471W,460ft,5,160,3000ft,122.800,\"Home Field, Finish Point, Turn Point, 3B3, RW width: 40, CTAF: 122.8, Fuel: 100LL\"";
    private String beverly = "\"Beverly Muni\",\"BVY\",US,4235.050N,07054.983W,108ft,5,157,5001ft,125.200,\"Turn Point, BVY, 09/27 50A, RW width: 100, ATIS: 119.2, Tower: 125.2, UNICOM: 122.95, Fuel: 100LL\"";
    private String candelight = "\"Candlelight\",\"11N\",US,4134.000N,07327.967W,676ft,2,167,2900ft,122.900,\"Turn Point, 11N, 16/34 29T, RW width: 50, CTAF: 122.9\"";
    private String claremont = " \"Claremont\",\"CNH\",US,4322.233N,07222.067W,545ft,5,109,3098ft,122.700,\"Turn Point, CNH, 05/23 20A, RW width: 100, UNICOM: 122.7, Fuel: 100LL\"";


    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule =
            new InstantTaskExecutorRule();

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        appRepository = AppRepository.getAppRepository(context,null, null, null, null, null, null);
        //appRepository.deleteAllTurnpoints();
    }

    @After
    public void closeDb() {
        appDatabase.close();
    }

    @Test
    public void savedTurnpointToDatabaseTest() {
        turnpoint = Turnpoint.createTurnpointFromCSVDetail(sterling);
        long result = appRepository.insertTurnpoint(turnpoint);
        Timber.d("Result : %1$d", result);
        assertTrue("result should be > 0 is" + result, result > 0);
    }

    @Test
    public void found1TurnPointTest() {
        savedTurnpointToDatabaseTest();
        Turnpoint turnpoint = appRepository.getTurnpoint("Sterling", "3B3").blockingGet();
        assertNotNull(turnpoint);
    }

    @Test
    public void foundTurnPointsTest() {
        turnpoint = Turnpoint.createTurnpointFromCSVDetail(sterling);
        appRepository.insertTurnpoint(turnpoint);
        turnpoint = Turnpoint.createTurnpointFromCSVDetail(beverly);
        appRepository.insertTurnpoint(turnpoint);
        turnpoint = Turnpoint.createTurnpointFromCSVDetail(candelight);
        appRepository.insertTurnpoint(turnpoint);
        turnpoint = Turnpoint.createTurnpointFromCSVDetail(claremont);
        appRepository.insertTurnpoint(turnpoint);
        List<Turnpoint> turnpoints = appRepository.findTurnpoints("Sterling").blockingGet();
        assertNotNull(turnpoints);
        assert (turnpoints.size() == 4);
    }

    @Test
    public void sterlingUpdatedOK() {
        turnpoint = Turnpoint.createTurnpointFromCSVDetail(sterling);
        appRepository.insertTurnpoint(turnpoint);

        Turnpoint turnpointUpdate = Turnpoint.createTurnpointFromCSVDetail(sterlingUpdate);

        long result = appRepository.insertTurnpoint(turnpointUpdate);
        Turnpoint updatedTurnpoint = appRepository.getTurnpoint(turnpointUpdate.getTitle(), turnpointUpdate.getCode()).blockingGet();

        assertEquals("Latitude not updated ", turnpointUpdate.getLatitudeDeg(), updatedTurnpoint.getLatitudeDeg());
        assertEquals("longitude not updated ", turnpointUpdate.getLongitudeDeg(), updatedTurnpoint.getLongitudeDeg());
        assertEquals("Country not updated", turnpointUpdate.getCountry(), updatedTurnpoint.getCountry());
        assertEquals("Elevation not updated", turnpointUpdate.getElevation(), updatedTurnpoint.getElevation());
        assertEquals("style not updated", turnpointUpdate.getStyle(), updatedTurnpoint.getStyle());
        assertEquals("Direction not updated", turnpointUpdate.getDirection(), updatedTurnpoint.getDirection());
        assertEquals("Length not update", turnpointUpdate.getLength(), updatedTurnpoint.getLength());
        assertEquals("Frequency not updated", turnpointUpdate.getFrequency(), updatedTurnpoint.getFrequency());
        assertEquals("Description not update", turnpointUpdate.getDescription(), updatedTurnpoint.getDescription());

    }


    // TODO need to import turnpoints first as starting with empty file
    @Test
    public void getTurnpointsForRegionTest() {
        List<Turnpoint> turnpointList = appRepository.getTurnpointsInRegion(new LatLng(41.2665329, -73.6473083), new LatLng(45.0120811, -70.5046997)).blockingGet();
        assertNotNull(turnpointList);
        assert (turnpointList.size() > 0);
        for (Turnpoint turnpoint : turnpointList) {
            Timber.d(turnpoint.toString());
        }
    }

}
