package org.soaringforecast.rasp.repository;

import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

import java.util.List;

/**
 * Base DAO
 *
 * @param <T>
 */
interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(T obj);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(T... obj);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(List<T> list);

    @Update
    void update(T obj);

}