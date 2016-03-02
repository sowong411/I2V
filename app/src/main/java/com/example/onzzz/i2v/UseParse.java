package com.example.onzzz.i2v;

import com.parse.Parse;

/**
 * Created by onzzz on 26/2/2016.
 */
public class UseParse extends android.app.Application {
    @Override
    public void onCreate(){
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this);
    }
}