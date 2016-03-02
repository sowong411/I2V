package com.example.onzzz.i2v;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ViewSwitcher;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    String[] photoUri = new String[MAX_PHOTO_SELECTED];

    String userObjectId;
    String eventObjectId;
    String photoObjectId;
    int newPhotoIndex;

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

        btnGalleryPickMul = (Button) findViewById(R.id.btnGalleryPickMul);
        btnGalleryPickMul.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
                startActivityForResult(i, 200);
            }
        });

        btnPhotoUpload = (Button) findViewById(R.id.btnUpload);
        btnPhotoUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < numOfPhotoSelected; i++) {
                    ParseObject photo = new ParseObject("Photo");
                    Bitmap bmp = BitmapFactory.decodeFile(getFile(i).getAbsolutePath());
                    String encodedString = encodeTobase64(bmp);
                    photo.put("Image", encodedString);
                    photo.put("Location", "");
                    photo.put("Time", "");
                    photo.put("Event", eventObjectId);
                    photo.put("UploadedBy", userObjectId);
                    photo.put("FaceNumber", 0);
                    photo.put("AverageSmileLevel", 0);
                    photo.put("MaleNumber", 0);
                    photo.put("FemaleNumber", 0);
                    photo.put("AverageAge", 0);
                    photo.saveInBackground();
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
                photoUri[numOfPhotoSelected] = string;
                numOfPhotoSelected++;
                dataT.add(item);
            }

            viewSwitcher.setDisplayedChild(0);
            adapter.addAll(dataT);
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

    private File getFile(int i) {
        File image_file = new File(photoUri[i]);
        return image_file;
    }


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