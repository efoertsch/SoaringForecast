package com.fisincorporated.aviationweather.repository;

import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Update;

/**
 * Base DAO
 * @param <T>
 */
public interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(T obj);

    @Insert
    long[] insert(T... obj);

    @Update
    void update(T obj);

    @Delete
    int delete(T obj);

}