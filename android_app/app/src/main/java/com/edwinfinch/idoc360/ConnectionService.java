package com.edwinfinch.idoc360;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by edwinfinch on 15-02-14.
 */
public class ConnectionService extends IntentService {

    private NotificationCompat.Builder mBuilder;

    public ConnectionService() {
        super("UploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent deleteIntent = new Intent(this, ConnectionReciever.class);
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(this, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //building the notification
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_menu_upload)
                .setContentTitle("Uploading Media...")
                .setTicker("Starting uploads")
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel Upload", pendingIntentCancel);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setProgress(100, 0, true);

        startForeground(12345, mBuilder.build());

        for(int i=0;i<10;i++){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
