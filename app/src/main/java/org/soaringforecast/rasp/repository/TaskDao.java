package org.soaringforecast.rasp.repository;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Maybe;

@Dao
public interface TaskDao extends BaseDao<Task> {

    @Query("Select * from task order by taskOrder")
    Maybe<List<Task>> listAllTasks();

    @Query("Select * from task where id = :taskId")
    Maybe<Task> getTask(long taskId);

    @Query("Delete from task where id = :taskId")
    int deleteTask(long taskId);


}
