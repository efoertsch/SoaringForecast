package org.soaringforecast.rasp.task.list;

import android.annotation.SuppressLint;
import android.app.Application;

import org.soaringforecast.rasp.common.ObservableViewModel;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Task;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

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
                            Timber.e(t);
                        });
        compositeDisposable.add(disposable);
    }

    @Override
    public void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }


}
