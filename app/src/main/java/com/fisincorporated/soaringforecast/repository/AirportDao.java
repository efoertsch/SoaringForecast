package com.fisincorporated.soaringforecast.repository;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Single;

@Dao
public interface AirportDao extends BaseDao<Airport> {

    //TODO figure out how to return LiveData results but that can also be testable under AndroidJUnit4

    @Query("SELECT * FROM airport WHERE ident like :searchTerm or name like :searchTerm  or " +
            "municipality like :searchTerm  collate nocase")
    Maybe<List<Airport>> findAirports(String searchTerm );

    @Query("SELECT * FROM airport WHERE ident = :ident  collate nocase")
    Maybe<Airport> getAirportByIdent(String ident);

    @Query("Select * from airport order by state, name")
    Maybe<List<Airport>> listAllAirports();

    @Query("Select * from airport where ident in (:iacoAirports)")
    Maybe<List<Airport>> selectIcaoIdAirports(List<String> iacoAirports);

    @Query("SELECT count(*) FROM airport")
    Single<Integer> getCountOfAirports();

    @Query("Delete from airport")
    int deleteAll();
}
