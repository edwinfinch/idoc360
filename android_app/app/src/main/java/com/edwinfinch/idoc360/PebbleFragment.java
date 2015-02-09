package com.edwinfinch.idoc360;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class PebbleFragment extends Fragment {

    private TextView debug_log;
    private int logStackTop = -1;
    private String[] logs = new String[8];

    public TextView connection_textview;
    public Button test_signal_button, install_button;
    public ToggleButton toggle_button;
    public boolean attached = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity faActivity = super.getActivity();
        faActivity.setTitle("Pebble Manager");

        RelativeLayout llLayout = (RelativeLayout)inflater.inflate(R.layout.pebble_fragment, container, false);

        debug_log = (TextView) llLayout.findViewById(R.id.debugLog);
        connection_textview = (TextView) llLayout.findViewById(R.id.pebbledebug_textview);

        test_signal_button = (Button) llLayout.findViewById(R.id.sendtestmessage_button);
        install_button = (Button) llLayout.findViewById(R.id.installapp_button);

        toggle_button = (ToggleButton) llLayout.findViewById(R.id.openCloseToggle);

        log("Pebble manager init");

        return llLayout;
    }

    @Override
    public void onAttach(Activity view){
        super.onAttach(view);
        attached = true;
    }

    public void log(String toLog){
        debug_log.setText("");
        logStackTop++;
        if(logStackTop > 7){
            logs = new String[8];
            logStackTop = 0;
        }
        logs[logStackTop] = System.currentTimeMillis() + ": " + toLog;
        for(int i = 0; i < logStackTop+1; i++){
            debug_log.append(logs[i] + "\n");
        }
    }
}
