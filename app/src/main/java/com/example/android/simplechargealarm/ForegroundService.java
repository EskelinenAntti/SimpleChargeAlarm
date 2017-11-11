package com.example.android.simplechargealarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

/**
 * Foreground service that checks the battery state every time it is broadcast. If the battery is
 * recharging and battery level is over 80% the service will give a notification to the user.
 */

public class ForegroundService extends Service {
    private static final String LOG_TAG = "ForegroundService";
    private static final int NOTIFICATION_ID_1 = 1;

    // A global boolean for the toggle-button to know if the service is running.
    public static Boolean serviceIsRunning = false;

    // A private boolean for the service to know if it has already sent a notification.
    private static Boolean displayingNotification = false;

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Set the notification for the foreground service when started with the toggle button
        // and start it.
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            serviceIsRunning = true;
            Log.i(LOG_TAG,"Received startIntent.");
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                    0);
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Simple Charge Alarm")
                    .setTicker("Simple Charge Alarm")
                    .setContentText("Charge alarm is on.")
                    .setSmallIcon(R.drawable.ic_lightbulb_outline_white_24dp)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true).build();
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);

        // Stop the foreground service.
        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG,"Received stopIntent.");
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        displayingNotification = false;
        serviceIsRunning = false;
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID_1);
        unregisterReceiver(this.batteryInfoReceiver);
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case of bound services.
        return null;
    }

    public final BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {

            // Get the information about battery state every time the system broadcasts it.
            int rawLevel = intent.getIntExtra("level",-1);
            int scale = intent.getIntExtra("scale", -1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            float batteryPercent = rawLevel / (float)scale;

            // Send the notification if the battery level is over 80% and the phone is charging.
            // The notification updates the foreground service notification. Thus it doesn't
            // show another new icon next to the foreground service icon.
            if (batteryPercent >= 0.8 && isCharging ){
                setNotification(NOTIFICATION_ID_1);
            } else if (displayingNotification){
                setNotification(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE);
            }

        }
    };

    private void setNotification(int id){
        /**
         * Set the types of different notifications, one for the foreground service and one for
         * the "remove charger"-notification.
         */
        if (id == Constants.NOTIFICATION_ID.FOREGROUND_SERVICE){
            serviceIsRunning = true;
            Log.i(LOG_TAG,"Received startIntent.");
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                    0);
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Simple Charge Alarm")
                    .setTicker("Simple Charge Alarm")
                    .setContentText("Charge alarm is on.")
                    .setSmallIcon(R.drawable.ic_lightbulb_outline_white_24dp)
                    .setContentIntent(pendingIntent)
                    // Normally this is not needed to switch off the led but for example in LG G3
                    // updating the notification without this won't stop the led blinking.
                    .setLights(0x01000000,1000,1000)
                    .setOngoing(true).build();

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);
            displayingNotification = false;
        } else if(id == NOTIFICATION_ID_1){
            Uri alarmSound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.blob);
            android.support.v4.app.NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(getApplicationContext())
                            .setSound(alarmSound)
                            .setContentTitle("Simple Charge Alarm")
                            .setContentText("Unplug your charger to save battery.")
                            .setLights(0xFFFFAE00,1000,1000)
                            .setSmallIcon(R.drawable.ic_check_circle_white_24dp);


            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addParentStack(MainActivity.class);

            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            builder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    builder.build());
            displayingNotification = true;
        }

    }
}
