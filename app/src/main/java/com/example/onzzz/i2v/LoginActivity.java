package com.example.onzzz.i2v;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LoginActivity";

    private static final int RC_SIGN_IN = 9001;
    private SignInButton googleLoginButton;
    private GoogleApiClient mGoogleApiClient;

    private LoginButton fbLoginButton;
    private CallbackManager callbackManager;

    private boolean fbLogin;
    private boolean googleLogin;

    private String userObjectId;
    Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        // save template photo
        /*new Thread(){
            public void run(){saveTemplateImage();}
        }.start();*/

        /***************Google Login***************/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        googleLoginButton = (SignInButton) findViewById(R.id.googlelogin);
        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleLogin = true;
                fbLogin = false;
                signIn();
            }
        });
        googleLoginButton.setSize(SignInButton.SIZE_STANDARD);
        googleLoginButton.setScopes(gso.getScopeArray());

        /***************Facebook Login***************/
        fbLoginButton = (LoginButton)findViewById(R.id.fblogin);
        fbLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbLogin = true;
                googleLogin = false;
            }
        });
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
               // Toast.makeText(LoginActivity.this, "Facebook Login Succeed", Toast.LENGTH_SHORT).show();
                Snackbar snackbar = Snackbar
                        .make((findViewById(R.id.coordinatorLayoutLogin)), "Facebook Login Succeed", Snackbar.LENGTH_LONG);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                //    textView.setTextColor(Color.YELLOW);
                snackbar.show();

                final Profile profile = Profile.getCurrentProfile();

                /***************Check Existence of Same Facebook Account***************/
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Account");
                query.whereEqualTo("Id", profile.getId());
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (objects.size() == 0) {
                            /***************Upload Facebook Account Information***************/
                            ParseObject user = new ParseObject("Account");
                            user.put("Name", profile.getName());
                            user.put("Id", profile.getId());
                            user.put("Event", Arrays.asList());
                            user.put("Friends", Arrays.asList());
                            user.put("ProfilePicUri", profile.getProfilePictureUri(400, 400).toString());
                            user.put("LoginMethod", "Facebook");
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null){
                                        ParseQuery<ParseObject> accountQuery = ParseQuery.getQuery("Account");
                                        accountQuery.whereEqualTo("Id", profile.getId());
                                        accountQuery.findInBackground(new FindCallback<ParseObject>() {
                                            @Override
                                            public void done(List<ParseObject> objects, ParseException e) {
                                                if (e == null) {
                                                    userObjectId = objects.get(0).getObjectId();
                                                    Intent intent = new Intent();
                                                    intent.setClass(LoginActivity.this, MainActivity.class);
                                                    intent.putExtra("UserObjectId", userObjectId);
                                                    startActivity(intent);
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        } else {
                            userObjectId = objects.get(0).getObjectId();
                            Intent intent = new Intent();
                            intent.setClass(LoginActivity.this, MainActivity.class);
                            intent.putExtra("UserObjectId", userObjectId);
                            startActivity(intent);
                        }
                    }
                });
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        });
    }

    /***************Google Login Related Function***************/
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
         //   Toast.makeText(LoginActivity.this, "Google Login Succeed", Toast.LENGTH_SHORT).show();
            Snackbar snackbar = Snackbar
                    .make((findViewById(R.id.coordinatorLayoutLogin)), "Google Login Succeed", Snackbar.LENGTH_LONG);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            //    textView.setTextColor(Color.YELLOW);
            snackbar.show();

            // Signed in successfully, show authenticated UI.
            final GoogleSignInAccount acct = result.getSignInAccount();

            /***************Check Existence of Same Google Account***************/
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Account");
            query.whereEqualTo("Id", acct.getId());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (objects.size() == 0) {
                        /***************Upload Google Account Information***************/
                        ParseObject user = new ParseObject("Account");
                        user.put("Name", acct.getDisplayName());
                        user.put("Id", acct.getId());
                        // adding event array-yui
                        user.put("Event", Arrays.asList());
                        user.put("Friends", Arrays.asList());
                        if (acct.getPhotoUrl().toString().equals(null)){
                            user.put("ProfilePicUri", "https://lh3.googleusercontent.com/-ey9WZml7lB0/AAAAAAAAAAI/AAAAAAAAAAA/J_iBGpIPDmA/photo.jpg");
                        }
                        else {
                            user.put("ProfilePicUri", acct.getPhotoUrl().toString());
                        }
                        user.put("LoginMethod", "Google");
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null){
                                    ParseQuery<ParseObject> accountQuery = ParseQuery.getQuery("Account");
                                    accountQuery.whereEqualTo("Id", acct.getId());
                                    accountQuery.findInBackground(new FindCallback<ParseObject>() {
                                        @Override
                                        public void done(List<ParseObject> objects, ParseException e) {
                                            if (e == null) {
                                                userObjectId = objects.get(0).getObjectId();
                                                Intent intent = new Intent();
                                                intent.setClass(LoginActivity.this, MainActivity.class);
                                                intent.putExtra("UserObjectId", userObjectId);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        userObjectId = objects.get(0).getObjectId();
                        Intent intent = new Intent();
                        intent.setClass(LoginActivity.this, MainActivity.class);
                        intent.putExtra("UserObjectId", userObjectId);
                        startActivity(intent);
                    }
                }
            });
        } else {

        }
    }

    /***************Google Login Related Function***************/
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /***************Google Login Related Function***************/
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    //    getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (fbLogin) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        else if (googleLogin) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    private void saveTemplateImage(){
        String[] templateFileName = {"christmas1_1.jpg", "christmas1_2.jpg", "christmas1_3.jpg",
                "wedding1_1.jpg", "wedding1_2.jpg", "wedding1_3.jpg", "wedding1_4.jpg",
                "love2_1.jpg", "love2_2.jpg", "love2_3.jpg",
                "energetic_1.jpg","energetic_2.jpg","energetic_3.jpg",
                "black.jpg"};
        int[] resourceID = {R.drawable.christmas1_1, R.drawable.christmas1_2, R.drawable.christmas1_3,
                R.drawable.wedding1_1, R.drawable.wedding1_2, R.drawable.wedding1_3, R.drawable.wedding1_4,
                R.drawable.love2_1, R.drawable.love2_2, R.drawable.love2_3,
                R.drawable.energetic_1, R.drawable.energetic_2, R.drawable.energetic_3,
                R.drawable.black};

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize =1;
        for (int i=0; i<templateFileName.length; i++) {
            bmp = BitmapFactory.decodeResource(getResources(), resourceID[i], options);
            File f = new File("/sdcard/I2V/template/", templateFileName[i]);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
            }
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                bmp.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Path is here :  " + f.getAbsolutePath());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void Close(View view) {
        finish();
    }
}