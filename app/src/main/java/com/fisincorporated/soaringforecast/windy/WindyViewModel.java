package com.fisincorporated.soaringforecast.windy;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.webkit.JavascriptInterface;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.repository.TaskTurnpoint;
import com.fisincorporated.soaringforecast.soaring.json.Model;
import com.fisincorporated.soaringforecast.soaring.json.ModelForecastDate;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class WindyViewModel extends AndroidViewModel {

    Context context;
    MutableLiveData<String> command;

    // TODO put zoom in appPreferences
    private int zoom = 7;
    private LatLng defaultLatLng = new LatLng(43.1393051, -72.076004);
    private AppPreferences appPreferences;
    private AppRepository appRepository;
    private List<TaskTurnpoint> taskTurnpoints = new ArrayList();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private MutableLiveData<Boolean> startUpComplete;
    private MutableLiveData<Boolean> taskSelected ;
    private ModelForecastDate selectedModelForecastDate;
    private LatLng selectedLatLng;


    public WindyViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
    }

     WindyViewModel setAppPreferences(AppPreferences appPreferences){
        this.appPreferences =  appPreferences;
        return this;
    }

    WindyViewModel setAppRepository(AppRepository appRepository){
        this.appRepository =  appRepository;
        return this;
    }

    public MutableLiveData<Boolean> getStartUpComplete() {
        if (startUpComplete == null) {
            startUpComplete = new MutableLiveData<>();
            getSelectedModelForecastDate();
            getTask();
        }
        return startUpComplete;
    }

    public MutableLiveData<Boolean> getTaskSelected(){
        if (taskSelected == null) {
            taskSelected = new MutableLiveData<>();
            taskSelected.setValue(appPreferences.getSelectedTaskId() > 0);
        }
        return taskSelected;
    }


    public MutableLiveData<String> getCommand() {
        if (command == null) {
            command = new MutableLiveData<>();
        }
        return command;
    }

    public void  setCommand(String stringCommand) {
        // Using postValue as it seems Android doesn't like to have
        // javascript call Android app and app logic then send javascript to webview
        // So postValue allows webview call to end first, the command gets sent a bit later
        command.postValue(stringCommand);
    }

    public void removeTaskTurnpoints() {
        taskTurnpoints.clear();
        setCommand( new StringBuilder().append("javascript:").append("removeTaskFromMap()").toString());
        setTaskId(-1);
        taskSelected.setValue(false);
    }

    @JavascriptInterface
    public void resetHeight() {
        command.setValue("javascript:setHeight('200px')");
    }

    @JavascriptInterface
    public String getWindyKey() {
        return context.getString(R.string.WindyKey);
    }

    @JavascriptInterface
    public double getLat() {
        return selectedLatLng.latitude;
    }

    @JavascriptInterface
    public double getLong() {
        return selectedLatLng.longitude;
    }

    @JavascriptInterface
    public double getZoom() {
        return appPreferences.getWindyZoomLevel();
    }

    @JavascriptInterface
    public void getTaskTurnpointsForMap() {
        plotTask(taskTurnpoints);

    }

    @JavascriptInterface
    public void mapLoaded(){
        // Windy initialized and ready
        getTask();  // may not be one but if so draw it.
    }

    public void setTaskId(long taskId) {
        appPreferences.setSelectedTaskId(taskId);
        if (taskId != -1) {
            getTask(taskId);
        }
    }


    public void getTask(){
        long taskId = appPreferences.getSelectedTaskId();
        if (taskId != -1) {
            getTask(taskId);
        } else {
            startUpComplete.setValue(true);
        }
    }

    public void getTask(long taskId) {
        Disposable disposable = appRepository.getTaskTurnpionts(taskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(taskTurnpointList -> {
                            taskTurnpoints = taskTurnpointList;
                            taskSelected.setValue(true);
                            if (startUpComplete != null
                                    && startUpComplete.getValue() != null
                                    && startUpComplete.getValue()){
                                // 2nd or later request
                                plotTask(taskTurnpointList);
                            } else {
                                startUpComplete.setValue(true);
                            }
                        },
                        t -> {
                            //TODO email stack trace
                            Timber.e(t);
                        });
        compositeDisposable.add(disposable);
    }

    private void plotTask(List<TaskTurnpoint> taskTurnpoints) {
        Gson gson = new Gson();
        String taskJson = gson.toJson(taskTurnpoints);
        String command = new StringBuilder().append("javascript:").append("drawTask(")
                .append(taskJson).append(")").toString();
        setCommand(command);
    }


    public void getSelectedModelForecastDate() {
        selectedModelForecastDate = appPreferences.getSelectedModelForecastDate();
        if (selectedModelForecastDate != null ) {
            Model model = selectedModelForecastDate.getModel();
            if (model != null) {
                selectedLatLng = new LatLng(model.getCenter().get(0), model.getCenter().get(1));
                return;
            }
        }
        selectedLatLng = defaultLatLng;
    }

    @Override
    public void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }

}
