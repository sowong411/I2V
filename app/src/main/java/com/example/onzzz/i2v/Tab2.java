package com.example.onzzz.i2v;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static java.lang.Math.min;
import static java.lang.Math.random;
import static org.bytedeco.javacpp.opencv_core.multiplyPut;
import static org.bytedeco.javacpp.opencv_core.norm;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.GaussianBlur;
import static org.bytedeco.javacpp.opencv_core.addWeighted;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.putText;
import static org.bytedeco.javacpp.opencv_imgproc.resize;
import static org.bytedeco.javacpp.opencv_core.Point;

public class Tab2 extends Fragment {
    final String tag = "Tab 2 is here";
    private Button getButton, makeButton, combineButton, uploadButton, downloadButton;
    private ProgressBar progressBar;
    private TextView statusText;
    private int completed=0;
    private Handler handler;
    VideoView videoview;
    ImageButton download_video,upload_video;
    private int maxFaceNum;

    private int transitionFrameDuration;
    private int mainFrameDuration;

    ArrayList<Photo> landscapes = new ArrayList<Photo>(); //風景相
    ArrayList<Photo> photoWithOneFace = new ArrayList<Photo>(); //獨照
    ArrayList<Photo> normalPhotos = new ArrayList<Photo>(); //人相
    ArrayList<Photo> groupPhoto = new ArrayList<Photo>(); //大合照

    ArrayList<Photo> myPhotos = new ArrayList<Photo>();
    ArrayList<Photo> myPhotosWithOrder = new ArrayList<Photo>();

    ArrayList<opencv_core.Mat> images = new ArrayList<opencv_core.Mat>();
    ArrayList<opencv_core.Mat> imagesWithBackground = new ArrayList<opencv_core.Mat>();
    ArrayList<opencv_core.Mat> imagesWithEffect1 = new ArrayList<opencv_core.Mat>();
    ArrayList<opencv_core.Mat> imagesWithEffect2 = new ArrayList<opencv_core.Mat>();
    ArrayList<opencv_core.Mat> imagesWithEffect3 = new ArrayList<opencv_core.Mat>();

    ArrayList<opencv_core.Mat> zooming = new ArrayList<opencv_core.Mat>();
    int zoomWidth, zoomHeight, zoomStartingColumn, zoomStartingRow;

    ArrayList<opencv_core.Mat> multipleInOne = new ArrayList<opencv_core.Mat>();

    String userObjectId;
    String eventObjectId;
    String eventName;

    File makevideo;
    File combine;

    String chosenMusicPath;
    int firstTemplateDecision;
    int secondTemplateDecision;
    int effectDecision;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        System.out.println(tag);
        View v =inflater.inflate(R.layout.tab_2,container,false);
        EventContentActivity activity = (EventContentActivity) getActivity();

