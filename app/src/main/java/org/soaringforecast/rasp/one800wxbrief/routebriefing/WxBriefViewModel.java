package org.soaringforecast.rasp.one800wxbrief.routebriefing;

import android.annotation.SuppressLint;
import android.app.Application;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.BR;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.ObservableViewModel;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.TaskTurnpoint;
import org.soaringforecast.rasp.repository.messages.DataBaseError;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
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
public class WxBriefViewModel extends ObservableViewModel {

    private static final SimpleDateFormat departureDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private AppRepository appRepository;
    private long taskId = 0;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Boolean working = true;
    private String taskTitle;

    private String wxBriefWebUserName;
    private Pattern wxBriefWebUserNamePattern = Pattern.compile("^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$");

    private String defaultRouteCorridorWidth = "25";
    private String routeCorridorWidth = defaultRouteCorridorWidth;
    private String windsAloftCorridor;
    private String defaultWindsAloftCorridor = "100";
    private String turnpointList;
    private String aircraftId;
    private Pattern aircraftIdPattern = Pattern.compile("[A-Z0-9]{2,7}");
    private ArrayList<String> briefingDates;
    private Integer selectedBriefingDatePosition;
    private ArrayList<String> departureTimes;
    private Integer selectedDepartureTimePosition;
    private ArrayList<String> briefingFormats;
    private Integer selectedBriefingFormatPosition;

    private RouteBriefingRequest routeBriefingRequest;
    private AppPreferences appPreferences;
    private String aircraftIdErrorText;
    private String corridorWidthErrorText;
    private String wxBriefUserNameErrorText;
    private String windsAloftCorridorErrorText;


    public WxBriefViewModel(@NonNull Application application) {
        super(application);
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
        setCorridorValues();
        loadTask();
        loadTaskTurnpoints();

    }

    private void setCorridorValues() {
        routeBriefingRequest.setRouteCorridorWidth(routeCorridorWidth);
        notifyPropertyChanged(BR.routeCorridorWidth);
        routeBriefingRequest.setWindsAloftCorridorWidth(windsAloftCorridor);
        notifyPropertyChanged(BR.windsAloftCorridor);
    }


    private void loadTask() {
        Disposable disposable = appRepository.getTask(taskId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(task -> {
                            setTaskTitle(task.getTaskName());
                            setWorkingFlag();
                        },
                        t -> {
                            post(new DataBaseError(getApplication().getString(R.string.error_loading_task), t));
                        });

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
        setTurnpointList(sb.toString());
    }

    @Bindable
    public boolean getWorking() {
        return working;
    }


    private void setWorkingFlag() {
        if (turnpointList != null && taskTitle != null) {
            working = false;
        }
        notifyPropertyChanged(BR.working);
    }

    private void setWorkingFlag(boolean flag) {
        working = flag;
        notifyPropertyChanged(BR.working);
    }


    @Bindable
    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
        notifyPropertyChanged(BR.taskTitle);
    }

    @Bindable
    public String getTurnpointList() {
        return turnpointList;
    }

    @Bindable
    public void setTurnpointList(String turnpointList) {
        this.turnpointList = turnpointList;
        routeBriefingRequest.setRoute(turnpointList);
        notifyPropertyChanged(BR.turnpointList);
    }

    @Bindable
    public String getAircraftId() {
        aircraftId = appPreferences.getAircraftRegistration();
        return aircraftId;
    }

    @Bindable
    public void setAircraftId(String aircraftId) {
        this.aircraftId = aircraftId.toUpperCase();
        if (aircraftIdPattern.matcher(aircraftId).matches()) {
            routeBriefingRequest.setAircraftIdentifier(this.aircraftId);
            appPreferences.setAircraftRegistration(this.aircraftId);
            setAircraftIdErrorText(null);
        } else {
            setAircraftIdErrorText(getApplication().getString(R.string.invalid_aircraftid));
        }
    }

    @Bindable
    public String getAircraftIdErrorText() {
        return aircraftIdErrorText;
    }

    private void setAircraftIdErrorText(String errorText) {
        aircraftIdErrorText = errorText;
        notifyPropertyChanged(BR.aircraftIdErrorText);
    }

    @Bindable
    public String getWxBriefUserName() {
        if (wxBriefWebUserName == null) {
            wxBriefWebUserName = appPreferences.getOne800WxBriefUserId();
            // validate
            setWxBriefUserName(wxBriefWebUserName);
        }
        return wxBriefWebUserName;
    }


    @Bindable
    public void setWxBriefUserName(String wxBriefUserName) {
        if (wxBriefWebUserNamePattern.matcher(wxBriefUserName).matches()) {
            appPreferences.setOne800WxBriefUserId(wxBriefUserName);
            assignUserNameAndEmailAddress(wxBriefUserName);
            setWxBriefUserNameErrorText(null);
        } else {
            setWxBriefUserNameErrorText(getApplication().getString(R.string.invalid_wxbrief_web_username));
        }
    }

    private void assignUserNameAndEmailAddress(String username) {
        routeBriefingRequest.setEmailAddress(username);
        routeBriefingRequest.setWebUserName(username);
    }

    private void setWxBriefUserNameErrorText(String errorText) {
        wxBriefUserNameErrorText = errorText;
        notifyPropertyChanged(BR.wxBriefUserNameErrorText);
    }

    @Bindable
    public String getWxBriefUserNameErrorText() {
        return wxBriefUserNameErrorText;
    }


    @Bindable
    public String getRouteCorridorWidth() {
        return routeCorridorWidth;
    }

