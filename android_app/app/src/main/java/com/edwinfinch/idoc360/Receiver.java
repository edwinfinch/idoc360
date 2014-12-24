package com.edwinfinch.idoc360;

import android.bluetooth.BluetoothAdapter; //Soon
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

/**
 * Created by edwinfinch on 14-12-23.
 */
public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Constants.INTENT_APP_RECEIVE)) {
            final UUID receivedUuid = (UUID) intent.getSerializableExtra(Constants.APP_UUID);
            UUID MY_UUID = UUID.fromString("54f918be-893e-433d-aa87-a84e55e24f6b");

            if (!MY_UUID.equals(receivedUuid)) {
                return;
            }

            final int transactionId = intent.getIntExtra(Constants.TRANSACTION_ID, -1);
            final String jsonData = intent.getStringExtra(Constants.MSG_DATA);
            if (jsonData == null || jsonData.isEmpty()) {
                return;
            }

            try{
                PebbleKit.sendAckToPebble(context, transactionId);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

}
