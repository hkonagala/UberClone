package com.example.harikakonagala.uberclone;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Button call, logout;
    Boolean isActive = false;
    Boolean driverActive = true;

    TextView infoTextView;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        call = (Button) findViewById(R.id.call_uber);
        call.setOnClickListener(this);
        logout = (Button) findViewById(R.id.logout_btn);
        logout.setOnClickListener(this);
        infoTextView = (TextView) findViewById(R.id.infoTextView);


        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null && objects.size() >0 ){
                    isActive = true;
                    call.setText("cancel uber");

                    checkForUpdates();
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateMap(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


            } else {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lastKnownLocation != null) {

                    updateMap(lastKnownLocation);

                }


            }


        }
    }

    private void updateMap(Location location) {

        if(driverActive == false){

            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(userLocation).title("your location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateMap(lastKnownLocation);
                }

            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.logout_btn:
                ParseUser.logOut();
                Intent intent = new Intent(RiderActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.call_uber:
                if(isActive){

                    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
                    query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if(e == null && objects.size() >0 ){
                                for(ParseObject object : objects){
                                    object.deleteInBackground();
                                }
                                isActive = false;
                                call.setText("call uber");
                            }
                        }
                    });

                }else{

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(lastKnownLocation !=null){
                            ParseObject request = new ParseObject("Request");
                            request.put("username", ParseUser.getCurrentUser().getUsername());
                            ParseGeoPoint point = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            request.put("location", point);

                            request.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e == null){
                                        call.setText("cancel uber");
                                        isActive = true;

                                    }
                                }
                            });
                        }else{
                            Toast.makeText(getApplicationContext(),"could not find location", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                break;
        }
    }

    private void checkForUpdates() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.whereExists("driverUsername");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null && objects.size() > 0){

                    driverActive = true;
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo("username", objects.get(0).getString("driverUsername"));
                    query.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> objects, ParseException e) {
                            if(e == null && objects.size()>0 ){
                                ParseGeoPoint driverLocation = objects.get(0).getParseGeoPoint("location");
                                if (ContextCompat.checkSelfPermission(RiderActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    if(lastKnownLocation !=null) {
                                        ParseGeoPoint userLocation = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                                        Double distanceInMiles = driverLocation.distanceInMilesTo(userLocation);

                                        if (distanceInMiles < 0.01) {

                                            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
                                            query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());

                                            query.findInBackground(new FindCallback<ParseObject>() {
                                                @Override
                                                public void done(List<ParseObject> objects, ParseException e) {
                                                    if(e == null){
                                                        for(ParseObject object: objects){
                                                            object.deleteInBackground();
                                                        }
                                                    }
                                                }
                                            });
                                            infoTextView.setText("your driver is here");
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {

                                                    infoTextView.setText("");
                                                    call.setVisibility(View.VISIBLE);
                                                    call.setText("call uber");
                                                    isActive = false;
                                                    driverActive = false;





                                                }
                                            }, 5000);
                                        } else {


                                        Double distanceInOneDp = Double.valueOf(Math.round(distanceInMiles * 10) / 10);
                                        infoTextView.setText("your driver is " + distanceInOneDp.toString() + " miles away");


                                        LatLng driverLocationLatlng = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());
                                        LatLng requestLocationLatlng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

                                        ArrayList<Marker> markers = new ArrayList<>();
                                            mMap.clear();
                                        markers.add(mMap.addMarker(new MarkerOptions().position(driverLocationLatlng).title("Driver location")));
                                        markers.add(mMap.addMarker(new MarkerOptions().position(requestLocationLatlng).title("your location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                        for (Marker marker : markers) {
                                            builder.include(marker.getPosition());
                                        }

                                        LatLngBounds bounds = builder.build();
                                        int padding = 60;
                                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                        mMap.animateCamera(cameraUpdate);
                                            call.setVisibility(View.VISIBLE);

                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {

                                                }
                                            }, 2000);

                                    }
                                    }

                                }
                            }
                        }
                    });


                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        checkForUpdates();
                    }
                }, 2000);
            }
        });
    }
}
