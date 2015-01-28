package com.edwinfinch.idoc360;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

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

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = new UartService();
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect, btnLightOn, btnLightOff;

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
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        ((TextView) llLayout.findViewById(R.id.deviceName)).setText(mDevice.getName() + " - ready");
                        listAdapter.add("[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                        mState = UART_PROFILE_CONNECTED;
                        btnConnectDisconnect.setText(R.string.disconnect);
                        btnLightOn.setEnabled(true);
                        btnLightOff.setEnabled(true);
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        ((TextView) llLayout.findViewById(R.id.deviceName)).setText("Not Connected");
                        listAdapter.add("[" + currentDateTimeString + "] Disconnected to: " + mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();
                        btnConnectDisconnect.setText(R.string.connect);
                        btnLightOn.setEnabled(false);
                        btnLightOff.setEnabled(false);
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
                            listAdapter.add("[" + currentDateTimeString + "] RX: " + text);

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
                    ((TextView) llLayout.findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                    mService.connect(deviceAddress);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getActivity(), "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity faActivity = super.getActivity();
        faActivity.setTitle("Device Manager");

        llLayout = (RelativeLayout)inflater.inflate(R.layout.main, container, false);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            getActivity().finish();
            return null;
        }
        listAdapter = new ArrayAdapter<String>(this.getActivity().getApplicationContext(), R.layout.message_detail);
        service_init();

        btnConnectDisconnect = (Button)llLayout.findViewById(R.id.btn_select);
        btnLightOff = (Button)llLayout.findViewById(R.id.lightOff);
        btnLightOn = (Button)llLayout.findViewById(R.id.lightOn);

        mService.mBluetoothAdapter = mBtAdapter;
        //mService.mBluetoothManager =


        // Handler Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                    if (btnConnectDisconnect.getText().equals("Connect")){

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
        });

        btnLightOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "on\n";
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    Toast.makeText(getActivity().getApplicationContext(), "Sending command: on", Toast.LENGTH_LONG).show();
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Toast.makeText(getActivity().getApplicationContext(), "Error sending message: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        btnLightOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "off\n";
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    Toast.makeText(getActivity().getApplicationContext(), "Sending command: off", Toast.LENGTH_LONG).show();
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Toast.makeText(getActivity().getApplicationContext(), "Error sending message: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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
