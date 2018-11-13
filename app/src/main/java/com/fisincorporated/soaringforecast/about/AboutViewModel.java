package com.fisincorporated.soaringforecast.about;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;

import com.fisincorporated.soaringforecast.BuildConfig;
import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.messages.SnackbarMessage;
import com.fisincorporated.soaringforecast.repository.AppRepository;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AboutViewModel extends AndroidViewModel {

    AppRepository appRepository;
    MutableLiveData<Spanned> aboutSoaringWeather;

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
            loadAboutText();
        }
        return aboutSoaringWeather;
    }

    // Keep just in case
    private void loadAboutText() {
        StringBuilder sb = new StringBuilder();
        AssetManager assetManager = getApplication().getAssets();
        InputStream inputStream;
        try {
            inputStream = assetManager.open("about.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (FileNotFoundException e) {
            EventBus.getDefault().post(new SnackbarMessage(getApplication().getString(R.string.oops_about_file_is_missing)));
        } catch (IOException e) {
            EventBus.getDefault().post(new SnackbarMessage(getApplication().getString(R.string.oops_error_reading_about_file)));
        }
        aboutSoaringWeather.setValue(Html.fromHtml(sb.toString()));
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
