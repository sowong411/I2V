package com.example.onzzz.i2v;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddFriendActivity extends ActionBarActivity {
    Button search_friend;
    EditText friend_name;
    ListView friend_list;
    String userObjectId;
    String eventObjectId;
    String eventName;
    ArrayList<String> friendId = new ArrayList<String>();
    ArrayList<String> friendName = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        Intent intent = getIntent();
        userObjectId = intent.getStringExtra("UserObjectId");
        assert (intent != null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        findViewById(R.id.search_friend_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                populateEventList();
                registerClickCallback();
            }
        });
    }

    /***************Event List Display Related Function***************/
    private void  populateEventList() {
        friend_name = (EditText)findViewById(R.id.friend_name);
        String name = friend_name.getText().toString();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Account");
        query.whereContains("Name", name);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() == 0) {
                        Toast.makeText(getApplicationContext(), "No result match your key",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        for (int i = 0; i < objects.size(); i++) {
                            String id = objects.get(0).getObjectId();
                            String name = objects.get(0).getString("Name");
                            friendId.add(id);
                            friendName.add(name);
                            Toast.makeText(getApplicationContext(), "The" + i + "result is " + name, Toast.LENGTH_SHORT).show();
                            populateListView();
                        }

                    }
                }
            }
        });
        friend_name.setText("");

    }

    /***************Event List Display Related Function***************/
    private void populateListView() {
        ArrayAdapter<String> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.friend_list);
        list.setAdapter(adapter);
    }

    /***************Event List Display Related Function***************/
    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.friend_list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {

                String clickedfriend = friendName.get(position);
                Toast.makeText(AddFriendActivity.this, "the clicking friend name is : " + clickedfriend, Toast.LENGTH_LONG).show();
            }
        });
    }

    /***************Event List Display Related Class***************/
    private class MyListAdapter extends ArrayAdapter<String> {

        //Constructor
        public MyListAdapter() {
            super(AddFriendActivity.this, R.layout.user_item_view, friendName );
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Make sure we have a view to work with (may have been given null)
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.user_item_view, parent, false);
            }

            // Find the car to work with.
            String currentFriend = friendName.get(position);

            // Fill the view
            ImageView imageView = (ImageView)itemView.findViewById(R.id.user_item_icon);
            imageView.setImageResource(R.drawable.no_media);
            // Make:
            TextView user_name = (TextView) itemView.findViewById(R.id.friend_name);
            user_name.setText(currentFriend);

            return itemView;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_friend, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
