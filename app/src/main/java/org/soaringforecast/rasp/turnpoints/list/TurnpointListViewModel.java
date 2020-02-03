package org.soaringforecast.rasp.turnpoints.list;

import android.annotation.SuppressLint;
import android.app.Application;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.ObservableViewModel;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.repository.messages.DataBaseError;

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

public class TurnpointListViewModel extends ObservableViewModel {

    private AppRepository appRepository;
    private MutableLiveData<List<Turnpoint>> turnpoints;
    private MutableLiveData<Integer> numberTurnpoints;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private MutableLiveData<Boolean> working = new MutableLiveData();
    private MutableLiveData<Integer> numberTurnpointsDeleted;

    public TurnpointListViewModel(@NonNull Application application) {
        super(application);
    }

    public TurnpointListViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }

    public LiveData<List<Turnpoint>> getTurnpoints() {
        if (turnpoints == null) {
            turnpoints = new MutableLiveData<>();
            searchTurnpoints("%");
        }
        return turnpoints;
    }

    public LiveData<Integer> getNumberOfTurnpoints() {
        if (numberTurnpoints == null) {
            numberTurnpoints = new MutableLiveData<>();
            getTotalNumberOfTurnpoints();
        }
        return numberTurnpoints;

    }

    public LiveData<Boolean> getWorking() {
        if (working.getValue() == null){
            working.setValue(false);
        }
        return working;
    }


    @SuppressLint("CheckResult")
    public void searchTurnpoints(String search) {
        working.setValue(true);
        Disposable disposable = appRepository.findTurnpoints("%" + search + "%")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(turnpointList -> {
                            working.setValue(false);
                            if (turnpointList == null) {
                                turnpointList = new ArrayList<>();
                            }
                            turnpoints.setValue(turnpointList);
                        },
                        t -> {
                            working.setValue(false);
                            post(new DataBaseError(getApplication().getString(R.string.error_searching_turnpoints), t));

                        });
        compositeDisposable.add(disposable);
    }


    @SuppressLint("CheckResult")
    private void getTotalNumberOfTurnpoints() {
        working.setValue(true);
        Disposable disposable = appRepository.getCountOfTurnpoints()
                .subscribe(count -> {
                            working.setValue(false);
                            numberTurnpoints.setValue(count);
                        }
                        , throwable -> {
                            working.setValue(false);
                            //TODO
                            Timber.e(throwable);
                            post(new DataBaseError(getApplication().getString(R.string.error_getting_number_of_turnpoints), throwable));
                        });
        compositeDisposable.add(disposable);

    }

    public void clearTurnpointDatabase() {
        working.setValue(true);
        Disposable disposable = appRepository.deleteAllTurnpoints()
                .subscribe(numberDeleted -> {
                    searchTurnpoints("%");
                });
        compositeDisposable.add(disposable);

    }


    public void writeTurnpointsToDownloadsFile() {
        working.setValue(true);
        Disposable disposable = appRepository.findTurnpoints("%").flatMapCompletable(
                turnpointList ->
                appRepository.writeTurnpointsToCupFile(turnpointList))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                            working.setValue(false);
                            post(new SnackbarMessage(getApplication().getString(R.string.turnpoints_exported_to_download_directory)));
                        }
                        , error -> {
                            working.setValue(false);
                            post(new DataBaseError(getApplication().getString(R.string.error_searching_turnpoints), error));
                        }
                );
        compositeDisposable.add(disposable);
    }


    private void post(Object post) {
        EventBus.getDefault().post(post);
    }

    @Override
    public void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }
}
