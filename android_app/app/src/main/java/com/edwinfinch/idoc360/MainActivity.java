package com.edwinfinch.idoc360;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static com.getpebble.android.kit.PebbleKit.*;


public class MainActivity extends ActionBarActivity implements NavigationDrawerCallbacks {

    private Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private PebbleFragment pebble_fragment = new PebbleFragment();
    private DeviceFragment device_fragment = new DeviceFragment();
    private TutorialFragment tutorial_fragment = new TutorialFragment();
    private SettingsFragment settings_fragment = new SettingsFragment();
    final private UUID watchAppUUID = UUID.fromString("54f918be-893e-433d-aa87-a84e55e24f6b");
    private boolean connected = false, appOpen = false;

    public void sideloadInstall(Context ctx, String assetFilename){
        try{
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
        }
        catch (IOException e) {
            Toast.makeText(ctx, "App install failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void installIDOC360(View view){
        if(connected){
            sideloadInstall(getApplicationContext(), "idoc360.pbw");
        }
    }

    public void toggleApp(View view){
        appOpen = !appOpen;
        if(appOpen){
            PebbleKit.startAppOnPebble(getApplicationContext(), watchAppUUID);
            pebble_fragment.log("Opening pebble app");
        }
        else{
            PebbleKit.closeAppOnPebble(getApplicationContext(), watchAppUUID);
            pebble_fragment.log("Closing pebble app");
        }
        refreshElements();
    }

    private BroadcastReceiver disconnectedRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            connected = false;
            refreshElements();
        }
    };

    private BroadcastReceiver connectedRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            connected = true;
            refreshElements();
        }
    };

    private ToggleButton.OnClickListener open_close_listener = new ToggleButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(pebble_fragment.toggle_button.isChecked()){
                pebble_fragment.log("Sending open request");
                PebbleKit.startAppOnPebble(getApplicationContext(), watchAppUUID);
            }
            else{
                pebble_fragment.log("Sending close request");
                PebbleKit.closeAppOnPebble(getApplicationContext(), watchAppUUID);
            }
        }
    };

    private PebbleDataReceiver pebble_data_rec = new PebbleDataReceiver(watchAppUUID) {
        @Override
        public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
            int dataInt = data.getUnsignedIntegerAsLong(0).intValue();
            switch(dataInt){
                case 0:

                    break;
            }
        }
    };

    public void refreshElements(){
        pebble_fragment.toggle_button.setEnabled(connected);
        pebble_fragment.install_button.setEnabled(connected);
        pebble_fragment.test_signal_button.setEnabled(connected);

        if(appOpen && connected){
            pebble_fragment.connection_textview.setText("Watch connected and app open.");
        }
        else if(connected && !appOpen){
            pebble_fragment.connection_textview.setText("Watch connected but app not open.");
            pebble_fragment.test_signal_button.setEnabled(appOpen);
        }
        else{
            pebble_fragment.connection_textview.setText("No connection what-so-ever :(");
        }
    }

    public void sendTestSignal(View view){
        pebble_fragment.log("Sending test signal");

        PebbleDictionary testSignal = new PebbleDictionary();
        testSignal.addInt32(0, 0);
        sendDataToPebble(getApplicationContext(), watchAppUUID, testSignal);
    }

    /*
    * Activity overridden functions
    * Such as onCreate, onDestroy, etc.
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);

        registerPebbleConnectedReceiver(getApplicationContext(), connectedRec);
        registerPebbleDisconnectedReceiver(getApplicationContext(), disconnectedRec);

        connected = PebbleKit.isWatchConnected(this);

        PebbleKit.registerReceivedDataHandler(getApplicationContext(), pebble_data_rec);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Fragment fragment = new DefaultFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch(position) {
            case 0:
                fragmentManager.beginTransaction().replace(R.id.container, pebble_fragment).commit();
                break;
            case 1:
                fragmentManager.beginTransaction().replace(R.id.container, device_fragment).commit();
                break;
            case 2:
                fragmentManager.beginTransaction().replace(R.id.container, tutorial_fragment).commit();
                break;
            case 3:
                fragmentManager.beginTransaction().replace(R.id.container, settings_fragment).commit();
                break;
            default:
                fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            mNavigationDrawerFragment.closeDrawer();
        }
        else {
            super.onBackPressed();
        }
    }

}
