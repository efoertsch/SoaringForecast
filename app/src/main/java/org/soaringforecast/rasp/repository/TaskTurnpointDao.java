package org.soaringforecast.rasp.repository;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Maybe;

@Dao
public interface TaskTurnpointDao extends BaseDao<TaskTurnpoint> {

    @Query("Select * from taskturnpoint where taskId = :taskId order by taskOrder")
    Maybe<List<TaskTurnpoint>> getTaskTurnpoints(long taskId);

    @Query("Select max(taskOrder) from taskturnpoint where taskId = :taskId")
    Maybe<Integer> getMaxTaskOrderForTask(long taskId);

    @Query("Delete from taskturnpoint where taskId = :taskId")
    int deleteTaskTurnpoints(long taskId);

    @Query("Delete from taskturnpoint where id = :id and taskId = :taskId ")
    int deleteTaskTurnpoint(long id, long taskId);


}
