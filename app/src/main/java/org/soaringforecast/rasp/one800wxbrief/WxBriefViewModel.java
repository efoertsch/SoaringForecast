package org.soaringforecast.rasp.one800wxbrief;

import android.annotation.SuppressLint;
import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.Constants;
import org.soaringforecast.rasp.common.ObservableViewModel;
import org.soaringforecast.rasp.common.messages.ProgramError;
import org.soaringforecast.rasp.one800wxbrief.messages.WXBriefDownloadsPermission;
import org.soaringforecast.rasp.one800wxbrief.options.BriefingOption;
import org.soaringforecast.rasp.one800wxbrief.options.BriefingOptions;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.ReturnCodedMessage;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.RouteBriefing;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.RouteBriefingRequest;
import org.soaringforecast.rasp.one800wxbrief.routebriefing.WxBriefRequestResponse;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.TaskTurnpoint;
import org.soaringforecast.rasp.repository.messages.DataBaseError;
import org.soaringforecast.rasp.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
    private static final SimpleDateFormat departureInstantFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static final long ONE_HOUR_IN_MILLISECS = 60 * 60 * 1000;
    private static final long ONE_DAY_IN_MILLISEC = 24 * ONE_HOUR_IN_MILLISECS;
    private static final String NOTAMS_BRIEF = "NOTAMS_BRIEF";
    private static final String REGULAR_BRIEF = "REGULAR_BRIEF";

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
    private MutableLiveData<BriefingOptions> masterBriefingOptions = new MutableLiveData<>();
    private MutableLiveData<String> wxBriefWebUserName;
    private MutableLiveData<Boolean> tailoringListUpdatedFlag;
    private MutableLiveData<Boolean> validBriefingData = new MutableLiveData<>();
    private MutableLiveData<Uri> wxBriefUri;
    private MutableLiveData<String> simpleBriefingText = new MutableLiveData<>();
    //private MutableLiveData<String> aircraftRegistration = new MutableLiveData<>();
    private MutableLiveData<String> accountName = new MutableLiveData<>();
    private MutableLiveData<Boolean> pdfBrief = new MutableLiveData<>();
    private MutableLiveData<Boolean> displayProductCodes = new MutableLiveData<>();


    private MediatorLiveData validationMediator = new MediatorLiveData<Void>();

    // Should point to standard brief
    private Integer defaultTypeOfBriefPosition = 1;
    private Pattern wxBriefWebUserNamePattern = Pattern.compile("^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$");
    private int minRouteCorridorWidth = 25;
    private String defaultRouteCorridorWidth = minRouteCorridorWidth + "";
    private int maxRouteCorridorWidth = 100;
    private int minWindsAloftCorridor = 100;
    private String defaultWindsAloftCorridor = minWindsAloftCorridor + "";
    private int maxWindAloftCorridor = 600;
    private Pattern aircraftIdPattern = Pattern.compile("[A-Z0-9]{2,7}");
    private RouteBriefingRequest routeBriefingRequest;
    private AppPreferences appPreferences;
    private Constants.TypeOfBrief selectedTypeOfBrief;
    private boolean isInitialized = false;

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

    private Constants.BriefingFormat selectedBriefingFormat = Constants.BriefingFormat.EMAIL;

    public WxBriefViewModel(@NonNull Application application) {
        super(application);
    }

    public WxBriefViewModel setRepository(AppRepository appRepository) {
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

    public void init(Constants.TypeOfBrief typeOfBrief) {
        // Since viewmodel shared among fragments only initialize once
        if (isInitialized) {
            return;
        }
        isInitialized = true;
        if (routeBriefingRequest == null) {
            routeBriefingRequest = RouteBriefingRequest.newInstance();
        }
        getOfficialBriefing();
        getBriefFormats();
        getAircraftId();
        getWxBriefUserName();
        getDisplayEmailAddressField();
        getBriefingDates();
        getDepartureTimes();
        formatDepartureInstant();
        setCorridorValues();
        getTurnpointList();
        loadTask();
        loadTaskTurnpoints();
        // a bit of a hack
        // set this to load in appropriate products and filter
        if (typeOfBrief != null && typeOfBrief == Constants.TypeOfBrief.NOTAMS) {
            defaultTypeOfBriefPosition = Constants.TypeOfBrief.NOTAMS.ordinal();
        }
        getTypesOfBriefs();

    }


    // Using MediatorLiveData to handle post LiveData updates for validation checks etc.
    public void startListening() {
        validationMediator.addSource(officialBriefing, isChecked -> updateOfficialBriefing((Boolean) isChecked));
        validationMediator.addSource(selectedTypeOfBriefPosition, position -> setSelectedTypeOfBriefPosition((Integer) position));
        validationMediator.addSource(aircraftId, aircraftId -> validateAircraftId((String) aircraftId));
        validationMediator.addSource(wxBriefWebUserName, wxBriefWebUserName -> validateWxBriefUserName((String) wxBriefWebUserName));
        validationMediator.addSource(routeCorridorWidth, routeCorridorWidth -> validateRouteCorridorWidth((String) routeCorridorWidth));
        validationMediator.addSource(windsAloftCorridor, windsAloftCorridor -> validateWindsAloftCorridor((String) windsAloftCorridor));
        validationMediator.addSource(selectedBriefingDatePosition, selectedBriefingDatePosition -> setDepartureDate());
        //validationMediator.addSource(selectedDepartureTimePosition, selectedDepartureTimePosition -> formatDepartureInstant());
        validationMediator.addSource(selectedBriefFormatPosition, selectedBriefFormatPosition -> updateBriefingFormat((Integer) selectedBriefFormatPosition));
    }

    public void stopListening() {
        validationMediator.removeSource(officialBriefing);
        validationMediator.removeSource(selectedTypeOfBriefPosition);
        validationMediator.removeSource(aircraftId);
        validationMediator.removeSource(wxBriefWebUserName);
        validationMediator.removeSource(routeCorridorWidth);
        validationMediator.removeSource(windsAloftCorridor);
        validationMediator.removeSource(selectedBriefingDatePosition);
        validationMediator.removeSource(selectedDepartureTimePosition);
        validationMediator.removeSource(selectedBriefFormatPosition);
    }

    public MediatorLiveData<Void> getValidator() {
        return validationMediator;
    }

    private void loadProductsAndTailoringOptions() {
        Single<ArrayList<BriefingOption>> singleProductCodes = appRepository.getWxBriefProductCodes(selectedTypeOfBrief);
        Single<ArrayList<BriefingOption>> singleTailoringOptions;
        if (selectedBriefingFormat == Constants.BriefingFormat.EMAIL) {
            singleTailoringOptions = appRepository.getWxBriefNonNGBV2TailoringOptions(selectedTypeOfBrief);
        } else {
            singleTailoringOptions = appRepository.getWxBriefNGBV2TailoringOptions(selectedTypeOfBrief);
        }
        Disposable disposable = Single.zip(
                singleProductCodes,
                singleTailoringOptions,
                (productCodes, tailoringOptions) -> createBriefingOptions(productCodes, tailoringOptions,selectedBriefingFormat ))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(briefingOptions -> {
                            setMasterBriefingOptions(briefingOptions);
                        },
                        // TODO replace with error msg
                        Timber::e);

        compositeDisposable.add(disposable);
    }

    private BriefingOptions createBriefingOptions(ArrayList<BriefingOption> productCodes, ArrayList<BriefingOption> tailoringOptions, Constants.BriefingFormat  briefingFormat) {
        return new BriefingOptions(productCodes, tailoringOptions,   briefingFormat );
    }

    public MutableLiveData<BriefingOptions> getMasterBriefingOptions() {
        return masterBriefingOptions;
    }

    private void setMasterBriefingOptions(BriefingOptions masterBriefingOptions) {
        this.masterBriefingOptions.setValue(masterBriefingOptions);
        routeBriefingRequest.setProductCodes(masterBriefingOptions.getProductCodesForBriefing());
        routeBriefingRequest.setTailoringOptions(masterBriefingOptions.getTailoringOptionsForBriefing());
        validateData();
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
     * For now assume all turnpoints are airports
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


    // always make it official briefing for now
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
        // bypass as all briefing formats valid for both official/informal brief
        // updateBriefingFormatList();
    }

    /**
     * Type of Brief - Outlook, Standard, Abbreviated) - skip NOTAMS
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
     * Based on selected brief  (Outlook, Standard, Abbreviated, NOTAMS)
     * set various briefing options
     *
     * @param position
     */
    public void setSelectedTypeOfBriefPosition(int position) {
        selectedTypeOfBrief = Constants.TypeOfBrief.values()[position];
        routeBriefingRequest.setTypeOfBrief(selectedTypeOfBrief);
        displayProductCodes.setValue(selectedTypeOfBrief == Constants.TypeOfBrief.ABBREVIATED);
        loadProductsAndTailoringOptions();
    }

    public MutableLiveData<Boolean> getDisplayProductCodes() {
        return displayProductCodes;
    }


    public MutableLiveData<String> getAircraftId() {
        if (aircraftId == null) {
            aircraftId = new MutableLiveData<>();
            String savedAircraftId = appPreferences.getAircraftRegistration();
            aircraftId.setValue(savedAircraftId.isEmpty() ? "" : savedAircraftId);
            validateAircraftId(savedAircraftId);
        }
        return aircraftId;
    }

    public void validateAircraftId(String aircraftId) {
        String trimmedId = aircraftId.trim();
        if (aircraftId != null &&
                aircraftIdPattern.matcher(trimmedId).matches()) {
            routeBriefingRequest.setAircraftIdentifier(trimmedId);
            appPreferences.setAircraftRegistration(trimmedId);
            setAircraftIdErrorText(null);
        } else {
            setAircraftIdErrorText(getApplication().getString(R.string.invalid_aircraft_id));
        }
        validateData();
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
            displayEmailAddressField.setValue(selectedBriefingFormat == Constants.BriefingFormat.EMAIL);
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
        validateData();
        return wxBriefWebUserName;
    }

    public void validateWxBriefUserName(String userName) {
        if (userName != null && userName.length() > 2
                && wxBriefWebUserNamePattern.matcher(userName).matches()) {
            appPreferences.setOne800WxBriefUserId(userName);
            assignUserNameAndEmailAddress(userName);
            setWxBriefUserNameErrorText(null);
        } else {
            setWxBriefUserNameErrorText(getApplication().getString(R.string.invalid_wxbrief_web_username));
        }
        validateData();
    }

    private void assignUserNameAndEmailAddress(String username) {
        routeBriefingRequest.setWebUserName(username);
        routeBriefingRequest.setEmailAddress(username);
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
            if (width >= minRouteCorridorWidth && width <= maxRouteCorridorWidth) {
                routeBriefingRequest.setRouteCorridorWidth(width + "");
                setCorridorWidthErrorText(null);
            }
        } catch (NumberFormatException nfe) {
            setCorridorWidthErrorText(getApplication()
                    .getString(R.string.route_corridor_width_min_and_max_range, minRouteCorridorWidth, maxRouteCorridorWidth));
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
            windsAloftCorridor = new MutableLiveData<>();
            windsAloftCorridor.setValue(defaultWindsAloftCorridor);
        }
        return windsAloftCorridor;
    }


    public void validateWindsAloftCorridor(String windsAloftCorridor) {
        try {
            int width = Integer.parseInt(windsAloftCorridor);
            if (width >= minWindsAloftCorridor && width <= maxWindAloftCorridor) {
                routeBriefingRequest.setWindsAloftCorridorWidth(width + "");
                setCorridorWidthErrorText(null);
            }
        } catch (NumberFormatException nfe) {
            setWindsAloftCorridorErrorText(getApplication()
                    .getString(R.string.winds_aloft_corridor_min_to_max_value, minWindsAloftCorridor, maxWindAloftCorridor));
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
            dateList.add(departureDateFormat.format(currentTime + ONE_DAY_IN_MILLISEC));
            dateList.add(departureDateFormat.format(currentTime + (2 * ONE_DAY_IN_MILLISEC)));
            briefingDates.setValue(dateList);
            getSelectedBriefingDatePosition();
        }
        return briefingDates;
    }

    private void setDepartureDate() {
        // if not current day, then set to outlook briefing
        if (selectedBriefingDatePosition.getValue() != 0) {
            if (selectedTypeOfBriefPosition.getValue() == Constants.TypeOfBrief.STANDARD.ordinal()) {
                selectedTypeOfBriefPosition.setValue(0);
            }
        }
        formatDepartureInstant();

    }

    /**
     * Convert selected local/date and  time to Zulu
     * Time must be in format of yyyy-MM-ddTHH:mm:ss.S
     */
    private void formatDepartureInstant() {
        // if today assume flight 1 hr in future
        if (selectedBriefingDatePosition.getValue() == 0) {
            routeBriefingRequest.setDepartureInstant(
                    TimeUtils.convertLocalTimeToZulu(departureInstantFormat.format(System.currentTimeMillis() + ONE_HOUR_IN_MILLISECS)));
        } else { // assume 9AM local time departure
            StringBuilder sb = new StringBuilder();
            sb.append(briefingDates.getValue().get(selectedBriefingDatePosition.getValue()))
                    .append("T")
                    .append("09:00:00.000");
            routeBriefingRequest.setDepartureInstant(TimeUtils.convertLocalTimeToZulu(sb.toString()));
        }
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
            int time = 9;
            for (int i = 0; i < 8; ++i) {
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
            briefingFormats.setValue(getBriefingFormatList());
            getSelectedBriefFormatPosition();
        }
        return briefingFormats;
    }

    public ArrayList<String> getBriefingFormatList() {
        ArrayList<String> briefingTypes = new ArrayList<>();
        for (Constants.BriefingFormat briefingFormat : Constants.BriefingFormat.values()) {
            boolean isOfficialBriefing = getOfficialBriefing().getValue();
            if (isOfficialBriefing
                    || (!isOfficialBriefing && briefingFormat.isValidForNotABrief())) {
                briefingTypes.add(briefingFormat.getDisplayValue());
            }
        }
        return briefingTypes;
    }

    // Update allowed briefing formats due to change in official briefing flag
    public void updateBriefingFormatList() {
        if (selectedBriefFormatPosition != null && briefingFormats != null) {
            selectedBriefFormatPosition.setValue(0);
            briefingFormats.setValue(getBriefingFormatList());
            updateBriefingFormat(selectedBriefFormatPosition.getValue());
        }

    }

    // Briefing is Email, or PDF, Simple
    public MutableLiveData<Integer> getSelectedBriefFormatPosition() {
        if (selectedBriefFormatPosition == null) {
            selectedBriefFormatPosition = new MutableLiveData<>();
            selectedBriefFormatPosition.setValue(0);
        }
        return selectedBriefFormatPosition;
    }

    public void resetSelectedBriefFormatPosition() {
        if (selectedBriefFormatPosition == null) {
            getSelectedBriefFormatPosition();
            return;
        }
        selectedBriefFormatPosition.setValue(0);
    }

    public Constants.BriefingFormat getBriefingFormatBasedOnDisplayValue(String displayValue) {
        for (Constants.BriefingFormat briefingFormat : Constants.BriefingFormat.values()) {
            if (briefingFormat.getDisplayValue().equals(displayValue)) {
                return briefingFormat;
            }
        }
        // Oh-oh something really wrong
        return null;
    }


    public void updateBriefingFormat(int selectedBriefingFormatPosition) {
        selectedBriefingFormat = getBriefingFormatBasedOnDisplayValue(
                briefingFormats.getValue().get(selectedBriefingFormatPosition));
        routeBriefingRequest.setSelectedBriefingType(selectedBriefingFormat.name());
        displayEmailAddressField.setValue(selectedBriefingFormat == Constants.BriefingFormat.EMAIL);
        toggleTailoringListUpdatedFlag();
        if (selectedBriefingFormat == Constants.BriefingFormat.NGBV2) {
            post(new WXBriefDownloadsPermission());
        }
        loadProductsAndTailoringOptions();
    }

    // currently always true unless we implement option for BriefingFormat.SIMPLE
    public MutableLiveData<Boolean> getDisplayTailoringOptions() {
        if (displayTailoringOptions == null) {
            displayTailoringOptions = new MutableLiveData<>();
            displayTailoringOptions.setValue(true);
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

    // always assume official briefing
    private void validateData() {
        validBriefingData.setValue(
                aircraftIdErrorText.getValue() == null
                        && wxBriefUserNameErrorText.getValue() == null
                        && corridorWidthErrorText.getValue() == null
                        && windsAloftCorridorErrorText.getValue() == null
                        && masterBriefingOptions.getValue() != null);
    }

    public MutableLiveData<Boolean> isValidData() {
        validateData();
        return validBriefingData;
    }

    public void updateProductCodesSelected(boolean[] selectedProductCodes) {
        masterBriefingOptions.getValue().updateProductCodesSelected(selectedProductCodes);
        routeBriefingRequest.setProductCodes(masterBriefingOptions.getValue().getProductCodesForBriefing());

    }

    public void updateTailoringOptionsSelected(boolean[] selectedTailoringOptions) {
        masterBriefingOptions.getValue().updateTailoringOptionsSelected(selectedTailoringOptions);
        routeBriefingRequest.setTailoringOptions(masterBriefingOptions.getValue().getTailoringOptionsForBriefing());
    }

    // NOTAMS assume abbreviated briefing with departure 1 hr from now.
    public void submitNOTAMSBriefingRequest() {
        working.setValue(true);
        // override various values
        officialBriefing.setValue(true);
        formatDepartureInstant();
        // set departure 1 hr in future must be in zulu time
        submitBriefingRequest();
    }

    public void submitBriefingRequest() {
        validateData();
        if (!isValidData().getValue()) {
            return;
        }
        setWorkingFlag(true);
        Disposable disposable = null;
        try {
            disposable = appRepository.submitWxBriefBriefingRequest(routeBriefingRequest.getRestParmString())
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
        } catch (Exception e) {
            post(new ProgramError(e));
        }
        compositeDisposable.add(disposable);
    }


    private void evaluateRouteBriefingCall(RouteBriefing routeBriefing) {
        setWorkingFlag(false);
        if (routeBriefing != null && routeBriefing.returnStatus && routeBriefing.returnMessages.size() == 0) {
            // request submitted OK
            if (selectedBriefingFormat.equals(Constants.BriefingFormat.EMAIL)) {
                post(new WxBriefRequestResponse(getApplication().getString(R.string.your_briefing_should_arrive_in_your_mailbox_shortly), false));
            } else if (selectedBriefingFormat.equals(Constants.BriefingFormat.NGBV2)) {
                // need to get NGBV2 briefing from
                createRouteBriefingPDF(routeBriefing.ngbv2PdfBriefing);
            } else if ((selectedBriefingFormat.equals(Constants.BriefingFormat.SIMPLE))) {
                createSimpleRouteBriefing(routeBriefing.simpleWeatherBriefing);
            }
        } else {
            // Error in request
            if (routeBriefing == null) {
                post(new WxBriefRequestResponse(getApplication()
                        .getString((R.string.undefined_error_occurred_on_1800wxbrief_request)), true));
            } else if (routeBriefing.returnCodedMessage != null && routeBriefing.returnCodedMessage.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (ReturnCodedMessage returnCodedMessage : routeBriefing.returnCodedMessage) {
                    sb.append(returnCodedMessage.code)
                            .append(":")
                            .append(returnCodedMessage.message)
                            .append('\n');
                }
                post(new WxBriefRequestResponse(sb.toString(), true));
            } else { // error but no error msg
                post(new WxBriefRequestResponse(getApplication()
                        .getString((R.string.undefined_error_occurred_on_1800wxbrief_request)), true));
            }
        }
    }

    public MutableLiveData<String> getSimpleBriefingText() {
        return simpleBriefingText;
    }

    public void removeSimpleBriefingText() {
        simpleBriefingText.setValue(null);

    }

    private void createSimpleRouteBriefing(String simpleWeatherBriefing) {
        simpleBriefingText.setValue(simpleWeatherBriefing.replace("_NL_", "\n"));
    }

    private void createRouteBriefingPDF(String ngbv2PdfBriefing) {
        setWorkingFlag(true);
        Disposable disposable = null;

        disposable = appRepository.writeWxBriefToDirectory(ngbv2PdfBriefing)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pdfBriefUri -> {
                            setWxBriefUri(pdfBriefUri);
                            setWorkingFlag(false);
                        },
                        t -> {
                            //TODO email stack trace
                            Timber.e(t);
                            post(new WxBriefRequestResponse(getApplication()
                                    .getString(R.string.undefined_error_occurred_on_1800wxbrief_request), true, new Exception(t)));
                            setWorkingFlag(false);
                        });
        compositeDisposable.add(disposable);
    }

    public void requestNOTAMSBriefing() {
        //construct standard brief only requesting NOTAMS

    }

    public LiveData<Uri> getWxBriefUri() {
        if (wxBriefUri == null) {
            wxBriefUri = new MutableLiveData<>();
        }
        return wxBriefUri;
    }

    private void setWxBriefUri(Uri pdfBriefUri) {
        // Do this as right now using fixed name for pdf file, so make sure the observer will fire
        // for subsequent briefings
        wxBriefUri.setValue(Uri.EMPTY);
        wxBriefUri.setValue(pdfBriefUri);
    }

    public void saveDefaultSettings() {
        appPreferences.setAircraftRegistration(aircraftId.getValue());
        appPreferences.setOne800WxBriefUserId(wxBriefWebUserName.getValue());
    }

    public void showWxBriefDisclaimer() {
        appPreferences.setWxBriefShowDisclaimer(true);
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