    @Bindable
    public void setRouteCorridorWidth(String routeCorridorWidth) {
        if (routeCorridorWidth.isEmpty()) {
            this.routeCorridorWidth = defaultRouteCorridorWidth;
            routeBriefingRequest.setRouteCorridorWidth(this.routeCorridorWidth);
            setCorridorWidthErrorText(null);
            notifyPropertyChanged(BR.routeCorridorWidth);
        } else {
            try {
                int width = Integer.parseInt(routeCorridorWidth);
                if (width >= 25 && width <= 100) {
                    routeBriefingRequest.setRouteCorridorWidth(width + "");
                    setCorridorWidthErrorText(null);
                }
            } catch (NumberFormatException nfe) {
                setCorridorWidthErrorText(getApplication().getString(R.string.route_corridor_width_must_be_between_25_and_100));
            }
        }
    }

    @Bindable
    public String getCorridorWidthErrorText() {
        return corridorWidthErrorText;
    }

    private void setCorridorWidthErrorText(String errorText) {
        corridorWidthErrorText = errorText;
        notifyPropertyChanged(BR.corridorWidthErrorText);
    }

    @Bindable
    public String getWindsAloftCorridor() {
        return windsAloftCorridor;
    }

    public void setWindsAloftCorridor(String windsAloftCorridor) {
        if (windsAloftCorridor.isEmpty()) {
            this.windsAloftCorridor = defaultWindsAloftCorridor;
            routeBriefingRequest.setWindsAloftCorridorWidth(this.windsAloftCorridor);
            setWindsAloftCorridorErrorText(null);
            notifyPropertyChanged(BR.windsAloftCorridor);
        } else {
            try {
                int width = Integer.parseInt(windsAloftCorridor);
                if (width >= 100 && width <= 600) {
                    routeBriefingRequest.setWindsAloftCorridorWidth(width + "");
                    setCorridorWidthErrorText(null);
                }
            } catch (NumberFormatException nfe) {
                setWindsAloftCorridorErrorText(getApplication().getString(R.string.winds_aloft_corridor_must_be_between_100_and_600));

            }
        }
    }


    @Bindable
    public String getWindsAloftCorridorErrorText() {
        return windsAloftCorridorErrorText;
    }

    private void setWindsAloftCorridorErrorText(String errorText) {
        windsAloftCorridorErrorText = errorText;
        notifyPropertyChanged(BR.windsAloftCorridorErrorText);
    }


    @Bindable
    public ArrayList<String> getBriefingDates() {
        if (briefingDates == null) {
            ArrayList<String> dateList = new ArrayList<>();
            // Just use today and tomorrow
            dateList.add(departureDateFormat.format(System.currentTimeMillis()));
            dateList.add(departureDateFormat.format(System.currentTimeMillis() + (24 * 60 * 60 * 1000)));
            briefingDates = dateList;
            selectedBriefingDatePosition = 0;
            formatDepartureInstant();
        }
        return briefingDates;
    }

    private void formatDepartureInstant() {
        //TODO format date/time for route request
    }

    @Bindable
    public int getSelectedBriefingDatePosition() {
        return selectedBriefingDatePosition;
    }

    @Bindable
    public void setSelectedBriefingDatePosition(int selectedBriefingDatePosition) {
        this.selectedBriefingDatePosition = selectedBriefingDatePosition;
    }

    @Bindable
    public ArrayList<String> getDepartureTimes() {
        if (departureTimes == null) {
            ArrayList<String> timeList = new ArrayList<>();
            int time = 6;
            // For early risers
            for (int i = 0; i < 12; ++i) {
                timeList.add(String.format(getApplication().getString(R.string.time_format), time));
                time = time + 1;
            }
            departureTimes = timeList;
            // a reasonable launch hr - 10:00
            selectedDepartureTimePosition = 4;
        }
        return departureTimes;
    }

    @Bindable
    public int getSelectedDepartureTimePosition() {
        return selectedDepartureTimePosition;
    }

    @Bindable
    public void setSelectedDepartureTimePosition(int selectedDepartureTimePosition) {
        this.selectedDepartureTimePosition = selectedDepartureTimePosition;
    }

    @Bindable
    public ArrayList<String> getBriefingFormats() {
        if (briefingFormats == null) {
            briefingFormats = new ArrayList<>();
            for (RouteBriefingRequest.BriefingTypes briefingType : RouteBriefingRequest.BriefingTypes.values()) {
                briefingFormats.add(briefingType.getDisplayValue());
            }
            setSelectedBriefingFormatPosition(0);
        }
        return briefingFormats;
    }

    @Bindable
    public int getSelectedBriefingFormatPosition() {
        return selectedBriefingFormatPosition;
    }

    @Bindable
    public void setSelectedBriefingFormatPosition(int selectedBriefingFormatPosition) {
        this.selectedBriefingFormatPosition = selectedBriefingFormatPosition;
        routeBriefingRequest.setBriefingType(RouteBriefingRequest.BriefingTypes.values()[selectedBriefingFormatPosition].name());
    }

    public void submitBriefingRequest() {
        setWorkingFlag(true);
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
        setWorkingFlag(false);
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
            if (routeBriefing == null) {
                post(new Email1800WxBriefRequestResponse(getApplication()
                        .getString((R.string.undefined_error_occurred_on_1800wxbrief_request))));
            } else if (routeBriefing.returnCodedMessage != null && routeBriefing.returnCodedMessage.size() > 0) {
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
