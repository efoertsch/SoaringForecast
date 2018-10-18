package com.fisincorporated.aviationweather.task.edit;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.fisincorporated.aviationweather.common.ObservableViewModel;
import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.repository.Task;
import com.fisincorporated.aviationweather.repository.TaskTurnpoint;
import com.fisincorporated.aviationweather.repository.Turnpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class EditTaskViewModel extends ObservableViewModel {

    private AppRepository appRepository;
    private long taskId = -1;
    private Task task = null;
    private List<TaskTurnpoint> deletedTaskTurnpoints = new ArrayList<>();
    private MutableLiveData<List<TaskTurnpoint>> taskTurnpoints = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private boolean retrievedTask = false;
    private boolean retrievedTaskTurnpoints = false;
    private boolean needToSaveUpdates;
    private boolean firstTimeCall = true;
    private int maxTurnpointOrderNumber;
    private MutableLiveData<List<Turnpoint>> turnpoints = new MutableLiveData<>();
    private MutableLiveData<Integer> numberSearchableTurnpoints;


    public EditTaskViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }

    public EditTaskViewModel setTaskId(long taskId) {
        if (this.taskId == -1 || this.taskId != taskId) {
            task = null;
            taskTurnpoints.setValue(new ArrayList<>());
            retrievedTaskTurnpoints = false;
            needToSaveUpdates = false;
        }
        this.taskId = taskId;
        return this;
    }

    @SuppressLint("CheckResult")
    public Task getTask() {
        if (!retrievedTask) {
            appRepository.getTask(taskId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(task -> {
                                this.task = task;
                                retrievedTask = true;
                                notifyChange();
                            },
                            t -> {
                                Timber.e(t);
                            });
        }
        return task;
    }

    @SuppressLint("CheckResult")
    public LiveData<List<TaskTurnpoint>> getTaskTurnpoints() {
        if (!retrievedTaskTurnpoints) {
            appRepository.getTaskTurnpionts(taskId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(newTaskTurnpoints -> {
                                taskTurnpoints.setValue(newTaskTurnpoints);
                                retrievedTaskTurnpoints = true;
                                notifyChange();
                            },
                            t -> {
                                Timber.e(t);
                            });
        }
        return taskTurnpoints;
    }

    public String getTaskName() {
        if (task != null) {
            return task.getTaskName();
        } else {
            return "";
        }
    }

    public void setTaskName(String value) {
        if (task != null) {
            if (!task.getTaskName().equals(value)) {
                task.setTaskName(value);
                needToSaveUpdates = true;
            }
        }
    }

    public void deleteTaskTurnpoint(TaskTurnpoint taskTurnpoint) {
        Completable completable = appRepository.deleteTaskTurnpoint(taskTurnpoint);
        Disposable disposable = completable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    //complete
                }, throwable -> {
                    // TODO Display some error
                });
        compositeDisposable.add(disposable);
    }

    public void updateTaskTurnpoints(List<TaskTurnpoint> taskTurnpoints) {
        Completable completable = appRepository.updateTaskTurnpoints(taskTurnpoints);
        Disposable disposable = completable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    //complete
                }, throwable -> {
                    // TODO Display some error

                });
        compositeDisposable.add(disposable);
    }

    public void saveTask() {
        //TO DO save task and task turnpoints
        appRepository.updateTask(task);
        appRepository.updateTaskTurnpoints(taskTurnpoints.getValue());
    }

    public void swapTurnpoints(int fromPosition, int toPosition) {
        Collections.swap(taskTurnpoints.getValue(), fromPosition, toPosition);
        renumberTurnpoints();
        needToSaveUpdates = true;
    }

    private void renumberTurnpoints() {
        int i = 0;
        for (TaskTurnpoint taskTurnpoint : taskTurnpoints.getValue()) {
            taskTurnpoint.setTaskOrder(i++);
        }
    }

    public void deleteTaskTurnpoint(int position) {
        deletedTaskTurnpoints.add(taskTurnpoints.getValue().get(position));
        taskTurnpoints.getValue().remove(position);
        needToSaveUpdates = true;
    }

    public int getMaxTurnpointOrder() {
        int size = taskTurnpoints.getValue().size();
        if (size == 0) {
            maxTurnpointOrderNumber = 0;
        } else {
            maxTurnpointOrderNumber = taskTurnpoints.getValue().get(size - 1).getTaskOrder();
        }
        return maxTurnpointOrderNumber;

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
        taskTurnpoint.setTaskOrder(++maxTurnpointOrderNumber);
        taskTurnpoints.getValue().add(taskTurnpoint);
    }

    public  MutableLiveData<Integer> getNumberOfSearchableTurnpoints() {
        if (numberSearchableTurnpoints == null){
            numberSearchableTurnpoints = new MutableLiveData<>();
            getTotalSearchableTurnpoints();
        }
        return  numberSearchableTurnpoints;

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

}
