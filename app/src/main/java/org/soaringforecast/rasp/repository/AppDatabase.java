package org.soaringforecast.rasp.repository;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;

@Database(entities = {Airport.class, Turnpoint.class, Task.class, TaskTurnpoint.class}, version = 2, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract AirportDao getAirportDao();

    public abstract TurnpointDao getTurnpointDao();

    public abstract TaskDao getTaskDao();

    public abstract TaskTurnpointDao getTaskTurnpointDao();


    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app_database")
                             .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public int getDatabaseVersion(){
        return (INSTANCE != null ? INSTANCE.getOpenHelper().getReadableDatabase().getVersion() : 0);
    }

    /**
     *  Add runway width to turnpoint database
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE  Turnpoint  ADD COLUMN runwayWidth TEXT ");
        }
    };

}
