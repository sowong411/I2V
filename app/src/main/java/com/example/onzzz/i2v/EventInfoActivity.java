package com.example.onzzz.i2v;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by WAICHONG on 31/12/2015.
 */
public class EventInfoActivity extends ActionBarActivity {

    private ImageButton dateButton;
    private ImageButton timeButton;

    private TextView moreless;
    private TextView date;
    private TextView time;

    private int eventDay;
    private int eventMonth;
    private int eventYear;
    private int eventHour;
    private int eventMinute;

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

        final Calendar c = Calendar.getInstance();
        final int mYear = eventYear = c.get(Calendar.YEAR);
        final int mMonth = eventMonth = c.get(Calendar.MONTH);
        eventMonth++;
        final int mDay = eventDay = c.get(Calendar.DAY_OF_MONTH);
        final int mHour = eventHour = c.get(Calendar.HOUR_OF_DAY);
        final int mMinute = eventMinute = c.get(Calendar.MINUTE);

        // after typing all info (date time place , app will create event)
        findViewById(R.id.create_event_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText eventNameText = (EditText) findViewById(R.id.name);
                eventName = eventNameText.getText().toString();
                if(eventName.length() == 0) {
                    Snackbar snackbar = Snackbar
                            .make((findViewById(R.id.coordinatorLayoutEventInfo)), "Please enter event name", Snackbar.LENGTH_LONG);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                //    textView.setTextColor(Color.YELLOW);
                    snackbar.show();
                    return;
                }
                    /***************Upload Current Event Information***************/
                final ParseObject event = new ParseObject("Event");
                event.put("EventName", eventName);
                event.put("Date", eventYear+"-"+eventMonth+"-"+eventDay);
                if (eventMinute < 10){
                    event.put("Time", eventHour+":0"+eventMinute);
                }
                else {
                    event.put("Time", eventHour+":"+eventMinute);
                }
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
                i.putExtra("UserObjectId", userObjectId);
                startActivityForResult(i, 100);
            }
        });



        dateButton = (ImageButton) findViewById(R.id.date_button);
        date = (TextView) findViewById(R.id.date);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog dpd = new DatePickerDialog(EventInfoActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                eventYear = year;
                                eventMonth = monthOfYear+1;
                                eventDay = dayOfMonth;
                                date.setText(eventYear + "-" + eventMonth + "-" + eventDay);
                            }
                        }, mYear, mMonth, mDay);
                dpd.show();
            }
        });

        time = (TextView) findViewById(R.id.time);
        timeButton = (ImageButton) findViewById(R.id.time_button);
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog tpd = new TimePickerDialog(EventInfoActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                eventHour = hourOfDay;
                                eventMinute = minute;
                                if (eventMinute < 10){
                                    time.setText(eventHour+":0"+eventMinute);
                                }
                                else {
                                    time.setText(eventHour+":"+eventMinute);
                                }
                            }
                        }, mHour, mMinute, true);
                tpd.show();
            }
        });



        moreless = (TextView) findViewById(R.id.more);
        moreless.setText("show more");
        moreless.setOnClickListener(new View.OnClickListener() {
            boolean moreop=false;
            LinearLayout lay1= (LinearLayout) findViewById(R.id.dateContainer);
            LinearLayout lay2= (LinearLayout) findViewById(R.id.timeContainer);
            @Override
            public void onClick(View v) {
                if(!moreop){
                    moreless.setText("show less");
                    lay1.setVisibility(View.VISIBLE);
                    lay2.setVisibility(View.VISIBLE);
                    moreop = true;
                }else{
                    moreless.setText("show more");
                    lay1.setVisibility(View.GONE);
                    lay2.setVisibility(View.GONE);
                    moreop=false;
                }

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
            Intent intent = new Intent();
            intent.setClass(EventInfoActivity.this, UserInfoActivity.class);
            intent.putExtra("UserObjectId", userObjectId);
            startActivity(intent);
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