        userObjectId = activity.getUserObjectId();
        eventObjectId = activity.getEventObjectId();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        query.getInBackground(eventObjectId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    eventName = object.getString("EventName");
                    System.out.println(eventName);
                    System.out.println(eventName.length());
                }
            }
        });

        download_video = (ImageButton)v.findViewById(R.id.download_button);
        upload_video = (ImageButton) v.findViewById(R.id.upload_button);

        handler = new Handler();
        progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        videoview = (VideoView) v.findViewById(R.id.video01);
        statusText = (TextView) v.findViewById(R.id.status_text);
        progressBar.setMax(100);

        v.findViewById(R.id.make_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Tab2.this.getContext(), VideoSettingActivity.class);
                startActivityForResult(intent, 200);
            }
        });

        //upload the encoded video to server
        upload_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File video2 = new File("/sdcard/MemorVi/" + eventObjectId + "/combine.mp4");
                ParseObject VVV = new ParseObject("video");
                byte[] data = videoTobyte(video2);
                System.out.println("data to string " + data.toString());
                ParseFile file = new ParseFile("66.mp4", data);
                file.saveInBackground();
                VVV.put("file", file);
                VVV.put("eventID", eventObjectId);
                VVV.put("generatedBy", userObjectId);
                VVV.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Toast.makeText(getActivity(), "video uploaded", Toast.LENGTH_LONG).show();
                        System.out.println("video uploaded");
                    }
                });
            }
        });

        download_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("video");
                query.whereEqualTo("eventID", eventObjectId);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> videos, ParseException e) {
                        if (e == null) {
                            if (videos.size() == 0) {
                                Toast.makeText(getActivity(), " No Existing Video", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ParseObject o = videos.get(0);
                            ParseFile file = o.getParseFile("file");
                            try {
                                byte[] data = file.getData();
                                byteToVideo(data);
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                        } else {
                            Log.d("score", "Error: " + e.getMessage());
                        }
                        Toast.makeText(getActivity(), "video downloaded", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200){
            chosenMusicPath = data.getExtras().getString("chosenMusicPath");
            firstTemplateDecision = data.getExtras().getInt("chosenTemplate");
            secondTemplateDecision = data.getExtras().getInt("chosenInnerTemplate");
            effectDecision = data.getExtras().getInt("chosenEffect");
            Thread download_photo_thread = new Thread(download_photo_worker);
            download_photo_thread.start();
        }
    }

    private Runnable download_photo_worker = new Runnable() {
        public  void run() {
            completed = 0;
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(getActivity(), "Downloading Photo", Toast.LENGTH_LONG).show();
                    progressBar.setProgress(completed);
                    statusText.setText(String.format("Completed %d", completed));
                }
            });
            myPhotos = EventContentActivity.getMyPhotos();


            // get max number of face
            for (int i=0; i<myPhotos.size(); i++){
                if (maxFaceNum < myPhotos.get(i).getNumberOfFace()){
                    maxFaceNum = myPhotos.get(i).getNumberOfFace();
                }
            }

            //  divide photo into  four arrays
            for (int i=0; i<myPhotos.size(); i++){
                if (myPhotos.get(i).getNumberOfFace() == 0)
                    landscapes.add(myPhotos.get(i));
                else if (myPhotos.get(i).getNumberOfFace() == 1)
                    photoWithOneFace.add(myPhotos.get(i));
                else if (myPhotos.get(i).getNumberOfFace() == maxFaceNum)
                    groupPhoto.add(myPhotos.get(i));
                else
                    normalPhotos.add(myPhotos.get(i));
            }
            System.out.println("全部:" + myPhotos.size());
            System.out.println("風景相:" + landscapes.size());
            System.out.println("獨照:" + photoWithOneFace.size());
            System.out.println("人相:" + normalPhotos.size());
            System.out.println("大合照:" + groupPhoto.size());

            //   write photos in device storage and set path , mat for each photo
            for (int i =0 ; i< myPhotos.size() ; i++){
                byte[] decodedByte = Base64.decode(myPhotos.get(i).getPhotoString(), 0);

                //set path in photo
                //SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                //String currentDateandTime = sdf.format(new Date());
                String newPath = getBitmapPath(decodedByte, "iv_" + i);
                myPhotos.get(i).setPhotoPath(newPath);
                System.out.println("set path in photo is done" );

                //set mat in photo
                opencv_core.Mat mat = imread(newPath);
                myPhotos.get(i).setMat(mat);
                System.out.println("set Mat in photo is done");

                completed = (int)(( (float)i/(float)myPhotos.size())*100);
                handler.post(new Runnable() {
                    public void run() {
                        progressBar.setProgress(completed);
                        statusText.setText(String.format("Completed %d", completed));
                    }
                });
            }

            sortByLevelOfSmile(photoWithOneFace);
            sortByLevelOfSmile(normalPhotos);

            myPhotosWithOrder.addAll(landscapes);
            myPhotosWithOrder.addAll(photoWithOneFace);
            myPhotosWithOrder.addAll(normalPhotos);
            myPhotosWithOrder.addAll(sortByLevelOfSmile(groupPhoto));

            handler.post(new Runnable() {
                public void run() {
                    progressBar.setProgress(100);
                    statusText.setText(String.format("Completed %d", 100));
                }
            });

            Thread make_video_thread = new Thread(make_video_worker);
            make_video_thread.start();
        }
    };

    private Runnable make_video_worker = new Runnable() {
        public  void run() {
            completed = 0;

            switch (firstTemplateDecision){
                case 0: break;
                case 1: switch (secondTemplateDecision){
                            case 0: images.add(imread("/sdcard/MemorVi/template/christmas1_1.jpg"));
                                    if (eventName.length()%2 == 0){
                                        putText(images.get(0), eventName, new Point(280-(eventName.length()/2)*13,350), 7, 1.5, new opencv_core.Scalar(0, 0, 255, 0));
                                    }
                                    else {
                                        putText(images.get(0), eventName, new Point(280-(eventName.length()/2)*25,350), 7, 1.5, new opencv_core.Scalar(0, 0, 255, 0));
                                    }
                                    break;
                            case 1: images.add(imread("/sdcard/MemorVi/template/christmas2_1.jpg"));
                                    if (eventName.length()%2 == 0){
                                        putText(images.get(0), eventName, new Point(280-(eventName.length()/2)*18,560), 3, 1.5, new opencv_core.Scalar(0, 0, 0, 0));
                                    }
                                    else {
                                        putText(images.get(0), eventName, new Point(280-(eventName.length()/2)*30,560), 3, 1.5, new opencv_core.Scalar(0, 0, 0, 0));
                                    }
                                    break;
                        }
                        break;
                case 2: switch (secondTemplateDecision){
                            case 0: images.add(imread("/sdcard/MemorVi/template/wedding1_1.jpg"));
                                    if (eventName.length()%2 == 0){
                                        putText(images.get(0), eventName, new Point(80,65), 6, 2, new opencv_core.Scalar(0, 0, 255, 0));
                                    }
                                    else {
                                        putText(images.get(0), eventName, new Point(80,65), 6, 2, new opencv_core.Scalar(0, 0, 255, 0));
                                    }
                                    break;
                            case 1: images.add(imread("/sdcard/MemorVi/template/wedding2_1.jpg"));
                                    if (eventName.length()%2 == 0){
                                        putText(images.get(0), eventName, new Point(300-(eventName.length()/2)*30,320), 3, 2, new opencv_core.Scalar(0, 100, 100, 0));
                                    }
                                    else {
                                        putText(images.get(0), eventName, new Point(300-(eventName.length()/2)*45,320), 3, 2, new opencv_core.Scalar(0, 100, 100, 0));
                                    }
                                    break;
                        }
                        break;
                case 3: switch(secondTemplateDecision){
                            case 0: images.add(imread("/sdcard/MemorVi/template/love1_1.jpg"));
                                    break;
                            case 1: images.add(imread("/sdcard/MemorVi/template/love2_1.jpg"));
                                    break;
                        }
                        break;
                case 4: images.add(imread("/sdcard/MemorVi/template/energetic1.jpg"));
                        break;
            }

            for (int k = 0 ; k< myPhotosWithOrder.size(); k++){
                switch (firstTemplateDecision){
                    case 0: break;
                    case 1: switch (secondTemplateDecision){
                                case 0: if (k == myPhotosWithOrder.size()/2){
                                            images.add(imread("sdcard/MemorVi/template/christmas1_2.jpg"));
                                        }
                                        if (k == myPhotosWithOrder.size()-groupPhoto.size()-1){
                                            images.add(imread("/sdcard/MemorVi/template/christmas1_3.jpg"));
                                        }
                                        break;
                                case 1: if (k == myPhotosWithOrder.size()/2){
                                            images.add(imread("sdcard/MemorVi/template/christmas2_2.jpg"));
                                        }
                                        if (k == myPhotosWithOrder.size()-groupPhoto.size()-1){
                                            images.add(imread("/sdcard/MemorVi/christmas2_3.jpg"));
                                        }
                                        break;
                            }
                            break;
                    case 2: switch (secondTemplateDecision){
                                case 0: if (k == myPhotosWithOrder.size()/3){
                                            images.add(imread("sdcard/MemorVi/template/wedding1_2.jpg"));
                                        }
                                        if (k == myPhotosWithOrder.size()/3*2){
                                            images.add(imread("sdcard/MemorVi/template/wedding1_3.jpg"));
                                        }
                                        if (k == myPhotosWithOrder.size()-groupPhoto.size()-1){
                                            images.add(imread("/sdcard/MemorVi/template/wedding1_4.jpg"));
                                        }
                                        break;
                                case 1: if (k == myPhotosWithOrder.size()/3){
                                            images.add(imread("sdcard/MemorVi/template/wedding2_2.jpg"));
                                        }
                                        if (k == myPhotosWithOrder.size()/3*2){
                                            images.add(imread("sdcard/MemorVi/template/wedding2_3.jpg"));
                                        }
                                        if (k == myPhotosWithOrder.size()-groupPhoto.size()-1){
                                            images.add(imread("/sdcard/MemorVi/template/wedding2_4.jpg"));
                                        }
                                        break;
                            }
                            break;
                    case 3: switch(secondTemplateDecision){
                                case 0: if (k == myPhotosWithOrder.size()/2)
                                            images.add(imread("sdcard/MemorVi/template/love1_2.jpg"));
                                        if (k == myPhotosWithOrder.size()-groupPhoto.size()-1)
                                            images.add(imread("/sdcard/MemorVi/template/love1_3.jpg"));
                                        break;
                                case 1: if (k == myPhotosWithOrder.size()/2)
                                            images.add(imread("sdcard/MemorVi/template/love2_2.jpg"));
                                        if (k == myPhotosWithOrder.size()-groupPhoto.size()-1)
                                            images.add(imread("/sdcard/MemorVi/template/love2_3.jpg"));
                                        break;
                            }
                            break;
                    case 4: if (k == myPhotosWithOrder.size()/2)
                                images.add(imread("/sdcard/MemorVi/template/energetic2.jpg"));
                            if (k == myPhotosWithOrder.size()-groupPhoto.size()-1)
                                images.add(imread("/sdcard/MemorVi/template/energetic3.jpg"));
                            break;
                }
                opencv_core.Mat m = myPhotosWithOrder.get(k).getMat();
                images.add(m);
            }

            // add background
            opencv_core.Mat black = imread("/sdcard/MemorVi/template/black.jpg");
            for (int k = 0 ; k<images.size(); k++){
                imagesWithBackground.add(addBackground(images.get(k), black));
                imagesWithEffect1.add(addBackground(images.get(k), black));
                imagesWithEffect2.add(addBackground(images.get(k), black));
                imagesWithEffect3.add(addBackground(images.get(k), black));
            }

            switch (effectDecision){
                case 0: {
                    break;
                }
                case 1: {
                    for (int i=0; i<images.size(); i++){
                        int typeOfPhoto = checkPhotoType(images.get(i).cols(), images.get(i).rows());
                        setZoom(typeOfPhoto);

                        for (int k=0; k<=40; k++){
                            opencv_core.Mat temp = images.get(i).clone();
                            if (k==37 && i!=images.size()-1){
                                int typeOfPhoto2 = checkPhotoType(images.get(i+1).cols(), images.get(i+1).rows());
                                switch (typeOfPhoto2){
                                    case 1: {
                                        addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.8, customZoom(images.get(i+1), black, 640, 480, 160, 120), 0.2, 0.0, temp);
                                        break;
                                    }
                                    case 2: {
                                        addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.8, customZoom(images.get(i+1), black, 360, 480, 300, 120), 0.2, 0.0, temp);
                                        break;
                                    }
                                    case 3: {
                                        addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.8, customZoom(images.get(i+1), black, 600, 600, 180, 60), 0.2, 0.0, temp);
                                        break;
                                    }
                                }
                                zooming.add(temp);
                            }
                            else if (k==38 && i!=images.size()-1){
                                int typeOfPhoto2 = checkPhotoType(images.get(i+1).cols(), images.get(i+1).rows());
                                switch (typeOfPhoto2){
                                    case 1: {
                                        addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.6, customZoom(images.get(i+1), black, 648, 486, 156, 117), 0.4, 0.0, temp);
                                        break;
                                    }
                                    case 2: {
                                        addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.6, customZoom(images.get(i+1), black, 364, 486, 298, 117), 0.4, 0.0, temp);
                                        break;
                                    }
                                    case 3: {
                                        addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.6, customZoom(images.get(i+1), black, 603, 603, 179, 59), 0.4, 0.0, temp);
                                        break;
                                    }
                                }
                                zooming.add(temp);
                            }
                            else if (k==39 && i!=images.size()-1){
                                int typeOfPhoto2 = checkPhotoType(images.get(i+1).cols(), images.get(i+1).rows());
                                switch (typeOfPhoto2){
                                    case 1: {
                                        addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.4, customZoom(images.get(i+1), black, 656, 492, 152, 114), 0.6, 0.0, temp);
                                        break;
                                    }
                                    case 2: {
                                        addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.4, customZoom(images.get(i+1), black, 368, 492, 296, 114), 0.6, 0.0, temp);
                                        break;
                                    }
                                    case 3: {
                                        addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.4, customZoom(images.get(i+1), black, 606, 606, 178, 58), 0.6, 0.0, temp);
                                        break;
                                    }
                                }
                                zooming.add(temp);
                            }
                            else if (k==40 && i!=images.size()-1){
                                int typeOfPhoto2 = checkPhotoType(images.get(i+1).cols(), images.get(i+1).rows());
                                switch (typeOfPhoto2){
                                    case 1: {
                                        addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.2, customZoom(images.get(i+1), black, 664, 498, 148, 111), 0.8, 0.0, temp);
                                        break;
                                    }
                                    case 2: {
                                        addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.2, customZoom(images.get(i+1), black, 372, 498, 294, 111), 0.8, 0.0, temp);
                                        break;
                                    }
                                    case 3: {
                                        addWeighted(zoom(images.get(i), black, typeOfPhoto), 0.2, customZoom(images.get(i+1), black, 609, 609, 177, 57), 0.8, 0.0, temp);
                                        break;
                                    }
                                }
                                zooming.add(temp);
                            }
                            else {
                                if (k>=0 && k<4 && i!=0){
                                    zoom(images.get(i), black, typeOfPhoto);
                                }
                                else {
                                    zooming.add(zoom(images.get(i), black, typeOfPhoto));
                                }
                            }
                        }
                    }
                    break;
                }
                case 2: {
                    int count = images.size();
                    for (int i=0; i<images.size();){
                        int randomNumber = (int) (random()*100) % 8;
                        if (count < 4){
                            multipleInOne.add(addBackground(images.get(i++), black));
                            count--;
                        }
                        else {
                            switch (randomNumber) {
                                case 0: {
                                    if (count >= 4) {
                                        multipleInOne.add(combine_style_1a(images.get(i++), images.get(i++), images.get(i++), images.get(i++)));
                                        count -= 4;
                                    }
                                    break;
                                }
                                case 1: {
                                    if (count >= 4) {
                                        multipleInOne.add(combine_style_1b(images.get(i++), images.get(i++), images.get(i++), images.get(i++)));
                                        count -= 4;
                                    }
                                    break;
                                }
                                case 2: {
                                    if (count >= 5) {
                                        multipleInOne.add(combine_style_2a(images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++)));
                                        count -= 5;
                                    }
                                    break;
                                }
                                case 3: {
                                    if (count >= 5) {
                                        multipleInOne.add(combine_style_2b(images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++)));
                                        count -= 5;
                                    }
                                    break;
                                }
                                case 4: {
                                    if (count >= 5) {
                                        multipleInOne.add(combine_style_3a(images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++)));
                                        count -= 5;
                                    }
                                    break;
                                }
                                case 5: {
                                    if (count >= 5) {
                                        multipleInOne.add(combine_style_3b(images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++)));
                                        count -= 5;
                                    }
                                    break;
                                }
                                case 6: {
                                    if (count >= 4) {
                                        multipleInOne.add(combine_style_4(images.get(i++), images.get(i++), images.get(i++), images.get(i++)));
                                        count -= 4;
                                    }
                                    break;
                                }
                                case 7: {
                                    if (count >= 9) {
                                        multipleInOne.add(combine_style_5(images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++), images.get(i++)));
                                        count -= 9;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
            }

            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(getActivity(), "Making Video", Toast.LENGTH_LONG).show();
                    progressBar.setProgress(completed);
                    statusText.setText(String.format("Completed %d", completed));
                }
            });

            makevideo = new File("/sdcard/MemorVi/" + eventObjectId + "/makevideo.mp4");
            combine = new File("/sdcard/MemorVi/" + eventObjectId + "/combine.mp4");

            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(makevideo.getAbsolutePath(), 640, 480,1);
            Frame captured_frame;
            try {
                recorder.setFrameRate(20);
                transitionFrameDuration = 4;
                mainFrameDuration = 80;
                recorder.start();
                if (effectDecision == 0){
                    for (int i = 0; i < imagesWithBackground.size(); i++) {
                        captured_frame = converter.convert(imagesWithBackground.get(i));
                        for(int j =0 ; j<mainFrameDuration; j++) {
                            recorder.record(captured_frame);
                        }
                        completed = (int)(( (float)i/(float) imagesWithBackground.size())*100);
                        handler.post(new Runnable() {
                            public void run() {
                                progressBar.setProgress(completed);
                                statusText.setText(String.format("Completed %d", completed));
                            }
                        });
                    }
                }
                else if (effectDecision == 1){
                    for (int i = 0; i < zooming.size(); i++) {
                        captured_frame = converter.convert(zooming.get(i));
                        for (int j=0; j<transitionFrameDuration; j++){
                            recorder.record(captured_frame);
                        }

                        completed = (int)(( (float)i/(float) zooming.size())*100);
                        handler.post(new Runnable() {
                            public void run() {
                                progressBar.setProgress(completed);
                                statusText.setText(String.format("Completed %d", completed));
                            }
                        });
                    }
                }
                else if (effectDecision == 2){
                    for (int i=0; i<multipleInOne.size(); i++){
                        captured_frame = converter.convert(multipleInOne.get(i));
                        for (int j=0; j<mainFrameDuration; j++){
                            recorder.record(captured_frame);
                        }
                        completed = (int)(( (float)i/(float) multipleInOne.size())*100);
                        handler.post(new Runnable() {
                            public void run() {
                                progressBar.setProgress(completed);
                                statusText.setText(String.format("Completed %d", completed));
                            }
                        });
                    }
                }
                /*if (effectDecision == 0 || effectDecision == 1){
                    for (int i = 0; i < imagesWithBackground.size(); i++) {
                        captured_frame = converter.convert(imagesWithEffect3.get(i));
                        for (int j=0; j<transitionFrameDuration; j++){
                            recorder.record(captured_frame);
                        }
                        captured_frame = converter.convert(imagesWithEffect2.get(i));
                        for (int j=0; j<transitionFrameDuration; j++){
                            recorder.record(captured_frame);
                        }
                        captured_frame = converter.convert(imagesWithEffect1.get(i));
                        for (int j=0; j<transitionFrameDuration; j++){
                            recorder.record(captured_frame);
                        }
                        captured_frame = converter.convert(imagesWithBackground.get(i));
                        for(int j =0 ; j<mainFrameDuration; j++) {
                            recorder.record(captured_frame);
                        }
                        captured_frame = converter.convert(imagesWithEffect1.get(i));
                        for (int j=0; j<transitionFrameDuration; j++){
                            recorder.record(captured_frame);
                        }
                        captured_frame = converter.convert(imagesWithEffect2.get(i));
                        for (int j=0; j<transitionFrameDuration; j++){
                            recorder.record(captured_frame);
                        }
                        captured_frame = converter.convert(imagesWithEffect3.get(i));
                        for (int j=0; j<transitionFrameDuration; j++){
                            recorder.record(captured_frame);
                        }
                        completed = (int)(( (float)i/(float) imagesWithBackground.size())*100);
                        handler.post(new Runnable() {
                            public void run() {
                                progressBar.setProgress(completed);
                                statusText.setText(String.format("Completed %d", completed));
                            }
                        });
                    }
                }
                else if (effectDecision == 2){
                    for (int i = 0; i < imagesWithBackground.size(); i++) {
                        captured_frame = converter.convert(imagesWithBackground.get(i));
                        for(int j =0 ; j<mainFrameDuration ; j++) {
                            recorder.record(captured_frame);
                        }
                        captured_frame = converter.convert(imagesWithEffect1.get(i));
                        for (int j=0; j<transitionFrameDuration; j++){
                            recorder.record(captured_frame);
                        }
                        captured_frame = converter.convert(imagesWithEffect2.get(i));
                        for (int j=0; j<transitionFrameDuration; j++){
                            recorder.record(captured_frame);
                        }
                        captured_frame = converter.convert(imagesWithEffect3.get(i));
                        for (int j=0; j<transitionFrameDuration; j++){
                            recorder.record(captured_frame);
                        }
                        completed = (int)(( (float)i/(float) imagesWithBackground.size())*100);
                        handler.post(new Runnable() {
                            public void run() {
                                progressBar.setProgress(completed);
                                statusText.setText(String.format("Completed %d", completed));
                            }
                        });
                    }
                }
                else if (effectDecision == 3){
                    for (int i = 0; i < zooming.size(); i++) {
                        captured_frame = converter.convert(zooming.get(i));
                        for (int j=0; j<transitionFrameDuration; j++){
                            recorder.record(captured_frame);
                        }

                        completed = (int)(( (float)i/(float) zooming.size())*100);
                        handler.post(new Runnable() {
                            public void run() {
                                progressBar.setProgress(completed);
                                statusText.setText(String.format("Completed %d", completed));
                            }
                        });
                    }
                }
                else if (effectDecision == 4){
                    for (int i=0; i<multipleInOne.size(); i++){
                        captured_frame = converter.convert(multipleInOne.get(i));
                        for (int j=0; j<mainFrameDuration; j++){
                            recorder.record(captured_frame);
                        }

                        completed = (int)(( (float)i/(float) multipleInOne.size())*100);
                        handler.post(new Runnable() {
                            public void run() {
                                progressBar.setProgress(completed);
                                statusText.setText(String.format("Completed %d", completed));
                            }
                        });
                    }
                }*/
                recorder.stop();
                recorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.post(new Runnable() {
                public void run() {
                    progressBar.setProgress(100);
                    statusText.setText(String.format("Completed %d", 100));
                    Thread combine_thread = new Thread(combine_worker);
                    combine_thread.start();
                }
            });
        }
    };

    private Runnable combine_worker = new Runnable() {
        File audio;
        public void run() {
            completed = 0;
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(getActivity(), "Adding Sound", Toast.LENGTH_LONG).show();
                    progressBar.setProgress(completed);
                    statusText.setText(String.format("Completed %d", completed));
                }
            });
            int randomNumber = (int) (random()*100);
            switch (firstTemplateDecision){
                case 0: randomNumber %= 3;
                        if (randomNumber == 0)
                            audio = new File("/sdcard/Download/general1_1.mp3");
                        if (randomNumber == 1)
                            audio = new File("/sdcard/Download/general1_2.mp3");
                        if (randomNumber == 2)
                            audio = new File("/sdcard/Download/general1_3.mp3");
                        break;
                case 1: switch(secondTemplateDecision){
                            case 0: randomNumber %= 3;
                                    if (randomNumber == 0)
                                        audio = new File("/sdcard/Download/christmas1_1.mp3");
                                    if (randomNumber == 1)
                                        audio = new File("/sdcard/Download/christmas1_2.mp3");
                                    if (randomNumber == 2)
                                        audio = new File("/sdcard/Download/christmas1_3.mp3");
                                    break;
                            case 1: audio = new File("/sdcard/Download/christmas2.mp3");
                                    break;
                        }
                        break;
                case 2: switch(secondTemplateDecision){
                            case 0: randomNumber %= 2;
                                    if (randomNumber == 0)
                                        audio = new File("/sdcard/Download/wedding1_1.mp3");
                                    if (randomNumber == 1)
                                        audio = new File("/sdcard/Download/wedding1_2.mp3");
                                    break;
                            case 1: randomNumber %= 2;
                                    if (randomNumber == 0)
                                        audio = new File("/sdcard/Download/wedding2_1.mp3");
                                    if (randomNumber == 1)
                                        audio = new File("/sdcard/Download/wedding2_2.mp3");
                                    break;
                        }
                        break;
                case 3: switch(secondTemplateDecision){
                            case 0: randomNumber %= 2;
                                    if (randomNumber == 0)
                                        audio = new File("/sdcard/Download/general_love1_1.mp3");
                                    if (randomNumber == 1)
                                        audio = new File("/sdcard/Download/general_love1_2.mp3");
                                    break;
                            case 1: audio = new File("/sdcard/Download/classical_love2.mp3");
                                    break;
                        }
                        break;
                case 4: randomNumber %= 2;
                        if (randomNumber == 0)
                            audio = new File("/sdcard/Download/energetic1_1.mp3");
                        if (randomNumber == 1)
                            audio = new File("/sdcard/Download/energetic1_2.mp3");
                        break;
            }
            FFmpegFrameGrabber grabber1 = new FFmpegFrameGrabber(makevideo.getAbsolutePath());
            FFmpegFrameGrabber grabber2 = new FFmpegFrameGrabber(audio.getAbsolutePath());
            Frame video_frame = null;
            Frame audio_frame = null;
            FFmpegFrameRecorder recorder;
            try {
                grabber1.start();
                grabber2.start();
                final int totalFrame = grabber1.getLengthInFrames();
                int addedFrame = 0;
                recorder = new FFmpegFrameRecorder(combine.getAbsolutePath(), grabber1.getImageWidth(), grabber1.getImageHeight(), grabber2.getAudioChannels());
                recorder.setFrameRate(grabber1.getFrameRate() * 2);
                //recorder.setSampleRate(grabber2.getSampleRate());
                //recorder.setFormat("mp4");
                recorder.setSampleRate(grabber2.getSampleRate());
                //recorder.setVideoBitrate(192000); // set 太底會濛
                recorder.start();
                //(video_frame = grabber1.grabFrame(true,true,true,false)
                //while(((video_frame = grabber1.grabFrame())!= null) && ((audio_frame = grabber2.grabFrame())!= null)){
                while (addedFrame < totalFrame) {
                    audio_frame = grabber2.grabFrame();
                    video_frame = grabber1.grabFrame();
                    recorder.record(video_frame);
                    recorder.record(audio_frame);
                    addedFrame++;
                    completed = (int) (((float) addedFrame / (float) totalFrame) * 100);
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(completed);
                            statusText.setText(String.format("Completed %d", completed));
                        }
                    });
                }
                grabber1.stop();
                grabber2.stop();
                recorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.post(new Runnable() {
                public void run() {
                    progressBar.setProgress(100);
                    statusText.setText(String.format("Completed %d", 100));
                    playVideoOnView(combine);
                }
            });
        }
    };


    private ArrayList<Photo> sortByLevelOfSmile (ArrayList<Photo> photos){
        Collections.sort(photos);
        return photos;
    }

    private opencv_core.Mat addBackground ( opencv_core.Mat image , opencv_core.Mat background) {
        resize(background, background, new opencv_core.Size(640, 480));
        resize(image, image, new opencv_core.Size(620, 460));
        opencv_core.Mat tempBlackGround = background.clone();
        image.copyTo(tempBlackGround.rowRange(10, 470).colRange(10,630));
        return tempBlackGround;
    }

    private int checkPhotoType(int width, int height){
        if (width > height)
            return 1;
        else if (width < height)
            return 2;
        else
            return 3;
    }

    private opencv_core.Mat move (opencv_core.Mat image, opencv_core.Mat background, int startingColumn, int startingRow){
        resize(background, background, new opencv_core.Size(960, 720));
        opencv_core.Mat tempBlackGround = background.clone();
        image.copyTo(tempBlackGround.colRange(startingColumn, startingColumn + image.cols()).rowRange(startingRow, startingRow + image.rows()));
        return tempBlackGround;
    }

    private opencv_core.Mat zoom (opencv_core.Mat image, opencv_core.Mat background, int typeOfPhoto){
        resize(background, background, new opencv_core.Size(960, 720));
        opencv_core.Mat tempBlackGround = background.clone();
        opencv_core.Mat imageCopy = image.clone();
        resize(imageCopy, imageCopy, new opencv_core.Size(zoomWidth, zoomHeight));
        imageCopy.copyTo(tempBlackGround.colRange(zoomStartingColumn, zoomStartingColumn + zoomWidth).rowRange(zoomStartingRow, zoomStartingRow + zoomHeight));
        changeZoom(typeOfPhoto);
        return tempBlackGround;
    }

    private opencv_core.Mat customZoom(opencv_core.Mat image, opencv_core.Mat background, int width, int height, int startingColumn, int startingRow){
        resize(background, background, new opencv_core.Size(960, 720));
        opencv_core.Mat tempBlackGround = background.clone();
        opencv_core.Mat imageCopy = image.clone();
        resize(imageCopy, imageCopy, new opencv_core.Size(width, height));
        imageCopy.copyTo(tempBlackGround.colRange(startingColumn, startingColumn + width).rowRange(startingRow, startingRow + height));
        return tempBlackGround;
    }

    private void changeZoom(int typeOfPhoto){
        switch (typeOfPhoto){
            case 1: {
                zoomWidth+=8;
                zoomHeight+=6;
                zoomStartingColumn-=4;
                zoomStartingRow-=3;
                break;
            }
            case 2: {
                zoomWidth+=4;
                zoomHeight+=6;
                zoomStartingColumn-=2;
                zoomStartingRow-=3;
                break;
            }
            case 3: {
                zoomWidth+=3;
                zoomHeight+=3;
                zoomStartingColumn-=1;
                zoomStartingRow-=1;
                break;
            }
        }
    }

    private void setZoom(int typeOfPhoto){
        switch (typeOfPhoto){
            case 1: {
                zoomWidth = 640;
                zoomHeight = 480;
                zoomStartingColumn = 160;
                zoomStartingRow = 120;
                break;
            }
            case 2: {
                zoomWidth = 360;
                zoomHeight = 480;
                zoomStartingColumn = 300;
                zoomStartingRow = 120;
                break;
            }
            case 3: {
                zoomWidth = 600;
                zoomHeight = 600;
                zoomStartingColumn = 180;
                zoomStartingRow = 60;
                break;
            }
        }
    }

    private opencv_core.Mat combine_style_1a (opencv_core.Mat a, opencv_core.Mat b, opencv_core.Mat c, opencv_core.Mat d){
        opencv_core.Mat black = imread("/sdcard/MemorVi/template/black.jpg");
        opencv_core.Mat temp = black.clone();
        resize(temp, temp, new opencv_core.Size(960, 720));
        resize(a, a, new opencv_core.Size(310, 230));
        resize(b, b, new opencv_core.Size(310, 230));
        resize(c, c, new opencv_core.Size(310, 230));
        resize(d, d, new opencv_core.Size(630, 710));
        a.copyTo(temp.rowRange(5, 235).colRange(5, 315));
        b.copyTo(temp.rowRange(245, 475).colRange(5, 315));
        c.copyTo(temp.rowRange(485, 715).colRange(5, 315));
        d.copyTo(temp.rowRange(5, 715).colRange(325, 955));
        imwrite("/sdcard/DCIM/Camera//" + "testinginging.jpg", black);
        return temp;
    }


    private opencv_core.Mat combine_style_1b (opencv_core.Mat a, opencv_core.Mat b, opencv_core.Mat c, opencv_core.Mat d){
        opencv_core.Mat black = imread("/sdcard/MemorVi/template/black.jpg");
        opencv_core.Mat temp = black.clone();
        resize(temp, temp, new opencv_core.Size(960, 720));
        resize(a, a, new opencv_core.Size(310, 230));
        resize(b, b, new opencv_core.Size(310, 230));
        resize(c, c, new opencv_core.Size(310, 230));
        resize(d, d, new opencv_core.Size(630, 710));
        a.copyTo(temp.rowRange(5, 235).colRange(645, 955));
        b.copyTo(temp.rowRange(245, 475).colRange(645, 955));
        c.copyTo(temp.rowRange(485, 715).colRange(645, 955));
        d.copyTo(temp.rowRange(5, 715).colRange(5, 635));
        imwrite("/sdcard/DCIM/Camera//"+"testinginging.jpg", black);
        return temp;
    }

    private opencv_core.Mat combine_style_2a (opencv_core.Mat a, opencv_core.Mat b, opencv_core.Mat c , opencv_core.Mat d, opencv_core.Mat e){
        opencv_core.Mat black = imread("/sdcard/MemorVi/template/black.jpg");
        opencv_core.Mat temp = black.clone();
        resize(temp, temp, new opencv_core.Size(960, 720));
        resize(a, a, new opencv_core.Size(310, 230));
        resize(b, b, new opencv_core.Size(310, 230));
        resize(c, c, new opencv_core.Size(310, 230));
        resize(d, d, new opencv_core.Size(630, 350));
        resize(e, e, new opencv_core.Size(630, 350));
        a.copyTo(temp.rowRange(5, 235).colRange(5, 315));
        b.copyTo(temp.rowRange(245, 475).colRange(5, 315));
        c.copyTo(temp.rowRange(485, 715).colRange(5, 315));
        d.copyTo(temp.rowRange(5, 355).colRange(325, 955));
        e.copyTo(temp.rowRange(365, 715).colRange(325, 955));
        imwrite("/sdcard/DCIM/Camera//"+"testinginging.jpg", black);
        return temp;
    }


    private opencv_core.Mat combine_style_2b (opencv_core.Mat a, opencv_core.Mat b, opencv_core.Mat c , opencv_core.Mat d, opencv_core.Mat e){
        opencv_core.Mat black = imread("/sdcard/MemorVi/template/black.jpg");
        opencv_core.Mat temp = black.clone();
        resize(temp, temp, new opencv_core.Size(960, 720));
        resize(a, a, new opencv_core.Size(310, 230));
        resize(b, b, new opencv_core.Size(310, 230));
        resize(c, c, new opencv_core.Size(310, 230));
        resize(d, d, new opencv_core.Size(630, 350));
        resize(e, e, new opencv_core.Size(630, 350));
        a.copyTo(temp.rowRange(5, 235).colRange(645, 955));
        b.copyTo(temp.rowRange(245, 475).colRange(645, 955));
        c.copyTo(temp.rowRange(485, 715).colRange(645, 955));
        d.copyTo(temp.rowRange(5, 355).colRange(5, 635));
        e.copyTo(temp.rowRange(365, 715).colRange(5, 635));
        imwrite("/sdcard/DCIM/Camera//"+"testinginging.jpg", black);
        return temp;
    }

    private opencv_core.Mat combine_style_3a (opencv_core.Mat a, opencv_core.Mat b, opencv_core.Mat c , opencv_core.Mat d, opencv_core.Mat e){
        opencv_core.Mat black = imread("/sdcard/MemorVi/template/black.jpg");
        opencv_core.Mat temp = black.clone();
        resize(temp, temp, new opencv_core.Size(960, 720));
        resize(a, a, new opencv_core.Size(450, 370));
        resize(b, b, new opencv_core.Size(450, 370));
        resize(c, c, new opencv_core.Size(300, 250));
        resize(d, d, new opencv_core.Size(300, 250));
        resize(e, e, new opencv_core.Size(300, 250));
        a.copyTo(temp.rowRange(20, 390).colRange(5, 455));
        b.copyTo(temp.rowRange(20, 390).colRange(485, 935));
        c.copyTo(temp.rowRange(420, 670).colRange(5, 305));
        d.copyTo(temp.rowRange(420, 670).colRange(325, 625));
        e.copyTo(temp.rowRange(420, 670).colRange(645, 945));
        imwrite("/sdcard/DCIM/Camera//"+"testinginging.jpg", black);
        return temp;
    }


    private opencv_core.Mat combine_style_3b (opencv_core.Mat a, opencv_core.Mat b, opencv_core.Mat c , opencv_core.Mat d, opencv_core.Mat e){
        opencv_core.Mat black = imread("/sdcard/MemorVi/template/black.jpg");
        opencv_core.Mat temp = black.clone();
        resize(temp, temp, new opencv_core.Size(960, 720));
        resize(a, a, new opencv_core.Size(450, 370));
        resize(b, b, new opencv_core.Size(450, 370));
        resize(c, c, new opencv_core.Size(300, 250));
        resize(d, d, new opencv_core.Size(300, 250));
        resize(e, e, new opencv_core.Size(300, 250));
        a.copyTo(temp.rowRange(305, 675).colRange(5, 455));
        b.copyTo(temp.rowRange(305, 675).colRange(485, 935));
        c.copyTo(temp.rowRange(20, 270).colRange(5, 305));
        d.copyTo(temp.rowRange(20, 270).colRange(325, 625));
        e.copyTo(temp.rowRange(20, 270).colRange(645, 945));
        imwrite("/sdcard/DCIM/Camera//"+"testinginging.jpg", black);
        return temp;
    }

    private opencv_core.Mat combine_style_4 (opencv_core.Mat a, opencv_core.Mat b, opencv_core.Mat c, opencv_core.Mat d){
        opencv_core.Mat black = imread("/sdcard/MemorVi/template/black.jpg");
        opencv_core.Mat temp = black.clone();
        resize(temp, temp, new opencv_core.Size(960, 720));
        resize(a, a, new opencv_core.Size(470, 350));
        resize(b, b, new opencv_core.Size(470, 350));
        resize(c, c, new opencv_core.Size(470, 350));
        resize(d, d, new opencv_core.Size(470, 350));
        a.copyTo(temp.rowRange(5, 355).colRange(5, 475));
        b.copyTo(temp.rowRange(5, 355).colRange(485, 955));
        c.copyTo(temp.rowRange(365, 715).colRange(5, 475));
        d.copyTo(temp.rowRange(365, 715).colRange(485, 955));
        imwrite("/sdcard/DCIM/Camera//"+"testinginging.jpg", black);
        return temp;
    }

    private opencv_core.Mat combine_style_5 (opencv_core.Mat a, opencv_core.Mat b, opencv_core.Mat c, opencv_core.Mat d, opencv_core.Mat e, opencv_core.Mat f, opencv_core.Mat g, opencv_core.Mat h,opencv_core.Mat i){
        opencv_core.Mat black = imread("/sdcard/MemorVi/template/black.jpg");
        opencv_core.Mat temp = black.clone();
        resize(temp, temp, new opencv_core.Size(960, 720));
        resize(a, a, new opencv_core.Size(310, 230));
        resize(b, b, new opencv_core.Size(310, 230));
        resize(c, c, new opencv_core.Size(310, 230));
        resize(d, d, new opencv_core.Size(310, 230));
        resize(e, e, new opencv_core.Size(310, 230));
        resize(f, f, new opencv_core.Size(310, 230));
        resize(g, g, new opencv_core.Size(310, 230));
        resize(h, h, new opencv_core.Size(310, 230));
        resize(i, i, new opencv_core.Size(310, 230));
        a.copyTo(temp.rowRange(5, 235).colRange(5, 315));
        b.copyTo(temp.rowRange(5, 235).colRange(325, 635));
        c.copyTo(temp.rowRange(5, 235).colRange(645, 955));
        d.copyTo(temp.rowRange(245, 475).colRange(5, 315));
        e.copyTo(temp.rowRange(245, 475).colRange(325, 635));
        f.copyTo(temp.rowRange(245, 475).colRange(645, 955));
        g.copyTo(temp.rowRange(485,715).colRange(5 ,315));
        h.copyTo(temp.rowRange(485,715).colRange(325, 635));
        i.copyTo(temp.rowRange(485,715).colRange(645, 955));
        imwrite("/sdcard/DCIM/Camera//"+"testinginging.jpg", black);
        return temp;
    }

    private void playVideoOnView(File video) {
        videoview.setVideoPath(video.getAbsolutePath());
        videoview.setMediaController(new MediaController(Tab2.this.getContext()));
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int h = displaymetrics.heightPixels;
        int w = displaymetrics.widthPixels;
        videoview.setMinimumHeight(h*3);
        videoview.setMinimumWidth(w * 4);
        videoview.start();
    }

    private String getBitmapPath(byte[] bitmapArray , String filename){
        File f = new File("/sdcard/MemorVi/"+eventObjectId+"/", filename+".jpg");
        if (!f.exists()){
            f.getParentFile().mkdirs();
        }
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
//write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            fos.write(bitmapArray);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  f.getAbsolutePath();
    }

    //  The functions  below have not been tested  **********

    private void insertInbetween( ArrayList<Photo> first, ArrayList<Photo> second  ){
        Random rand = new Random();
        final int startAt = 5;
        final int range = first.size()-startAt*2;
        for (int i =0 ; i< second.size() ; i++ ) {
            int rnd = rand.nextInt(range) + startAt;
            first.add(rnd, second.get(i));
        }
    }

    // can not use so Far
    private ArrayList<Photo> blurring (Photo toBlur){
        ArrayList<Photo> blurPhotos = new ArrayList<Photo>();
        opencv_core.Mat toBlurMat = new opencv_core.Mat(toBlur.getMat().rows(), toBlur.getMat().cols());
        int v = 99;
        for (int j = 0; j<5 ; j++) {
            opencv_core.Mat temp = new opencv_core.Mat();
            GaussianBlur(toBlurMat, temp, new opencv_core.Size(v, v), 50);

            Photo p = new Photo ();
            p.setMat(temp);
            blurPhotos.add(p);
            v = v-22;
        }
        return blurPhotos;
    }

    public byte[] videoTobyte(File video) {
        String encodedBase64 = null;
        byte[] bytes = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(video);
            bytes = new byte[(int) video.length()];
            fileInputStream.read(bytes);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }


    public File byteToVideo(byte[] b){
        File outfile = new File("/sdcard/MemorVi/"+eventObjectId+"/byteToVideo.mp4");
        try {
            FileOutputStream os = new FileOutputStream(outfile);
            os.write(b);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outfile;
    }

    private ArrayList<opencv_core.Mat> combineIntoOne( ArrayList<opencv_core.Mat> inputArray){
        ArrayList<opencv_core.Mat> temp = new ArrayList<opencv_core.Mat>();

        return temp;
    }
}