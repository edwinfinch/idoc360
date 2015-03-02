package com.edwinfinch.idoc360;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * Created by edwinfinch on 15-02-14.
 */
public class ConnectionReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent();
        service.setComponent(new ComponentName(context, UartService.class));
        context.stopService(service);
    }

}