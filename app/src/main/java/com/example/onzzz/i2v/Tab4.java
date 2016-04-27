package com.example.onzzz.i2v;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class Tab4 extends Fragment {

    String userObjectId;
    String eventObjectId;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_4,container,false);

        EventContentActivity activity = (EventContentActivity) getActivity();
        userObjectId = activity.getUserObjectId();
        eventObjectId = activity.getEventObjectId();

        final TextView eventName = (TextView) v.findViewById(R.id.eventName);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        query.getInBackground(eventObjectId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                eventName.setText(object.getString("EventName"));
            }
        });

        return v;
    }
}