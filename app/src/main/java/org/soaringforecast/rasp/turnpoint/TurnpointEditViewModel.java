package org.soaringforecast.rasp.turnpoint;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.ObservableViewModel;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.repository.messages.DataBaseError;
import org.soaringforecast.rasp.turnpoint.cup.CupStyle;

import java.util.List;
import java.util.regex.Pattern;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class TurnpointEditViewModel extends ObservableViewModel {

    private AppRepository appRepository;
    private long turnpointId = 0;
    private Turnpoint turnpoint;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private String titleErrorText = null;
    private String codeErrorText = null;
    private String countryErrorText = null;
    private String latitudeErrorText = null;
    private String longitudeErrorText = null;
    private String elevationErrorText = null;
    private String directionErrorText = null;
    private String lengthErrorText = null;
    private String frequencyErrorText = null;

    private String tempTitle = "";
    private String tempCode = "";
    private String tempCountry = "";
    private String tempLatitude = "";
    private String tempLongitude = "";
    private String tempElevation = "";
    private String tempDirection = "";
    private String tempLength = "";
    private String tempFrequency = "";
    private String tempStyle = "0";
    private String tempDescription = "";


    private MutableLiveData<List<CupStyle>> cupStyles = new MutableLiveData<>();
    private MutableLiveData<Integer> cupStylePosition = new MutableLiveData<>();

    // Handy site for regex development/testing https://www.debuggex.com/
    private static final String latitudeCupRegex = "(9000\\.000|[0-8][0-9][0-5][0-9]\\.[0-9]{3})[NS]";
    private static final String longitudeCupRegex = "(18000\\.000|(([0-1][0-7])|([0][0-9]))[0-9][0-5][0-9]\\.[0-9]{3})[EW]";
    private static final String elevationRegex = "([0-9]{1,4}(\\.[0-9])?|(\\.[0-9]))(m|ft)";
    private static final String directionRegex = "(?:360|(3[0-5][0-9])|([12][0-9][0-9])|(0[0-9][0-9]))";
    private static final String lengthRegex = "([0-9]{1,5}(\\.[0-9])?|(\\.[0-9]))(m|ft)";
    private static final String frequencyRegex = "1[1-3][0-9]\\.[0-9][0-9](0|5)";
    private static final Pattern longitudeCupPattern = Pattern.compile(longitudeCupRegex);
    private static final Pattern latitudeCupPattern = Pattern.compile(latitudeCupRegex);
    private static final Pattern elevationPattern = Pattern.compile(elevationRegex);
    private static final Pattern directionPattern = Pattern.compile(directionRegex);
    private static final Pattern lengthPattern = Pattern.compile(lengthRegex);
    private static final Pattern frequencyPatten = Pattern.compile(frequencyRegex);

    public TurnpointEditViewModel(@NonNull Application application) {
        super(application);
    }

    public TurnpointEditViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }


    TurnpointEditViewModel setTurnpointId(long turnpointId) {
        this.turnpointId = turnpointId;
        if (turnpointId < 0) {
            turnpoint = new Turnpoint();
            loadTempEditFields();
        } else {
            getTurnpoint();
        }
        return this;
    }

    private void loadTempEditFields() {
        tempTitle = turnpoint.getTitle();
        tempCode = turnpoint.getCode();
        tempCountry = turnpoint.getCountry();
        tempLatitude = turnpoint.getLatitudeInCupFormat();
        tempLongitude = turnpoint.getLongitudeInCupFormat();
        tempElevation = turnpoint.getElevation();
        tempDirection = turnpoint.getDirection();
        tempLength = turnpoint.getLength();
        tempFrequency = turnpoint.getFrequency();
        tempStyle = turnpoint.getStyle();
        tempDescription = turnpoint.getDescription();
    }

    private void getTurnpoint() {
        Disposable disposable = appRepository.getTurnpoint(turnpointId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(turnpoint -> {
                            this.turnpoint = turnpoint;
                            loadTempEditFields();
                            notifyChange();
                        },
                        t -> EventBus.getDefault().post(new DataBaseError(getApplication().getString(R.string.error_reading_turnpoint), t)));
        compositeDisposable.add(disposable);

    }

    @Bindable
    public String getTitle() {
        return tempTitle;
    }


    @Bindable
    public void setTitle(String value) {
        tempTitle = value;
        if (value == null || value.isEmpty()) {
            titleErrorText = getApplication().getString(R.string.turnpoint_title_error_msg);
        } else {
            titleErrorText = null;
        }
        notifyPropertyChanged(org.soaringforecast.rasp.BR.titleErrorText);
    }


    @Bindable
    public String getCode() {
        return tempCode;
    }

    @Bindable
    public void setCode(String value) {
        tempCode = value;
        if (value == null || value.isEmpty()) {
            codeErrorText = getApplication().getString(R.string.turnpoint_code_error_msg);
        } else {
            codeErrorText = null;
        }
        notifyPropertyChanged(org.soaringforecast.rasp.BR.codeErrorText);
    }

    @Bindable
    public String getCountry() {
        return tempCountry;
    }

    @Bindable
    public void setCountry(String value) {
        tempCountry = value;
    }

    @Bindable
    public String getLatitude() {
        return tempLatitude;
    }

    @Bindable
    public void setLatitude(String value) {
        tempLatitude = value;
        try {
            if (latitudeCupPattern.matcher(value).matches()) {
                latitudeErrorText = null;
            } else {
                latitudeErrorText = getApplication().getString(R.string.turnpoint_latitude_error);
            }
        } catch (Exception e) {
            latitudeErrorText = getApplication().getString(R.string.turnpoint_latitude_error);
        }
       notifyPropertyChanged(org.soaringforecast.rasp.BR.latitudeErrorText);
    }

    @Bindable
    public String getLongitude() {
        return tempLongitude;
    }

    @Bindable
    public void setLongitude(String value) {
        tempLongitude = value;
        try {
            if (longitudeCupPattern.matcher(value).matches()) {
                longitudeErrorText = null;
            } else {
                longitudeErrorText = getApplication().getString(R.string.turnpoint_longitude_error);
            }
        } catch (Exception e) {
            longitudeErrorText = getApplication().getString(R.string.turnpoint_longitude_error);
        }
        notifyPropertyChanged(org.soaringforecast.rasp.BR.longitudeErrorText);

    }

    @Bindable
    public String getElevation() {
        return tempElevation;
    }

    @Bindable
    public void setElevation(String value) {
        tempElevation = value;
        if (elevationPattern.matcher(value).matches()) {
            elevationErrorText = null;
        } else {
            elevationErrorText = getApplication().getString(R.string.elevation_error);
        }
        notifyPropertyChanged(org.soaringforecast.rasp.BR.elevationErrorText);
    }

//    @Bindable
//    public String getStyle() {
//        return tempStyle;
//    }
//
//    @Bindable
//    public void setStyle(String value) {
//        tempStyle = value;
//    }

    @Bindable
    public String getDirection() {
        return tempDirection;
    }

    @Bindable
    public void setDirection(String value) {
        tempDirection = value;
        if (directionPattern.matcher(value).matches()) {
            directionErrorText = null;
        } else {
            directionErrorText = getApplication().getString(R.string.direction_error);
        }
        notifyPropertyChanged(org.soaringforecast.rasp.BR.directionErrorText);
    }

    @Bindable
    public String getLength() {
        return tempLength;
    }

    // Only used with Waypoint style types 2, 3, 4and 5
    @Bindable
    public void setLength(String value) {
        tempLength = value;
        if (lengthPattern.matcher(value).matches()) {
            lengthErrorText = null;
        } else {
            lengthErrorText = getApplication().getString(R.string.runway_length_error);
        }
        notifyPropertyChanged(org.soaringforecast.rasp.BR.lengthErrorText);
    }

    @Bindable
    public String getFrequency() {
        return tempFrequency;
    }

    @Bindable
    public void setFrequency(String value) {
        tempFrequency = value;
        if (frequencyPatten.matcher(value).matches()) {
            frequencyErrorText = null;
        } else {
            frequencyErrorText = getApplication().getString(R.string.frequency_format_errror);
        }
        notifyPropertyChanged(org.soaringforecast.rasp.BR.frequencyErrorText);
    }

    @Bindable
    public String getDescription() {
        return tempDescription;
    }

    @Bindable
    public void setDescription(String value) {
        tempDescription = value;
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
                            setInitialCupStylePosition(cupStyles.getCupStyles());
                        },
                        Timber::e);

        compositeDisposable.add(disposable);
    }

    private void setInitialCupStylePosition(List<CupStyle> cupStyles) {
        for (CupStyle cupStyle : cupStyles) {
            if (turnpoint.getStyle().equals(cupStyle.getStyle())) {
                try {
                    tempStyle = cupStyle.getStyle();
                    cupStylePosition.setValue(Integer.parseInt(tempStyle));
                    break;
                } catch (NumberFormatException nfe) {
                    cupStylePosition.setValue(0);
                }

            }
        }
    }

    public MutableLiveData<Integer> getCupStylePosition() {
        return cupStylePosition;

    }

    void setInitialCupStylePosition(int newCupStylePosition) {
        cupStylePosition.setValue(newCupStylePosition);
        tempStyle = newCupStylePosition + "";

    }
}
