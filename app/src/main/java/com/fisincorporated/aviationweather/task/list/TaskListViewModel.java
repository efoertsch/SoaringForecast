package com.fisincorporated.aviationweather.task.list;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.fisincorporated.aviationweather.common.ObservableViewModel;
import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.repository.Task;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class TaskListViewModel extends ObservableViewModel {

    private AppRepository appRepository;
    private MutableLiveData<List<Task>> tasks = new MutableLiveData<>();

    public TaskListViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        tasks = new MutableLiveData<>();
        tasks.setValue(new ArrayList<>());
        return this;
    }

    @SuppressLint("CheckResult")
    public LiveData<List<Task>> listTasks() {
        appRepository.listAllTasks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(taskList -> {
                            tasks.setValue(taskList);
                            notifyChange();
                        },
                        t -> {
                            Timber.e(t);
                        });
        return tasks;
    }


}
