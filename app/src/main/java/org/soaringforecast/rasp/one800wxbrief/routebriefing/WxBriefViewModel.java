package org.soaringforecast.rasp.one800wxbrief.routebriefing;

import android.annotation.SuppressLint;
import android.app.Application;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.TaskTurnpoint;
import org.soaringforecast.rasp.repository.messages.DataBaseError;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Retrieve the task turnpoints to get the names of airports
 * (but not all turnpoints  may  be to airports)
 * Then populate the UI fields that are used to create the routeBriefing request
 */
public class WxBriefViewModel extends AndroidViewModel {

    private static final SimpleDateFormat departureDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private AppRepository appRepository;
    private long taskId = 0;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private MutableLiveData<Boolean> working = new MutableLiveData<>();
    private MutableLiveData<String> taskTitle = new MutableLiveData<>();
    private MutableLiveData<String> corridorWidth = new MutableLiveData<>();
    private MutableLiveData<String> webUserName = new MutableLiveData<>();
    private MutableLiveData<String> windsAloftCorridor = new MutableLiveData<>();
    private MutableLiveData<String> turnpointList = new MutableLiveData<>();
    private MutableLiveData<String> aircraftId = new MutableLiveData<>();
    private MutableLiveData<ArrayList<String>> briefingDates = new MutableLiveData<>();
    private MutableLiveData<Integer> selectedBriefingDatePosition = new MutableLiveData<>();
    private MutableLiveData<ArrayList<String>> departureTimes = new MutableLiveData<>();
    private MutableLiveData<Integer> selectedDepartureTimePosition = new MutableLiveData<>();
    private MutableLiveData<ArrayList<String>> briefingFormats = new MutableLiveData<>();
    private MutableLiveData<Integer> selectedBriefingFormatPosition = new MutableLiveData<>();

    private RouteBriefingRequest routeBriefingRequest;
    private AppPreferences appPreferences;


    public WxBriefViewModel(@NonNull Application application) {
        super(application);
        working.setValue(true);
    }

