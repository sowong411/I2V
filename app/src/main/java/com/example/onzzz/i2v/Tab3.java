package com.example.onzzz.i2v;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;

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
        // Construct the data source
        ArrayList<User> arrayOfUsers = new ArrayList<User>();
        // Create the adapter to convert the array to views
        UsersAdapter adapter = new UsersAdapter( activity, arrayOfUsers);

        // Add item to adapter
        User newUser = new User("Eva Tsang", R.drawable.fish);
        User newUser2 = new User("Liu Wai Chong");
        adapter.add(newUser);
        adapter.add(newUser2);

        // Attach the adapter to a ListView
        ListView listView = (ListView) v.findViewById(R.id.member_list);
        listView.setAdapter(adapter);

        return v;
    }
    public class UsersAdapter extends ArrayAdapter<User> {
        public UsersAdapter(Context context, ArrayList<User> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            User user = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_item_view, parent, false);
            }

            // Lookup view for data population
            ImageView icon = (ImageView) convertView.findViewById(R.id.user_item_icon);
            TextView fdname = (TextView) convertView.findViewById(R.id.friend_name);

            // Populate the data into the template view using the data object
            icon.setImageResource(user.icon);
            fdname.setText(user.name);
            // Return the completed view to render on screen
            return convertView;
        }
    }

    public class User {
        public String name;
        public int icon;

        public User(String name, int icon) {
            this.name = name;
            this.icon = icon;
        }
        public User(String name) {
            this.name = name;
            this.icon = R.drawable.no_media;
        }
    }

}