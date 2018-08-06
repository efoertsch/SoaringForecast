package com.fisincorporated.aviationweather.repository;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface AirportDao {

    @Query("SELECT * FROM airport WHERE ident like :ident or name like :name or " +
            "municipality like :municipality")
    LiveData<List<Airport>> findAirports(String ident, String name, String municipality);

    @Query("SELECT * FROM airport WHERE ident = :ident")
    LiveData<Airport> getAirportByIdent(String ident);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAirport(Airport airport);

    @Delete
    void deleteAirport(Airport airport);

    @Query("SELECT count(*) FROM airport")
    int getCountOfAirports();

}
