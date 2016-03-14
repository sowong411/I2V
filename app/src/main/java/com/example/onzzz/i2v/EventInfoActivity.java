package com.example.onzzz.i2v;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.Arrays;
import java.util.List;

/**
 * Created by WAICHONG on 31/12/2015.
 */
public class EventInfoActivity extends ActionBarActivity {

    private Button dateButton;
    private Button timeButton;
    private Button locationButton;

    String userObjectId;
    String eventObjectId;
    String eventName;

    String[] memberId = new String[20];
    int numOfMember = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event_info);

        Intent intent = getIntent();
        userObjectId = intent.getStringExtra("UserObjectId");

        memberId[0] = userObjectId;

        assert (intent != null);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // after typing all info (date time place , app will create event)
        findViewById(R.id.create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText eventNameText = (EditText) findViewById(R.id.name);
                eventName = eventNameText.getText().toString();

                /***************Upload Current Event Information***************/
                final ParseObject event = new ParseObject("Event");
                event.put("EventName", eventName);
                event.put("Date", "");
                event.put("Time", "");
                event.put("PhotoNumber", 0);
                event.put("MemberNumber", numOfMember);
                event.addAllUnique("Member", Arrays.asList(memberId));
                event.put("EventHolder", memberId[0]);
                event.put("VideoNumber", 0);
                event.put("Video", "");
                event.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            ParseQuery<ParseObject> eventQuery = ParseQuery.getQuery("Event");
                            eventQuery.orderByDescending("createdAt");
                            eventQuery.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> objects, ParseException e) {
                                    eventObjectId = objects.get(0).getObjectId();
                                    for (int i = 0; i < numOfMember; i++) {
                                        /***************Update User's Event***************/
                                        ParseQuery<ParseObject> accountQuery = ParseQuery.getQuery("Account");
                                        accountQuery.getInBackground(memberId[i], new GetCallback<ParseObject>() {
                                            @Override
                                            public void done(ParseObject object, ParseException e) {
                                                if (e == null) {
                                                    object.add("Event", eventObjectId);
                                                    object.saveInBackground();
                                                }
                                            }
                                        });
                                    }
                                    Intent intent = new Intent();
                                    intent.setClass(EventInfoActivity.this, EventContentActivity.class);
                                    intent.putExtra("UserObjectId", userObjectId);
                                    intent.putExtra("EventObjectId", eventObjectId);
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                });
                eventNameText.setText("");
            }
        });

        findViewById(R.id.addmem_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EventInfoActivity.this, AddMemberActivity.class);
                startActivityForResult(i, 100);
            }
        });

        findViewById(R.id.date_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog();
            }
        });
        findViewById(R.id.time_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeDialog();
            }
        });
        findViewById(R.id.location_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocationDialog();
            }
        });
    }

    public void showDateDialog() {
        AlertDialog.Builder builder;
        AlertDialog alertDialog;
        Context mContext = EventInfoActivity.this;

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.input_date, null);
        //   TextView text = (TextView) layout.findViewById(R.id.location);
        //  text.setText("Hello, Welcome to Mr Wei's blog!");
        //   ImageView image = (ImageView) layout.findViewById(R.id.image);
        //  image.setImageResource(R.drawable.icon);
        builder = new AlertDialog.Builder(mContext);
        builder.setView(layout);
        alertDialog = builder.create();
        alertDialog.show();
    }
    public void showTimeDialog() {
        AlertDialog.Builder builder;
        AlertDialog alertDialog;
        Context mContext = EventInfoActivity.this;

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.input_time, null);
        //   TextView text = (TextView) layout.findViewById(R.id.location);
        //  text.setText("Hello, Welcome to Mr Wei's blog!");
        //   ImageView image = (ImageView) layout.findViewById(R.id.image);
        //  image.setImageResource(R.drawable.icon);
        builder = new AlertDialog.Builder(mContext);
        builder.setView(layout);
        alertDialog = builder.create();
        alertDialog.show();
    }
    public void showLocationDialog() {
        AlertDialog.Builder builder;
        AlertDialog alertDialog;
        Context mContext = EventInfoActivity.this;

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.input_location, null);
        //   TextView text = (TextView) layout.findViewById(R.id.location);
        //  text.setText("Hello, Welcome to Mr Wei's blog!");
        //   ImageView image = (ImageView) layout.findViewById(R.id.image);
        //  image.setImageResource(R.drawable.icon);
        builder = new AlertDialog.Builder(mContext);
        builder.setView(layout);
        alertDialog = builder.create();
        alertDialog.show();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (100) : {
                if (resultCode == Activity.RESULT_OK) {
                    String[] newText = data.getStringArrayExtra("Member");
                    numOfMember = data.getIntExtra("NumOfMember", 1);
                    // TODO Update your TextView.
                    for (int i=1; i<numOfMember; i++){
                        memberId[i] = newText[i];
                    }
                }
                break;
            }
        }
    }

    public void Close(View view) {
        finish();
    }
}
