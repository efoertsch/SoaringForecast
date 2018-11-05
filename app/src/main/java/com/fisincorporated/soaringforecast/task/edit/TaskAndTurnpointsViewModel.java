package com.fisincorporated.soaringforecast.task.edit;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.res.Resources;
import android.databinding.Bindable;
import android.location.Location;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.common.ObservableViewModel;
import com.fisincorporated.soaringforecast.messages.DataBaseError;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.repository.Task;
import com.fisincorporated.soaringforecast.repository.TaskTurnpoint;
import com.fisincorporated.soaringforecast.repository.Turnpoint;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class TaskAndTurnpointsViewModel extends ObservableViewModel {

    private AppRepository appRepository;
    private long taskId = -1;
    private Task task = null;
    private List<TaskTurnpoint> deletedTaskTurnpoints = new ArrayList<>();
    private MutableLiveData<List<TaskTurnpoint>> taskTurnpoints = new MutableLiveData<>();
    private MutableLiveData<List<Turnpoint>> turnpoints = new MutableLiveData<>();
    private MutableLiveData<Integer> numberSearchableTurnpoints = new MutableLiveData<>();
    private MutableLiveData<Boolean> needToSaveUpdates = new MutableLiveData<>();
    private MutableLiveData<Float> taskDistance = new MutableLiveData<>();
    private boolean retrievedTask = false;
    private boolean retrievedTaskTurnpoints = false;
    private MutableLiveData<Boolean> working = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public TaskAndTurnpointsViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        working.setValue(false);
        return this;
    }

    public TaskAndTurnpointsViewModel setTaskId(long taskId) {
        if (taskId == -1 || this.taskId != taskId) {
            this.taskId = taskId;
            task = null;
            retrievedTask = false;
            retrievedTaskTurnpoints = false;
            needToSaveUpdates.setValue(false);
            taskTurnpoints.setValue(new ArrayList<>());
            taskDistance.setValue(0f);
            if (taskId == -1) {
                task = new Task();
                task.setId(taskId);  //-1
                task.setTaskName(Resources.getSystem().getString(R.string.new_task_name));
            } else {
                getTask();
            }
        }
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
    private void loadTask() {
        appRepository.getTask(taskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(task -> {
                            this.task = task;
                            notifyChange();
                        },
                        t -> {
                            EventBus.getDefault().post(new DataBaseError(Resources.getSystem().getString(R.string.error_reading_task), t));
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
                            EventBus.getDefault().post(new DataBaseError(Resources.getSystem().getString(R.string.error_loading_task_and_turnpoints), t));
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
        working.setValue(true);
        Disposable disposable;
        if (task.getId() != -1) {
            disposable = appRepository.updateTaskAndTurnpoints(task, taskTurnpoints.getValue(), deletedTaskTurnpoints)
                    .subscribe(() -> {
                        needToSaveUpdates.setValue(false);
                    }, t -> {
                        EventBus.getDefault().post(new DataBaseError(Resources.getSystem().getString(R.string.error_adding_task_and_turnpoints), t));
                    });

        } else { // adding new task/turnpoints
            disposable = appRepository.addNewTaskAndTurnpoints(task, taskTurnpoints.getValue())
                    .subscribe(taskId -> {
                        this.taskId = taskId;
                        needToSaveUpdates.setValue(false);
                    }, t -> {
                        EventBus.getDefault().post(new DataBaseError(Resources.getSystem().getString(R.string.error_adding_task_and_turnpoints), t));
                    });

        }
        compositeDisposable.add(disposable);

    }

    public void renumberTurnpoints() {
        List<TaskTurnpoint> taskTurnpointList = taskTurnpoints.getValue();
        if (taskTurnpointList == null) {
            return;
        }
        int size = taskTurnpointList.size();
        for (int i = 0; i < size; ++i) {
            TaskTurnpoint taskTurnpoint = taskTurnpointList.get(i);
            taskTurnpoint.setTaskOrder(i);
            taskTurnpoint.setLastTurnpoint(i == (size - 1));
            calcTurnpointDistances(i);
        }
        setTaskDistance();
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
                            EventBus.getDefault().post(new DataBaseError(Resources.getSystem().getString(R.string.error_searching_turnpoints), t));
                        });
        return turnpoints;
    }

    public void addTaskTurnpoint(TaskTurnpoint taskTurnpoint) {
        taskTurnpoints.getValue().add(taskTurnpoint);
        int numberTurnpoints = taskTurnpoints.getValue().size();
        if (numberTurnpoints > 1) {
            taskTurnpoints.getValue().get(numberTurnpoints - 2).setLastTurnpoint(false);
        }
        taskTurnpoint.setLastTurnpoint(true);
        taskTurnpoint.setTaskOrder(numberTurnpoints - 1);
        calcTurnpointDistances(numberTurnpoints - 1);
        setTaskDistance();
        needToSaveUpdates.setValue(true);
    }

    private void setTaskDistance() {
        List<TaskTurnpoint> taskTurnpointList = taskTurnpoints.getValue();
        if (taskTurnpointList == null || taskTurnpointList.size() == 0) {
            task.setDistance(0);
        } else {
            task.setDistance(taskTurnpointList.get(taskTurnpointList.size() - 1).getDistanceFromStartingPoint());
        }
        taskDistance.setValue(task.getDistance());
    }

    public MutableLiveData<Float> getTaskDistance() {
        return taskDistance;
    }


    private void calcTurnpointDistances(int turnpointNumber) {
        List<TaskTurnpoint> taskTurnpointList = taskTurnpoints.getValue();
        TaskTurnpoint fromTaskTurnpoint;
        TaskTurnpoint toTaskTurnpoint;
        float[] results = new float[1];
        if (taskTurnpointList == null || taskTurnpointList.size() == 0) {
            return;
        }

        if (turnpointNumber == 0) {
            fromTaskTurnpoint = taskTurnpointList.get(0);
            fromTaskTurnpoint.setDistanceFromPriorTurnpoint(0);
            fromTaskTurnpoint.setDistanceFromStartingPoint(0);
        } else {
            // get lat/long from prior turnpoint and calc distance from that one to current turnpoint
            taskTurnpointList = taskTurnpoints.getValue();
            fromTaskTurnpoint = taskTurnpointList.get(turnpointNumber - 1);
            toTaskTurnpoint = taskTurnpointList.get(turnpointNumber);
            Location.distanceBetween(fromTaskTurnpoint.getLatitudeDeg(), fromTaskTurnpoint.getLongitudeDeg(),
                    toTaskTurnpoint.getLatitudeDeg(), toTaskTurnpoint.getLongitudeDeg(), results);
            toTaskTurnpoint.setDistanceFromPriorTurnpoint(results[0] / 1000);
            toTaskTurnpoint.setDistanceFromStartingPoint(fromTaskTurnpoint.getDistanceFromStartingPoint() + results[0] / 1000);
        }
    }

    public MutableLiveData<Integer> getNumberOfSearchableTurnpoints() {
        if (numberSearchableTurnpoints.getValue() == null || numberSearchableTurnpoints.getValue() == 0) {
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
                            EventBus.getDefault().post(new DataBaseError(Resources.getSystem().getString(R.string.error_getting_number_of_turnpoints), throwable));
                        });

    }

    public MutableLiveData<Boolean> getNeedToSaveUpdates() {
        return needToSaveUpdates;
    }

    public MutableLiveData<Boolean> getWorking() {
        return working;
    }


    @Override
    public void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();

    }

}
