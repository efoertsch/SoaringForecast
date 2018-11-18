package com.fisincorporated.soaringforecast.repository;

import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Base DAO
 * @param <T>
 */
public interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(T obj);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(T... obj);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(List<T> list);

    @Update
    void update(T obj);

}