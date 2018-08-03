package com.fisincorporated.aviationweather.airports;

import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

public interface AirportDao {

    @Query("SELECT * FROM airport WHERE ident like :ident or name like :name or " +
            "municipality like :municipality")
    List<Airport> findAirpors(String ident, String name, String municipality);

    @Query("SELECT * FROM airport WHERE ident = :ident")
    Airport getAirportByIdent(String ident);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAirport(Airport airport);

    @Delete
    void deleteAirport(Airport airport);
}
