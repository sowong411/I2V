package com.example.onzzz.i2v;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.app.Activity;
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
import java.util.List;

/**
 * Created by WAICHONG on 31/12/2015.
 */
public class AddMemberActivity extends ActionBarActivity {
    String userObjectId;
    private Button memButton;
    ArrayList<String> friendId = new ArrayList<String>();
    ArrayList<String> friendName = new ArrayList<String>();
    String clickedfriend;
    String clickedFriendId;
    String[] memberId = new String[20];
    int numOfMember = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        userObjectId = intent.getStringExtra("UserObjectId");

        // construct friend as member list
        populateFriendList();
        registerClickCallback();

        findViewById(R.id.search_for_alluser_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText memberName = (EditText) findViewById(R.id.memlist);
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Account");
                query.whereEqualTo("Name", memberName.getText().toString());
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                            if (objects.size() == 0) {
                                Toast.makeText(getApplicationContext(), "User not found",
                                        Toast.LENGTH_SHORT).show();
                            }
                            if (objects.size() == 1) {
                                memberId[numOfMember] = objects.get(0).getObjectId();
                                numOfMember++;
                                Toast.makeText(getApplicationContext(), "Member added",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                memberName.setText("");
            }
        });

        findViewById(R.id.finish_add_member).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("Member", memberId);
                resultIntent.putExtra("NumOfMember", numOfMember);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    //***************Event List Display Related Function***************/
    private void  populateFriendList() {
        ParseQuery accountQuery = ParseQuery.getQuery("Account");
        accountQuery.getInBackground(userObjectId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    if (object.getList("Friends").size() != 0) {
                        System.out.println(object.getList("Friends"));
                        for (int i = 0; i < object.getList("Friends").size(); i++) {
                            String tempId = object.getList("Friends").get(i).toString();
                            friendId.add(tempId);
                            System.out.println("tempId is : " + tempId);
                            ParseQuery<ParseObject> userquery = ParseQuery.getQuery("Account");
                            userquery.getInBackground(tempId, new GetCallback<ParseObject>() {
                                public void done(ParseObject object, ParseException e) {
                                    if (e == null) {
                                        String tempName = object.getString("Name").toString();
                                        friendName.add(tempName);
                                        populateListView();
                                        System.out.println("tempName is : " + tempName);
                                    } else {
                                        return;
                                    }
                                }
                            });

                        }
                    } else {
                        return;
                    }
                }
            }
        });
    }


    /***************Event List Display Related Function***************/
    private void populateListView() {
        ArrayAdapter<String> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.friend_as_member_list);
        list.setAdapter(adapter);
    }


    /***************Event List Display Related Function***************/
    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.friend_as_member_list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {
                clickedfriend = friendName.get(position);
                clickedFriendId = friendId.get(position);

                memberId[numOfMember] = clickedFriendId ;
                numOfMember++;
                Toast.makeText(getApplicationContext(), "Member added", Toast.LENGTH_SHORT).show();
                friendName.remove(position);
                friendId.remove(position);
                populateListView();
            }
        });
    }

    private class MyListAdapter extends ArrayAdapter<String> {

        //Constructor
        public MyListAdapter() {
            super(AddMemberActivity.this, R.layout.user_item_view, friendName );
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

    public void Close(View view) {
        finish();
    }
}
