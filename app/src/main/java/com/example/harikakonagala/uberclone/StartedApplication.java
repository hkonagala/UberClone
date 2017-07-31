package com.example.harikakonagala.uberclone;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;

/**
 * Created by Harika Konagala on 7/30/2017.
 */

public class StartedApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("f3b7934cfcabcbecfe372e237eb06520fae15a49")
                .clientKey( "a7eb4a23d50f3a6fcc8bf7a7a6037489e8108607")
                .server("http://ec2-13-59-5-36.us-east-2.compute.amazonaws.com:80/parse/")
                .build()
        );


        //disabling automaticUser gives us the control over sign up and login in parse server
        //ParseUser.enableAutomaticUser();

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }
}
