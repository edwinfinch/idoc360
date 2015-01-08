package com.edwinfinch.idoc360;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class SettingsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity faActivity = super.getActivity();
        faActivity.setTitle("Settings");

        RelativeLayout llLayout = (RelativeLayout)inflater.inflate(R.layout.settings_fragment, container, false);

        return llLayout;
    }
}
