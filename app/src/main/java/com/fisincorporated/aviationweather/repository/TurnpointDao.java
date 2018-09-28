package com.fisincorporated.aviationweather.repository;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Maybe;

@Dao
public interface TurnpointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTurnpoint(Turnpoint turnpoint);

    @Delete
    void deleteTurnpoint(Turnpoint turnpoint);

    @Query("Select * from turnpoint where title like :searchTerm or code like :searchTerm collate nocase")
    Maybe<List<Turnpoint>> findTurnpoints(String searchTerm);

    @Query("Select * from turnpoint where title = :title and code = :code collate nocase")
    Maybe<Turnpoint> getTurnpoint(String title, String code);




}
