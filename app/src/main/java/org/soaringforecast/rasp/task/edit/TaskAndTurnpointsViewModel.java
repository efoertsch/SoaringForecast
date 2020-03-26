package org.soaringforecast.rasp.task.edit;

import android.annotation.SuppressLint;
import android.app.Application;
import android.location.Location;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.ObservableViewModel;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Task;
import org.soaringforecast.rasp.repository.TaskTurnpoint;
import org.soaringforecast.rasp.repository.messages.DataBaseError;
import org.soaringforecast.rasp.soaring.messages.DisplayTurnpointSatelliteView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TaskAndTurnpointsViewModel extends ObservableViewModel {

    private AppRepository appRepository;
    private long taskId = 0;
    private Task task = null;
    private List<TaskTurnpoint> deletedTaskTurnpoints = new ArrayList<>();
    private MutableLiveData<List<TaskTurnpoint>> taskTurnpoints = new MutableLiveData<>();
    private MutableLiveData<Boolean> needToSaveUpdates = new MutableLiveData<>();
    private MutableLiveData<Float> taskDistance = new MutableLiveData<>();
    private boolean retrievedTask = false;
    private boolean retrievedTaskTurnpoints = false;
    private MutableLiveData<Boolean> working = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public TaskAndTurnpointsViewModel(@NonNull Application application) {
        super(application);
    }

    public TaskAndTurnpointsViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        working.setValue(false);
        needToSaveUpdates.setValue(false);
        return this;
    }

    public TaskAndTurnpointsViewModel setTaskId(long taskId) {
        if (taskId == 0 || this.taskId != taskId) {
            this.taskId = taskId;
            task = null;
            retrievedTask = false;
            retrievedTaskTurnpoints = false;
            needToSaveUpdates.setValue(false);
            taskTurnpoints.setValue(new ArrayList<>());
            taskDistance.setValue(0f);
            if (taskId == 0)  {
                task = new Task();
                task.setTaskName(getApplication().getString(R.string.new_task_name));
            } else {
                getTask();
            }
        }
        return this;
    }

    /**
     * Call this if changes made but you don't want to save them
     * Since this viewmodel is shared with search/add turnpoints at the activity level
     * you must reset in case you come back to same task from task list
     */
    public void reset() {
        taskId = 0;
        task = null;
        taskTurnpoints.setValue(null);

    }

    public long getTaskId() {
        return  taskId;
    }


    @SuppressLint("CheckResult")
    private Task getTask() {
        //Do just once
        if (!retrievedTask) {
            retrievedTask = true;
            loadTask();
        }
        return task;
    }

    @SuppressLint("CheckResult")
    private void loadTask() {
        Disposable disposable = appRepository.getTask(taskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(task -> {
                            this.task = task;
                            taskDistance.setValue(task.getDistance());
                            notifyChange();
                        },
                        t -> {
                            post(new DataBaseError(getApplication().getString(R.string.error_reading_task), t));
                        });
        compositeDisposable.add(disposable);
    }

    @SuppressLint("CheckResult")
    public LiveData<List<TaskTurnpoint>> getTaskTurnpoints() {
        if (!retrievedTaskTurnpoints) {
            retrievedTaskTurnpoints = true;
            loadTaskTurnpoints();
        }
        return taskTurnpoints;
    }

    public void getTurnpointFromTaskTurnpoint(TaskTurnpoint taskTurnpoint) {
        Disposable disposable = appRepository.getTurnpoint(taskTurnpoint.getTitle(), taskTurnpoint.getCode())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(turnpoint -> {
                            post(new DisplayTurnpointSatelliteView(turnpoint));
                        },
                        t -> {
                            post(new DataBaseError(getApplication().getString(R.string.error_reading_turnpoint, taskTurnpoint.getTitle(),
                                    taskTurnpoint.getCode()), t));
                        });
        compositeDisposable.add(disposable);
    }

    @SuppressLint("CheckResult")
    private void loadTaskTurnpoints() {
        Disposable disposable = appRepository.getTaskTurnpoints(taskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(newTaskTurnpoints -> {
                            taskTurnpoints.setValue(newTaskTurnpoints);
                        },
                        t -> {
                            post(new DataBaseError(getApplication().getString(R.string.error_loading_task_and_turnpoints), t));
                        });
        compositeDisposable.add(disposable);
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

    public void cloneTask(){
        task.setId(0);
        taskId = 0;
        List<TaskTurnpoint> clonedTaskTurnpoints = taskTurnpoints.getValue();
        if (clonedTaskTurnpoints != null) {
            for (TaskTurnpoint taskTurnpoint : clonedTaskTurnpoints){
                taskTurnpoint.setId(0);
                taskTurnpoint.setTaskId(0);
            }
        }
        deletedTaskTurnpoints.clear();
        needToSaveUpdates.setValue(true);
        taskTurnpoints.setValue(clonedTaskTurnpoints);
    }

    public void saveTask() {
        Disposable disposable;
        working.setValue(true);
        // make sure task distance saved
        setTaskDistance();
        if (task.getId() == 0) {
            // adding new task/turnpoints
            disposable = appRepository.addNewTaskAndTurnpoints(task, taskTurnpoints.getValue())
                    .subscribe(taskId -> {
                        this.taskId = taskId;
                        task.setId(taskId);
                        needToSaveUpdates.setValue(false);
                        working.setValue(false);
                    }, t -> {
                        post(new DataBaseError(getApplication().getString(R.string.error_adding_task_and_turnpoints), t));
                    });
        } else {
            disposable = appRepository.updateTaskAndTurnpoints(task, taskTurnpoints.getValue(), deletedTaskTurnpoints)
                    .subscribe(() -> {
                        deletedTaskTurnpoints.clear();
                        needToSaveUpdates.setValue(false);
                        working.setValue(false);
                    }, t -> {
                        post(new DataBaseError(getApplication().getString(R.string.error_adding_task_and_turnpoints), t));
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
        // Doing this way so observers fired with update to turnpoint array in taskTurnpoints
        List<TaskTurnpoint> taskTurnpointList = taskTurnpoints.getValue();
        taskTurnpoints.setValue(taskTurnpointList);

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


    public MutableLiveData<Boolean> getNeedToSaveUpdates() {
        return needToSaveUpdates;
    }

    public MutableLiveData<Boolean> getWorking() {
        return working;
    }


    // TODO Put into superclass
    private void post(Object object){
        EventBus.getDefault().post(object);
    }

    @Override
    public void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }
}
