package com.example.onzzz.i2v;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hp1 on 21-01-2015.
 */
public class Tab2 extends Fragment {

    String userObjectId;
    String eventObjectId;

    ArrayList<String> photoString = new ArrayList<String>();

    private GridView gridView;
    private GridViewAdapter gridAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.tab_2, container, false);
        final Context context = this.getContext();

        EventContentActivity activity = (EventContentActivity) getActivity();
        userObjectId = activity.getUserObjectId();
        eventObjectId = activity.getEventObjectId();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Photo");
        query.whereEqualTo("Event", eventObjectId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){
                    for (int i=0; i<objects.size(); i++){
                        photoString.add(objects.get(i).getString("Image"));
                    }
                    gridView = (GridView) v.findViewById(R.id.gridView);
                    gridAdapter = new GridViewAdapter(context, R.layout.grid_item_layout, getData());
                    gridView.setAdapter(gridAdapter);
                }
            }
        });

        v.findViewById(R.id.upload_photo_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(v.getContext(), PhotoUploadActivity.class);
                intent.putExtra("UserObjectId", userObjectId);
                intent.putExtra("EventObjectId", eventObjectId);
                startActivity(intent);
            }
        });

        return v;
    }

    private ArrayList<ImageItem> getData() {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        for (int i=0; i<photoString.size(); i++) {
            imageItems.add(new ImageItem(decodeBase64(photoString.get(i))));
        }
        return imageItems;
    }

    public static Bitmap decodeBase64(String input){
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}