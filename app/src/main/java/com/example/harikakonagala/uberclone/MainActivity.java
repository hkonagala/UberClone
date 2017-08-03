package com.example.harikakonagala.uberclone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Switch user;
    Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        user = (Switch) findViewById(R.id.toggle_switch);
        start = (Button) findViewById(R.id.search_go_btn);

        start.setOnClickListener(this);

        if(ParseUser.getCurrentUser() == null){
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e == null){
                        Log.i("info", "anonymous login successful");
                    }else {

                        Log.i("info", "anonymous login failed");
                    }
                }
            });
        }else {
            if(ParseUser.getCurrentUser().get("riderOrDriver") !=null){
                Log.i("info", "redirecting as " + ParseUser.getCurrentUser().get("riderOrDriver"));
                redirect();
            }
        }
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    @Override
    public void onClick(View v) {
        //goto maps
        Log.i("switch value", String.valueOf(user.isChecked()));
        String userType = "rider";

        if(user.isChecked()){
            userType = "driver";
        }
        ParseUser.getCurrentUser().put("riderOrDriver", userType);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                redirect();
            }
        });
        Log.i("info", "redirecting as " + userType);

    }

    public void redirect(){
        if(ParseUser.getCurrentUser().get("riderOrDriver").equals("rider")){
            Intent intent = new Intent(MainActivity.this, RiderActivity.class);
            startActivity(intent);
        }else {
            Intent i = new Intent(MainActivity.this, ViewRequestActivity.class);
            startActivity(i);
        }
    }
}
