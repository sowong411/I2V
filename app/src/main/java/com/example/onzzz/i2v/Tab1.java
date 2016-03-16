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

    private int numOfFace;
    private double averageSmile;
    private double averageAge;
    private double varianceAge;
    private int numOfMale;
    private int numOfFemale;
    private double genderRatio;
    private String photoString;
    private int facePosition;

    private ArrayList<Photo> myPhotos = new ArrayList<Photo>();

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
                        photoString = objects.get(i).getString("Image");

                        numOfFace = objects.get(i).getInt("FaceNumber");
                        averageSmile = objects.get(i).getDouble("AverageSmileLevel");
                        averageAge = objects.get(i).getDouble("AverageAge");
                        varianceAge = objects.get(i).getDouble("VarianceAge");
                        numOfMale = objects.get(i).getInt("MaleNumber");
                        numOfFemale = objects.get(i).getInt("FemaleNumber");
                        facePosition = objects.get(i).getInt("FacePosition");

                        if (numOfMale!=0 && numOfFemale!=0){
                            genderRatio = numOfMale/(double)numOfFemale; //Ratio大，陽盛陰衰；Ratio細，陰盛陽衰。
                        }
                        else if (numOfMale!=0 && numOfFemale==0){
                            genderRatio = 1000; //全男班，正氣，正數
                        }
                        else if (numOfMale==0 && numOfFemale!=0){
                            genderRatio = -1000; //全女班，陰氣，負數
                        }

                        myPhotos.add(new Photo(photoString, numOfFace, averageSmile, averageAge, varianceAge, genderRatio, facePosition));
                    }
                    gridView = (GridView) v.findViewById(R.id.gridView);
                    gridAdapter = new GridViewAdapter(context, R.layout.grid_item_layout, getData());
                    gridView.setAdapter(gridAdapter);
                }
                ((EventContentActivity)getActivity()).setMyPhotos(myPhotos);
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
        for (int i=0; i<myPhotos.size(); i++) {
            imageItems.add(new ImageItem(decodeBase64(myPhotos.get(i).getPhotoString())));
        }
        return imageItems;
    }

    public static Bitmap decodeBase64(String input){
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
     /*public ArrayList<String> getImagesEncodedString (){
         return photoString;
     }*/
}