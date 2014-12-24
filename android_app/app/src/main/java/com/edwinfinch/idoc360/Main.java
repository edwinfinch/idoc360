package com.edwinfinch.idoc360;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.UUID;


public class Main extends Activity {
    UUID watchAppUUID;
    TextView debugTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        watchAppUUID = UUID.fromString("82f429b8-5336-41b2-830c-17639cce5d86");
        PebbleKit.startAppOnPebble(getApplicationContext(), watchAppUUID);

        boolean connected = PebbleKit.isWatchConnected(this);

        debugTextView = (TextView) findViewById(R.id.pebbledebug_textview);

        PebbleKit.FirmwareVersionInfo firmwareInfo = PebbleKit.getWatchFWVersion(this);
        String firmware = firmwareInfo.getMajor() + firmwareInfo.getMinor() + firmwareInfo.getPoint() + firmwareInfo.getTag();

        Calendar c = Calendar.getInstance();
        long timestamp = c.getTimeInMillis();

        PebbleDictionary dict = new PebbleDictionary();
        dict.addString(0, "Hello pebble with firmware " + firmware + "! This is " + android.os.Build.MODEL + " talking. Timestamp: " + timestamp);

        PebbleKit.sendDataToPebble(this, watchAppUUID, dict);
        if(connected){
            debugTextView.setText("Connected. Firmware: " + firmware);
        }
        else {
            debugTextView.setText("Disconnected!");
        }
    }

    public static void sideloadInstall(Context ctx, String assetFilename) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            File file = new File(ctx.getExternalFilesDir(null), assetFilename);
            InputStream is = ctx.getResources().getAssets().open(assetFilename);
            OutputStream os = new FileOutputStream(file);
            byte[] pbw = new byte[is.available()];
            is.read(pbw);
            os.write(pbw);
            is.close();
            os.close();
            intent.setDataAndType(Uri.fromFile(file), "application/pbw");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        } catch (IOException e) {
            Toast.makeText(ctx, "App install failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void installIDOC360(View view){
        if(PebbleKit.isWatchConnected(this)){
            sideloadInstall(getApplicationContext(), "idoc360.pbw");
        }
        else{
            Toast.makeText(this, "Pebble not connected!", Toast.LENGTH_LONG).show();
        }
    }

    public void sendTestSignal(View view){
        PebbleDictionary dict = new PebbleDictionary();
        dict.addInt32(0, 1);
        PebbleKit.sendDataToPebble(this, watchAppUUID, dict);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent launchSettingsIntent = new Intent(Main.this, Settings.class);
            Main.this.startActivity(launchSettingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
