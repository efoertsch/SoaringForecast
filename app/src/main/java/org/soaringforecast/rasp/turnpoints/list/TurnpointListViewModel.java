package org.soaringforecast.rasp.turnpoints.list;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.ObservableViewModel;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.repository.messages.DataBaseError;
import org.soaringforecast.rasp.turnpoints.messages.SendEmail;

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
    private MutableLiveData<List<Turnpoint>> turnpoints = new MutableLiveData<>();
    private MutableLiveData<Integer> numberTurnpoints = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private MutableLiveData<Boolean> working = new MutableLiveData();
    private MutableLiveData<Integer> numberTurnpointsDeleted;
    private boolean emailCupFile = false;

    public TurnpointListViewModel(@NonNull Application application) {
        super(application);
    }

    public TurnpointListViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }

    public LiveData<List<Turnpoint>> getTurnpoints() {
        if (turnpoints.getValue() == null) {
            searchTurnpoints("%");
        }
        return turnpoints;
    }

    public LiveData<Integer> getNumberOfTurnpoints() {
        if (numberTurnpoints.getValue() == null || numberTurnpoints.getValue() == 0) {
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

    public void setEmailTurnpoint() {
        emailCupFile = true;
    }


    public void writeTurnpointsToDownloadsFile() {
        working.setValue(true);
        Disposable disposable = appRepository.selectAllTurnpointsForDownload().flatMap(
                turnpointList ->
                appRepository.writeTurnpointsToCupFile(turnpointList))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((exportFilename) -> {
                            working.setValue(false);
                            post(new SnackbarMessage(getApplication().getString(R.string.turnpoints_exported_to_download_directory)));
                            if (emailCupFile) {
                                emailCupFile = false;
                                sendTurnpointsViaEmail(exportFilename);
                            }
                        }
                        , error -> {
                            working.setValue(false);
                            emailCupFile = false;
                            post(new DataBaseError(getApplication().getString(R.string.error_searching_turnpoints), error));

                        }
                );
        compositeDisposable.add(disposable);
    }


    public void sendTurnpointsViaEmail(String exportFilename) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Turnpoints " );
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + exportFilename));
            intent.putExtra(Intent.EXTRA_TEXT, getApplication().getString(R.string.updated_or_new_turnpoint));
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            post(new SendEmail(intent));
        } catch (Exception e) {
            post(new SnackbarMessage(getApplication().getString(R.string.error_in_emailing_turnpoint)));
        }
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
