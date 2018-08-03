package com.fisincorporated.aviationweather.airports;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Airport.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract Airport airportDao();

}
