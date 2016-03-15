package com.example.onzzz.i2v;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.bytedeco.javacpp.opencv_core.flip;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.putText;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

public class Tab2 extends Fragment {
    final String tag = "Tab 2 is here";
    private Button getButton, makeButton, combineButton, uploadButton, downloadButton;
    private ProgressBar progressBar;
    private TextView statusText;
    private int completed=0;
    private Handler handler;
    VideoView videoview;
    ArrayList<Photo> myPhotos = new ArrayList<Photo>();
    ArrayList<Photo> landscapes = new ArrayList<Photo>(); //風景相
    ArrayList<Photo> photoWithOneFace = new ArrayList<Photo>(); //獨照
    ArrayList<Photo> normalPhotos = new ArrayList<Photo>(); //人相
    ArrayList<Photo> groupPhoto = new ArrayList<Photo>(); //大合照
    private int maxFaceNum;
    ArrayList<String> photoString = new ArrayList<String>();
    ArrayList<String> photoPaths = new ArrayList<String>();
    ArrayList<opencv_core.Mat> images = new ArrayList<opencv_core.Mat>();

    String userObjectId;
    String eventObjectId;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        System.out.println(tag);
        View v =inflater.inflate(R.layout.tab_2,container,false);
        EventContentActivity activity = (EventContentActivity) getActivity();

        userObjectId = activity.getUserObjectId();
        eventObjectId = activity.getEventObjectId();
        handler = new Handler();
        progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        videoview = (VideoView) v.findViewById(R.id.video01);
        statusText = (TextView) v.findViewById(R.id.status_text);
        progressBar.setMax(100);


        /*long startTime = System.currentTimeMillis();
        opencv_core.Mat temp = new opencv_core.Mat();
        opencv_core.Mat black = imread("/sdcard/Download/b1.jpg");
        resize(black,black,  new opencv_core.Size(800, 480));
        for (int i=1 ; i<31 ; i++){
            opencv_core.Mat m = imread ("/sdcard/Download/" +i+".jpg");
            opencv_core.Mat tempBlackGround = black.clone();
            resize(m, m, new opencv_core.Size(640, 480));
            m.copyTo(tempBlackGround.rowRange(0, 480).colRange(80, 720));
            images.add(tempBlackGround);
        }
        opencv_core.Scalar sca = new opencv_core.Scalar(255, 255, 255,1);
        opencv_core.Mat lastFrame = new opencv_core.Mat(640, 480, 1, sca);
        images.add(lastFrame);

        //makevideo(images);
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Time  to make an video:  " + totalTime);*/
        v.findViewById(R.id.get_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread download_photo_thread = new  Thread(download_photo_worker);
                download_photo_thread.start();
            }
        });

        v.findViewById(R.id.make_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread make_video_thread = new  Thread(make_video_worker);
                make_video_thread.start();
            }
        });

        v.findViewById(R.id.combine_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread combine_thread = new  Thread(combine_worker);
                combine_thread.start();
            }
        });
        return v;
    }

    private Runnable download_photo_worker = new Runnable() {
        public  void run() {
            completed = 0;
            handler.post(new Runnable() {
                public void run() {
                    progressBar.setProgress(completed);
                    statusText.setText(String.format("Completed %d", completed));
                }
            });
            myPhotos = EventContentActivity.getMyPhotos();
            for (int i=0; i<myPhotos.size(); i++){
                photoString.add(myPhotos.get(i).getPhotoString());
                if (maxFaceNum < myPhotos.get(i).getNumberOfFace()){
                    maxFaceNum = myPhotos.get(i).getNumberOfFace();
                }
            }
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
            System.out.println("風景相:" + landscapes.size());
            System.out.println("獨照:" + photoWithOneFace.size());
            System.out.println("人相:" + normalPhotos.size());
            System.out.println("大合照:" + groupPhoto.size());
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(getActivity(), "Start Downloading photos", Toast.LENGTH_LONG).show();
                }
            });
            for (int i =0 ; i< photoString.size() ; i++){
                byte[] decodedByte = Base64.decode(photoString.get(i), 0);
                photoPaths.add(getBitmapPath(decodedByte, "iv_" + i));
                completed = (int)(( (float)i/(float)photoString.size())*100);
                handler.post(new Runnable() {
                    public void run() {
                        progressBar.setProgress(completed);
                        statusText.setText(String.format("Completed %d", completed));
                    }
                });
            }
            handler.post(new Runnable() {
                public void run() {
                    progressBar.setProgress(100);
                    statusText.setText(String.format("Completed %d", 100));
                    Toast.makeText(getActivity(), "Photos are Downloaded ", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Runnable make_video_worker = new Runnable() {
        File makevideo = new File("/sdcard/DCIM/Camera/", "makevideo.mp4");
        public  void run() {
            completed = 0;
                opencv_core.Mat temp = new opencv_core.Mat();
                opencv_core.Mat black = imread("/sdcard/Download/b1.jpg");
                resize(black,black,  new opencv_core.Size(800, 480));
            for (int k = 0 ; k<photoPaths.size(); k++){
                    opencv_core.Mat m = imread (photoPaths.get(k));
                    opencv_core.Mat tempBlackGround = black.clone();
                    resize(m, m, new opencv_core.Size(640, 480));
                    m.copyTo(tempBlackGround.rowRange(0, 480).colRange(80, 720));
                    images.add(tempBlackGround);
                }
            handler.post(new Runnable() {
                public  void run() {
                    progressBar.setProgress(completed);
                    statusText.setText(String.format("Completed %d", completed));
                }
            });
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(makevideo.getAbsolutePath(), 640, 480,1);
            Frame captured_frame;
            try {
                recorder.setFrameRate(20);
                recorder.start();
                for (int i = 0; i < images.size(); i++) {
                    captured_frame = converter.convert(images.get(i));
                    for(int j =0 ; j<40 ; j++) {
                        recorder.record(captured_frame);
                    }
                    completed = (int)(( (float)i/(float)images.size())*100);
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(completed);
                            statusText.setText(String.format("Completed %d", completed));
                        }
                    });
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
                    playVideoOnView(makevideo);
                }
            });
        }
    };

    private Runnable combine_worker = new Runnable() {
        File makevideo = new File("/sdcard/DCIM/Camera/", "makevideo.mp4");
        File audio = new File("/sdcard/Download/2015.mp3");
        File combine = new File("/sdcard/DCIM/Camera/combine.mp4");
        public void run() {
            completed = 0;
            handler.post(new Runnable() {
                public void run() {
                    progressBar.setProgress(completed);
                    statusText.setText(String.format("Completed %d", completed));
                }
            });
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

    private void playVideoOnView(File video) {
        videoview.setVideoPath(video.getAbsolutePath());
        videoview.setMediaController(new MediaController(Tab2.this.getContext()));
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int h = displaymetrics.heightPixels;
        int w = displaymetrics.widthPixels;
        videoview.setMinimumHeight(h*3);
        videoview.setMinimumWidth(w*4);
        videoview.start();
    }



    private String getBitmapPath(byte[] bitmapArray , String filename){
        File f = new File("/sdcard/DCIM/Camera/", filename+".jpg");
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
        System.out.println("Path is here :  "   + f.getAbsolutePath() );
        return  f.getAbsolutePath();
    }

}