package com.example.timelynoteapp;

import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    public static void displayNotification(Context context, String title, String body) {

        // display notification builder
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.timely_logo)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(context);
        mNotificationMgr.notify(1, mBuilder.build());
    }
}
