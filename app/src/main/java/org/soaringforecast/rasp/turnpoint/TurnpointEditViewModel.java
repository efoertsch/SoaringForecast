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

    private MutableLiveData<List<CupStyle>> cupStyles = new MutableLiveData<>();
    private MutableLiveData<Integer> cupStylePosition = new MutableLiveData<>();

    // Handy site for regex development/testing https://www.debuggex.com/
    public static final String latitudeCupRegex = "(9000\\.000|[0-8][0-9][0-5][0-9]\\.[0-9]{3})[NS]";
    public static final String longitudeCupRegex = "(18000\\.000|(([0-1][0-7])|([0][0-9]))[0-9][0-5][0-9]\\.[0-9]{3})[EW]";
    public static final String elevationRegex = "([0-9]{1,4}(\\.[0-9])?|(\\.[0-9]))[m|ft]";
    public static final String directionRegex = "(?:360|(3[0-5][0-9])|([12][0-9][0-9])|(0[0-9][0-9]))";
    public static final String lengthRegex = "([0-9]{1,5}(\\.[0-9])?|(\\.[0-9]))[m|ft]";
    public static final String frequencyRegex = "1[1-3][0-9]\\.[0-9][0-9](0|5)";
    public static final Pattern longitudeCupPattern = Pattern.compile(longitudeCupRegex);
    public static final Pattern latitudeCupPattern = Pattern.compile(latitudeCupRegex);
    public static final Pattern elevationPattern = Pattern.compile(elevationRegex);
    public static final Pattern directionPattern = Pattern.compile(directionRegex);
    public static final Pattern lengthPattern = Pattern.compile(lengthRegex);
    public static final Pattern frequencyPatten = Pattern.compile(frequencyRegex);



    public TurnpointEditViewModel(@NonNull Application application) {
        super(application);
    }

    public TurnpointEditViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }


    public TurnpointEditViewModel setTurnpointId(long turnpointId) {
        this.turnpointId = turnpointId;
        if (turnpointId < 0) {
            turnpoint = new Turnpoint();
        } else {
            getTurnpoint();
        }
        return this;
    }

    private void getTurnpoint() {
        Disposable disposable = appRepository.getTurnpoint(turnpointId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(turnpoint -> {
                            this.turnpoint = turnpoint;
                            notifyChange();
                        },
                        t -> {
                            EventBus.getDefault().post(new DataBaseError(getApplication().getString(R.string.error_reading_turnpoint), t));
                        });
        compositeDisposable.add(disposable);

    }

    @Bindable
    public String getTitle() {
        if (turnpoint != null) {
            return turnpoint.getTitle();
        } else {
            return "";
        }
    }


    @Bindable
    public void setTitle(String value) {
        if (value == null || value.isEmpty()) {
            titleErrorText = getApplication().getString(R.string.turnpoint_title_error_msg);
            return;
        }
        titleErrorText = null;
        turnpoint.setTitle(value);
    }


    @Bindable
    public String getCode() {
        if (turnpoint != null) {
            return turnpoint.getCode();
        } else {
            return "";
        }
    }


    @Bindable
    public void setCode(String value) {
        if (value == null || value.isEmpty()) {
            codeErrorText = getApplication().getString(R.string.turnpoint_code_error_msg);
            return;
        }
        codeErrorText = null;
        turnpoint.setCode(value);

    }

    @Bindable
    public String getCountry() {
        if (turnpoint != null) {
            return turnpoint.getCountry();
        } else {
            return "";
        }
    }

    @Bindable
    public void setCountry(String value) {
        turnpoint.setCountry(value);
    }

    @Bindable
    public String getLatitude() {
        if (turnpoint != null) {
            return turnpoint.getLatitudeInCupFormat();
        } else {
            return "0000.000N";
        }
    }

    @Bindable
    public void setLatitude(String value) {
        if (turnpoint != null) {
            try {
                if (latitudeCupPattern.matcher(value).matches()) {
                    turnpoint.setLatitudeDeg(Turnpoint.convertToLat(value));
                    latitudeErrorText = null;
                } else {
                    latitudeErrorText = getApplication().getString(R.string.turnpoint_latitude_error);
                }
            } catch (Exception e) {
                latitudeErrorText = getApplication().getString(R.string.turnpoint_latitude_error);
            }
        }
    }

    @Bindable
    public String getLongitude() {
        if (turnpoint != null) {
            return turnpoint.getLongitudeInCupFormat();
        } else {
            return "00000.000W";
        }
    }

    @Bindable
    public void setLongitude(String value) {
        if (turnpoint != null) {
            try {
                if (longitudeCupPattern.matcher(value).matches()) {
                    turnpoint.setLongitudeDeg(Turnpoint.convertToLong(value));
                    longitudeErrorText = null;
                } else {
                    longitudeErrorText = getApplication().getString(R.string.turnpoint_longitude_error);
                }
            } catch (Exception e) {
                longitudeErrorText = getApplication().getString(R.string.turnpoint_longitude_error);
            }
        }
    }

    @Bindable
    public String getElevation() {
        if (turnpoint != null) {
            return turnpoint.getElevation();
        } else {
            return "";
        }
    }


    @Bindable
    public void setElevation(String value) {
        if (turnpoint != null) {
            if (elevationPattern.matcher(value).matches()) {
                elevationErrorText = null;
                turnpoint.setElevation(value);
            } else {
                elevationErrorText = getApplication().getString(R.string.elevation_error);
            }
        }
    }

    @Bindable
    public String getStyle() {
        if (turnpoint != null) {
            return turnpoint.getStyle();
        } else {
            return "";
        }
    }


    @Bindable
    public void setStyle(String value) {
        if (turnpoint != null) {
            turnpoint.setStyle(value);
        }
    }

    @Bindable
    public String getDirection() {
        if (turnpoint != null) {
            return turnpoint.getDirection();
        } else {
            return "";
        }
    }


    @Bindable
    public void setDirection(String value) {
        if (turnpoint != null) {
            if (directionPattern.matcher(value).matches()) {
                directionErrorText = null;
                turnpoint.setDirection(value);
            } else {
                directionErrorText = getApplication().getString(R.string.direction_error);
            }
        }
    }

    @Bindable
    public String getLength() {
        if (turnpoint != null) {
            return turnpoint.getLength();
        } else {
            return "";
        }
    }

    // Only used with Waypoint style types 2, 3, 4and 5
    @Bindable
    public void setLength(String value) {
        if (turnpoint != null) {
            if (lengthPattern.matcher(value).matches()) {
                lengthErrorText = null;
                turnpoint.setLength(value);
            } else {
                lengthErrorText = getApplication().getString(R.string.runway_length_error);
            }
        }
    }

    @Bindable
    public String getFrequency() {
        if (turnpoint != null) {
            return turnpoint.getFrequency();
        } else {
            return "";
        }
    }

    @Bindable
    public void setFrequency(String value) {
        if (turnpoint != null) {
            if (frequencyPatten.matcher(value).matches()) {
                turnpoint.setFrequency(value);
                frequencyErrorText = null;
                turnpoint.setFrequency(value);
            } else {
                frequencyErrorText = getApplication().getString(R.string.frequency_format_errror);
            }
        }
    }


    @Bindable
    public String getDescription() {
        if (turnpoint != null) {
            return turnpoint.getDescription();
        } else {
            return "";
        }
    }

    @Bindable
    public void setDescription(String value) {
        if (turnpoint != null) {
            turnpoint.setDescription(value);
        }
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

    private void setCupStylePosition(List<CupStyle> cupStyles) {
        for (CupStyle cupStyle : cupStyles) {
            if (turnpoint.getStyle().equals(cupStyle.getStyle())) {
                try {
                    cupStylePosition.setValue(Integer.parseInt(cupStyle.getStyle()));
                    break;
                } catch (NumberFormatException nfe) {
                    cupStylePosition.setValue(0);
                }

            }
        }
    }

    public MutableLiveData<Integer> getCupStylePosition(){
        return cupStylePosition;

    }
    public void setCupStylePosition(int newCupStylePosition) {
        cupStylePosition.setValue(newCupStylePosition);

    }
}
