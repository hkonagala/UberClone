package com.example.harikakonagala.uberclone;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ViewRequestActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter arrayAdapter;
    ArrayList<String> requests = new ArrayList<>();
    LocationManager locationManager;
    LocationListener locationListener;

    ArrayList<Float> requestLatitudes = new ArrayList<Float>();
    ArrayList<Float> requestLongitudes = new ArrayList<Float>();
    ArrayList<String> usernames = new ArrayList<String >();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request);
        setTitle("Nearby Requests");
        listView = (ListView) findViewById(R.id.request_list);

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, requests);
        requests.clear();
        requests.add("getting nearby locations...");
        listView.setAdapter(arrayAdapter);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateListView(location);
                ParseUser.getCurrentUser().put("location", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
                ParseUser.getCurrentUser().saveInBackground();
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

                    updateListView(lastKnownLocation);

                }


            }


        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (ActivityCompat.checkSelfPermission(ViewRequestActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ViewRequestActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(requestLatitudes.size() > position && requestLongitudes.size() > position && lastKnownLocation !=null && usernames.size() > position){

                    Intent intent = new Intent(ViewRequestActivity.this, DriverActivity.class);
                    intent.putExtra("requestLatitudes", requestLatitudes.get(position));
                    intent.putExtra("requestLongitudes", requestLongitudes.get(position));
                    intent.putExtra("driverLatitude",lastKnownLocation.getLatitude());
                    intent.putExtra("driverLongitude", lastKnownLocation.getLongitude());
                    intent.putExtra("username", usernames.get(position));
                    startActivity(intent);
                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_options, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.logout){
            ParseUser.logOut();
            Intent intent = new Intent(ViewRequestActivity.this, MainActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateListView(lastKnownLocation);
                }

            }
        }
    }
    
    public void updateListView(Location location){

        if(location !=null){
            requests.clear();
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
            final ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
            query.whereNear("location", point);
            //query.whereDoesNotExist("driverUsername");
            query.setLimit(10);

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null){
                        requests.clear();
                        requestLongitudes.clear();
                        requestLatitudes.clear();

                        if(objects.size() > 0){
                            for(ParseObject object : objects){
                                ParseGeoPoint requestGeoPoint = (ParseGeoPoint) object.get("location");
                                if(requestGeoPoint !=null){
                                    Double distanceInMiles = point.distanceInMilesTo(requestGeoPoint);
                                    Double distanceInOneDp = Double.valueOf(Math.round(distanceInMiles * 10)/ 10);
                                    requests.add(distanceInOneDp.toString() + "miles");

                                    requestLatitudes.add((float) requestGeoPoint.getLatitude());
                                    requestLongitudes.add((float) requestGeoPoint.getLongitude());
                                    usernames.add(object.getString("username"));
                                }


                            }

                        }else {
                            requests.add("no requests nearby");
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            });

        }

    }
}