    public WxBriefViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }

    public WxBriefViewModel setAppPreferences(AppPreferences appPreferences) {
        this.appPreferences = appPreferences;
        return this;
    }

    public WxBriefViewModel setTaskId(long taskId) {
        this.taskId = taskId;
        return this;
    }


    public void init() {
        routeBriefingRequest = RouteBriefingRequest.newInstance();

        getBriefingFormats();
        getWxBriefUserName();
        getBriefingDates();
        getDepartureTimes();
        windsAloftCorridor.setValue("100");
        routeBriefingRequest.setWindsAloftCorridorWidth("100");
        corridorWidth.setValue("50");
        routeBriefingRequest.setRouteCorridorWidth("50");
        loadTask();
        loadTaskTurnpoints();

    }


    private void loadTask() {
        Disposable disposable = appRepository.getTask(taskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(task -> {
                            taskTitle.setValue((task.getTaskName()));
                            setWorkingFlag();
                        },
                        t -> {
                            post(new DataBaseError(getApplication().getString(R.string.error_loading_task), t));
                        });

    }

    private void setWorkingFlag() {
        if (turnpointList.getValue() != null && taskTitle.getValue() != null) {
            working.setValue(false);
        }
    }

    @SuppressLint("CheckResult")
    private void loadTaskTurnpoints() {
        Disposable disposable = appRepository.getTaskTurnpoints(taskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(taskTurnpoints -> {
                            setDepartureRouteAndDestination(taskTurnpoints);
                            setWorkingFlag();
                        },
                        t -> {
                            post(new DataBaseError(getApplication().getString(R.string.error_loading_task_and_turnpoints), t));
                        });
        compositeDisposable.add(disposable);
    }


    /**
     * For not assume all turnpoints are airports
     *
     * @param taskTurnpoints
     */
    private void setDepartureRouteAndDestination(List<TaskTurnpoint> taskTurnpoints) {
        ArrayList<String> turnpointIds = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < taskTurnpoints.size(); ++i) {
            if (i == 0) {
                routeBriefingRequest.setDeparture(taskTurnpoints.get(0).getCode());
            } else if (i == taskTurnpoints.size() - 1) {
                routeBriefingRequest.setDestination(taskTurnpoints.get(i).getCode());
            } else {
                turnpointIds.add(taskTurnpoints.get(i).getCode());
            }
            sb.append(taskTurnpoints.get(i).getCode()).append(" ");
        }
        turnpointList.setValue(sb.toString());
        routeBriefingRequest.setRoute(sb.toString());

    }

    public MutableLiveData<Boolean> getWorking() {
        return working;
    }

    public MutableLiveData<String> getTaskTitle() {
        return taskTitle;
    }

    public MutableLiveData<String> getTurnpointList() {
        return turnpointList;
    }

    public MutableLiveData<String> getAircraftId() {
        return aircraftId;
    }

    public void setAircraftId(MutableLiveData<String> aircraftId) {
        this.aircraftId = aircraftId;
    }

    public MutableLiveData<String> getAircraftIdErrorText() {
        return null;
    }

    public MutableLiveData<String> getTitle() {
        return null;
    }

    public MutableLiveData<String> getCorridorWidthErrorText() {
        return null;
    }

    public MutableLiveData<String> getCorridorWidth() {
        return corridorWidth;
    }

    public void setCorridorWidth(String corridorWidth) {
        this.corridorWidth.setValue(corridorWidth);
    }


    public MutableLiveData<String> getWxBriefUserName() {
        if (webUserName.getValue() == null) {
            webUserName.setValue(appPreferences.getOne800WxBriefUserId());
        }
        return webUserName;
    }

    public void setWxBriefUserName(String wxBriefUserName) {
        appPreferences.setOne800WxBriefUserId(wxBriefUserName);
        webUserName.setValue(wxBriefUserName);
    }


    public MutableLiveData<String> getWebUserNameErrorText() {
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

    public MutableLiveData<String> getWindsAloftCorridorErrorText() {
        return null;
    }


    public MutableLiveData<ArrayList<String>> getBriefingDates() {
        if (briefingDates.getValue() == null) {
            ArrayList<String> dateList = new ArrayList<>();
            // Just use today and tomorrow
            dateList.add(departureDateFormat.format(System.currentTimeMillis()));
            dateList.add(departureDateFormat.format(System.currentTimeMillis() + (24 * 60 * 60 * 1000)));
            briefingDates.setValue(dateList);
            selectedBriefingDatePosition.setValue(0);
        }
        return briefingDates;
    }

    public MutableLiveData<Integer> getSelectedBriefingDatePosition() {
        return selectedBriefingDatePosition;
    }

    public void setSelectedBriefingDatePosition(MutableLiveData<Integer> selectedBriefingDatePosition) {
        this.selectedBriefingDatePosition = selectedBriefingDatePosition;
    }

    public MutableLiveData<Integer> getSelectedDepartureTimePosition() {
        return selectedDepartureTimePosition;
    }

    public void setSelectedDepartureTimePosition(MutableLiveData<Integer> selectedDepartureTimePosition) {
        this.selectedDepartureTimePosition = selectedDepartureTimePosition;
    }

    public MutableLiveData<ArrayList<String>> getDepartureTimes() {
        if (departureTimes.getValue() == null) {
            ArrayList<String> timeList = new ArrayList<>();
            int time = 6;
            // For early risers
            for (int i = 0; i < 12; ++i) {
                timeList.add(String.format(getApplication().getString(R.string.time_format), time));
                time = time + 1;
            }
            departureTimes.setValue(timeList);
            // a reasonable launch hr - 10:00
            selectedDepartureTimePosition.setValue(4);
        }
        return departureTimes;
    }

    public MutableLiveData<ArrayList<String>> getBriefingFormats() {
        if (briefingFormats.getValue() == null) {
            ArrayList<String> formatList = new ArrayList<>();
            for (RouteBriefingRequest.BriefingTypes briefingType : RouteBriefingRequest.BriefingTypes.values()) {
                formatList.add(briefingType.getDisplayValue());
            }
            briefingFormats.setValue(formatList);
            setSelectedBriefingFormatPosition(0);
        }
        return briefingFormats;
    }

    public void setBriefingFormats(MutableLiveData<ArrayList<String>> briefingFormats) {
        this.briefingFormats = briefingFormats;
    }

    public MutableLiveData<Integer> getSelectedBriefingFormatPosition() {
        return selectedBriefingFormatPosition;
    }

    public void setSelectedBriefingFormatPosition(int selectedBriefingFormatPosition) {
        this.selectedBriefingFormatPosition.setValue(selectedBriefingFormatPosition);
        routeBriefingRequest.setBriefingType(RouteBriefingRequest.BriefingTypes.values()[selectedBriefingFormatPosition].name());
    }

    public void submitBriefingRequest() {
        working.setValue(true);
        Disposable disposable = appRepository.submitWxBriefBriefingRequest(routeBriefingRequest.getRestParmString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(routeBriefing -> {
                            evaluateRouteBriefingCall(routeBriefing);
                        },
                        t -> {
                            //TODO email stack trace
                            Timber.e(t);

                        });
        compositeDisposable.add(disposable);

    }

    private void evaluateRouteBriefingCall(RouteBriefing routeBriefing) {
        if (routeBriefing != null && routeBriefing.returnStatus) {
            // request submitted OK
            if (routeBriefingRequest.getBriefingType().equals(RouteBriefingRequest.BriefingTypes.EMAIL.name())) {
                post(new Email1800WxBriefRequestResponse());
            } else {
                // need to get NGBV2 briefing from
                createRouteBriefingPDF(routeBriefing.ngbv2PdfBriefing);
            }
        } else {
            // Error in request
            if (routeBriefing.returnCodedMessage != null && routeBriefing.returnCodedMessage.size() > 0) {
                ReturnCodedMessage returnCodedMessage = routeBriefing.returnCodedMessage.get(0);
                post(new Email1800WxBriefRequestResponse(returnCodedMessage.code + '\n'
                        + returnCodedMessage.message));
            } else { // error but no error msg
                post(new Email1800WxBriefRequestResponse(getApplication()
                        .getString((R.string.undefined_error_occurred_on_1800wxbrief_request))));
            }

        }
    }


    private void createRouteBriefingPDF(String ngbv2PdfBriefing) {
        // 1. String in base64 so need to decode
        // 2. Pass decoded string which is in PDF format to
    }

    // TODO Put into superclass
    private void post(Object object) {
        EventBus.getDefault().post(object);
    }


    @Override
    public void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }

}
