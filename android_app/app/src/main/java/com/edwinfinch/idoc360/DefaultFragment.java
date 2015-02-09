package com.edwinfinch.idoc360;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DefaultFragment extends Fragment {

    private TextView testTV;
    private int logStackTop;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity faActivity = super.getActivity();
        faActivity.setTitle("Error");
        RelativeLayout llLayout = (RelativeLayout) inflater.inflate(R.layout.default_fragment, container, false);
        testTV = (TextView) llLayout.findViewById(R.id.testTV);

        return llLayout;
    }
}
