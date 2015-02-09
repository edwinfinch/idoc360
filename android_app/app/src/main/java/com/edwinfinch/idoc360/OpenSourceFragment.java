package com.edwinfinch.idoc360;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class OpenSourceFragment extends Fragment {

    private Button blksButton, uartButton;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity faActivity = super.getActivity();
        faActivity.setTitle("Open Source");
        RelativeLayout llLayout = (RelativeLayout) inflater.inflate(R.layout.open_source_fragment, container, false);

        blksButton = (Button) llLayout.findViewById(R.id.blksButton);
        uartButton = (Button) llLayout.findViewById(R.id.uartButton);

        return llLayout;
    }
}