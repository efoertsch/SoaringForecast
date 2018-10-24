package com.fisincorporated.soaringforecast.app;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.fisincorporated.soaringforecast.BuildConfig;
import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.dagger.AppModule;
import com.fisincorporated.soaringforecast.dagger.DaggerDiComponent;
import com.fisincorporated.soaringforecast.dagger.DiComponent;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.workmanager.DownloadAirportsWorker;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SoaringWeatherApplication extends DaggerApplication {

    protected DiComponent component;

    // Ensure that defaults set prior to doing anything
    @Inject
    AppPreferences appPreferences;

    @Inject
    @Named("CHANNEL_ID")
    String channelId;

    @Inject
    AppRepository appRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        initTimber();
        configureEventBus();
        checkIfAirportDownloadNeeded();
    }

    @SuppressLint("CheckResult")
    void checkIfAirportDownloadNeeded() {
        appRepository.getCountOfAirports()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(count -> {
                            if (count < 2000) {
                                createNotificationChannel();
                                submitAirportDownloadJob();
                            }
                        }, throwable -> {
                            //TODO
                            Timber.e(throwable);
                        }
                );
    }

    private void submitAirportDownloadJob() {
        OneTimeWorkRequest downloadWork =
                new OneTimeWorkRequest.Builder(DownloadAirportsWorker.class)
                        .build();
        WorkManager.getInstance().enqueue(downloadWork);

    }

    private void initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            // Update if adding crash reporting
            Timber.plant(new Timber.DebugTree());
        }
    }

    private void configureEventBus() {
        EventBus.builder().throwSubscriberException(BuildConfig.DEBUG).installDefaultEventBus();
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        component = DaggerDiComponent.builder().application(this).appModule(new AppModule(this)).build();
        component.inject(this);
        return component;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getApplicationContext().getString(R.string.channel_name);
            String description = getApplicationContext().getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
