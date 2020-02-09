package org.soaringforecast.rasp.turnpoints.edit;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.common.ObservableViewModel;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.repository.messages.DataBaseError;
import org.soaringforecast.rasp.soaring.messages.DisplayTurnpoint;
import org.soaringforecast.rasp.turnpoints.cup.CupStyle;
import org.soaringforecast.rasp.turnpoints.json.ElevationQuery;
import org.soaringforecast.rasp.turnpoints.json.NationalMap;
import org.soaringforecast.rasp.turnpoints.messages.SendEmail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Single;
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
    private String frequencyErrorText = null;
    private String elevationPreference;
    private boolean noErrors = true;

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
    private static final String lengthRegex = "([0-9]{1,5}(\\.[0-9])?|(\\.[0-9]))(m|ft)";
    private static final String frequencyRegex = "1[1-3][0-9]\\.[0-9][0-9](0|5)";
    private static final Pattern longitudeCupPattern = Pattern.compile(longitudeCupRegex);
    private static final Pattern latitudeCupPattern = Pattern.compile(latitudeCupRegex);
    private static final Pattern elevationPattern = Pattern.compile(elevationRegex);
    private static final Pattern directionPattern = Pattern.compile(directionRegex);
    private static final Pattern lengthPattern = Pattern.compile(lengthRegex);
    private static final Pattern frequencyPatten = Pattern.compile(frequencyRegex);
    private AppPreferences appPreferences;
    private boolean emailCupFile = false;


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
        if (this.turnpoint == null) {
            this.turnpoint = turnpoint;
            originalTurnpoint = Turnpoint.newInstance(turnpoint);
            okToSave.setValue(false);
            needToSaveUpdates.setValue(false);
            amWorking.setValue(false);
        }
        return this;
    }

    /**
     * Since viewmodel held at activity level and can bounce display multiple turnpoints from the
     * turnpoint list, make sure to reset the viewmodel whenever you go to display a turnpoint
     * in  TurnpointEditFragment
     */
    public TurnpointEditViewModel reset() {
        resetErrorText();
        okToSave.setValue(false);
        needToSaveUpdates.setValue(false);
        amWorking.setValue(false);
        turnpoint = null;
        originalTurnpoint = null;
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
            turnpoint.setTitle(value);
            needToSaveUpdates.setValue(true);
            titleErrorText = null;
        }
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
            turnpoint.setCode(value);
            needToSaveUpdates.setValue(true);
            codeErrorText = null;
        }
        setSaveIndicator();
        notifyPropertyChanged(org.soaringforecast.rasp.BR.codeErrorText);
    }

    @Bindable
    public String getCountry() {
        return turnpoint.getCountry().toUpperCase();
    }

    @Bindable
    public void setCountry(String value) {
        value = value.trim().toUpperCase();
        turnpoint.setCountry(value);
        needToSaveUpdates.setValue(true);
        setSaveIndicator();
    }

    @Bindable
    public String getLatitude() {
        return turnpoint.getLatitudeInCupFormat();
    }

    @Bindable
    public void setLatitude(String value) {
        value = value.trim();
        try {
            if (latitudeCupPattern.matcher(value.trim()).matches()) {
                turnpoint.setLatitudeDeg(Turnpoint.convertToLat(value.trim()));
                latitudeErrorText = null;
                needToSaveUpdates.setValue(true);
            } else {
                latitudeErrorText = getApplication().getString(R.string.turnpoint_latitude_error);
            }
        } catch (Exception e) {
            latitudeErrorText = getApplication().getString(R.string.turnpoint_latitude_error);
        }
        setSaveIndicator();
        notifyPropertyChanged(org.soaringforecast.rasp.BR.latitudeErrorText);
    }

    @Bindable
    public String getLongitude() {
        return turnpoint.getLongitudeInCupFormat();
    }

    @Bindable
    public void setLongitude(String value) {
        value = value.trim();
        try {
            if (longitudeCupPattern.matcher(value.trim()).matches()) {
                turnpoint.setLongitudeDeg(Turnpoint.convertToLong(value.trim()));
                longitudeErrorText = null;
                needToSaveUpdates.setValue(true);
            } else {
                longitudeErrorText = getApplication().getString(R.string.turnpoint_longitude_error);
            }
        } catch (Exception e) {
            longitudeErrorText = getApplication().getString(R.string.turnpoint_longitude_error);
        }
        setSaveIndicator();
        notifyPropertyChanged(org.soaringforecast.rasp.BR.longitudeErrorText);

    }

    @Bindable
    public String getElevation() {
        return turnpoint.getElevation();
    }

    @Bindable
    public void setElevation(String value) {
        value = value.trim();
        if (elevationPattern.matcher(value).matches()) {
            turnpoint.setElevation(value);
            elevationErrorText = null;
            needToSaveUpdates.setValue(true);
        } else {
            elevationErrorText = getApplication().getString(R.string.elevation_error);
        }
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
        if ((value.isEmpty() && !isLandable()) ||
                (value.length() == 3 && isLandable() && directionPattern.matcher(value).matches())) {
            turnpoint.setDirection(value);
            directionErrorText = null;
            needToSaveUpdates.setValue(true);
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

    // Only used with Waypoint style types 2, 3, 4and 5
    @Bindable
    public void setLength(String value) {
        value = value.trim();
        if ((value.isEmpty() && !isLandable())
                || (isLandable() && lengthPattern.matcher(value).matches()
                && (value.endsWith("ft") || value.equals("m")))) {
            turnpoint.setLength(value);
            needToSaveUpdates.setValue(true);
            lengthErrorText = null;
        } else {
            lengthErrorText = getApplication().getString(R.string.runway_length_error);
        }
        setSaveIndicator();
        notifyPropertyChanged(org.soaringforecast.rasp.BR.lengthErrorText);
    }

    @Bindable
    public String getFrequency() {
        return turnpoint.getFrequency();
    }

    @Bindable
    public void setFrequency(String value) {
        value = value.trim();
        if (value.isEmpty() ||
                (value.length() == 7 && frequencyPatten.matcher(value).matches())) {
            turnpoint.setFrequency(value);
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
        turnpoint.setDescription(value);
        needToSaveUpdates.setValue(true);
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
    public String getFrequencyErrorText() {
        return frequencyErrorText;
    }

    LiveData<List<CupStyle>> getCupStyles() {
        if (cupStyles.getValue() == null) {
            loadCupStyles();
        }
        return cupStyles;
    }

    private void loadCupStyles() {
        Disposable disposable = appRepository.getCupStyles()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cupStyles -> {
                            this.cupStyles.setValue(cupStyles.getCupStyles());
                            setCupStylePosition(cupStyles.getCupStyles());
                        },
                        Timber::e);

        compositeDisposable.add(disposable);
    }

    // Called after the JSON list of cup styles loaded
    private void setCupStylePosition(List<CupStyle> cupStyles) {
        for (CupStyle cupStyle : cupStyles) {
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

    // Called by UI after turpoint style changed
    void setCupStylePosition(int newCupStylePosition) {
        cupStylePosition.setValue(newCupStylePosition);
        turnpoint.setStyle(newCupStylePosition + "");
        needToSaveUpdates.setValue(true);
        setSaveIndicator();

    }

    public MutableLiveData<Integer> getCupStylePosition() {
        return cupStylePosition;
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
        longitudeErrorText = null;
        elevationErrorText = null;
        directionErrorText = null;
        lengthErrorText = null;
        frequencyErrorText = null;
        noErrors = true;
    }

    private void setSaveIndicator() {
        noErrors = (titleErrorText == null
                && codeErrorText == null
                && countryErrorText == null
                && longitudeErrorText == null
                && elevationErrorText == null
                && directionErrorText == null
                && lengthErrorText == null
                && frequencyErrorText == null);

        okToSave.setValue(noErrors);
    }

    MutableLiveData<Boolean> getOKToSaveFlag() {
        return okToSave;
    }

    public Single<Integer> deleteTurnpoint() {
        return appRepository.deleteTurnpoint(turnpoint.getId());
    }

    public void onClickGpsIcon() {
        post(new DisplayTurnpoint(turnpoint));
    }

    @SuppressLint("DefaultLocale")
    public void updateTurnpointLatLngAndElevation(Location location) {
        turnpoint.setLatitudeDeg(location.getLatitude());
        turnpoint.setLongitudeDeg(location.getLongitude());
        double altitude = location.getAltitude();
        if (elevationPreference.equals("m")) {
            turnpoint.setElevation(String.format("%.1fm", altitude));
        } else {
            turnpoint.setElevation(String.format("%.1fft", (altitude * 3.281)));
        }
        notifyChange();
    }

    @Bindable
    public String getFormattedTurnpointDetails() {
        return turnpoint.getFormattedTurnpointDetails();
    }

    public void getElevationAtLatLng(LatLng latLng) {
        amWorking.setValue(true);
        final String units = appPreferences.getAltitudeDisplay().equalsIgnoreCase("ft") ? "Feet" : "Meters";
        Disposable disposable = appRepository.getElevationAtLatLong(latLng.latitude, latLng.longitude, units)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(nationalMap -> {
                            setElevationFromUsgs(nationalMap);
                            notifyPropertyChanged(org.soaringforecast.rasp.BR.formattedTurnpointDetails);
                            amWorking.setValue(false);
                        },
                        t -> {
                            Timber.e(t);
                            EventBus.getDefault().post(new SnackbarMessage(getApplication().getString(R.string.error_finding_elevation)));
                            setElevation(String.format("%1$.1f%2$s", 0, (units.equalsIgnoreCase("Feet") ? "ft" : "m")));
                            notifyPropertyChanged(org.soaringforecast.rasp.BR.formattedTurnpointDetails);
                            amWorking.setValue(false);
                        });

        compositeDisposable.add(disposable);
    }

    private void setElevationFromUsgs(NationalMap nationalMap) {
        if (nationalMap != null && nationalMap.getUSGSElevationPointQueryService() != null
                && nationalMap.getUSGSElevationPointQueryService().getElevationQuery() != null) {
            ElevationQuery elevationQuery = nationalMap.getUSGSElevationPointQueryService().getElevationQuery();
            if (elevationQuery.getElevation() != -1000000) {
                // best make sure this format matches elevation edit
                setElevation(String.format("%.1f", elevationQuery.getElevation())
                        + (elevationQuery.getUnits().equalsIgnoreCase("Feet") ? "ft" : "m"));
            } else {
                post(new SnackbarMessage(getApplication().getString(R.string.error_finding_elevation)));
            }
        }

    }

    public MutableLiveData<Boolean> getNeedToSaveUpdates() {
        return needToSaveUpdates;
    }

    public void setInEditMode(boolean inEditMode) {
        this.inEditMode.postValue(inEditMode);
    }

    public MutableLiveData<Boolean> getEditMode() {
        if (inEditMode.getValue() == null) {
            inEditMode.setValue(false);
        }
        return inEditMode;
    }

    public void resetTurnpointPosition() {
        turnpoint.setLatitudeDeg(originalTurnpoint.getLatitudeDeg());
        turnpoint.setLongitudeDeg(originalTurnpoint.getLongitudeDeg());
        turnpoint.setElevation(originalTurnpoint.getElevation());
        latitudeErrorText = null;
        longitudeErrorText = null;
        elevationErrorText = null;
        setSaveIndicator();
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
        notifyPropertyChanged(org.soaringforecast.rasp.BR.formattedTurnpointDetails);
    }

    public void setLongitudeDeg(double longitude) {
        turnpoint.setLongitudeDeg(longitude);
        latitudeErrorText = null;
        needToSaveUpdates.setValue(true);
        setSaveIndicator();
        notifyPropertyChanged(org.soaringforecast.rasp.BR.formattedTurnpointDetails);
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

}
