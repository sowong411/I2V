package com.example.onzzz.i2v;

import android.accounts.Account;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;

import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Tab3 extends Fragment {
    final String tag = "Tab 3 is here";
    String userObjectId;
    String eventObjectId;
    private List<Account> accounts = new ArrayList<Account>();
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
       final UsersAdapter adapter = new UsersAdapter( activity, arrayOfUsers);

        // Add item to adapter
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        query.getInBackground(eventObjectId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, com.parse.ParseException e) {

                if ((e == null) && (object.getList("Member").size() != 0)) {
                    for (int i = 0; i < object.getList("Member").size()-1; i++) {
                        final String accountId = object.getList("Member").get(i).toString();
                        ParseQuery<ParseObject> accountQuery = ParseQuery.getQuery("Account");
                        accountQuery.getInBackground(accountId, new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject object, com.parse.ParseException e) {
                                String memberName = object.getString("Name");
                                String login = object.getString("LoginMethod");
                                String iconUri = object.getString("ProfilePicUri");
                                User addMember = new User(memberName, iconUri, login);
                                adapter.add(addMember);
                            }
                        });
                    }
                }
            }
        });

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
            ImageView loginIcon = (ImageView) convertView.findViewById(R.id.login_icon);
            // Populate the data into the template view using the data object
            new LoadProfileImage(icon).execute(user.iconUri);
            fdname.setText(user.name);
            if(user.loginBy=="Facebook")
                loginIcon.setImageResource(R.drawable.fb_icon);
            else
               loginIcon.setImageResource(R.drawable.common_google_signin_btn_icon_dark_normal);

            // Return the completed view to render on screen
            return convertView;
        }
    }
    /***************Background Async task to load user profile picture from url***************/
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public class User {
        public String name;
        public String iconUri;
        public String loginBy;

        public User(String name, String icon, String loginBy) {
            this.name = name;
            this.iconUri = icon;
            this.loginBy = loginBy;
        }
    }

}