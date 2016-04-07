package com.example.onzzz.i2v;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    String userObjectId;
    String eventObjectId;
    String eventName;

    File makevideo;
    File combine;

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

        handler = new Handler();
        progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        videoview = (VideoView) v.findViewById(R.id.video01);
        statusText = (TextView) v.findViewById(R.id.status_text);
        progressBar.setMax(100);

        v.findViewById(R.id.make_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread download_photo_thread = new Thread(download_photo_worker);
                download_photo_thread.start();
                final AlertDialog.Builder firstTemplateBuilder = new AlertDialog.Builder(Tab2.this.getContext());
                LayoutInflater firstTemplateInflater = getLayoutInflater(savedInstanceState);
                firstTemplateBuilder.setTitle("Choose Template");
                firstTemplateBuilder.setSingleChoiceItems(new String[]{"Christmas", "Wedding"}, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        firstTemplateDecision = which;
                    }
                });
                firstTemplateBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder secondTemplateBuilder = new AlertDialog.Builder(Tab2.this.getContext());
                        LayoutInflater secondTemplateInflater = getLayoutInflater(savedInstanceState);
                        secondTemplateBuilder.setTitle("Choose Template");
                        String[] template = new String[]{};
                        switch (firstTemplateDecision){
                            case 0: template = new String[]{"Style1", "Style2"};
                                    break;
                            case 1: template = new String[]{"Style1", "Style2"};
                                    break;
                        }
                        secondTemplateBuilder.setSingleChoiceItems(template, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                secondTemplateDecision = which;
                            }
                        });
                        secondTemplateBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder effectBuilder = new AlertDialog.Builder(Tab2.this.getContext());
                                LayoutInflater effectInflater = getLayoutInflater(savedInstanceState);
                                effectBuilder.setTitle("Choose Effect");
                                effectBuilder.setSingleChoiceItems(new String[]{"閃光過場", "模糊過場", "交錯過場", "Zooming"}, 0, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        effectDecision = which;
                                    }
                                });
                                effectBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        makevideo = new File("/sdcard/MemorVi/"+eventObjectId+"/makevideo.mp4");
                                        combine = new File("/sdcard/MemorVi/"+eventObjectId+"/combine.mp4");
                                        Thread make_video_thread = new Thread(make_video_worker);
                                        make_video_thread.start();
                                    }
                                });
                                effectBuilder.setNegativeButton("Cancel", null);
                                effectBuilder.show();
                            }
                        });
                        secondTemplateBuilder.setNegativeButton("Cancel", null);
                        secondTemplateBuilder.show();
                    }
                });
                firstTemplateBuilder.setNegativeButton("Cancel", null);
                firstTemplateBuilder.show();
            }
        });

         //upload the encoded video to server
        v.findViewById(R.id.upload_video_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File video2 = new File("/sdcard/MemorVi/"+eventObjectId+"/combine.mp4");
                ParseObject VVV = new ParseObject("video");
                byte[] data = videoTobyte(video2);
                System.out.println("data to string " + data.toString());
                ParseFile file = new ParseFile("66.mp4", data);
                file.saveInBackground();
                VVV.put("file", file);
                VVV.put("eventID" , eventObjectId);
                VVV.put("generatedBy" ,userObjectId);
                VVV.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Toast.makeText(getActivity(), "video uploaded", Toast.LENGTH_LONG).show();
                        System.out.println("video uploaded");
                    }
                });
            }
        });

        v.findViewById(R.id.download_video_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("video");
                query.whereEqualTo("eventID", eventObjectId);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> videos, ParseException e) {
                        if (e == null) {
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

    private Runnable download_photo_worker = new Runnable() {
        public  void run() {
            completed = 0;
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(getActivity(), "Downloading photos", Toast.LENGTH_SHORT).show();
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


            int indexOfLandscape = 0;
            int indexOfOneFace = 0;
            int indexOfNormal = 0;

            while (indexOfLandscape+indexOfOneFace+indexOfNormal < landscapes.size()+photoWithOneFace.size()+normalPhotos.size()){
                int random = (int) random()*100;
                if (random % 3 == 0 && indexOfLandscape < landscapes.size()){
                    myPhotosWithOrder.add(landscapes.get(indexOfLandscape));
                    indexOfLandscape++;
                }
                else if (random % 3 == 1 && indexOfOneFace < photoWithOneFace.size()){
                    myPhotosWithOrder.add(photoWithOneFace.get(indexOfOneFace));
                    indexOfOneFace++;
                }
                else {
                    myPhotosWithOrder.add(normalPhotos.get(indexOfNormal));
                    indexOfNormal++;
                }
            }

            myPhotosWithOrder.addAll(sortByLevelOfSmile(groupPhoto));

            handler.post(new Runnable() {
                public void run() {
                    progressBar.setProgress(100);
                    statusText.setText(String.format("Completed %d", 100));
                    Toast.makeText(getActivity(), "Photos are Downloaded ", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private Runnable make_video_worker = new Runnable() {
        public  void run() {
            completed = 0;

            switch (firstTemplateDecision){
                case 0: switch (secondTemplateDecision){
                            case 0: images.add(imread("/sdcard/Download/christmas1_1.jpg"));
                                    if (eventName.length()%2 == 0){
                                        putText(images.get(0), eventName, new Point(280-(eventName.length()/2)*13,350), 7, 1.5, new opencv_core.Scalar(0, 0, 255, 0));
                                    }
                                    else {
                                        putText(images.get(0), eventName, new Point(280-(eventName.length()/2)*25,350), 7, 1.5, new opencv_core.Scalar(0, 0, 255, 0));
                                    }
                                    break;
                            case 1: images.add(imread("/sdcard/Download/christmas2_1.jpg"));
                                    if (eventName.length()%2 == 0){
                                        putText(images.get(0), eventName, new Point(280-(eventName.length()/2)*18,560), 3, 1.5, new opencv_core.Scalar(0, 0, 0, 0));
                                    }
                                    else {
                                        putText(images.get(0), eventName, new Point(280-(eventName.length()/2)*30,560), 3, 1.5, new opencv_core.Scalar(0, 0, 0, 0));
                                    }
                                    break;
                        }
                        break;
                case 1: switch (secondTemplateDecision){
                            case 0: images.add(imread("/sdcard/Download/wedding1_1.jpg"));
                                    if (eventName.length()%2 == 0){
                                        putText(images.get(0), eventName, new Point(80,65), 6, 2, new opencv_core.Scalar(0, 0, 255, 0));
                                    }
                                    else {
                                        putText(images.get(0), eventName, new Point(80,65), 6, 2, new opencv_core.Scalar(0, 0, 255, 0));
                                    }
                                    break;
                            case 1: images.add(imread("/sdcard/Download/wedding2_1.jpg"));
                                    if (eventName.length()%2 == 0){
                                        putText(images.get(0), eventName, new Point(300-(eventName.length()/2)*30,320), 3, 2, new opencv_core.Scalar(0, 100, 100, 0));
                                    }
                                    else {
                                        putText(images.get(0), eventName, new Point(300-(eventName.length()/2)*45,320), 3, 2, new opencv_core.Scalar(0, 100, 100, 0));
                                    }
                                    break;
                        }
                        break;
            }

            for (int k = 0 ; k< myPhotosWithOrder.size(); k++){
                switch (firstTemplateDecision){
                    case 0: switch (secondTemplateDecision){
                                case 0: if (k == myPhotosWithOrder.size()/2){
                                            images.add(imread("sdcard/Download/christmas1_2.jpg"));
                                        }
                                        if (k == myPhotosWithOrder.size()-groupPhoto.size()-1){
                                            images.add(imread("/sdcard/Download/christmas1_3.jpg"));
                                        }
                                        break;
                                case 1: if (k == myPhotosWithOrder.size()/2){
                                            images.add(imread("sdcard/Download/christmas2_2.jpg"));
                                        }
                                        if (k == myPhotosWithOrder.size()-groupPhoto.size()-1){
                                            images.add(imread("/sdcard/Download/christmas2_3.jpg"));
                                        }
                                        break;
                            }
                            break;
                    case 1: switch (secondTemplateDecision){
                                case 0: if (k == myPhotosWithOrder.size()/3){
                                            images.add(imread("sdcard/Download/wedding1_2.jpg"));
                                        }
                                        if (k == myPhotosWithOrder.size()/3*2){
                                            images.add(imread("sdcard/Download/wedding1_3.jpg"));
                                        }
                                        if (k == myPhotosWithOrder.size()-groupPhoto.size()-1){
                                            images.add(imread("/sdcard/Download/wedding1_4.jpg"));
                                        }
                                        break;
                                case 1: if (k == myPhotosWithOrder.size()/3){
                                            images.add(imread("sdcard/Download/wedding2_2.jpg"));
                                        }
                                        if (k == myPhotosWithOrder.size()/3*2){
                                            images.add(imread("sdcard/Download/wedding2_3.jpg"));
                                        }
                                        if (k == myPhotosWithOrder.size()-groupPhoto.size()-1){
                                            images.add(imread("/sdcard/Download/wedding2_4.jpg"));
                                        }
                                        break;
                            }
                            break;
                }
                opencv_core.Mat m = myPhotosWithOrder.get(k).getMat();
                images.add(m);
            }

            // add background
            opencv_core.Mat black = imread("/sdcard/Download/b1.jpg");
            for (int k = 0 ; k<images.size(); k++){
                imagesWithBackground.add(addBackground(images.get(k), black));
                imagesWithEffect1.add(addBackground(images.get(k), black));
                imagesWithEffect2.add(addBackground(images.get(k), black));
                imagesWithEffect3.add(addBackground(images.get(k), black));
            }

            switch (effectDecision){
                case 0: {
                    for (int i=0; i<imagesWithBackground.size(); i++){
                        imagesWithEffect1.get(i).convertTo(imagesWithEffect1.get(i), -1, 1.0, 20);
                        imagesWithEffect2.get(i).convertTo(imagesWithEffect2.get(i), -1, 1.0, 40);
                        imagesWithEffect3.get(i).convertTo(imagesWithEffect3.get(i), -1, 1.0, 60);
                    }
                    break;
                }
                case 1: {
                    for (int i=0; i<imagesWithBackground.size(); i++){
                        GaussianBlur(imagesWithEffect1.get(i), imagesWithEffect1.get(i), new opencv_core.Size(23,23), 0);
                        GaussianBlur(imagesWithEffect2.get(i), imagesWithEffect2.get(i), new opencv_core.Size(45,45), 0);
                        GaussianBlur(imagesWithEffect3.get(i), imagesWithEffect3.get(i), new opencv_core.Size(69,69), 0);
                    }
                    break;
                }
                case 2: {
                    for (int i=0; i<imagesWithBackground.size()-1; i++){
                        addWeighted(imagesWithEffect1.get(i), 0.8, imagesWithEffect1.get(i+1), 0.2, 0.0, imagesWithEffect1.get(i));
                        addWeighted(imagesWithEffect2.get(i), 0.5, imagesWithEffect2.get(i+1), 0.5, 0.0, imagesWithEffect2.get(i));
                        addWeighted(imagesWithEffect3.get(i), 0.2, imagesWithEffect3.get(i+1), 0.8, 0.0, imagesWithEffect3.get(i));
                    }
                    break;
                }
                case 3: {
                    break;
                }
            }

            handler.post(new Runnable() {
                public void run() {
                    progressBar.setProgress(completed);
                    statusText.setText(String.format("Completed %d", completed));
                }
            });
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(makevideo.getAbsolutePath(), 640, 480,1);
            Frame captured_frame;
            try {
                recorder.setFrameRate(20);
                transitionFrameDuration = 4;
                mainFrameDuration = 80;
                recorder.start();
                if (effectDecision == 0 || effectDecision == 1){
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
                    for (int i = 0; i < imagesWithBackground.size(); i++) {
                        int randomNumber = (int) (random()*100);
                        if (randomNumber % 2 == 0){
                            imagesWithBackground.get(i).adjustROI(2,2,2,2);
                            for (int k=0; k<20; k++){
                                captured_frame = converter.convert(imagesWithBackground.get(i).adjustROI(-2, -2, -2, -2));
                                for (int j=0; j<transitionFrameDuration; j++){
                                    recorder.record(captured_frame);
                                }
                            }
                        }
                        else {
                            imagesWithBackground.get(i).adjustROI(-40,-40,-40,-40);
                            for (int k=19; k>=0; k--){
                                captured_frame = converter.convert(imagesWithBackground.get(i).adjustROI(2, 2, 2, 2));
                                for (int j=0; j<transitionFrameDuration; j++){
                                    recorder.record(captured_frame);
                                }
                            }
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
                recorder.stop();
                recorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.post(new Runnable() {
                public void run() {
                    progressBar.setProgress(100);
                    statusText.setText(String.format("Completed %d", 100));
                    //playVideoOnView(makevideo);
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
                    progressBar.setProgress(completed);
                    statusText.setText(String.format("Completed %d", completed));
                }
            });
            switch (firstTemplateDecision){
                case 0: switch(secondTemplateDecision){
                            case 0: int randomNumber = (int) (random()*100%3);
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
                case 1: switch(secondTemplateDecision){
                            case 0: int randomNumber = (int) (random()*100%2);
                                    if (randomNumber == 0)
                                        audio = new File("/sdcard/Download/wedding1_1.mp3");
                                    if (randomNumber == 1)
                                        audio = new File("/sdcard/Download/wedding1_2.mp3");
                                    break;
                            case 1: audio = new File("/sdcard/Download/wedding2.mp3");
                                    break;
                        }
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
        resize(background, background, new opencv_core.Size(800, 480));
        opencv_core.Mat tempBlackGround = background.clone();
        if (image.cols() < image.rows()){
            resize(image, image, new opencv_core.Size(320, 480));
            image.copyTo(tempBlackGround.rowRange(0, 480).colRange(240, 560));
        }
        else {
            resize(image, image, new opencv_core.Size(640, 480));
            image.copyTo(tempBlackGround.rowRange(0, 480).colRange(80, 720));
        }
        return tempBlackGround;
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
        System.out.println("Path is here :  " + f.getAbsolutePath());
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