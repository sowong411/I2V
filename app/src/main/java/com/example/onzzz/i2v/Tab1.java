package com.example.onzzz.i2v;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static org.bytedeco.javacpp.opencv_core.flip;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.putText;

public class Tab1 extends Fragment {

    private ArrayList<opencv_core.Mat> images = new ArrayList<opencv_core.Mat>();

    String userObjectId;
    String eventObjectId;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_1,container,false);

        EventContentActivity activity = (EventContentActivity) getActivity();
        userObjectId = activity.getUserObjectId();
        eventObjectId = activity.getEventObjectId();

        v.findViewById(R.id.makevideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 1; i <= 5; i++) {
                    opencv_core.Mat m = imread("/sdcard/Download/" + i +".jpg");
                    images.add(m);
                }
                File video = makevideo(images);
                playVideoOnView(video);
            }
        });

        v.findViewById(R.id.combine_with_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File audio = new File("/sdcard/Download/2015.mp3");
                File video2 = new File("/sdcard/DCIM/Camera/makevideo.mp4");
                playVideoOnView(combine(video2, audio));
            }
        });


        return v;
    }

    public File makevideo(ArrayList<opencv_core.Mat> images) {
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        File makevideo = new File("/sdcard/DCIM/Camera/", "makevideo.mp4");
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
            }
            recorder.stop();
            recorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return makevideo;
    }

    public void tryReadWrite(File file) {
        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(file.getAbsolutePath());
        Frame frame = null;
        FFmpegFrameRecorder recorder;
        try {
            frameGrabber.start();
            File newfile = new File("/sdcard/DCIM/Camera/try.mp4");
            recorder = new FFmpegFrameRecorder(newfile.getAbsolutePath(), frameGrabber.getImageWidth(), frameGrabber.getImageHeight(), frameGrabber.getAudioChannels());
            //recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            //recorder.setSampleFormat(frameGrabber.getSampleFormat());
            //recorder.setSampleRate(frameGrabber.getSampleRate());
            recorder.start();
            while (true) {
                try {
                    frame = frameGrabber.grabFrame();
                    if (frame == null) {
                        System.out.println("!!! Failed cvQueryFrame");
                        break;
                    }
                    recorder.record(frame);
                } catch (Exception e) {
                }
            }
            recorder.stop();
            recorder.release();
            frameGrabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File combine(File video, File audio) {
        FFmpegFrameGrabber grabber1 = new FFmpegFrameGrabber(video.getAbsolutePath());
        FFmpegFrameGrabber grabber2 = new FFmpegFrameGrabber(audio.getAbsolutePath());
        Frame video_frame = null;
        Frame audio_frame = null;

        FFmpegFrameRecorder recorder;
        File file = new File("/sdcard/DCIM/Camera/combine.mp4");
        try {
            grabber1.start();
            grabber2.start();
            int remainingFrame = grabber1.getLengthInFrames();
            recorder = new FFmpegFrameRecorder(file.getAbsolutePath(), grabber1.getImageWidth(), grabber1.getImageHeight(), grabber2.getAudioChannels());
            recorder.setFrameRate(grabber1.getFrameRate()*2);
            //recorder.setSampleRate(grabber2.getSampleRate());
            //recorder.setFormat("mp4");
            recorder.setSampleRate(grabber2.getSampleRate());
            //recorder.setVideoBitrate(192000); // set 憭芸???
            recorder.start();
            //(video_frame = grabber1.grabFrame(true,true,true,false)

            //while(((video_frame = grabber1.grabFrame())!= null) && ((audio_frame = grabber2.grabFrame())!= null)){
            while (remainingFrame > 0 ){
                audio_frame = grabber2.grabFrame();
                video_frame = grabber1.grabFrame();
                recorder.record(video_frame);
                recorder.record(audio_frame);
                remainingFrame--;
            }
            grabber1.stop();
            grabber2.stop();
            recorder.stop();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public void flipAll(ArrayList<opencv_core.Mat> images) {
        ArrayList<opencv_core.Mat> temp = new ArrayList<opencv_core.Mat>();

        for (int i = 0; i < images.size(); i++) {
            temp.add(new opencv_core.Mat(images.get(i)));
        }

        for (int i = 0; i < images.size(); i++) {
            flip(images.get(i), images.get(i), 1);
        }
        images.addAll(temp);
    }

    public void showPara(File file) {
        System.out.println("Showing  " + file.getName() + " Paras...");
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file.getAbsolutePath());
        Frame frame = null;
        try {
            grabber.start();
            System.out.println(" AbsolutePath : " + file.getAbsolutePath());
            System.out.println("Image Height:  " + grabber.getImageHeight());
            System.out.println("Image Width : " + grabber.getImageWidth());
            System.out.println("Audio Channels:  " + grabber.getAudioChannels());
            System.out.println("Audio Bitrate:  " + grabber.getAudioBitrate());
            System.out.println("Audio Codec:  " + grabber.getAudioCodec());
            System.out.println("Format:  " + grabber.getFormat());
            System.out.println("Frame Rate:  " + grabber.getFrameRate());
            System.out.println("Length In Frames:  " + grabber.getLengthInFrames());
            System.out.println("Length In Time:  " + grabber.getLengthInTime());
            System.out.println("Frame Number:  " + grabber.getFrameNumber());
            System.out.println("Time stamp:  " + grabber.getTimestamp());

            grabber.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  opencv_core.Mat addComment(String comment){
        opencv_core.Scalar sca = new opencv_core.Scalar(255, 255, 255,1);
        opencv_core.Mat white = new opencv_core.Mat(320, 240, 0, sca);
        putText(white, comment, new opencv_core.Point(white.cols() / 12, white.rows() / 2), 2, 0.7, new opencv_core.Scalar(0, 255, 0, 1));
        return white;
    }

    public void playVideoOnView(File video) {
        VideoView videoview;
        videoview = (VideoView) this.getView().findViewById(R.id.video01);
        videoview.setVideoPath(video.getAbsolutePath());

        videoview.setMediaController(new MediaController(Tab1.this.getContext()));
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int h = displaymetrics.heightPixels;
        int w = displaymetrics.widthPixels;
        videoview.setMinimumHeight(h*3);
        videoview.setMinimumWidth(w*4);
        videoview.start();
    }

    public ArrayList<Bitmap> matToBitmap(ArrayList<opencv_core.Mat> mats ) {
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        AndroidFrameConverter frameConverter = new AndroidFrameConverter();
        ArrayList<Bitmap> bitMaps = new ArrayList<Bitmap>();
        for (int i = 0; i < mats.size(); i++) {
            Frame tempFrame = converter.convert(mats.get(i));
            Bitmap tempBit = frameConverter.convert(tempFrame);
            bitMaps.add(tempBit);
        }
        return bitMaps;
    }

    public ArrayList<opencv_core.Mat> bitmapToMat(ArrayList<Bitmap> bitmaps ) {
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        AndroidFrameConverter frameConverter = new AndroidFrameConverter();
        ArrayList<opencv_core.Mat> mats = new ArrayList<opencv_core.Mat>();
        for (int i = 0; i < bitmaps.size(); i++) {
            Frame tempFrame = frameConverter.convert(bitmaps.get(i));
            opencv_core.Mat tempMat = converter.convert(tempFrame);
            mats.add(tempMat);
        }
        return mats;
    }

    public File byteToVideo(byte[] b){
        File outfile = new File("/sdcard/DCIM/Camera/byteToVideo.mp4");
        try {
            FileOutputStream os = new FileOutputStream(outfile);
            os.write(b);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outfile;
    }

    public  File stringToVideo(String encoded) {
        byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
        return byteToVideo( decoded);
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

    public String videoToString(File video) {
        String encodedBase64 = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(video);
            byte[] bytes = new byte[(int) video.length()];
            fileInputStream.read(bytes);
            encodedBase64 = Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encodedBase64;
    }

    public String bitmapToString(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] b = baos.toByteArray();
        try {
            baos.close();
            baos = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        return imageEncoded;
    }

    public Bitmap stringToBitmap(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }


}