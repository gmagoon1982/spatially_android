package com.spatially.spatially_android;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pub.devrel.easypermissions.EasyPermissions;

public class activity_addfence extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    MapView mMapView;
    private GoogleMap googleMap;
    private String email, password;

    // Starting with 50m radius for fence;
    double default_fence_radius = 50;
    LatLng center = new LatLng(0, 0);
    float addfence_zoomlevel;

    EditText titlefence;
    ImageButton decrease;
    ImageButton increase;
    ImageButton home;
    EditText Edit;
    Button Done, Cancel;
    ImageButton changemap;
    TextView AddFence;
    Integer map_type;

    // Empty name by default
    String fence_name="";
    Integer code = 1;

    Marker marker;
    Toast toast;
    Boolean live;
    Location oldLocation;
    Intent LocationServiceGPS;

    Boolean logging=false;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            if(getApplicationContext()!=null) {
                sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

                if(sharedPref.contains("logging"))
                {
                    logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
                }

                if (getIntent().hasExtra("email")) {
                    email = getIntent().getExtras().getString("email");
                }
                if (getIntent().hasExtra("password")) {
                    password = getIntent().getExtras().getString("password");
                }
                if(getIntent().hasExtra("live"))
                {
                    live = getIntent().getBooleanExtra("live", false);
                    LocationServiceGPS = new Intent(getApplicationContext(), locationservice_fuse.class);
                    LocationServiceGPS.putExtra("live", live);
                }

                setContentView(R.layout.activity_addfence);

                this.getSupportActionBar().hide();
                setTheme(R.style.AppTheme_NoActionBar);

                titlefence = (EditText) findViewById(R.id.Edit);
                decrease = (ImageButton) findViewById(R.id.decrease);
                increase = (ImageButton) findViewById(R.id.increase);
                home = (ImageButton) findViewById(R.id.home);
                Edit = (EditText) findViewById(R.id.Edit);
                Done = (Button) findViewById(R.id.Done);
                Cancel = (Button) findViewById(R.id.Cancel);
                changemap = (ImageButton) findViewById(R.id.changemap);
                AddFence = (TextView) findViewById(R.id.AddFence);

                mMapView = (MapView) findViewById(R.id.mapView);

                mMapView.onCreate(savedInstanceState);
                mMapView.onResume();

                try {
                    if(getApplicationContext()!=null)
                    {
                        MapsInitializer.initialize(getApplicationContext());
                    }
                } catch (Exception e) {

                    if(getApplicationContext()!=null) {
                        ShowErrorDetails(Log.getStackTraceString(e), getApplicationContext());
                    }

                    /*
                    toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                     */

                    if(logging==true)
                    {
                        Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                    }

                }

                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap mMap) {
                        googleMap = mMap;

                        googleMap.getUiSettings().setMapToolbarEnabled(false);

                        addfence_zoomlevel = Float.parseFloat(getResources().getString(R.string.addfence_zoomlevel));

                        // map_type = 0, 1, 2 meaning NORMAL, HYBRID, SATELLITE
                        if (getIntent().hasExtra("map_type")) {

                            map_type = getIntent().getExtras().getInt("map_type");
                            googleMap.setMapType(map_type);

                        } else {
                            set_map_type(0);
                        }

                        // latitude and longitude are passed in as extra keys, then set camera location to that position
                        if ((getIntent().hasExtra("latitude")) && (getIntent().hasExtra("longitude"))) {

                            Double latitude = getIntent().getExtras().getDouble("latitude");
                            Double longitude = getIntent().getExtras().getDouble("longitude");

                            //System.out.println("Latitude arguments: " + getIntent().getExtras().getDouble("latitude"));
                            //System.out.println("Longitude arguments: " + getIntent().getExtras().getDouble("longitude"));

                            googleMap.clear();

                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), addfence_zoomlevel));
                            CircleOptions fence_circleoptions = new CircleOptions().radius(default_fence_radius).center(new LatLng(latitude, longitude)).strokeColor(getResources().getColor(R.color.add_fence_border_colour)).fillColor(getResources().getColor(R.color.add_fence_fill_colour)).strokeWidth(getResources().getDimension(R.dimen.add_fence_border_width));
                            googleMap.addCircle(fence_circleoptions);
                            googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)));

                            // else start with defaults (0,0)
                        } else {

                            googleMap.clear();
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, addfence_zoomlevel));
                            CircleOptions fence_circleoptions = new CircleOptions().radius(default_fence_radius).center(center).strokeColor(getResources().getColor(R.color.add_fence_border_colour)).fillColor(getResources().getColor(R.color.add_fence_fill_colour)).strokeWidth(getResources().getDimension(R.dimen.add_fence_border_width));
                            googleMap.addCircle(fence_circleoptions);
                            googleMap.addMarker(new MarkerOptions().position(new LatLng(center.latitude, center.longitude)));
                        }

                        Cancel = (Button) findViewById(R.id.Cancel);
                        Cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        });


                        Button Done = (Button) findViewById(R.id.Done);
                        Done.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if (titlefence.getText().toString().equals("") || titlefence.getText() == null) {
                                    toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.need_valid_name_for_fence), Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();

                                    if(logging==true) {
                                        Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.need_valid_name_for_fence));
                                    }

                                } else {

                                    // Making sure latitude and longitude have 3 decimal places
                                    DecimalFormat df = new DecimalFormat(getResources().getString(R.string.No_of_Decimals_for_add_fence));
                                    df.setRoundingMode(RoundingMode.CEILING);

                                    // Hard coded host and port for the server are in the strings.xml resources file
                                    String URL = sharedPref.getString("Spatially_Host", getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/add-fence.cgi?email=" + email + "&password=" + password + "&fence_name=" + titlefence.getText().toString() + "&lat=" + df.format(center.latitude) + "&lng=" + df.format(center.longitude) + "&radius=" + default_fence_radius;

                                    OkHttpClient client = new OkHttpClient();

                                    Request request = new Request.Builder()
                                            .url(URL)
                                            .build();

                                    // Asynchronous Get
                                    // Runs in a background thread (off the main thread)
                                    client.newCall(request).enqueue(new Callback() {


                                        @Override
                                        public void onFailure(Call call, IOException e) {

                                            if(logging==true) {
                                                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                                            }

                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {

                                            String reply = response.body().string().trim();

                                            if (response.isSuccessful()) {

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        if(reply.equals("OK"))
                                                        {
                                                            Vibrate(Long.parseLong(getResources().getString(R.string.fence_delete_vibration_time)) / 10);
                                                            toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.fence_added), Toast.LENGTH_LONG);
                                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                                            toast.show();

                                                            // Here the fence has been added now need to update list

                                                        }
                                                        else if(reply.equals("ERROR"))
                                                        {
                                                            toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_occurred), Toast.LENGTH_LONG);
                                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                                            toast.show();
                                                        }

                                                        finish();

                                                    }
                                                });


                                            }

                                        }

                                    });

                                }

                            }
                        });

                        increase.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                googleMap.clear();
                                default_fence_radius = default_fence_radius * 2;
                                CircleOptions fence_circleoptions = new CircleOptions().radius(default_fence_radius).center(center).strokeColor(getResources().getColor(R.color.add_fence_border_colour)).fillColor(getResources().getColor(R.color.add_fence_fill_colour)).strokeWidth(getResources().getDimension(R.dimen.add_fence_border_width));
                                googleMap.addCircle(fence_circleoptions);
                                marker = km_or_m_marker();
                                marker.hideInfoWindow();

                            }
                        });

                        decrease.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                googleMap.clear();
                                default_fence_radius = default_fence_radius / 2;

                                if (default_fence_radius > 50) {
                                    CircleOptions fence_circleoptions = new CircleOptions().radius(default_fence_radius).center(center).strokeColor(getResources().getColor(R.color.add_fence_border_colour)).fillColor(getResources().getColor(R.color.add_fence_fill_colour)).strokeWidth(getResources().getDimension(R.dimen.add_fence_border_width));
                                    googleMap.addCircle(fence_circleoptions);
                                    marker = km_or_m_marker();
                                    marker.hideInfoWindow();
                                }

                            }

                        });

                        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {

                                marker.showInfoWindow();
                                return true;
                            }
                        });

                        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                            @Override
                            public void onCameraMove() {

                                center = new LatLng(googleMap.getCameraPosition().target.latitude, googleMap.getCameraPosition().target.longitude);

                                addfence_zoomlevel = (googleMap.getCameraPosition()).zoom;

                                googleMap.clear();
                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(center));

                                marker = km_or_m_marker();
                                marker.hideInfoWindow();

                                CircleOptions circleOptions = new CircleOptions();
                                circleOptions.center(googleMap.getCameraPosition().target);
                                circleOptions.radius(default_fence_radius);
                                circleOptions.strokeColor(getResources().getColor(R.color.add_fence_border_colour)).fillColor(getResources().getColor(R.color.add_fence_fill_colour));
                                circleOptions.strokeWidth(getResources().getDimension(R.dimen.add_fence_border_width));
                                googleMap.addCircle(circleOptions);

                            }


                        });

                        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                            @Override
                            public void onCameraIdle() {

                                if(marker!=null)
                                {
                                    marker.showInfoWindow();
                                }

                            }
                        });
                        // This is the map selection type button
                        // Changes between Map / Satellite / Hybrid
                        changemap.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String[] maptype = {"Standard", "Hybrid", "Satellite"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(activity_addfence.this, R.style.AlertDialogStyle);

                                builder.setTitle("Select Map Type");

                                builder.setSingleChoiceItems(maptype, -1, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int item) {

                                        map_type = item;
                                        set_map_type(map_type);
                                        dialog.dismiss();

                                    }
                                });

                                AlertDialog alert = builder.create();
                                alert.show();

                            }

                        });

                        // This is the gps location button
                        // This shows the user their current location
                        // https://www.thecrazyprogrammer.com/2017/01/how-to-get-current-location-in-android.html
                        // https://stackoverflow.com/questions/22471100/how-to-show-current-position-marker-on-map-in-android
                        home.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.

                                    toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.enable_locations), Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();

                                    if(logging==true) {
                                        Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.enable_locations));
                                    }

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, code);
                                    }
                                    else
                                    {
                                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, code);
                                    }

                                }

                            }
                        });

                        // This is used to capture entered text into edittext box
                        titlefence.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {

                                if (!fence_name.equals("")) {

                                    fence_name = titlefence.getText().toString();

                                }
                            }
                        });


                    }


                });

            }

        }

         catch (Exception e) {

             if(getApplicationContext()!=null) {
                 ShowErrorDetails(Log.getStackTraceString(e), getApplicationContext());
             }

            /*
            toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

             */

             if(logging==true) {
                 Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
             }

            return;
        }

    }

    public void set_map_type(int map_type) {

        if (map_type == 0) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            changemap.setColorFilter(Color.BLACK);
            titlefence.setTextColor(Color.BLACK);
            decrease.setColorFilter(Color.BLACK);
            increase.setColorFilter(Color.BLACK);
            home.setColorFilter(Color.BLACK);
            Edit.setTextColor(Color.BLACK);
            Edit.setHintTextColor(Color.BLACK);
            Done.setTextColor(Color.BLACK);
            Cancel.setTextColor(Color.BLACK);
            AddFence.setTextColor(Color.BLACK);

        } else if (map_type == 1) {

            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            changemap.setColorFilter(Color.WHITE);
            titlefence.setTextColor(Color.WHITE);
            decrease.setColorFilter(Color.WHITE);
            increase.setColorFilter(Color.WHITE);
            home.setColorFilter(Color.WHITE);
            Edit.setTextColor(Color.WHITE);
            Edit.setHintTextColor(Color.WHITE);
            Done.setTextColor(Color.WHITE);
            Cancel.setTextColor(Color.WHITE);
            AddFence.setTextColor(Color.WHITE);

        } else if (map_type == 2) {

            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            changemap.setColorFilter(Color.WHITE);
            titlefence.setTextColor(Color.WHITE);
            decrease.setColorFilter(Color.WHITE);
            increase.setColorFilter(Color.WHITE);
            home.setColorFilter(Color.WHITE);
            Edit.setTextColor(Color.WHITE);
            Edit.setHintTextColor(Color.WHITE);
            Done.setTextColor(Color.WHITE);
            Cancel.setTextColor(Color.WHITE);
            AddFence.setTextColor(Color.WHITE);

        }
    }

    private Marker km_or_m_marker(){

        if(default_fence_radius < 1000) {

            MarkerOptions markerOptions = new MarkerOptions().title("Lat: " + new DecimalFormat("#.##").format(center.latitude) + ", Lng: " + new DecimalFormat("#.##").format(center.longitude) + ", Radius: " + default_fence_radius + "m").position(new LatLng(center.latitude, center.longitude));
            Marker marker = googleMap.addMarker(markerOptions);
            return (marker);

        }
        else
        {
            MarkerOptions markerOptions = new MarkerOptions().title("Lat: " + new DecimalFormat("#.##").format(center.latitude) + ", Lng: " + new DecimalFormat("#.##").format(center.longitude) + ", Radius: " + (default_fence_radius /1000) + "km").position(new LatLng(center.latitude, center.longitude));
            Marker marker = googleMap.addMarker(markerOptions);
            return (marker);
        }

    }

    public void Vibrate(Long time_ms) {

        Vibrator v = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(time_ms);

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();


    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(getApplicationContext()!=null)
        {

            if(receiver!=null)
            {
                unregisterReceiver(receiver);
            }
        }

    }

    private void ShowErrorDetails(String exception_message, Context context)
    {

        if(context!=null)
        {

            sharedPref = context.getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

            if(sharedPref!=null && exception_message!=null)
            {
                if(sharedPref.contains("Errors") && sharedPref.getBoolean("Errors", getResources().getBoolean(R.bool.Errors)))
                {
                Dialog dialog = new Dialog(context);
                dialog.setCanceledOnTouchOutside(true);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.error_layout, null);
                TextView message = view.findViewById(R.id.message);
                message.setText(exception_message);
                dialog.setContentView(view);
                dialog.show();
                }

            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

    }

    private void startLocationService() {

        // The location tracking service is started in the background
        if(LocationServiceGPS!=null)
        {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            {
                startService(LocationServiceGPS);
            }
            else
            {
                startForegroundService(LocationServiceGPS);
            }
        }

        intentFilter = new IntentFilter("LocationUpdates");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Location location = new Location("LocationUpdates");
                location.setLatitude(intent.getFloatExtra("Latitude", 0));
                location.setLongitude(intent.getFloatExtra("Longitude", 0));
                location.setAccuracy(intent.getFloatExtra("Accuracy", 0));
                location.setTime(intent.getLongExtra("Time", 0));

                if (location != null) {

                    if (oldLocation != null) {

                        LatLngBounds.Builder center_point = new LatLngBounds.Builder();
                        center_point.include(new LatLng(oldLocation.getLatitude(), oldLocation.getLongitude()));
                        center_point.include(new LatLng(location.getLatitude(), location.getLongitude()));

                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(center_point.build().getCenter())
                                .bearing(oldLocation.bearingTo(location))
                                .zoom(addfence_zoomlevel)
                                .build();

                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            googleMap.setMyLocationEnabled(true);
                        }

                        if (googleMap.isMyLocationEnabled()) {

                            googleMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
                                @Override
                                public void onMyLocationClick(@NonNull Location location) {
                                    toast.setDuration(Toast.LENGTH_SHORT);
                                    toast.setText(getResources().getString(R.string.control_map_for_zoom));
                                    toast.show();
                                }
                            });

                            // Only if location is enabled, it will be saved LOCALLY

                        }

                    }

                    //Toast.makeText(getApplicationContext(), "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    oldLocation = location;
                }


                //Toast.makeText(getContext(), "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude(), Toast.LENGTH_LONG).show();

            }


        };

        if(!receiver.isOrderedBroadcast())
        {
            registerReceiver(receiver, intentFilter);
        }

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

        startLocationService();

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setText(getResources().getString(R.string.permission_denied_request_cancelled));
                    toast.show();
                }
            });


    }


}



