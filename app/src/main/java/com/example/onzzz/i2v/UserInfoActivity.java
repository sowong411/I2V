package com.example.onzzz.i2v;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.InputStream;

/**
 * Created by onzzz on 26/2/2016.
 */
public class UserInfoActivity extends ActionBarActivity {

    String name;
    String profilePicUri;
    String loginMethod;
    String userObjectId;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        userObjectId = intent.getStringExtra("UserObjectId");

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Account");
        query.getInBackground(userObjectId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                name = object.getString("Name");
                profilePicUri = object.getString("ProfilePicUri");
                loginMethod = object.getString("LoginMethod");

                TextView loginMethodText = (TextView) findViewById(R.id.login_method);
                if (loginMethod.equals("Facebook")) {
                    loginMethodText.setText("You are logged in using Facebook");
                    //ProfilePictureView displayProfilePic = (ProfilePictureView) findViewById(R.id.profilePicFacebook);
                    ImageView displayProfilePic = (ImageView) findViewById(R.id.profilePicFacebook);
                    displayProfilePic.setVisibility(View.VISIBLE);
                    new LoadProfileImage(displayProfilePic).execute(profilePicUri);
                } else if (loginMethod.equals("Google")) {
                    loginMethodText.setText("You are logged in using Google");
                    ImageView displayProfilePic = (ImageView) findViewById(R.id.profilePicGoogle);
                    displayProfilePic.setVisibility(View.VISIBLE);
                    new LoadProfileImage(displayProfilePic).execute(profilePicUri);
                }

                TextView displayName = (TextView) findViewById(R.id.userName);
                displayName.setText(name);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.userButton) {
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                // API 5+ solution
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
        //  return super.onOptionsItemSelected(item);
    }

    /**
     * Background Async task to load user profile picture from url
     * */
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

    public void Close(View view) {
        finish();
    }
}
