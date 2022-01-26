package org.soaringforecast.rasp.about;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import android.content.res.AssetManager;
import androidx.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;

import org.soaringforecast.rasp.BuildConfig;
import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.messages.SnackbarMessage;
import org.soaringforecast.rasp.repository.AppRepository;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AboutViewModel extends AndroidViewModel {

    private AppRepository appRepository;
    private MutableLiveData<Spanned> aboutSoaringWeather;

    public AboutViewModel(@NonNull Application application) {
        super(application);
    }

    public AboutViewModel setRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }


    public MutableLiveData<Spanned> getAboutSoaringWeather() {
        if (aboutSoaringWeather == null) {
            aboutSoaringWeather = new MutableLiveData<>();
            aboutSoaringWeather.setValue(Html.fromHtml(appRepository.loadAssetText("about.txt",
                    R.string.oops_about_file_is_missing, R.string.oops_error_reading_about_file )));
        }
        return aboutSoaringWeather;
    }


    public int getDatabaseVersion() {
        return appRepository.getDatabaseVersion();
    }

    public int getAppVersionCode(){
        return  BuildConfig.VERSION_CODE;
    }

    public String getAppVersionName(){
        return  BuildConfig.VERSION_NAME;
    }


}
