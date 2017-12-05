package com.example.meet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static android.widget.Toast.LENGTH_LONG;
import static com.example.meet.R.id.map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    Circle mCircle;
    Marker mMarkerA;
    double Lat, Lang;
    public String url;
    public static String guid, glat, glang, gtype;
    private static long back_pressed;
    private static int uid;
    private static String lat;
    private static String lang;
    private static String type;
    public int i = 0;
    double radiusInMeters = 1000.0;
    int strokeColor = 0xffff0000; //Color Code you want
    int shadeColor = 0x44ff0000; //opaque red fill
    private static final String REGISTER_URL = "http://juspay-com.stackstaging.com/location.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Array List to Store all the details of the contact
        contactList = new ArrayList<>();

        //URL to return all the location in the database
        url = "http://juspay-com.stackstaging.com/returnLoc.php";

        getSupportActionBar().setTitle("Map Location Activity");

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFrag.getMapAsync(this);

        //Get the uid from login to fetch the particular ID's details
        Intent mIntent = getIntent();
        Bundle bundle = mIntent.getExtras();
        if (bundle != null) {
            uid = bundle.getInt("uid");
        }
    }

    @Override
    public void onPause() {
        //onPause, remove Location Updates
        super.onPause();
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        //When map is ready, set googlemap variable
        mGoogleMap = googleMap;
        //Custom styling of google map (Dark theme)
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json);
        googleMap.setMapStyle(style);
        //Check permission and set my marker
        mymarker();
    }


    public void mymarker() {
        //Check permissions before enabling MY_LOCATION
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();

                mGoogleMap.setMyLocationEnabled(true);

            }
        }
    }

    //Function to register(POST) current user's location to Database
    private void register(final String lat, final String lang, final String suid, final String status) {
        class RegisterUser extends AsyncTask<String, Void, String> {

            //Perform the POST request through Async Task
            RegisterUserClass ruc = new RegisterUserClass();


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

            }

            //In background thread, send the Latitude,Longitutde,Uid and status to database
            @Override
            protected String doInBackground(String... params) {

                HashMap<String, String> data = new HashMap<>();
                data.put("lat", params[0]);
                data.put("lang", params[1]);
                data.put("uid", params[2]);
                //Satus is 1 = User already exists and update his location
                //Status is 0 = User location is new so insert the location
                data.put("status", params[3]);
                String result = ruc.sendPostRequest(REGISTER_URL, data);
                return result;

            }
        }

        RegisterUser ru = new RegisterUser();
        ru.execute(lat, lang, suid, status);

    }


    //Setting GoogleApiClient Variable
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        //When the device is connected, request for the current location of the user
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


    @Override
    public void onLocationChanged(Location location) {
        //When the current location of the user is changed, it has to be Updated
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Hide Action Bar
        getSupportActionBar().hide();

        //Setting Marker to my current Location
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
        mCurrLocationMarker.setTitle("me");

        //Adding a vicinity circle. A circle is drawn around the current location with a defined radius in meters
        CircleOptions addCircle = new CircleOptions().center(latLng).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        mCircle = mGoogleMap.addCircle(addCircle);

        //Get latitude and Longitude of the current location of User
        Lat = location.getLatitude();
        Lang = location.getLongitude();

        //Send the current location co-ordinates to the database
        int status = 1;
        register(String.valueOf(Lat), String.valueOf(Lang), String.valueOf(uid), String.valueOf(status));

        //When clicked on a marker, that particular marker's uid should be passed to next activity
        //So that,that marker's details can be displayed
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //If the clicked marker is not my current location
                if (!(marker.getTitle().equals("me"))) {

                    //Get the uid and send it to next activity
                    int uuid = Integer.parseInt(marker.getTitle());
                    Intent i = new Intent(MapsActivity.this, Teacher.class);
                    i.putExtra("uuid", (int) uuid);
                    startActivity(i);
                    finish();

                }
                //If clicked on own location, display "this is your location"
                else
                    Toast.makeText(getApplicationContext(), "This is your location", Toast.LENGTH_SHORT).show();

                return true;

            }
        });

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(14));


        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        }

        //Execute GET Request
        new GetContacts().execute();
    }


    private String TAG = MapsActivity.class.getSimpleName();

    //ArrayList of ArrayList to get multiple Json Values (2D JSON)
    ArrayList<ArrayList<String>> contactList;

    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    //Fetching the JSON Object
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        //GET request done in background
        @Override
        protected Void doInBackground(Void... arg0) {
            //Calling the HTTPHandler
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {

                    //Create JSON Object
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    //Create Json Array
                    JSONArray contacts = jsonObj.getJSONArray("result");

                    //Fetch all the Values from the JSON OBject
                    for (i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        guid = c.getString("uid");
                        glat = c.getString("lat");
                        glang = c.getString("lang");
                        gtype = c.getString("type");

                        //Get all the attribute values from the Json Object
                        ArrayList<String> contact = new ArrayList<>();

                        contact.add(guid);
                        contact.add(glat);
                        contact.add(glang);
                        contact.add(gtype);

                        contactList.add(contact);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //On UI Thread, perform Front-End tasks
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /*
                    * Contact list is a array list of array list
                    * Here Multiple values for every uid is sent
                    * Every values of each uid is stored in a 1 dimensional arraylist
                    * Multiple of these values are again stored in another arraylist
                    * Hence, its a ArrayList of ArrayList
                    * */

                    /*
                    * Format of this ArrayList is - j is for traversing multiple uid values
                    * Insisde each array list {"uid","lat","lang","type"}
                    * Hence I have used index 0 to fetch uid,1 to fetch Latitude, 2 to fetch Longitude,3 to fetch type i.e student or teacher
                    * */

                    for (int j = 0; j < contactList.size(); j++) {
                        //Setting Marker for student
                        if ((contactList.get(j).get(3)).equals(String.valueOf(4))) {
                            //An if condition to prevent creation of marker on my own location
                            if (!(contactList.get(j).get(0)).equals(String.valueOf(uid))) {
                                mMarkerA = mGoogleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(Double.parseDouble(contactList.get(j).get(1)), Double.parseDouble(contactList.get(j).get(2)))).draggable(true));
                                mMarkerA.setTitle(contactList.get(j).get(0));

                                //Set Student image marker for student
                                mMarkerA.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.smarker));

                                //Check distance from my location to marker location, if greater than given radius, remove marker
                                double distance = SphericalUtil.computeDistanceBetween(mCurrLocationMarker.getPosition(), mMarkerA.getPosition());
                                if (distance > radiusInMeters) mMarkerA.remove();
                            }

                        }
                        //Setting Marker for teacher
                        else if ((contactList.get(j).get(3)).equals(String.valueOf(5))) {
                            //An if condition to prevent creation of marker on my own location
                            if (!(contactList.get(j).get(0)).equals(String.valueOf(uid))) {
                                mMarkerA = mGoogleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(Double.parseDouble(contactList.get(j).get(1)), Double.parseDouble(contactList.get(j).get(2)))).draggable(true));
                                mMarkerA.setTitle(contactList.get(j).get(0));

                                //Set Student image marker for teacher
                                mMarkerA.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.tmarker));

                                //Check distance from my location to marker location, if greater than given radius, remove marker
                                double distance = SphericalUtil.computeDistanceBetween(mCurrLocationMarker.getPosition(), mMarkerA.getPosition());
                                if (distance > radiusInMeters) mMarkerA.remove();
                            }

                        }

                    }
                }

            });
        }


    }


    @Override
    public void onBackPressed() {
        //On backpress twice within 2000 milliseconds, perform logout
        if (back_pressed + 2000 > System.currentTimeMillis()) {

            Intent i8 = new Intent(MapsActivity.this, MainActivity.class);
            startActivity(i8);
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Press back again to Logout ", Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }
    }
}
