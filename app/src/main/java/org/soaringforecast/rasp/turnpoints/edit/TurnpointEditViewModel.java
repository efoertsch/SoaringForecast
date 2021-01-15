package org.soaringforecast.rasp.turnpoints.edit;

import android.app.Application;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.ObservableViewModel;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.repository.messages.DataBaseError;
import org.soaringforecast.rasp.turnpoints.cup.CupStyle;
import org.soaringforecast.rasp.turnpoints.json.ElevationQuery;
import org.soaringforecast.rasp.turnpoints.json.NationalMap;
import org.soaringforecast.rasp.turnpoints.messages.DeletedTurnpoint;
import org.soaringforecast.rasp.turnpoints.messages.SendEmail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class TurnpointEditViewModel extends ObservableViewModel {

    private AppRepository appRepository;
    private Turnpoint turnpoint;
    private Turnpoint originalTurnpoint;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    // Make sure to add any additional error text feeds to reset method
    private String titleErrorText = null;
    private String codeErrorText = null;
    private String countryErrorText = null;
    private String latitudeErrorText = null;
    private String longitudeErrorText = null;
    private String elevationErrorText = null;
    private String directionErrorText = null;
    private String lengthErrorText = null;
    private String widthErrorText = null;
    private String frequencyErrorText = null;
    private String elevationPreference;
    private boolean noErrors = true;
    private boolean displayCupFormat = true;

    private MutableLiveData<List<CupStyle>> cupStyles = new MutableLiveData<>();
    private MutableLiveData<Integer> cupStylePosition = new MutableLiveData<>();
    private MutableLiveData<Boolean> okToSave = new MutableLiveData<>();
    private MutableLiveData<Boolean> needToSaveUpdates = new MutableLiveData<>();
    private MutableLiveData<Boolean> inEditMode = new MutableLiveData<>();
    private MutableLiveData<Boolean> amWorking = new MutableLiveData<>();

    // Handy site for regex development/testing https://www.debuggex.com/
    private static final String latitudeCupRegex = "(9000\\.000|[0-8][0-9][0-5][0-9]\\.[0-9]{3})[NS]";
    private static final String longitudeCupRegex = "(18000\\.000|(([0-1][0-7])|([0][0-9]))[0-9][0-5][0-9]\\.[0-9]{3})[EW]";
    private static final String elevationRegex = "([0-9]{1,4}(\\.[0-9])?|(\\.[0-9]))(m|ft)";
    private static final String directionRegex = "(360|(3[0-5][0-9])|([12][0-9][0-9])|(0[0-9][0-9]))";
    private static final String lengthRegex = "([0-9]{1,5}((\\.[0-9])?))(m|ft)";
    private static final String widthRegex = "([0-9]{1,3})(m|ft)";
    private static final String frequencyRegex = "1[1-3][0-9]\\.[0-9][0-9](0|5)";
    private static final Pattern longitudeCupPattern = Pattern.compile(longitudeCupRegex);
    private static final Pattern latitudeCupPattern = Pattern.compile(latitudeCupRegex);
    private static final Pattern elevationPattern = Pattern.compile(elevationRegex);
    private static final Pattern directionPattern = Pattern.compile(directionRegex);
    private static final Pattern lengthPattern = Pattern.compile(lengthRegex);
    private static final Pattern widthPattern = Pattern.compile(widthRegex);
    private static final Pattern frequencyPatten = Pattern.compile(frequencyRegex);
    private AppPreferences appPreferences;
    private boolean emailCupFile = false;
    private boolean enableTurnpointCodeEdit = false;


    public TurnpointEditViewModel(@NonNull Application application) {
        super(application);
    }

    public TurnpointEditViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }

    public TurnpointEditViewModel setAppPreferences(AppPreferences appPreferences) {
        this.appPreferences = appPreferences;
        elevationPreference = appPreferences.getAltitudeDisplay();
        return this;
    }

    public TurnpointEditViewModel setTurnpoint(Turnpoint turnpoint) {
        this.turnpoint = turnpoint;
        amWorking.setValue(false);
        if (originalTurnpoint == null) {
            originalTurnpoint = Turnpoint.newInstance(turnpoint);
            resetErrorText();
            setSaveIndicator();
        }
        setCupStylePositionForTurnpoint();

        return this;
    }



    @Bindable
    public String getTitle() {
        return turnpoint.getTitle();
    }


    @Bindable
    public void setTitle(String value) {
        value = value.trim();
        if (value == null || value.isEmpty()) {
            titleErrorText = getApplication().getString(R.string.turnpoint_title_error_msg);
        } else {
            if (!originalTurnpoint.getTitle().equals(value)) {
                needToSaveUpdates.setValue(true);
                titleErrorText = null;
            }
        }
        turnpoint.setTitle(value);
        setSaveIndicator();
        notifyPropertyChanged(org.soaringforecast.rasp.BR.titleErrorText);
    }


    @Bindable
    public String getCode() {
        return turnpoint.getCode();
    }

    // Note additional restrictions in layout xml
    @Bindable
    public void setCode(String value) {
        value = value.trim();
        if (value == null
                || value.isEmpty()
                || value.contains(" ")) {
            codeErrorText = getApplication().getString(R.string.turnpoint_code_error_msg);
        } else {
            if (!originalTurnpoint.getCode().equals(value)) {
                checkForDuplicateCode(value);
                needToSaveUpdates.setValue(true);
                codeErrorText = null;
            }
        }
        turnpoint.setCode(value);
        setSaveIndicator();
        notifyPropertyChanged(org.soaringforecast.rasp.BR.codeErrorText);
    }

    private void checkForDuplicateCode(String value) {
        if (value != null && !value.isEmpty()) {
            Disposable disposable = appRepository.getTurnpointByCode(value)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(turnpoint -> {
                                codeErrorText = getApplication().getString(R.string.duplicate_turnpoint_code);
                            },
                            //TODO
                            t -> {
                                post(new SnackbarMessage(getApplication().getString(R.string.error_checking_for_duplicate_turnpoint_code)));
                                Timber.e(t);
                            });

            compositeDisposable.add(disposable);
        }
    }

    @Bindable
    public String getCountry() {
        return turnpoint.getCountry().toUpperCase();
    }

    @Bindable
    public void setCountry(String value) {
        value = value.trim().toUpperCase();
        if (!originalTurnpoint.getCountry().equals(value)) {
            needToSaveUpdates.setValue(true);
            setSaveIndicator();
        }
        turnpoint.setCountry(value);
    }

    @Bindable
    public String getCupLatitude() {
        return turnpoint.getLatitudeInCupFormat();
    }

    @Bindable
    public void setCupLatitude(String value) {
        if (!displayCupFormat) {
            return;
        }
        value = value.trim();
        try {
            if (latitudeCupPattern.matcher(value.trim()).matches()) {
                if (Turnpoint.convertToLat(value.trim()) != originalTurnpoint.getLatitudeDeg()) {
                    turnpoint.setLatitudeDeg(Turnpoint.convertToLat(value.trim()));
                    needToSaveUpdates.setValue(true);
                }
                latitudeErrorText = null;
            } else {
                latitudeErrorText = getApplication().getString(R.string.turnpoint_cup_latitude_error);
            }
        } catch (Exception e) {
            latitudeErrorText = getApplication().getString(R.string.turnpoint_cup_latitude_error);
        }
        setSaveIndicator();
        notifyPropertyChanged(org.soaringforecast.rasp.BR.latitudeErrorText);
    }

    @Bindable
    public String getGoogleLatitude() {
        return String.format("%.5f", turnpoint.getLatitudeDeg());
    }

    @Bindable
    public void setGoogleLatitude(String googleLatitude) {
        if (displayCupFormat) {
            return;
        }
        try {
            float latitude = Float.valueOf(googleLatitude);
            if (latitude >= -90 && latitude <= 90) {
                if (originalTurnpoint.getLatitudeDeg() != latitude) {
                    turnpoint.setLatitudeDeg(latitude);
                    needToSaveUpdates.setValue(true);
                }
                latitudeErrorText = null;
            } else {
                latitudeErrorText = getApplication().getString(R.string.turnpoint_google_latitude_range_error);
            }
        } catch (Exception e) {
            latitudeErrorText = getApplication().getString(R.string.turnpoint_google_latitude_error);
        }
        setSaveIndicator();
        notifyPropertyChanged(org.soaringforecast.rasp.BR.latitudeErrorText);
    }

    @Bindable
    public String getCupLongitude() {
        return turnpoint.getLongitudeInCupFormat();
    }

    @Bindable
    public void setCupLongitude(String value) {
        if (!displayCupFormat) {
            return;
        }
        value = value.trim();
        try {
            if (longitudeCupPattern.matcher(value.trim()).matches()) {
                if (originalTurnpoint.getLongitudeDeg() != Turnpoint.convertToLong(value.trim())) {
                    turnpoint.setLongitudeDeg(Turnpoint.convertToLong(value.trim()));
                    needToSaveUpdates.setValue(true);
                }
                longitudeErrorText = null;
            } else {
                longitudeErrorText = getApplication().getString(R.string.turnpoint_cup_longitude_error);
            }
        } catch (Exception e) {
            longitudeErrorText = getApplication().getString(R.string.turnpoint_cup_longitude_error);
        }
        setSaveIndicator();
        notifyPropertyChanged(org.soaringforecast.rasp.BR.longitudeErrorText);
    }

    @Bindable
    public String getGoogleLongitude() {
        return String.format("%.5f", turnpoint.getLongitudeDeg());
    }

    @Bindable
    public void setGoogleLongitude(String googleLongitude) {
        if (displayCupFormat) {
            return;
        }
        try {
            float longitude = Float.valueOf(googleLongitude);
            if (longitude >= -180 && longitude <= 180) {
                if (originalTurnpoint.getLongitudeDeg() != longitude) {
                    turnpoint.setLongitudeDeg(longitude);
                    needToSaveUpdates.setValue(true);
                }
                longitudeErrorText = null;
            } else {
                longitudeErrorText = getApplication().getString(R.string.turnpoint_google_longitude_range_error);
            }
        } catch (Exception e) {
            longitudeErrorText = getApplication().getString(R.string.turnpoint_google_longitude_range_error);
        }
        setSaveIndicator();
        notifyPropertyChanged(org.soaringforecast.rasp.BR.longitudeErrorText);
    }

    @Bindable
    public boolean isDisplayCupFormat() {
        return displayCupFormat;
    }

    public void toggleLatLongFormat() {
        this.displayCupFormat = !displayCupFormat;
        notifyPropertyChanged(org.soaringforecast.rasp.BR.displayCupFormat);
        notifyPropertyChanged(org.soaringforecast.rasp.BR.formattedTurnpointDetails);
    }

    public LiveData<List<CupStyle>> getCupStyles() {
        if (cupStyles.getValue() == null || cupStyles.getValue().size() == 0) {
            loadCupStyles();
        }
        return cupStyles;
    }


    public List<CupStyle> getCupStylesList() {
        if (cupStyles.getValue() != null) {
            return cupStyles.getValue();
        } else {
            return new ArrayList<>();
        }
    }

    private void loadCupStyles() {
        Disposable disposable = appRepository.getCupStyles()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cupStyles -> {
                            this.cupStyles.setValue(cupStyles.getCupStyles());
                            setCupStylePositionForTurnpoint();
                        },
                        Timber::e);

        compositeDisposable.add(disposable);
    }

    // Called after the JSON list of cup styles loaded
    private void setCupStylePositionForTurnpoint() {
        if (cupStyles.getValue() == null || cupStyles.getValue().size() == 0) {
            loadCupStyles();
            return;
        }
        for (CupStyle cupStyle : cupStyles.getValue()) {
            if (turnpoint.getStyle().equals(cupStyle.getStyle())) {
                try {
                    cupStylePosition.setValue(Integer.parseInt(turnpoint.getStyle()));
                    return;
                } catch (NumberFormatException nfe) {
                    cupStylePosition.setValue(0);
                    setSaveIndicator();
                }
            }
        }
        // Here if didn't find style in list
        turnpoint.setStyle("0");
    }

    public MutableLiveData<Integer> getCupStylePosition() {
        return cupStylePosition;
    }


    // Called by UI after turnpoint style changed
    public void setCupStylePosition(int newCupStylePosition) {
        if (!originalTurnpoint.getStyle().equals(newCupStylePosition + "")) {
            cupStylePosition.setValue(newCupStylePosition);
            turnpoint.setStyle(newCupStylePosition + "");
            validateOtherFields();
            needToSaveUpdates.setValue(true);
            setSaveIndicator();
        }
    }

    @Bindable
    public String getElevation() {
        return turnpoint.getElevation();
    }

    @Bindable
    public void setElevation(String value) {
        value = value.trim();
        if (elevationPattern.matcher(value).matches()) {
            if (!originalTurnpoint.getElevation().equals(value)) {
                needToSaveUpdates.setValue(true);
            }
            elevationErrorText = null;
        } else {
            elevationErrorText = getApplication().getString(R.string.elevation_error);
        }
        turnpoint.setElevation(value);
        setSaveIndicator();
        notifyPropertyChanged(org.soaringforecast.rasp.BR.elevationErrorText);
    }

    @Bindable
    public String getDirection() {
        return turnpoint.getDirection();
    }

    @Bindable
    public void setDirection(String value) {
        value = value.trim();
        validateDirection(value);
        turnpoint.setDirection(value);
    }

    private void validateDirection(String value) {
        if ((value.isEmpty() && !isLandable()) ||
                (value.length() == 3 && isLandable() && directionPattern.matcher(value).matches())) {
            if (!originalTurnpoint.getDirection().equals(value)) {
                needToSaveUpdates.setValue(true);
            }
            directionErrorText = null;
        } else {
            directionErrorText = getApplication().getString(R.string.direction_error);
        }
        setSaveIndicator();
        notifyPropertyChanged(org.soaringforecast.rasp.BR.directionErrorText);
    }

    @Bindable
    public String getLength() {
        return turnpoint.getLength();
    }

    // Only used with Waypoint style types 2, 3, 4 and 5
    @Bindable
    public void setLength(String value) {
        value = value.trim();
        validateLength(value);
        turnpoint.setLength(value);
    }

    private void validateLength(String value) {
        if ((value.isEmpty() && !isLandable())
                || (isLandable() && lengthPattern.matcher(value).matches()
                && (value.endsWith("ft") || value.equals("m")))) {
            if (!originalTurnpoint.getLength().equals(value)) {
                needToSaveUpdates.setValue(true);
            }
            lengthErrorText = null;
        } else {
            lengthErrorText = getApplication().getString(R.string.runway_length_error);
        }
        setSaveIndicator();
        notifyPropertyChanged(org.soaringforecast.rasp.BR.lengthErrorText);
    }

    @Bindable
    public String getWidth() {
        return turnpoint.getRunwayWidth();
    }

    // Only used with Waypoint style types 2, 3, 4 and 5
    @Bindable
    public void setWidth(String value) {
        value = value.trim();
        validateWidth(value);
        turnpoint.setRunwayWidth(value);
    }

    private void validateWidth(String value) {
        if ((value.isEmpty() && !isLandable())
                || (isLandable() && widthPattern.matcher(value).matches())) {
            if (!originalTurnpoint.getRunwayWidth().equals(value)) {
                needToSaveUpdates.setValue(true);
            }
            widthErrorText = null;
        } else {
            widthErrorText = getApplication().getString(R.string.runway_width_error);
        }
        setSaveIndicator();
        notifyPropertyChanged(org.soaringforecast.rasp.BR.widthErrorText);
    }

    @Bindable
    public String getFrequency() {
        return turnpoint.getFrequency();
    }

    @Bindable
    public void setFrequency(String value) {
        value = value.trim();
        validateFrequency(value);
        turnpoint.setFrequency(value);
    }

    private void validateFrequency(String value) {
        if (value.isEmpty() ||
                (value.length() == 7 && frequencyPatten.matcher(value).matches())) {
            needToSaveUpdates.setValue(true);
            frequencyErrorText = null;
        } else {
            frequencyErrorText = getApplication().getString(R.string.frequency_format_errror);
        }
        setSaveIndicator();
        notifyPropertyChanged(org.soaringforecast.rasp.BR.frequencyErrorText);
    }

    @Bindable
    public String getDescription() {
        return turnpoint.getDescription();
    }

    @Bindable
    public void setDescription(String value) {
        value = value.trim();
        if (!originalTurnpoint.getDescription().equals(value)) {
            turnpoint.setDescription(value);
            needToSaveUpdates.setValue(true);
        }
        setSaveIndicator();
    }

    @Bindable
    public String getTitleErrorText() {
        return titleErrorText;
    }

    @Bindable
    public String getCodeErrorText() {
        return codeErrorText;
    }

    @Bindable
    public String getCountryErrorText() {
        return countryErrorText;
    }

    @Bindable
    public String getLatitudeErrorText() {
        return latitudeErrorText;
    }

    @Bindable
    public String getLongitudeErrorText() {
        return longitudeErrorText;
    }

    @Bindable
    public String getElevationErrorText() {
        return elevationErrorText;
    }

    @Bindable
    public String getDirectionErrorText() {
        return directionErrorText;
    }

    @Bindable
    public String getLengthErrorText() {
        return lengthErrorText;
    }

    @Bindable
    public String getWidthErrorText() {
        return widthErrorText;
    }

    @Bindable
    public String getFrequencyErrorText() {
        return frequencyErrorText;
    }

    /**
     * Validation based on cup style
     * 2 - AirfieldGrass
     * 3 - Outlanding
     * 4 - GliderSite
     * 5 - AirfieldSolid
     * runway direction and length needed
     */
    private void validateOtherFields() {
        if (turnpoint.isLandable()) {
            validateDirection(turnpoint.getDirection());
            validateLength(turnpoint.getLength());
            validateWidth(turnpoint.getRunwayWidth());
        } else {
            turnpoint.setDirection("");
            turnpoint.setLength("");
            turnpoint.setRunwayWidth("");
            directionErrorText = null;
            notifyPropertyChanged(org.soaringforecast.rasp.BR.directionErrorText);
            notifyPropertyChanged(org.soaringforecast.rasp.BR.direction);
            lengthErrorText = null;
            notifyPropertyChanged(org.soaringforecast.rasp.BR.lengthErrorText);
            notifyPropertyChanged(org.soaringforecast.rasp.BR.length);
            widthErrorText = null;
            notifyPropertyChanged(org.soaringforecast.rasp.BR.widthErrorText);
            notifyPropertyChanged(org.soaringforecast.rasp.BR.width);
        }
    }

    private boolean isLandable() {
        return turnpoint.isLandable();
    }

    public void saveTurnpoint() {
        //TODO if this is new turnpoint see if title and/or code already exists in db first
        try {
            Disposable disposable = appRepository.insertTurnpoint(turnpoint)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(id -> {
                                Timber.d("Saved turnpoint with id: %1$d", id);
                            },
                            t -> {
                                Timber.e(t);
                                post(new SnackbarMessage(getApplication().getString(R.string.error_in_turnpoint_validation)));
                            });

            compositeDisposable.add(disposable);
        } catch (Exception e) {
            post(new SnackbarMessage(getApplication().getString(R.string.error_saving_turnpoint)));
        }
    }


    void resetTurnpoint() {
        turnpoint = originalTurnpoint.newInstance(originalTurnpoint);
        okToSave.setValue(false);
        needToSaveUpdates.setValue(false);
        resetErrorText();
        setSaveIndicator();
        notifyChange();
    }

    private void resetErrorText() {
        titleErrorText = null;
        codeErrorText = null;
        countryErrorText = null;
        latitudeErrorText = null;
        longitudeErrorText = null;
        elevationErrorText = null;
        directionErrorText = null;
        lengthErrorText = null;
        widthErrorText = null;
        frequencyErrorText = null;
        noErrors = true;
        notifyChange();

    }

    private void setSaveIndicator() {
        noErrors = (titleErrorText == null
                && codeErrorText == null
                && countryErrorText == null
                && longitudeErrorText == null
                && elevationErrorText == null
                && directionErrorText == null
                && lengthErrorText == null
                && widthErrorText == null
                && frequencyErrorText == null);

        okToSave.setValue(noErrors);
    }

    MutableLiveData<Boolean> getOKToSaveFlag() {
        return okToSave;
    }

    @Bindable
    public String getFormattedTurnpointDetails() {
        return turnpoint.getFormattedTurnpointDetails(displayCupFormat);
    }

    public void getElevationAtLatLng(LatLng latLng) {
        amWorking.setValue(true);
        final String units = appPreferences.getAltitudeDisplay().equalsIgnoreCase("ft") ? "Feet" : "Meters";
        Disposable disposable = appRepository.getElevationAtLatLong(latLng.latitude, latLng.longitude, units)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(nationalMap -> {
                            setElevationFromUsgs(nationalMap);
                            notifyLatLongListeners();
                            amWorking.setValue(false);
                        },
                        t -> {
                            Timber.e(t);
                            EventBus.getDefault().post(new SnackbarMessage(getApplication().getString(R.string.error_finding_elevation)));
                            setElevation(String.format("%1$.1f%2$s", 0, (units.equalsIgnoreCase("Feet") ? "ft" : "m")));
                            notifyLatLongListeners();
                            amWorking.setValue(false);
                        });

        compositeDisposable.add(disposable);
    }

    /**
     * Only time this should be called is when adding a new turnpoint and the gps is used to set the
     * initial coordinates
     *
     * @param lastKnownLocation
     */
    public void setCurrentLocationFromGPS(Location lastKnownLocation) {
        originalTurnpoint.setLatitudeDeg(lastKnownLocation.getLatitude());
        turnpoint.setLatitudeDeg(originalTurnpoint.getLatitudeDeg());
        originalTurnpoint.setLongitudeDeg(lastKnownLocation.getLongitude());
        turnpoint.setLongitudeDeg(originalTurnpoint.getLongitudeDeg());
        originalTurnpoint.setElevation(String.format("%.1fft", lastKnownLocation.getAltitude() * 3.281));
        turnpoint.setElevation(originalTurnpoint.getElevation());
        notifyLatLongListeners();

    }

    private void setElevationFromUsgs(NationalMap nationalMap) {
        if (nationalMap != null && nationalMap.getUSGSElevationPointQueryService() != null
                && nationalMap.getUSGSElevationPointQueryService().getElevationQuery() != null) {
            ElevationQuery elevationQuery = nationalMap.getUSGSElevationPointQueryService().getElevationQuery();
            if (elevationQuery.getElevation() != -1000000) {
                // best make sure this format matches elevation edit
                setElevation(String.format("%.1fft", elevationQuery.getElevation()
                        * (elevationQuery.getUnits().equalsIgnoreCase("Feet") ? 1.0 : 3.281)));
                notifyLatLongListeners();
            } else {
                post(new SnackbarMessage(getApplication().getString(R.string.error_finding_elevation)));
            }
        }

    }

    public MutableLiveData<Boolean> getNeedToSaveUpdates() {
        return needToSaveUpdates;
    }

    public void setInEditMode(boolean inEditMode) {
        if (inEditMode) {
            enableTurnpointCodeEdit = originalTurnpoint.getCode().isEmpty();
        } else {
            enableTurnpointCodeEdit = false;
        }
        this.inEditMode.postValue(inEditMode);
    }

    public MutableLiveData<Boolean> getEditMode() {
        if (inEditMode.getValue() == null) {
            inEditMode.setValue(false);
        }
        return inEditMode;
    }

    public boolean isTurnpointCodeEditEnabled() {
        return enableTurnpointCodeEdit;
    }

    public void resetTurnpointPosition() {
        turnpoint.setLatitudeDeg(originalTurnpoint.getLatitudeDeg());
        turnpoint.setLongitudeDeg(originalTurnpoint.getLongitudeDeg());
        turnpoint.setElevation(originalTurnpoint.getElevation());
        latitudeErrorText = null;
        longitudeErrorText = null;
        elevationErrorText = null;
        setSaveIndicator();
        notifyLatLongListeners();
    }


    public MutableLiveData<Boolean> getAmWorking() {
        if (amWorking == null) {
            amWorking = new MutableLiveData<>();
            amWorking.setValue(false);
        }
        return amWorking;
    }

    public float getLatitudeDeg() {
        return (turnpoint != null ? turnpoint.getLatitudeDeg() : 0f);
    }

    public float getLongitudeDeg() {
        return (turnpoint != null ? turnpoint.getLongitudeDeg() : 0f);
    }

    public Turnpoint getTurnpoint() {
        return turnpoint;
    }

    public void setLatitudeDeg(double latitude) {
        turnpoint.setLatitudeDeg(latitude);
        latitudeErrorText = null;
        needToSaveUpdates.setValue(true);
        setSaveIndicator();
        notifyLatLongListeners();
    }

    private void notifyLatLongListeners() {
        notifyPropertyChanged(org.soaringforecast.rasp.BR.formattedTurnpointDetails);
        notifyPropertyChanged(org.soaringforecast.rasp.BR.cupLatitude);
        notifyPropertyChanged(org.soaringforecast.rasp.BR.cupLongitude);
        notifyPropertyChanged(org.soaringforecast.rasp.BR.googleLatitude);
        notifyPropertyChanged(org.soaringforecast.rasp.BR.googleLongitude);
        notifyPropertyChanged(org.soaringforecast.rasp.BR.elevation);
    }

    public void setLongitudeDeg(double longitude) {
        turnpoint.setLongitudeDeg(longitude);
        latitudeErrorText = null;
        needToSaveUpdates.setValue(true);
        setSaveIndicator();
        notifyLatLongListeners();
    }


    public void writeTurnpointToDownloadsFile() {
        Disposable disposable =
                appRepository.writeTurnpointToCupFile(turnpoint, getExportTurnpointCupFilename(turnpoint.getCode()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(exportFileName -> {
                                    post(new SnackbarMessage(getApplication().getString(R.string.turnpoint_exported_to_download_directory)));
                                    if (emailCupFile) {
                                        emailCupFile = false;
                                        sendTurnpointViaEmail(exportFileName);

                                    }
                                }
                                , error -> {
                                    emailCupFile = false;
                                    post(new DataBaseError(getApplication().getString(R.string.error_reading_turnpoints), error));
                                }
                        );
        compositeDisposable.add(disposable);
    }

    public void setEmailTurnpoint() {
        emailCupFile = true;
    }

    //TODO Don't build intent here. Put all parms in SendEmail msg and let reciever of msg(probably activity) build intent
    public void sendTurnpointViaEmail(String exportFileName) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Turnpoint: " + turnpoint.getCode());
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + exportFileName));
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_TEXT, getApplication().getString(R.string.updated_or_new_turnpoint));
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            post(new SendEmail(intent));
        } catch (Exception e) {
            post(new SnackbarMessage(getApplication().getString(R.string.error_in_emailing_turnpoint)));
        }
    }


    private String getExportTurnpointCupFilename(String turnpointCode) {
        String currentDate = getCupFileDateString();
        StringBuffer sb = new StringBuffer();
        sb.append("Turnpoint_")
                .append(turnpointCode)
                .append("_")
                .append(currentDate)
                .append(".cup");
        return sb.toString();

    }

    private String getCupFileDateString() {
        String pattern = "yyyy_MM_dd_H_m_s";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date());
    }

    private void post(Object post) {
        EventBus.getDefault().post(post);
    }


    public void deleteTurnpoint() {
        Disposable disposable = appRepository.deleteTurnpoint(turnpoint.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(numberDeleted -> {
                    if (numberDeleted == 1) {
                        post(new DeletedTurnpoint(originalTurnpoint));
                    } else {
                        post(new SnackbarMessage(getApplication().getString(R.string.turnpoint_delete_error)));
                    }
                });
        compositeDisposable.add(disposable);
    }

    /**
     * Restore (undo delete) of original turnpoint
     */
    void restoreOriginalTurnpoint() {
        try {
            Disposable disposable = appRepository.insertTurnpoint(originalTurnpoint)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(id -> {
                                // Restored turnpoint
                            },
                            t -> {
                                Timber.e(t);
                                post(new SnackbarMessage(
                                        getApplication().getString(R.string.error_saving_turnpoint), Snackbar.LENGTH_INDEFINITE));
                            });
            compositeDisposable.add(disposable);
        } catch (Exception e) {
            Timber.e(e);
            post(new SnackbarMessage(getApplication().getString(R.string.error_saving_turnpoint)));
        }

    }
}
