package com.example.onzzz.i2v;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
/**
 * Created by OnzzZ on 5/5/2016.
 */
public class FaceDetection implements Runnable {
    interface DetectCallback {
        void detectResult(JSONObject rst);
    }
    private Bitmap image;
    DetectCallback callback = null;
    public void setDetectCallback(DetectCallback detectCallback) {
        callback = detectCallback;
    }
    public FaceDetection(Bitmap bitmap){
        this.image = bitmap;
    }

    public void run(){
        HttpRequests httpRequests = new HttpRequests("4480afa9b8b364e30ba03819f3e9eff5", "Pz9VFT8AP3g_Pz8_dz84cRY_bz8_Pz8M", true, false);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        float scale = Math.min(1, Math.min(600f / image.getWidth(), 600f / image.getHeight()));
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap imgSmall = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, false);
        imgSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] array = stream.toByteArray();
        try {
            //detect
            JSONObject result = httpRequests.detectionDetect(new PostParameters().setImg(array));
            //finished , then call the callback function
            if (callback != null) {
                callback.detectResult(result);
            }
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }
    }
}
