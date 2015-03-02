package com.edwinfinch.idoc360;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;

public class DeviceFragment extends Fragment {

    RelativeLayout llLayout;

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    public boolean currentLightState = false;

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = new UartService();
    public BluetoothDevice mDevice = null;
    public BluetoothAdapter mBtAdapter = null;
    private Button btnConnectDisconnect, btnLightOn, btnLightOff, reconnectButton, clearLogsButton;
    private TextView previousDeviceTV, debugTextView;
    ScrollView debugScrollView;

    private Handler mHandler = new Handler() {
        @Override

        //Handler events that received from UART service
        public void handleMessage(Message msg) {

        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        FileManager.writeDevice(getActivity().getApplicationContext(), mDevice.getAddress());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        previousDeviceTV.setText(FileManager.getDeviceStatus(getActivity(), true));
                        debug("Connected to: " + mDevice.getAddress());
                        mState = UART_PROFILE_CONNECTED;
                        reconnectButton.setText(R.string.disconnectalready);
                        btnLightOn.setEnabled(true);
                        btnLightOff.setEnabled(true);
                        reconnectButton.setEnabled(true);
                        NotificationCompat.Action action1 = new NotificationCompat.Action(R.drawable.x_icon, "Disconnect", null);
                        NotificationCompat.Action action2 = new NotificationCompat.Action(R.drawable.settings_icon_notif, "Settings", null);
                        com.edwinfinch.idoc360.NotificationManager.manageConstantNotification(getActivity(), true, "Connected", FileManager.getDeviceStatus(getActivity(), true), action1, action2);
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        previousDeviceTV.setText(FileManager.getDeviceStatus(getActivity(), false));
                        debug("Disconnected to: " + mDevice.getAddress());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        reconnectButton.setText(R.string.connectalready);
                        btnLightOn.setEnabled(false);
                        btnLightOff.setEnabled(false);
                        reconnectButton.setEnabled(true);
                        NotificationCompat.Action action1 = new NotificationCompat.Action(R.drawable.refresh, "Reconnect", null);
                        com.edwinfinch.idoc360.NotificationManager.manageConstantNotification(getActivity(), false, "Disconnected", FileManager.getDeviceStatus(getActivity(), false), action1);
                    }
                });
            }


            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());

                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                //showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }


        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(getActivity(), UartService.class);
        getActivity().bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    previousDeviceTV.setText("...");
                    reconnectButton.setText(getResources().getString(R.string.attempting));
                    mService.connect(deviceAddress);
                    FileManager.writeDevice(getActivity().getApplicationContext(), deviceAddress);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getActivity(), "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
                    debug("Bluetooth request accepted");
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    this.getActivity().finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    public void sendLightMessage(boolean on){
        if(mService == null){
            return;
        }
        if(on){
            String message = "on\n";
            byte[] value;
            try {
                //send data to service
                value = message.getBytes("UTF-8");
                mService.writeRXCharacteristic(value);
                //Update the log with time stamp
                debug(getResources().getString(R.string.sendingcommand) + " on");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(getActivity().getApplicationContext(), "Error sending message: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }
        else{
            String message = "off\n";
            byte[] value;
            try {
                value = message.getBytes("UTF-8");
                mService.writeRXCharacteristic(value);
                debug(getResources().getString(R.string.sendingcommand) + " off");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Toast.makeText(getActivity().getApplicationContext(), "Error sending message: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }
        currentLightState = !currentLightState;
    }

    public void sendLightMessage(){
        sendLightMessage(currentLightState);
    }

    private void debug(String string){
         debugTextView.append(System.currentTimeMillis() + ": " + string + "\n");
    }

    private void fireSelector(){
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onClick - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else {
            if (btnConnectDisconnect.getText().equals(getResources().getString(R.string.connect))){

                //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

                Intent newIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
            } else {
                //Disconnect button pressed
                if (mDevice!=null)
                {
                    mService.disconnect();

                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity faActivity = super.getActivity();
        faActivity.setTitle("Device Manager");

        llLayout = (RelativeLayout)inflater.inflate(R.layout.main, container, false);

        debugScrollView = (ScrollView)llLayout.findViewById(R.id.debugScrollList);
        debugTextView = new TextView(getActivity().getApplicationContext());
        debugTextView.setText("Debug Log:\n");
        debugTextView.setTextColor(getResources().getColor(R.color.text));
        debugScrollView.addView(debugTextView);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            getActivity().finish();
            return null;
        }
        service_init();

        previousDeviceTV = (TextView)llLayout.findViewById(R.id.deviceName);

        btnConnectDisconnect = (Button)llLayout.findViewById(R.id.btn_select);
        btnLightOff = (Button)llLayout.findViewById(R.id.lightOff);
        btnLightOn = (Button)llLayout.findViewById(R.id.lightOn);
        reconnectButton = (Button)llLayout.findViewById(R.id.prevDeviceButton);
        clearLogsButton = (Button)llLayout.findViewById(R.id.clearLogButton);

        if(!mBtAdapter.isEnabled()){
            reconnectButton.setText(getResources().getString(R.string.btoff));
        }

        String deviceSaved = FileManager.getDeviceAddress(getActivity().getApplicationContext());
        if(deviceSaved != null){
            mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceSaved);
            previousDeviceTV.setText("iDoc " + FileManager.getDeviceName(getActivity().getApplicationContext()) + " " + getResources().getString(R.string.ex_dis));
        }
        else{
            previousDeviceTV.setText(getResources().getString(R.string.nodevice));
            reconnectButton.setEnabled(false);
            reconnectButton.setText("Select a device below");
        }


        mService.mBluetoothAdapter = mBtAdapter;
        //mService.mBluetoothManager =

        clearLogsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                debugTextView.setText("Debug Log:\n");
            }
        });

        // Handler Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FileManager.getDeviceAddress(getActivity().getApplicationContext()) != null) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.newdevice))
                            .setMessage(getResources().getString(R.string.newdevicewarning))
                            .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mService.disconnect();
                                    String add = FileManager.getDeviceName(getActivity().getApplicationContext());
                                    if(FileManager.deleteDevice(getActivity().getApplicationContext())){
                                        Toast.makeText(getActivity().getApplicationContext(), add + " " + getResources().getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(getActivity().getApplicationContext(), add + " " + getResources().getString(R.string.notdeleted), Toast.LENGTH_SHORT).show();
                                    }
                                    previousDeviceTV.setText(getResources().getString(R.string.nodevice));
                                    reconnectButton.setEnabled(false);
                                    reconnectButton.setText("Select a device below");
                                    debug("Device deleted");
                                    fireSelector();
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.cancel), null)
                            .show();
                }
                else{
                    fireSelector();
                }
            }
        });

        btnLightOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLightMessage(true);
            }
        });

        btnLightOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLightMessage(false);
            }
        });

        reconnectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(reconnectButton.getText().equals(getResources().getString(R.string.btoff))){
                    boolean result = mBtAdapter.enable();
                    debug("Enabling bluetooth... Result: " + result);
                    if(result){
                        reconnectButton.setText(getResources().getString(R.string.connectalready));
                    }
                    else{
                        debug("Failed to turn on Bluetooth. Please turn it on yourself.");
                    }
                    return;
                }
                if(reconnectButton.getText().equals(getResources().getString(R.string.connectalready))){
                    String deviceAddress = FileManager.getDeviceAddress(getActivity().getApplicationContext());
                    debug("Attempting to reconnect to " + FileManager.getDeviceAddress(getActivity().getApplicationContext()));
                    reconnectButton.setText(getResources().getString(R.string.reconnecting));
                    reconnectButton.setEnabled(false);
                    mService.connect(deviceAddress);
                    com.edwinfinch.idoc360.NotificationManager.reconnectNotification(getActivity());
                }
                else{
                    debug("Disconnecting");
                    mService.disconnect();
                    reconnectButton.setEnabled(false);
                }
            }
        });

        btnLightOn.setEnabled(false);
        btnLightOff.setEnabled(false);

        // Set initial UI state

        return llLayout;
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                getActivity().finish();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };
}
