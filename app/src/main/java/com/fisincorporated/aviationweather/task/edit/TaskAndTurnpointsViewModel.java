package com.fisincorporated.aviationweather.task.edit;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.Bindable;

import com.fisincorporated.aviationweather.common.ObservableViewModel;
import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.repository.Task;
import com.fisincorporated.aviationweather.repository.TaskTurnpoint;
import com.fisincorporated.aviationweather.repository.Turnpoint;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class TaskAndTurnpointsViewModel extends ObservableViewModel {

    private AppRepository appRepository;
    private long taskId = -1;
    private Task task = null;
    private List<TaskTurnpoint> deletedTaskTurnpoints = new ArrayList<>();
    private MutableLiveData<List<TaskTurnpoint>> taskTurnpoints = new MutableLiveData<>();
    private MutableLiveData<List<Turnpoint>> turnpoints = new MutableLiveData<>();
    private MutableLiveData<Integer> numberSearchableTurnpoints;
    private MutableLiveData<Boolean> needToSaveUpdates = new MutableLiveData<>();
    private boolean retrievedTask = false;
    private boolean retrievedTaskTurnpoints = false;

    public TaskAndTurnpointsViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }

    public TaskAndTurnpointsViewModel setTaskId(long taskId) {
        if (this.taskId == -1 || this.taskId != taskId) {
            task = null;
            retrievedTask = false;
            retrievedTaskTurnpoints = false;
            needToSaveUpdates.setValue(false);
            taskTurnpoints.setValue(new ArrayList<>());

        }
        this.taskId = taskId;
        return this;
    }

    @SuppressLint("CheckResult")
    public Task getTask() {
        if (!retrievedTask) {
            retrievedTask = true;
            loadTask();
        }
        return task;
    }

    @SuppressLint("CheckResult")
    public void loadTask() {
        appRepository.getTask(taskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(task -> {
                            this.task = task;
                            notifyChange();
                        },
                        t -> {
                            Timber.e(t);
                        });
    }

    @SuppressLint("CheckResult")
    public LiveData<List<TaskTurnpoint>> getTaskTurnpoints() {
        if (!retrievedTaskTurnpoints) {
            retrievedTaskTurnpoints = true;
            loadTaskTurnpoints();
        }
        return taskTurnpoints;
    }

    @SuppressLint("CheckResult")
    public void loadTaskTurnpoints() {
        appRepository.getTaskTurnpionts(taskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(newTaskTurnpoints -> {
                            taskTurnpoints.setValue(newTaskTurnpoints);
                        },
                        t -> {
                            Timber.e(t);
                        });
    }

    @Bindable
    public String getTaskName() {
        if (task != null) {
            return task.getTaskName();
        } else {
            return "";
        }
    }


    @Bindable
    public void setTaskName(String value) {
        if (task != null) {
            if (!task.getTaskName().equals(value)) {
                task.setTaskName(value);
                needToSaveUpdates.setValue(true);
            }
        }
    }

    public void saveTask() {
        appRepository.updateTaskAndTurnpoints(task, taskTurnpoints.getValue(), deletedTaskTurnpoints);
        needToSaveUpdates.setValue(false);

    }

    public void renumberTurnpoints() {
        int i = 0;
        for (TaskTurnpoint taskTurnpoint : taskTurnpoints.getValue()) {
            taskTurnpoint.setTaskOrder(i++);
        }
        needToSaveUpdates.setValue(true);
    }

    public void deleteTaskTurnpoint(int position) {
        deletedTaskTurnpoints.add(taskTurnpoints.getValue().get(position));
        taskTurnpoints.getValue().remove(position);
        renumberTurnpoints();
        needToSaveUpdates.setValue(true);
    }

    @SuppressLint("CheckResult")
    public LiveData<List<Turnpoint>> searchTurnpoints(String search) {
        appRepository.findTurnpoints("%" + search + "%")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(turnpointList -> {
                            turnpoints.setValue(turnpointList);
                        },
                        t -> {
                            Timber.e(t);
                        });
        return turnpoints;
    }

    public void addTaskTurnpoint(TaskTurnpoint taskTurnpoint) {
        taskTurnpoints.getValue().add(taskTurnpoint);
        taskTurnpoint.setTaskOrder(taskTurnpoints.getValue().size() - 1);
    }

    public MutableLiveData<Integer> getNumberOfSearchableTurnpoints() {
        if (numberSearchableTurnpoints == null) {
            numberSearchableTurnpoints = new MutableLiveData<>();
            getTotalSearchableTurnpoints();
        }
        return numberSearchableTurnpoints;

    }

    @SuppressLint("CheckResult")
    private void getTotalSearchableTurnpoints() {
        appRepository.getCountOfTurnpoints()
                .subscribe(count -> {
                            numberSearchableTurnpoints.setValue(count);
                        }
                        , throwable -> {
                            //TODO
                            Timber.e(throwable);
                        });

    }

    public MutableLiveData<Boolean> getNeedToSaveUpdates() {
        return needToSaveUpdates;
    }

}
