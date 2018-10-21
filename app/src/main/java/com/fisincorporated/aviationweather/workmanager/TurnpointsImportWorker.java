package com.fisincorporated.aviationweather.workmanager;

import android.app.Notification;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.common.Constants;
import com.fisincorporated.aviationweather.dagger.DaggerTurnpointsImportWorkerComponent;
import com.fisincorporated.aviationweather.repository.AppRepository;
import com.fisincorporated.aviationweather.task.TurnpointProcessor;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.work.Worker;

public class TurnpointsImportWorker extends Worker {

    private static final int NOTIFICATION_ID = 4321;

    private Context context;

    @Inject
    @Named("CHANNEL_ID")
    String CHANNEL_ID;

    @Inject
    AppRepository appRepository;

    @Inject
    TurnpointProcessor turnpointProcessor;

    private NotificationManagerCompat notificationManager;

    @NonNull
    @Override
    public Result doWork() {

        boolean success = false;

        context = getApplicationContext();
        DaggerTurnpointsImportWorkerComponent.builder().context(context).build().inject(this);

        notificationManager = NotificationManagerCompat.from(context);
        displayStartNotification();

        //TODO process turnpoint file
        String fileName = getInputData().getString(Constants.TURNPOINT_FILE_NAME);
        try {
           success = turnpointProcessor.importTurnpointFile(fileName);
        } catch (IOException ioe) {

        }
        displayCompletionNotification(success);

        // Indicate success or failure with your return value:
        return success ? Result.SUCCESS : Result.FAILURE;
    }

    private void displayStartNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                //.setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.started_turnpoints_processing))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        displayNotification(builder.build());
    }

    private void displayCompletionNotification(boolean loadedOK) {
        Context context = getApplicationContext();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                //.setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(loadedOK ? context.getString(R.string.turnpoints_processed_ok) :
                        context.getString(R.string.turnpoint_database_load_oops))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        displayNotification(builder.build());
    }

    private void displayNotification(Notification notification) {
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

}
