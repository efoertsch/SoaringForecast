package org.soaringforecast.rasp.one800wxbrief.routebriefing;

import android.annotation.SuppressLint;
import android.app.Application;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.TaskTurnpoint;
import org.soaringforecast.rasp.repository.messages.DataBaseError;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Retrieve the task turnpoints to get the names of airports
 *  (but not all turnpoints  may  be to airports)
 *  Then populate the UI fields that are used to create the routeBriefing request
 */
public class WxBriefViewModel extends AndroidViewModel {

    private AppRepository appRepository;
    private long taskId = 0;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private MutableLiveData<Boolean> working = new MutableLiveData<>();
    private MutableLiveData<String> taskTitle = new MutableLiveData<>();
    private MutableLiveData<String> corridorWidth = new MutableLiveData<>();
    private MutableLiveData<String> webUserName = new MutableLiveData<>();
    private MutableLiveData<String> windsAloftCorridor = new MutableLiveData<>();

    private RouteBriefingRequest routeBriefingRequest;

    public WxBriefViewModel(@NonNull Application application) {
        super(application);
        working.setValue(true);
    }

    public WxBriefViewModel  setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }

    public WxBriefViewModel setTaskId(long taskId) {
        this.taskId  = taskId;
        return this;
    }

    @SuppressLint("CheckResult")
    public void getTaskTurnpoints() {
        loadTaskTurnpoints();
    }

    @SuppressLint("CheckResult")
    private void loadTaskTurnpoints() {
        Disposable disposable = appRepository.getTaskTurnpoints(taskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(taskTurnpoints -> createWxBriefRequest(taskTurnpoints),
                        t -> {
                            post(new DataBaseError(getApplication().getString(R.string.error_loading_task_and_turnpoints), t));
                        });
        compositeDisposable.add(disposable);
    }


    /**
     * For not assume all turnpoints are airports
     * @param taskTurnpoints
     */
    private void createWxBriefRequest(List<TaskTurnpoint> taskTurnpoints){
        ArrayList<String> turnpointIds = new ArrayList<>();
        for (TaskTurnpoint taskTurnpoint: taskTurnpoints){
            turnpointIds.add(taskTurnpoint.getCode());
        }

        routeBriefingRequest = RouteBriefingRequest.newInstance(turnpointIds);
        working.setValue(false);
    }

    public MutableLiveData<Boolean> getWorking(){
        return working;
    }

    public MutableLiveData<String> getTaskTitle() {
        return taskTitle;
    }

    public MutableLiveData<String> getAircraftIdErrorText(){
        return null;
    }

    public MutableLiveData<String> getTitle(){
        return null;
    }

    public MutableLiveData<String> getCorridorWidthErrorText(){
        return null;
    }

    public MutableLiveData<String> getCorridorWidth() {
        return corridorWidth;
    }

    public void setCorridorWidth(MutableLiveData<String> corridorWidth) {
        this.corridorWidth = corridorWidth;
    }

    public MutableLiveData<String> getWebUserNameErrorText(){
        return null;
    }

    public MutableLiveData<String> getWebUserName() {
        return webUserName;
    }

    public void setWebUserName(MutableLiveData<String> webUserName) {
        this.webUserName = webUserName;
    }

    public MutableLiveData<String> getWindsAloftCorridor() {
        return windsAloftCorridor;
    }

    public void setWindsAloftCorridor(MutableLiveData<String> windsAloftCorridor) {
        this.windsAloftCorridor = windsAloftCorridor;
    }

    public MutableLiveData<String> getWindsAloftCorridorErrorText(){
        return null;
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
