package org.soaringforecast.rasp.task.search;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.ObservableViewModel;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.repository.messages.DataBaseError;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class TurnpointSearchViewModel extends ObservableViewModel {

    private AppRepository appRepository;
    private MutableLiveData<List<Turnpoint>> turnpoints = new MutableLiveData<>();
    private MutableLiveData<Integer> numberSearchableTurnpoints = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public TurnpointSearchViewModel (@NonNull Application application) {
        super(application);
    }

    public TurnpointSearchViewModel  setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }

    @SuppressLint("CheckResult")
    public LiveData<List<Turnpoint>> searchTurnpoints(String search) {
        Disposable disposable = appRepository.findTurnpoints("%" + search + "%")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(turnpointList -> {
                            turnpoints.setValue(turnpointList);
                        },
                        t -> {
                            EventBus.getDefault().post(new DataBaseError(getApplication().getString(R.string.error_searching_turnpoints), t));
                        });
        compositeDisposable.add(disposable);
        return turnpoints;
    }

    public MutableLiveData<Integer> getNumberOfSearchableTurnpoints() {
        if (numberSearchableTurnpoints.getValue() == null || numberSearchableTurnpoints.getValue() == 0) {
            getTotalSearchableTurnpoints();
        }
        return numberSearchableTurnpoints;

    }

    @SuppressLint("CheckResult")
    private void getTotalSearchableTurnpoints() {
        Disposable disposable = appRepository.getCountOfTurnpoints()
                .subscribe(count -> {
                            numberSearchableTurnpoints.setValue(count);
                        }
                        , throwable -> {
                            //TODO
                            Timber.e(throwable);
                            EventBus.getDefault().post(new DataBaseError(getApplication().getString(R.string.error_getting_number_of_turnpoints), throwable));
                        });
        compositeDisposable.add(disposable);

    }
}
