package com.fisincorporated.aviationweather.task.edit;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.fisincorporated.aviationweather.common.ObservableViewModel;
import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.repository.Task;
import com.fisincorporated.aviationweather.repository.TaskTurnpoint;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class EditTaskViewModel extends ObservableViewModel {

    private AppRepository appRepository;
    private Task selectedTask = null;
    private MutableLiveData<List<TaskTurnpoint>> taskTurnpoints = new MutableLiveData<>();


    public EditTaskViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        taskTurnpoints = new MutableLiveData<>();
        taskTurnpoints.setValue(new ArrayList<>());
        return this;
    }

    @SuppressLint("CheckResult")
    public Task getTask(long taskId) {
        appRepository.getTask(taskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(task -> {
                            selectedTask = task;
                            notifyChange();
                        },
                        t -> {
                            Timber.e(t);
                        });
        return selectedTask;
    }

    @SuppressLint("CheckResult")
    public LiveData<List<TaskTurnpoint>> listTaskTurnpoints(long taskId) {
        appRepository.listTaskTurnpionts(taskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(newTaskTurnpoints -> {
                            taskTurnpoints.setValue(newTaskTurnpoints);
                            notifyChange();
                        },
                        t -> {
                            Timber.e(t);
                        });
        return taskTurnpoints;
    }

    public String getTaskName() {

        if (selectedTask != null) {
            return selectedTask.getTaskName();
        } else {
            return "";
        }
    }

    public void setTaskName(String value) {
        if (selectedTask != null) {
            if (!selectedTask.getTaskName().equals(value)) {
                selectedTask.setTaskName(value);
                appRepository.updateTask(selectedTask);
            }
        }
    }

    public Task getTask() {
        return selectedTask;
    }
}
