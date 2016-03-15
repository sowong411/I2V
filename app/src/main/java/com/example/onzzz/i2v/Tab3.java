package com.example.onzzz.i2v;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Tab3 extends Fragment {
    final String tag = "Tab 3 is here";
    String userObjectId;
    String eventObjectId;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        System.out.println(tag);
        View v =inflater.inflate(R.layout.tab_3,container,false);
        EventContentActivity activity = (EventContentActivity) getActivity();
        userObjectId = activity.getUserObjectId();
        eventObjectId = activity.getEventObjectId();

        return v;
    }
}