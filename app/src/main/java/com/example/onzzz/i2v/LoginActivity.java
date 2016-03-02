package com.example.onzzz.i2v;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /***Google Login***/
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

        /***Facebook Login***/
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
                final Profile profile = Profile.getCurrentProfile();

                ParseQuery<ParseObject> query = ParseQuery.getQuery("Account");
                query.whereEqualTo("Id", profile.getId());
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (objects.size() == 0) {
                            ParseObject user = new ParseObject("Account");
                            user.put("Name", profile.getName());
                            user.put("Id", profile.getId());
                            user.put("ProfilePicUri", profile.getProfilePictureUri(400, 400).toString());
                            user.put("LoginMethod", "Facebook");
                            user.saveInBackground();
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

        findViewById(R.id.skip_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

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
        return super.onOptionsItemSelected(item);
    }

    public void Close(View view) {
        finish();
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

    @Override
    public void onStart() {
        super.onStart();
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            final GoogleSignInAccount acct = result.getSignInAccount();

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Account");
            query.whereEqualTo("Id", acct.getId());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (objects.size() == 0) {
                        ParseObject user = new ParseObject("Account");
                        user.put("Name", acct.getDisplayName());
                        user.put("Id", acct.getId());
                        if (acct.getPhotoUrl().toString().equals(null)){
                            user.put("ProfilePicUri", "https://lh3.googleusercontent.com/-ey9WZml7lB0/AAAAAAAAAAI/AAAAAAAAAAA/J_iBGpIPDmA/photo.jpg");
                        }
                        else {
                            user.put("ProfilePicUri", acct.getPhotoUrl().toString());
                        }
                        user.put("LoginMethod", "Google");
                        user.saveInBackground();
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

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
}