package org.soaringforecast.rasp.task.list;

import android.annotation.SuppressLint;
import android.app.Application;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.ObservableViewModel;
import org.soaringforecast.rasp.common.messages.UndoSuccessful;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Task;
import org.soaringforecast.rasp.repository.messages.DataBaseError;
import org.soaringforecast.rasp.task.messages.DeletedTaskDetails;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TaskListViewModel extends ObservableViewModel {

    private AppRepository appRepository;
    private MutableLiveData<List<Task>> tasks;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public TaskListViewModel(@NonNull Application application) {
        super(application);
    }

    public TaskListViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        tasks = new MutableLiveData<>();
        tasks.setValue(new ArrayList<>());
        return this;
    }

    public LiveData<List<Task>> getTaskList(){
        if  (tasks == null) {
            tasks = new MutableLiveData<>();
        }
        return tasks;
    }

    public void deleteTask(Task task){
        final DeletedTaskDetails deletedTaskDetails = new DeletedTaskDetails(task);
        Disposable disposable = appRepository.getTaskTurnpoints(task.getId()).flatMapCompletable(
                taskTurnpoints -> {
                    deletedTaskDetails.setTaskTurnpoints(taskTurnpoints);
                    return appRepository.deleteTask(task);
                    }
        ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    post(deletedTaskDetails);
                }, throwable -> {
                    post(new DataBaseError(getApplication().getString(R.string.error_occurred_deleting_task), throwable));
                });
        compositeDisposable.add(disposable);

    }

    @SuppressLint("CheckResult")
    public void listTasks() {
        Disposable disposable = appRepository.listAllTasks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(taskList -> {
                            tasks.setValue(taskList);
                            notifyChange();
                        },
                        t -> {
                            post(new DataBaseError(getApplication().getString(R.string.error_getting_task_list),t));
                        });
        compositeDisposable.add(disposable);
    }


    public void undeleteTask(DeletedTaskDetails deletedTaskDetails) {
        Disposable disposable = appRepository.addNewTaskAndTurnpoints(deletedTaskDetails.getTask(), deletedTaskDetails.getTaskTurnpoints())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(taskId -> {
                   post(new UndoSuccessful());
                }, t -> {
                    post(new DataBaseError(getApplication().getString(R.string.error_undoing_task_delete), t));
                });
        compositeDisposable.add(disposable);
    }

    public void updateTaskListOrder(List<Task> taskList) {
        Completable completable = appRepository.updateTaskListOrder(taskList);
        Disposable disposable = completable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    //complete
                }, throwable -> {
                    post(new DataBaseError(getApplication().getString(R.string.error_renumbering_task_list),throwable));

                });
        compositeDisposable.add(disposable);
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
