package com.fisincorporated.aviationweather.repository;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Single;

@Dao
public interface TurnpointDao extends BaseDao<Turnpoint> {

    @Query("Select * from turnpoint order by title")
    Maybe<List<Turnpoint>> listAllTurnpoints();

    @Query("Delete from turnpoint")
    int deleteAllTurnpoints();

    @Query("Select * from turnpoint where title like :searchTerm or code like :searchTerm  order by title, code collate nocase")
    Maybe<List<Turnpoint>> findTurnpoints(String searchTerm);

    @Query("Select * from turnpoint where title = :title and code = :code collate nocase")
    Maybe<Turnpoint> getTurnpoint(String title, String code);

    @Query("Select count(*) from turnpoint")
    Single<Integer> getTurnpointCount();

    @Query("Select * from turnpoint  ORDER BY ROWID ASC LIMIT 1")
    Maybe<Turnpoint> checkForAtLeastOneTurnpoint();

}
