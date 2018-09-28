package com.fisincorporated.aviationweather.app;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.fisincorporated.aviationweather.repository.AppDatabase;
import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.repository.Turnpoint;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class TurnpointTest {
    private AppRepository appRepository;
    private AppDatabase appDatabase;

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
    public void savedTurnpointToDatabaseTest() {
        String turnpointString = "\"Sterling\",\"3B3\",US,4225.500N,07147.470W,459ft,5,160,3086ft,122.900,\"Home Field, Finish Point, Turn Point, 3B3, RW width: 40, CTAF: 122.9, Fuel: 100LL\"";
        Turnpoint turnpoint = Turnpoint.createTurnpointFromCSVDetail(turnpointString);
        appRepository.insertTurnpoint(turnpoint);

    }

    @Test
    public void found1TurnPointTest() {
        savedTurnpointToDatabaseTest();
        Turnpoint turnpoint = appRepository.getTurnpoint("Sterling", "3B3").blockingGet();
        assertNotNull(turnpoint);
    }

    @Test
    public void foundTurnPointsTest() {
        savedTurnpointToDatabaseTest();
        List<Turnpoint> turnpoints = appRepository.findTurnpoints("Sterling").blockingGet();
        assertNotNull(turnpoints);
        assert(turnpoints.size() > 0 );
    }

}
