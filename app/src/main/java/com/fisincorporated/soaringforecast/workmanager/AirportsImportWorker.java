package com.fisincorporated.soaringforecast.workmanager;

import android.app.Notification;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.airport.AirportListDownloader;
import com.fisincorporated.soaringforecast.dagger.DaggerAirportsImportWorkerComponent;
import com.fisincorporated.soaringforecast.repository.AppRepository;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class AirportsImportWorker extends Worker {

    private static final int NOTIFICATION_ID = 1234;
    private static final int MIN_AIRPORT_COUNT = 20000;

    private Context context;

    @Inject
    @Named("CHANNEL_ID")
    String CHANNEL_ID;

    @Inject
    AppRepository appRepository;

    @Inject
    AirportListDownloader airportListDownloader;

    private NotificationManagerCompat notificationManager;

    public AirportsImportWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        int count = 0;
        context = getApplicationContext();
        DaggerAirportsImportWorkerComponent.builder().context(context).build().inject(this);

        notificationManager = NotificationManagerCompat.from(context);
        displayStartNotification();

        try {
            count = airportListDownloader.downloadAirportsToDB().blockingGet();
        } catch (Exception e) {
            displayCompletionNotification(-1);
        }
        boolean success = displayCompletionNotification(count);

        // Indicate success or failure with your return value:
        return success ? Result.SUCCESS : Result.RETRY;
    }

    private void displayStartNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.glider_notification_icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.download_of_airports_started))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        displayNotification(builder.build());
    }

    private boolean displayCompletionNotification(int count) {
        Context context = getApplicationContext();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.glider_notification_icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        if (count > MIN_AIRPORT_COUNT) {
            builder.setContentText(context.getString(R.string.airport_database_loaded_ok));
        } else {
            builder.setContentText(context.getString(R.string.airport_database_load_oops))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.airport_database_oops_long_text)));
        }
        displayNotification(builder.build());
        return count > MIN_AIRPORT_COUNT;
    }

    private void displayNotification(Notification notification) {
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    //TODO
    // If download failed, add intent to notification
    // Display error fragment
    // With user ok, resubmit job in 1hr (4 hrs, 1 day????)

}
