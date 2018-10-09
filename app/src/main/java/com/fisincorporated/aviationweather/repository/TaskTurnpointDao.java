package com.fisincorporated.aviationweather.repository;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Maybe;

@Dao
public interface TaskTurnpointDao extends BaseDao<TaskTurnpoint> {

    @Query("Select * from taskturnpoint where taskId = :taskId order by taskOrder")
    Maybe<List<TaskTurnpoint>> getTaskTurnpoints(long taskId);

}
