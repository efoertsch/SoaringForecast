package org.soaringforecast.rasp.one800wxbrief;

import android.annotation.SuppressLint;
import android.app.Application;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.Constants;
import org.soaringforecast.rasp.common.ObservableViewModel;
import org.soaringforecast.rasp.one800wxbrief.options.BriefingOption;
import org.soaringforecast.rasp.one800wxbrief.options.BriefingOptions;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.Email1800WxBriefRequestResponse;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.RouteBriefing;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.RouteBriefingRequest;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.TaskTurnpoint;
import org.soaringforecast.rasp.repository.messages.DataBaseError;
import org.soaringforecast.rasp.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Single;
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
    private static final long OneDayInMillisec = 24 * 60 * 60 * 1000;

    private AppRepository appRepository;
    private long taskId = 0;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private MutableLiveData<Boolean> working;
    private MutableLiveData<String> taskTitle;
    private MutableLiveData<String> turnpointList;
    private MutableLiveData<Boolean> officialBriefing;
    private MutableLiveData<List<String>> typeOfBriefs;
    private MutableLiveData<Integer> selectedTypeOfBriefPosition;
    private MutableLiveData<String> aircraftId;
    private MutableLiveData<String> aircraftIdErrorText = new MutableLiveData<>();
    private MutableLiveData<Boolean> displayEmailAddressField;
    private MutableLiveData<String> wxBriefUserNameErrorText = new MutableLiveData<>();
    private MutableLiveData<String> routeCorridorWidth;
    private MutableLiveData<String> corridorWidthErrorText = new MutableLiveData<>();
    private MutableLiveData<String> windsAloftCorridor;
    private MutableLiveData<String> windsAloftCorridorErrorText = new MutableLiveData<>();
    private MutableLiveData<ArrayList<String>> briefingDates;
    private MutableLiveData<Integer> selectedBriefingDatePosition;
    private MutableLiveData<ArrayList<String>> departureTimes;
    private MutableLiveData<Integer> selectedDepartureTimePosition;
    private MutableLiveData<ArrayList<String>> briefingFormats;
    private MutableLiveData<Integer> selectedBriefFormatPosition;
    private MutableLiveData<Boolean> displayTailoringOptions;
    private MutableLiveData<BriefingOptions> briefingOptions = new MutableLiveData<>();
    private MutableLiveData<String> wxBriefWebUserName;
    private MutableLiveData<Boolean> tailoringListUpdatedFlag;
    private MutableLiveData<Boolean> validBriefingData = new MutableLiveData<>();

    private MediatorLiveData postUpdateAction = new MediatorLiveData<>();

    // Should point to standard brief
    private Integer defaultTypeOfBriefPosition = 1;
    private Pattern wxBriefWebUserNamePattern = Pattern.compile("^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$");
    private String defaultRouteCorridorWidth = "25";
    private String defaultWindsAloftCorridor = "100";
    private Pattern aircraftIdPattern = Pattern.compile("[A-Z0-9]{2,7}");
    private RouteBriefingRequest routeBriefingRequest;
    private AppPreferences appPreferences;
    private Constants.TypeOfBrief selectedTypeOfBrief;

    /**
     * Product codes that can go into briefingPreferences  items array
     * {"items":["productCode","productCode",...,"productCode"], ...
     * <p>
     * The items parameter does not apply to  SIMPLE briefingType. (But we are ignoring SIMPLE briefingType in app for now)
     */
    ArrayList<BriefingOption> productCodes;

    /**
     * Tailoring options for non-NGBv2 briefing type (Email,NGB) briefingType that can go into the  tailoring array in briefingPreferences
     * or
     * Tailoring options for NGBv2 briefingType that can go into the  tailoring array in briefingPreferences
     * <p>
     * {"items":[...],"plainText":true,"tailoring":["tailoringOption","tailoringOption",...,"tailoringOption"]}
     */
    ArrayList<BriefingOption> tailoringOptions;



    public enum BriefingFormat {
        EMAIL("EMail"),
        // SIMPLE("Simple"),
        NGBV2("Online(PDF)");

        private String displayValue;

        public String getDisplayValue() {
            return displayValue;
        }

        BriefingFormat(String displayValue) {
            this.displayValue = displayValue;
        }
    }

    /**
     * In API call the briefing format is referred to as briefing type
     * One of
     * enum { 'RAW', 'HTML', 'SIMPLE', 'NGB', 'EMAIL', 'SUMMARY', 'NGBV2' }
     * numeration to indicate format of the briefing response.
     * SUMMARY and HTML types are for internal Leidos use only, and are disabled for
     * external customers. RAW type has been deprecated.
     * <p>
     * NGB not implemented as don't want to handle all returned data types
     * So left with those below
     */
    private BriefingFormat selectedBriefingFormat = BriefingFormat.EMAIL;

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
        getBriefFormats();
        getAircraftId();
        getTypesOfBriefs();
        getWxBriefUserName();
        getBriefingDates();
        getDepartureTimes();
        formatDepartureInstant();
        setCorridorValues();
        loadTask();
        loadTaskTurnpoints();
        loadProductsAndTailoringOptions();
        setupLiveDataMediator();
    }

    // Using MediatorLiveData to handle post LiveData updates for validation checks etc.
    private void setupLiveDataMediator() {
        postUpdateAction.addSource(officialBriefing, isChecked -> updateOfficialBriefing((boolean) isChecked));
        postUpdateAction.addSource(selectedTypeOfBriefPosition, position -> setSelectedTypeOfBriefPosition((int) position));
        postUpdateAction.addSource(aircraftId, aircraftId -> validateAircraftId((String) aircraftId));
        postUpdateAction.addSource(wxBriefWebUserName, wxBriefWebUserName -> validateWxBriefUserName((String) wxBriefWebUserName));
        postUpdateAction.addSource(routeCorridorWidth, routeCorridorWidth -> validateRouteCorridorWidth((String) routeCorridorWidth));
        postUpdateAction.addSource(windsAloftCorridor, windsAloftCorridor -> validateWindsAloftCorridor((String) windsAloftCorridor));
        postUpdateAction.addSource(selectedBriefingDatePosition, selectedBriefingDatePosition -> formatDepartureInstant());
        postUpdateAction.addSource(selectedDepartureTimePosition, selectedDepartureTimePosition-> formatDepartureInstant());
        postUpdateAction.addSource(selectedBriefFormatPosition, selectedBriefFormatPosition-> updateBriefingFormat((int) selectedBriefFormatPosition));
    }

    private void loadProductsAndTailoringOptions() {
        Single<ArrayList<BriefingOption>> singleProductCodes = appRepository.getWxBriefProductCodes(selectedTypeOfBrief);
        Single<ArrayList<BriefingOption>> singleTailoringOptions = appRepository.getWxBriefNGBV2TailoringOptions(selectedTypeOfBrief);
        Disposable disposable = Single.zip(
                singleProductCodes,
                singleTailoringOptions,
                (productCodes, tailoringOptions) -> createBriefingOptions(productCodes, tailoringOptions))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(briefingOptions -> {
                            setBriefingOptions(briefingOptions);
                        },
                        Timber::e);

        compositeDisposable.add(disposable);
    }

    private BriefingOptions createBriefingOptions(ArrayList<BriefingOption> productCodes, ArrayList<BriefingOption> tailoringOptions) {
        return new BriefingOptions(productCodes, tailoringOptions);
    }

    public MutableLiveData<BriefingOptions> getBriefingOptions() {
        return briefingOptions;
    }

    private void setBriefingOptions(BriefingOptions briefingOptions) {
        this.briefingOptions.setValue(briefingOptions);
    }



    private void setCorridorValues() {
        getRouteCorridorWidth();
        routeBriefingRequest.setRouteCorridorWidth(routeCorridorWidth.getValue());
        getWindsAloftCorridor();
        routeBriefingRequest.setWindsAloftCorridorWidth(windsAloftCorridor.getValue());
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
        compositeDisposable.add(disposable);

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

    public MutableLiveData<Boolean> getWorking() {
        if (working == null) {
            working = new MutableLiveData<>();
            working.setValue(true);
        }
        return working;
    }

    private void setWorkingFlag() {
        if (turnpointList != null && turnpointList.getValue() != null
                && taskTitle != null && taskTitle.getValue() != null) {
            working.setValue(false);
        }
    }

    private void setWorkingFlag(boolean flag) {
        working.setValue(flag);
    }

    public MutableLiveData<String> getTaskTitle() {
        if (taskTitle == null) {
            taskTitle = new MutableLiveData<>();
            taskTitle.setValue("");
        }
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle.setValue(taskTitle);
    }


    public MutableLiveData<String> getTurnpointList() {
        if (turnpointList == null) {
            turnpointList = new MutableLiveData<>();
        }
        return turnpointList;
    }


    public void setTurnpointList(String turnpointList) {
        this.turnpointList.setValue(turnpointList);
        routeBriefingRequest.setRoute(turnpointList);
    }


    public MutableLiveData<Boolean> getOfficialBriefing() {
        if (officialBriefing == null) {
            officialBriefing = new MutableLiveData<>();
            officialBriefing.setValue(true);
            updateOfficialBriefing(officialBriefing.getValue());
        }
        return officialBriefing;

    }

    // Note that an OfficialBriefing of true gets converted to a briefing request with
    // notABrief = false
    public void updateOfficialBriefing(Boolean isChecked) {
        routeBriefingRequest.setNotABriefing(!isChecked);
    }

    /**
     * Type of Brief - Outlook, Standard, Abbreviated)
     *
     * @return
     */
    public MutableLiveData<List<String>> getTypesOfBriefs() {
        if (typeOfBriefs == null) {
            typeOfBriefs = new MutableLiveData<>();
            ArrayList<String> listOfBriefs = new ArrayList<>();
            for (Constants.TypeOfBrief typeOfBrief : Constants.TypeOfBrief.values()) {
                listOfBriefs.add(typeOfBrief.displayValue);
            }
            typeOfBriefs.setValue(listOfBriefs);
            // set to the default type of brief so you can load product codes/options for initial display
            getSelectedTypeOfBriefPosition();
            setSelectedTypeOfBriefPosition(selectedTypeOfBriefPosition.getValue());
        }
        return typeOfBriefs;
    }



    public MutableLiveData<Integer> getSelectedTypeOfBriefPosition() {
        if (selectedTypeOfBriefPosition == null) {
            selectedTypeOfBriefPosition = new MutableLiveData<>();
            // default to standard brief
            selectedTypeOfBriefPosition.setValue(defaultTypeOfBriefPosition);

        }
        return selectedTypeOfBriefPosition;
    }

    /**
     * Based on selected brief  (Outlook, Standard, Abbreviated)
     * set various briefing options
     *
     * @param position
     */
    public void setSelectedTypeOfBriefPosition(int position) {
        selectedTypeOfBrief = Constants.TypeOfBrief.values()[position];
    }

//    Turn into MutableLiveDate
//    // Unfortunate 1800WXBrief naming convention
//    public Boolean getOfficialBriefing() {
//        return !routeBriefingRequest.getNotABriefing();
//    }
//
//    Update to turn into MutableLiveData
//    public void setOfficialBriefing(Boolean isOfficialBriefing) {
//        // **** Confusing - Note opposite meanings of flag ****
//        //  if notABriefing = 'true' then it is not an 'official' (recorded) briefing in 188WxBrief)
//        // If isOfficialBriefing = true, user wants briefing to be recorded in system, if false then
//        // briefing not recorded in 1800WxBrief
//        if (routeBriefingRequest.getNotABriefing() == isOfficialBriefing) {
//            routeBriefingRequest.setNotABriefing(!isOfficialBriefing);
//            //notifyPropertyChanged(BR.officialBriefing);
//        }
//        getBriefFormats();
//        notifyPropertyChanged(BR.briefingTypes);
//    }

    public MutableLiveData<String> getAircraftId() {
        if (aircraftId == null) {
            aircraftId = new MutableLiveData<>();
            aircraftId.setValue(appPreferences.getAircraftRegistration());
        }
        return aircraftId;
    }

    public void validateAircraftId(String aircraftId) {
        if (aircraftIdPattern.matcher(aircraftId).matches()) {
            routeBriefingRequest.setAircraftIdentifier(aircraftId);
            appPreferences.setAircraftRegistration(aircraftId);
            setAircraftIdErrorText(null);
        } else {
            setAircraftIdErrorText(getApplication().getString(R.string.invalid_aircraftid));
        }
    }


    public MutableLiveData<String> getAircraftIdErrorText() {
        if (aircraftIdErrorText == null) {
            aircraftIdErrorText = new MutableLiveData<>();
            aircraftIdErrorText.setValue(null);
        }
        return aircraftIdErrorText;
    }

    private void setAircraftIdErrorText(String errorText) {
        aircraftIdErrorText.setValue(errorText);
        validateData();
    }


    public MutableLiveData<Boolean> getDisplayEmailAddressField() {
        if (displayEmailAddressField == null) {
            displayEmailAddressField = new MutableLiveData<>();
            displayEmailAddressField.setValue(selectedBriefingFormat == BriefingFormat.EMAIL);
        }
        return displayEmailAddressField;
    }


    public MutableLiveData<String> getWxBriefUserName() {
        if (wxBriefWebUserName == null) {
            wxBriefWebUserName = new MutableLiveData<>();
            wxBriefWebUserName.setValue(appPreferences.getOne800WxBriefUserId());
            // validate
            validateWxBriefUserName(wxBriefWebUserName.getValue());
        }
        return wxBriefWebUserName;
    }

    public void validateWxBriefUserName(String wxBriefUserName) {
        if (selectedBriefingFormat == BriefingFormat.EMAIL
                && wxBriefUserName != null
                && wxBriefWebUserNamePattern.matcher(wxBriefUserName).matches()) {
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
        wxBriefUserNameErrorText.setValue(errorText);
        validateData();
    }


    public MutableLiveData<String> getWxBriefUserNameErrorText() {
        return wxBriefUserNameErrorText;
    }


    public MutableLiveData<String> getRouteCorridorWidth() {
        if (routeCorridorWidth == null) {
            routeCorridorWidth = new MutableLiveData<>();
            routeCorridorWidth.setValue(defaultRouteCorridorWidth);
        }
        return routeCorridorWidth;
    }

    public void validateRouteCorridorWidth(String routeCorridorWidth) {
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


    public MutableLiveData<String> getCorridorWidthErrorText() {
        return corridorWidthErrorText;
    }

    private void setCorridorWidthErrorText(String errorText) {
        corridorWidthErrorText.setValue(errorText);
        validateData();
    }


    public MutableLiveData<String> getWindsAloftCorridor() {
        if (windsAloftCorridor == null) {
            windsAloftCorridor = new MutableLiveData<String>();
            windsAloftCorridor.setValue(defaultWindsAloftCorridor);
        }
        return windsAloftCorridor;
    }


    public void validateWindsAloftCorridor(String windsAloftCorridor) {
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


    public MutableLiveData<String> getWindsAloftCorridorErrorText() {
        return windsAloftCorridorErrorText;
    }

    private void setWindsAloftCorridorErrorText(String errorText) {
        windsAloftCorridorErrorText.setValue(errorText);
        validateData();
    }


    /**
     * Note these dates are suppose to represent local date (not Zulu)
     *
     * @return
     */
    public MutableLiveData<ArrayList<String>> getBriefingDates() {
        if (briefingDates == null) {
            briefingDates = new MutableLiveData<>();
            ArrayList<String> dateList = new ArrayList<>();
            long currentTime = System.currentTimeMillis();
            dateList.add(departureDateFormat.format(currentTime));
            dateList.add(departureDateFormat.format(currentTime + OneDayInMillisec));
            dateList.add(departureDateFormat.format(currentTime + (2 * OneDayInMillisec)));
            briefingDates.setValue(dateList);
            getSelectedBriefingDatePosition();
        }
        return briefingDates;
    }

    /**
     * Convert seleced local/date and  time to Zulu
     * Time must be in format of yyyy-MM-ddTHH:mm:ss.S
     */
    private void formatDepartureInstant() {
        StringBuilder sb = new StringBuilder();
        sb.append(briefingDates.getValue().get(selectedBriefingDatePosition.getValue()))
                .append("T")
                .append(departureTimes.getValue().get(selectedDepartureTimePosition.getValue()))
                .append(":00.000");
        routeBriefingRequest.setDepartureInstant(TimeUtils.convertLocalTimeToZulu(sb.toString()));
    }

    public MutableLiveData<Integer> getSelectedBriefingDatePosition() {
        if (selectedBriefingDatePosition == null) {
            selectedBriefingDatePosition = new MutableLiveData<>();
            selectedBriefingDatePosition.setValue(0);
        }
        return selectedBriefingDatePosition;
    }


    /**
     * Note these times are suppose to represent local date (not Zulu)
     *
     * @return
     */

    public MutableLiveData<ArrayList<String>> getDepartureTimes() {
        if (departureTimes == null) {
            departureTimes = new MutableLiveData<>();
            ArrayList<String> timeList = new ArrayList<>();
            int time = 6;
            // For early risers
            for (int i = 0; i < 12; ++i) {
                timeList.add(String.format(getApplication().getString(R.string.time_format), time));
                time = time + 1;
            }
            departureTimes.setValue(timeList);
            // make sure defined
            getSelectedDepartureTimePosition();
        }
        return departureTimes;
    }


    public MutableLiveData<Integer> getSelectedDepartureTimePosition() {
        if (selectedDepartureTimePosition == null) {
            selectedDepartureTimePosition = new MutableLiveData<>();
            // a reasonable launch hr - 10:00
            selectedDepartureTimePosition.setValue(4);
        }
        return selectedDepartureTimePosition;
    }

    public MutableLiveData<ArrayList<String>> getBriefFormats() {
        if (briefingFormats == null) {
            briefingFormats = new MutableLiveData<>();
            briefingFormats.setValue(getBriefingTypeList());
            getSelectedBriefFormatPosition();
        }
        return briefingFormats;
    }


    // Briefing is Email, or PDF
    public MutableLiveData<Integer> getSelectedBriefFormatPosition() {
        if (selectedBriefFormatPosition == null) {
            selectedBriefFormatPosition = new MutableLiveData<>();
            selectedBriefFormatPosition.setValue(0);
        }
        return selectedBriefFormatPosition;
    }

    //TODO replace code with BriefingOptions object
    public void updateBriefingFormat(int selectedBriefingFormatPosition) {
        routeBriefingRequest.setSelectedBriefingType(getBriefTypeBasedOnDisplayValue(
                briefingFormats.getValue().get(selectedBriefingFormatPosition)).name());
        displayEmailAddressField.setValue(selectedBriefingFormat == BriefingFormat.EMAIL);
        // createTailoringOptionList();
//        if (BriefingFormat.values()[selectedBriefingFormatPosition] == BriefingFormat.SIMPLE) {
//            displayTailoringOptions = false;
//        } else {
//            displayTailoringOptions = true;
//        }
        displayTailoringOptions.setValue(true);
        toggleTailoringListUpdatedFlag();
    }

    public MutableLiveData<Boolean> getDisplayTailoringOptions() {
        if (displayTailoringOptions == null) {
            displayTailoringOptions = new MutableLiveData<>();
        }
        return displayTailoringOptions;
    }

    public void setDisplayTailoringOptions(boolean tailoringOption) {
        displayTailoringOptions.setValue(tailoringOption);
    }


    /**
     * Standard tailoring options
     */
    public MutableLiveData<Boolean> getTailoringListUpdatedFlag() {
        if (tailoringListUpdatedFlag == null) {
            tailoringListUpdatedFlag = new MutableLiveData<>();
            tailoringListUpdatedFlag.setValue(true);
        }
        return tailoringListUpdatedFlag;
    }

    public void toggleTailoringListUpdatedFlag() {
        if (tailoringListUpdatedFlag == null) {
            getTailoringListUpdatedFlag();
        }
        tailoringListUpdatedFlag.setValue(!tailoringListUpdatedFlag.getValue());
    }

    private void validateData() {
        validBriefingData.setValue(aircraftIdErrorText.getValue() == null
                && wxBriefUserNameErrorText.getValue() == null
                && corridorWidthErrorText.getValue() == null
                && windsAloftCorridorErrorText.getValue() == null);
    }


    public MutableLiveData<Boolean> isValidData() {
        return validBriefingData;
    }

    public void submitBriefingRequest() {
        Timber.d("WxBriefViewModel parm string: %1$s", routeBriefingRequest.getRestParmString());
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
                            evaluateRouteBriefingCall(null);

                        });
        compositeDisposable.add(disposable);

    }

    private void evaluateRouteBriefingCall(RouteBriefing routeBriefing) {
        setWorkingFlag(false);
        if (routeBriefing != null && routeBriefing.returnStatus) {
            // request submitted OK
            if (selectedBriefingFormat.equals(BriefingFormat.EMAIL.name())) {
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
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < routeBriefing.returnCodedMessage.size(); ++i) {
                    sb.append(routeBriefing.returnCodedMessage.get(0))
                            .append(":")
                            .append(routeBriefing.returnCodedMessage.get(0))
                            .append('\n');
                }
                post(new Email1800WxBriefRequestResponse(sb.toString()));
            } else { // error but no error msg
                post(new Email1800WxBriefRequestResponse(getApplication()
                        .getString((R.string.undefined_error_occurred_on_1800wxbrief_request))));
            }

        }
    }

    public ArrayList<String> getBriefingTypeList() {
        ArrayList<String> briefingTypes = new ArrayList<>();
        for (BriefingFormat briefingFormat : BriefingFormat.values()) {
            if (getOfficialBriefing().getValue()
                    // && (briefingType == BriefingType.EMAIL || briefingType == BriefingType.SIMPLE)
                    && briefingFormat == BriefingFormat.EMAIL) {
                // bypass
            } else {
                briefingTypes.add(briefingFormat.getDisplayValue());
            }
        }
        return briefingTypes;
    }

    public BriefingFormat getBriefTypeBasedOnDisplayValue(String displayValue) {
        for (BriefingFormat briefingFormat : BriefingFormat.values()) {
            if (briefingFormat.displayValue.equals(displayValue)) {
                return briefingFormat;
            }
        }
        // Oh-oh something really wrong
        return null;
    }

    ;

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
