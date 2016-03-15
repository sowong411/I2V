package com.example.onzzz.i2v;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;

/**
 * Created by hp1 on 21-01-2015.
 */
public class Tab1 extends Fragment {
    final String tag = "Tab 1 is here";
    String userObjectId;
    String eventObjectId;

    ArrayList<String> photoString = new ArrayList<String>();
    private GridView gridView;
    private GridViewAdapter gridAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        System.out.println(tag);
        final View v = inflater.inflate(R.layout.tab_1, container, false);
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
                ((EventContentActivity)getActivity()).setPhotoString(photoString);
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
     public ArrayList<String> getImagesEncodedString (){
         return photoString;
     }
}