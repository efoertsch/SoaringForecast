package com.fisincorporated.aviationweather.task.edit;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.repository.Task;
import com.fisincorporated.aviationweather.repository.TaskTurnpoint;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class EditTaskViewModel extends ViewModel {

    private AppRepository appRepository;
    private MutableLiveData<Task> selectedTask = new MutableLiveData<>();
    private MutableLiveData<List<TaskTurnpoint>> taskTurnpoints = new MutableLiveData<>();


    public EditTaskViewModel setAppRepository(AppRepository appRepository){
        this.appRepository = appRepository;
        taskTurnpoints = new MutableLiveData<>();
        taskTurnpoints.setValue(new ArrayList<>());
        return this;
    }

    @SuppressLint("CheckResult")
    public LiveData<Task> getTask(long taskId){
        appRepository.getTask(taskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(task -> {
                    selectedTask.setValue(task);
                },
                        t -> {      Timber.e(t);
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
                        },
                        t -> {
                            Timber.e(t);
                        });
        return taskTurnpoints;
    }
}
