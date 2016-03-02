package com.example.onzzz.i2v;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private List<Event> myEvents = new ArrayList<Event>();
    String userObjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        userObjectId = intent.getStringExtra("UserObjectId");

        populateEventList();

        findViewById(R.id.create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CreateEventActivity.class);
                intent.putExtra("UserObjectId", userObjectId);
                startActivity(intent);
            }
        });
    }

    private void  populateEventList() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        query.whereEqualTo("EventHolder", userObjectId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); i++) {
                        String eventname = objects.get(i).getString("EventName");
                        myEvents.add(new Event(eventname));
                        populateListView();
                    }
                }
            }
        });
    }

    private void populateListView() {
        ArrayAdapter<Event> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.event_list);
        list.setAdapter(adapter);
    }

    private class MyListAdapter extends ArrayAdapter<Event> {

        public MyListAdapter() {
            super(MainActivity.this, R.layout.event_item_view, myEvents);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Make sure we have a view to work with (may have been given null)
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.event_item_view, parent, false);
            }

            // Find the car to work with.
            Event currentEvent = myEvents.get(position);

            // Fill the view
            ImageView imageView = (ImageView)itemView.findViewById(R.id.item_icon);
            imageView.setImageResource(R.drawable.bug);
            // Make:
            TextView event_name = (TextView) itemView.findViewById(R.id.event_name);
            event_name.setText(currentEvent.getName());

            return itemView;
        }
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
            intent.setClass(MainActivity.this, UserInfoActivity.class);
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

    public void Close(View view) {
        finish();
    }

}

