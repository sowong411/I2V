package com.example.onzzz.i2v;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by WAICHONG on 31/12/2015.
 */
public class PhotoUploadActivity extends AppCompatActivity {


    GridView gridGallery;
    Handler handler;
    GalleryAdapter adapter;

    Button btnGalleryPickMul;
    Button btnPhotoUpload;

    ViewSwitcher viewSwitcher;
    ImageLoader imageLoader;

    static final int MAX_PHOTO_SELECTED = 90;

    int numOfPhotoSelected;

    ArrayList<String> photoUri = new ArrayList<String>();
    ArrayList<Integer> numberOfFaceList = new ArrayList<Integer>();
    ArrayList<Double> averageSmileList = new ArrayList<Double>();
    ArrayList<Boolean> readyToBeRemovedList = new ArrayList<Boolean>();
    String userObjectId;
    String eventObjectId;

    boolean FandSDetectorDone = false;
    private int numOfFace;
    private int totalSmile;
    private double averageSmile;
    private ArrayList<Integer> age = new ArrayList<Integer>();
    private int totalAge;
    private double totalSquareAge;
    private double averageAge;
    private double varianceAge;
    private int numOfMale;
    private int numOfFemale;
    private int facePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photo);
        Intent intent = getIntent();
        userObjectId = intent.getStringExtra("UserObjectId");
        eventObjectId = intent.getStringExtra("EventObjectId");
        assert (intent != null);

        /*findViewById(R.id.upload_photo_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                // TODO
                intent.setClass(PhotoUploadActivity.this, PhotoUploadActivity.class);
                startActivity(intent);
            }
        });*/

        initImageLoader();
        init();

    }

    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc().imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                this).defaultDisplayImageOptions(defaultOptions).memoryCache(
                new WeakMemoryCache());

        ImageLoaderConfiguration config = builder.build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }

    private void init() {

        handler = new Handler();
        gridGallery = (GridView) findViewById(R.id.gridGallery);
        gridGallery.setFastScrollEnabled(true);
        adapter = new GalleryAdapter(getApplicationContext(), imageLoader);
        adapter.setMultiplePick(false);
        gridGallery.setAdapter(adapter);

        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        viewSwitcher.setDisplayedChild(1);

        btnPhotoUpload = (Button) findViewById(R.id.btnUpload);
        btnPhotoUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // remove all low resolution photo in arraylist
                for (int k = 0; k < photoUri.size(); k++) {
                    if (new Detection().resDetection(photoUri.get(k))) {
                        System.out.println("Drop low res:" + photoUri.get(k));
                        photoUri.remove(k);
                        numOfPhotoSelected--;
                    }
                }
                // remove all blur photo in arraylist
                for (int k = 0; k < photoUri.size(); k++) {
                    if (new Detection().blurDetection(photoUri.get(k))) {
                        System.out.println(" Drop blur  :" + photoUri.get(k));
                        photoUri.remove(k);
                        numOfPhotoSelected--;
                    }
                }


                // first do number of face and smile detection and drop out sim photo
                Thread FandSDetector = new Thread(detect_FaceAndSmile_worker);
                FandSDetector.start();
                while (true) {
                    if (FandSDetectorDone == false) {
                        try {
                            Thread.sleep(2000);                 //1000 milliseconds is one second.
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        simChecking();
                        break;
                    }

                }


                //attribute detection
                for (int i = 0; i < photoUri.size(); i++) {
                    System.out.println("select : " + photoUri.get(i));
                    final ParseObject photo = new ParseObject("Photo");
                    final Bitmap bmp = BitmapFactory.decodeFile(photoUri.get(i));
                    final String encodedString = encodeTobase64(bmp);
                    FaceppDetect faceppDetect = new FaceppDetect();
                    faceppDetect.setDetectCallback(new DetectCallback() {
                        public void detectResult(JSONObject rst) {
                            try {
                                //find out all faces
                                numOfFace = rst.getJSONArray("face").length();
                                System.out.println("NumOfFace: " + numOfFace);
                                for (int i = 0; i < numOfFace; ++i) {

                                    //Way to detect smile
                                    totalSmile += rst.getJSONArray("face").getJSONObject(i)
                                            .getJSONObject("attribute").getJSONObject("smiling").getInt("value");

                                    //Way to detect age
                                    System.out.println("Age " + i + ": " + rst.getJSONArray("face").getJSONObject(i)
                                            .getJSONObject("attribute").getJSONObject("age").getInt("value"));
                                    age.add(rst.getJSONArray("face").getJSONObject(i)
                                            .getJSONObject("attribute").getJSONObject("age").getInt("value"));

                                    //Way to detect gender
                                    String gender = rst.getJSONArray("face").getJSONObject(i)
                                            .getJSONObject("attribute").getJSONObject("gender").getString("value");
                                    if (gender.equals("Male")) {
                                        numOfMale++;
                                    } else if (gender.equals("Female")) {
                                        numOfFemale++;
                                    }

                                    //Way to detect face position
                                    if (numOfFace == 1) {
                                        if (rst.getJSONArray("face").getJSONObject(i)
                                                .getJSONObject("position").getJSONObject("center").getInt("x") < 50) {
                                            facePosition = -1;
                                        } else if (rst.getJSONArray("face").getJSONObject(i)
                                                .getJSONObject("position").getJSONObject("center").getInt("x") > 50) {
                                            facePosition = 1;
                                        }
                                    }
                                }

                                if (numOfFace != 0) {
                                    for (int i = 0; i < age.size(); i++) {
                                        totalAge += age.get(i);
                                    }
                                    averageAge = totalAge / (double) numOfFace;
                                    varianceAge = varianceCalculation(age, averageAge, numOfFace);
                                    averageSmile = totalSmile / (double) numOfFace;
                                    averageSmileList.add(averageSmile);
                                }

                                photo.put("Image", encodedString);
                                photo.put("Location", "");
                                photo.put("Time", "");
                                photo.put("Event", eventObjectId);
                                photo.put("UploadedBy", userObjectId);
                                photo.put("FaceNumber", numOfFace);
                                photo.put("AverageSmileLevel", averageSmile);
                                photo.put("MaleNumber", numOfMale);
                                photo.put("FemaleNumber", numOfFemale);
                                photo.put("AverageAge", averageAge);
                                photo.put("VarianceAge", varianceAge);
                                photo.put("FacePosition", facePosition);
                                photo.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Toast.makeText(PhotoUploadActivity.this, "All photo uploaded", Toast.LENGTH_LONG).show();
                                            Intent data = new Intent();
                                            setResult(RESULT_OK, data);
                                            finish();
                                        }
                                    }
                                });

                                averageAge = 0;
                                varianceAge = 0;
                                averageSmile = 0;
                                totalSmile = 0;
                                totalSquareAge = 0;
                                totalAge = 0;
                                numOfMale = 0;
                                numOfFemale = 0;
                                facePosition = 0;

                            } catch (JSONException e) {
                                e.printStackTrace();
                                PhotoUploadActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                    }
                                });
                            }

                        }
                    });
                    faceppDetect.detect(bmp);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            String[] all_path = data.getStringArrayExtra("all_path");

            ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();

            numOfPhotoSelected = 0;

            for (String string : all_path) {
                CustomGallery item = new CustomGallery();
                item.sdcardPath = string;
                photoUri.add(string);
                numOfPhotoSelected++;
                dataT.add(item);
            }

            viewSwitcher.setDisplayedChild(0);
            adapter.addAll(dataT);
        }
    }
     public void simChecking(){
         new Detection().simChecking(photoUri , readyToBeRemovedList);
         for (int i = readyToBeRemovedList.size()-1 ; i>=0 ; i-- ){
             if (readyToBeRemovedList.get(i)== true){
                 System.out.println("Drop sim :" + photoUri.get(i));
                 photoUri.remove(i);
                 numOfPhotoSelected--;
             }
         }
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



    private double varianceCalculation(ArrayList<Integer> age, double averageAge, int numOfFace){
        double temp = 0;
        for (int i=0; i<age.size(); i++){
            temp += (age.get(i)-averageAge)*(age.get(i)-averageAge);
        }
        return temp/numOfFace;
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
                        PhotoUploadActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                            }
                        });
                    }
                }
            }).start();
        }
    }

    interface DetectCallback {
        void detectResult(JSONObject rst);
    }

    private Runnable detect_FaceAndSmile_worker = new Runnable() {
        public void run() {
            for (int k = 0; k < photoUri.size(); k++) {
                final Bitmap bmp = BitmapFactory.decodeFile(photoUri.get(k));
                FaceppDetect faceppDetect = new FaceppDetect();
                faceppDetect.setDetectCallback(new DetectCallback() {
                    public void detectResult(JSONObject rst) {
                        try {
                            //find out all faces
                            numOfFace = rst.getJSONArray("face").length();
                            System.out.println("TTT N" + numOfFace);
                            numberOfFaceList.add(numOfFace);
                            numOfFace=0;
                            readyToBeRemovedList.add(false);
                            for (int i = 0; i < numOfFace; ++i) {

                                //Way to detect smile
                                totalSmile += rst.getJSONArray("face").getJSONObject(i)
                                        .getJSONObject("attribute").getJSONObject("smiling").getInt("value");
                            }
                            if (numOfFace != 0) {
                                averageSmile = totalSmile / (double) numOfFace;
                                System.out.println("TTT S " + averageSmile);
                                totalSmile = 0;
                                averageSmile = 0;
                                averageSmileList.add(averageSmile);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            PhotoUploadActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                }
                            });
                        }
                    }
                });
                faceppDetect.detect(bmp);
                if ( k == photoUri.size()-1){FandSDetectorDone = true;}
            }
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.userButton) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void Close(View view) {
        finish();
    }
}