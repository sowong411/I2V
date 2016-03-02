package com.example.onzzz.i2v;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by WAICHONG on 31/12/2015.
 */
public class AddMemberActivity extends ActionBarActivity {

    String[] memberId = new String[20];
    int numOfMember = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.addmem_button).setOnClickListener(new View.OnClickListener() {
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
                            }
                        }
                    }
                });
                memberName.setText("");
            }
        });

        findViewById(R.id.finished).setOnClickListener(new View.OnClickListener() {
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
