package com.example.onzzz.i2v;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Tab3 extends Fragment {
    final String tag = "Tab 3 is here";
    String userObjectId;
    String eventObjectId;
    ListView listView ;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        System.out.println(tag);
        View v =inflater.inflate(R.layout.tab_3,container,false);
        EventContentActivity activity = (EventContentActivity) getActivity();
        userObjectId = activity.getUserObjectId();
        eventObjectId = activity.getEventObjectId();
        // Get ListView object from xml
       listView = (ListView) v.findViewById(R.id.member_list);

        // Defined Array values to show in ListView
        String[] values = new String[] { "Wong Sui On",
                "Liu Wai Chong",
                "Chan Ho Yui",
                "Eva Tsang",
        };

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                R.layout.user_item_view, R.id.friend_name, values);

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) listView.getItemAtPosition(position);

                // Show Alert
         //       Toast.makeText(getApplicationContext(),
          //              "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
           //             .show();

            }

        });
        return v;
    }
}