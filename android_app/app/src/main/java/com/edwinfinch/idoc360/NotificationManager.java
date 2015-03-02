package com.edwinfinch.idoc360;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

/**
 * Created by edwinfinch on 15-02-10.
 */
public class NotificationManager {
    public static int CONSTANT_NOTIFICATION = 110;

    public static void manageConstantNotification(Activity activity, boolean onGoing, String title, String subtitle, NotificationCompat.Action action1, NotificationCompat.Action action2){
        Bitmap largeIcon = BitmapFactory.decodeResource(activity.getResources(), R.drawable.logo);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(activity.getApplicationContext())
                        .setSmallIcon(R.drawable.notif_icon)
                        .setLargeIcon(largeIcon)
                        .setContentTitle(title)
                        .setContentText(subtitle)
                        .addAction(action1)
                        .addAction(action2)
                        .setPriority(Notification.PRIORITY_MIN)
                        .setOngoing(onGoing);
        Intent resultIntent = new Intent(activity.getApplicationContext(), MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(activity.getApplicationContext());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0, PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(CONSTANT_NOTIFICATION, mBuilder.build());
    }

    public static void manageConstantNotification(Activity activity, boolean onGoing, String title, String subtitle, NotificationCompat.Action action1){
        Bitmap largeIcon = BitmapFactory.decodeResource(activity.getResources(), R.drawable.logo);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(activity.getApplicationContext())
                        .setSmallIcon(R.drawable.notif_icon)
                        .setLargeIcon(largeIcon)
                        .setContentTitle(title)
                        .setContentText(subtitle)
                        .addAction(action1)
                        .setOngoing(onGoing);
        Intent reconnectIntent = new Intent(activity, ConnectionReciever.class);
        PendingIntent pendingIntentReconnect = PendingIntent.getBroadcast(activity, 0, reconnectIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(activity.getApplicationContext());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(reconnectIntent);

        mBuilder.setContentIntent(pendingIntentReconnect);
        android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(CONSTANT_NOTIFICATION, mBuilder.build());
    }

    public static void reconnectNotification(Activity activity){
        Bitmap largeIcon = BitmapFactory.decodeResource(activity.getResources(), R.drawable.logo);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(activity.getApplicationContext())
                        .setSmallIcon(R.drawable.notif_icon)
                        .setLargeIcon(largeIcon)
                        .setContentTitle("Reconnecting")
                        .setContentText("Please wait.")
                        .setProgress(0, 0, true)
                        .setOngoing(true);
        Intent resultIntent = new Intent(activity.getApplicationContext(), MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(activity.getApplicationContext());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0, PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(CONSTANT_NOTIFICATION, mBuilder.build());
    }
}
