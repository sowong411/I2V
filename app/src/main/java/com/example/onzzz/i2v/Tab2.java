package com.example.onzzz.i2v;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.GaussianBlur;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
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
    private int maxFaceNum;

    ArrayList<Photo> landscapes = new ArrayList<Photo>(); //風景相
    ArrayList<Photo> photoWithOneFace = new ArrayList<Photo>(); //獨照
    ArrayList<Photo> normalPhotos = new ArrayList<Photo>(); //人相
    ArrayList<Photo> groupPhoto = new ArrayList<Photo>(); //大合照

    ArrayList<Photo> myPhotos = new ArrayList<Photo>();
    ArrayList<Photo> myPhotosWithOrder = new ArrayList<Photo>();

    ArrayList<opencv_core.Mat> images = new ArrayList<opencv_core.Mat>();
    ArrayList<opencv_core.Mat> imagesWithBackground = new ArrayList<opencv_core.Mat>();
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

            myPhotosWithOrder.addAll(sortByLevelOfSmile(groupPhoto));
            myPhotosWithOrder.addAll(sortByLevelOfSmile(photoWithOneFace));
            myPhotosWithOrder.addAll(sortByLevelOfSmile(normalPhotos));
            myPhotosWithOrder.addAll(sortByLevelOfSmile(landscapes));


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
        File makevideo = new File("/sdcard/DCIM/Camera/", "makevideo.mp4");
        public  void run() {
            completed = 0;

            for (int k = 0 ; k< myPhotosWithOrder.size(); k++){
                opencv_core.Mat m = myPhotosWithOrder.get(k).getMat();
                images.add(m);
            }

            // add background
            opencv_core.Mat black = imread("/sdcard/Download/b1.jpg");
            for (int k = 0 ; k<images.size(); k++){
                imagesWithBackground.add(addBackground(images.get(k), black));
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
                recorder.start();
                for (int i = 0; i < imagesWithBackground.size(); i++) {
                    captured_frame = converter.convert(imagesWithBackground.get(i));
                    for(int j =0 ; j<40 ; j++) {
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

    private ArrayList<Photo> sortByLevelOfSmile (ArrayList<Photo> photos){
        Collections.sort(photos);
        return photos;
    }

    private opencv_core.Mat addBackground ( opencv_core.Mat image , opencv_core.Mat background) {
        resize(background, background, new opencv_core.Size(800, 480));
        opencv_core.Mat tempBlackGround = background.clone();
        resize(image, image, new opencv_core.Size(640, 480));
        image.copyTo(tempBlackGround.rowRange(0, 480).colRange(80, 720));
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



    private ArrayList<opencv_core.Mat> combineIntoOne( ArrayList<opencv_core.Mat> inputArray){
        ArrayList<opencv_core.Mat> temp = new ArrayList<opencv_core.Mat>();

        return temp;
    }
}