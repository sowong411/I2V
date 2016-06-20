package com.example.onzzz.i2v;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static java.lang.Math.random;

/**
 * Created by onzzz on 1/3/2016.
 */
public class CustomGalleryActivity extends Activity {

    GridView gridGallery;
    Handler handler;
    GalleryAdapter adapter;

    Button btnGalleryOk;
    ArrayList<Boolean> readyToBeRemovedList = new ArrayList<Boolean>();
    ArrayList<Photo>  photos = new ArrayList<Photo>();
    String action;
    private ImageLoader imageLoader;
    private int numOfFace;
    private int totalSmile;
    private double averageSmile;
    private ArrayList<String> allPath = new ArrayList<String>();
    private int numOfMale;
    private int numOfFemale;
    private String  userObjectId;
    private String  eventObjectId;

    FaceppDetect faceppDetect = new FaceppDetect();
    Bitmap bmp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gallery);
        Intent intent = getIntent();
        userObjectId = intent.getStringExtra("UserObjectId");
        eventObjectId = intent.getStringExtra("EventObjectId");
        assert (intent != null);
        initImageLoader();
        init();
    }

    private void initImageLoader() {
        try {
            String CACHE_DIR = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/.temp_tmp";
            new File(CACHE_DIR).mkdirs();

            File cacheDir = StorageUtils.getOwnCacheDirectory(getBaseContext(),
                    CACHE_DIR);

            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY)
                    .bitmapConfig(Bitmap.Config.RGB_565).build();
            ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                    getBaseContext())
                    .defaultDisplayImageOptions(defaultOptions)
                    .discCache(new UnlimitedDiscCache(cacheDir))
                    .memoryCache(new WeakMemoryCache());
            ImageLoaderConfiguration config = builder.build();
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);
        } catch (Exception e) {

        }
    }

    private void init() {
        handler = new Handler();
        gridGallery = (GridView) findViewById(R.id.gridGallery);
        gridGallery.setFastScrollEnabled(true);
        adapter = new GalleryAdapter(getApplicationContext(), imageLoader);
        PauseOnScrollListener listener = new PauseOnScrollListener(imageLoader,
                true, true);
        gridGallery.setOnScrollListener(listener);
        findViewById(R.id.llBottomContainer).setVisibility(View.VISIBLE);
        gridGallery.setOnItemClickListener(mItemMulClickListener);
        adapter.setMultiplePick(true);
        gridGallery.setAdapter(adapter);
        btnGalleryOk = (Button) findViewById(R.id.btnGalleryOk);
        btnGalleryOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<CustomGallery> selected = adapter.getSelected();
                for (int i = 0; i < selected.size(); i++) {
                    allPath.add( selected.get(i).sdcardPath );
                }

                // remove all low resolution photo in arraylist
                for (int k = 0; k < allPath.size(); k++) {
                    if (new Detection().resDetection(allPath.get(k))) {
                        Toast.makeText(CustomGalleryActivity.this, "Drop low res : " + allPath.get(k), Toast.LENGTH_SHORT).show();
                        allPath.remove(k);
                    }
                }
                // remove all blur photo in arraylist
                for (int k = 0; k < allPath.size(); k++) {
                    if (new Detection().blurDetection(allPath.get(k))) {
                        Toast.makeText(CustomGalleryActivity.this, "Drop blur : " + allPath.get(k), Toast.LENGTH_SHORT).show();
                        allPath.remove(k);
                    }
                }

                // remove all similar photo in arraylist
                simChecking();

                //attribute detection
                for (int i = 0; i < allPath.size(); i++) {
                    //if(i!= 0){ bmp.recycle();}
                    bmp = BitmapFactory.decodeFile(allPath.get(i));
                    final ParseObject photo = new ParseObject("Photo");
                    final String encodedString = encodeTobase64(bmp);
                    faceppDetect.setDetectCallback(new DetectCallback() {
                        public void detectResult(JSONObject rst) {
                            try {
                                //find out all faces
                                numOfFace = rst.getJSONArray("face").length();
                                System.out.println("Testing numOfFace:" + numOfFace);
                                for (int i = 0; i < numOfFace; ++i) {
                                    //Way to detect smile
                                    totalSmile += rst.getJSONArray("face").getJSONObject(i)
                                            .getJSONObject("attribute").getJSONObject("smiling").getInt("value");
                                    //Way to detect gender
                                    String gender = rst.getJSONArray("face").getJSONObject(i)
                                            .getJSONObject("attribute").getJSONObject("gender").getString("value");
                                    if (gender.equals("Male")) {
                                        numOfMale++;
                                    } else if (gender.equals("Female")) {
                                        numOfFemale++;
                                    }
                                    photo.put("Image", encodedString);
                                    photo.put("Event", eventObjectId);
                                    photo.put("UploadedBy", userObjectId);
                                    photo.put("FaceNumber", numOfFace);
                                    photo.put("AverageSmileLevel", averageSmile);
                                    photo.put("MaleNumber", numOfMale);
                                    photo.put("FemaleNumber", numOfFemale);
                                    photo.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                Toast.makeText(CustomGalleryActivity.this, "Uploading", Toast.LENGTH_LONG).show();
                                                Intent data = new Intent();
                                                setResult(RESULT_OK, data);
                                                finish();
                                            }
                                        }
                                    });

                                    averageSmile = 0;
                                    totalSmile = 0;
                                    numOfMale = 0;
                                    numOfFemale = 0;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                CustomGalleryActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                    }
                                });
                            }
                        }
                    });
                    Thread face_detect_thread = new Thread(face_detect_worker);
                    face_detect_thread.run();
                    //bmp.recycle();
                }
            }
        });
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.addAll(getGalleryPhotos());
                    }
                });
                Looper.loop();
            };
        }.start();
    }

    AdapterView.OnItemClickListener mItemMulClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> l, View v, int position, long id) {
            adapter.changeSelection(v, position);
        }
    };

    private ArrayList<CustomGallery> getGalleryPhotos() {
        ArrayList<CustomGallery> galleryList = new ArrayList<CustomGallery>();
        try {
            final String[] columns = { MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media._ID };
            final String orderBy = MediaStore.Images.Media._ID;
            Cursor imagecursor = managedQuery(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                    null, null, orderBy);
            if (imagecursor != null && imagecursor.getCount() > 0) {
                while (imagecursor.moveToNext()) {
                    CustomGallery item = new CustomGallery();
                    int dataColumnIndex = imagecursor
                            .getColumnIndex(MediaStore.Images.Media.DATA);
                    item.sdcardPath = imagecursor.getString(dataColumnIndex);
                    galleryList.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // show newest photo at beginning of the list
        Collections.reverse(galleryList);
        return galleryList;
    }

    public static String encodeTobase64(Bitmap image){
        Bitmap immagex=image;
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

//        Log.e("LOOK", imageEncoded);
        return imageEncoded;
    }

    private class FaceppDetect {
        DetectCallback callback = null;
        public void setDetectCallback(DetectCallback detectCallback) {
            callback = detectCallback;
        }
        public void detect(final Bitmap image) {
            new Thread(new Runnable() {
                public void run() {
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
                        CustomGalleryActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                            }
                        });
                    }
                }
            }).start();
        }
    }




    public void simChecking( ){
        for(int j =0 ; j< allPath.size() ; j++){
            readyToBeRemovedList.add(false);
        }
        new Detection().simChecking(allPath, readyToBeRemovedList);
        if (allPath.size() != 0){
            for (int i = readyToBeRemovedList.size() - 1; i >= 0; i--) {
                if (readyToBeRemovedList.get(i) == true) {
                    Toast.makeText(CustomGalleryActivity.this, "Drop sim :" + allPath.get(i), Toast.LENGTH_SHORT).show();
                    allPath.remove(i);
                }
            }
        }
    }

    interface DetectCallback {
        void detectResult(JSONObject rst);
    }

    private Runnable face_detect_worker = new Runnable() {

        public void run() {
            faceppDetect.detect(bmp);
        }
    };

}