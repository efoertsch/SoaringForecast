package com.fisincorporated.soaringforecast.repository;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Airport.class, Turnpoint.class, Task.class, TaskTurnpoint.class}, version = 1, exportSchema = true)
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
                            //.addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * When doing migration, the sql below was cut pasted from app/schemas/.../*.json
     * And that json file was created because exportSchema = true above and build.gradle file included
     *  javaCompileOptions {
     *     annotationProcessorOptions {
     *     arguments = ["room.schemaLocation": "$projectDir/schemas".toString()]
     *  }
     *  in the defaultConfig section
     *  See https://stackoverflow.com/questions/44322178/room-schema-export-directory-is-not-provided-to-the-annotation-processor-so-we/44424908#44424908
     *  and
     */
//    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
//        @Override
//        public void migrate(SupportSQLiteDatabase database) {
//            database.execSQL("CREATE TABLE IF NOT EXISTS `turnpoint` (`title` TEXT NOT NULL, `code` TEXT NOT NULL, `country` TEXT, `latitudeDeg` REAL NOT NULL, `longitudeDeg` REAL NOT NULL, `elevation` TEXT, `style` TEXT, `direction` TEXT, `length` TEXT, `frequency` TEXT, `description` TEXT, PRIMARY KEY(`title`, `code`))");
//            database.execSQL("CREATE UNIQUE INDEX `index_Turnpoint_title_code` ON `turnpoint` (`title`, `code`)");
//            database.execSQL("CREATE  INDEX `index_Turnpoint_code` ON `turnpoint` (`code`)");
//        }
//    };

}
