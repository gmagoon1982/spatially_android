package com.spatially.spatially_android;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pub.devrel.easypermissions.EasyPermissions;

public class map_fragment_gps extends Fragment implements EasyPermissions.PermissionCallbacks {

    MapView mMapView;
    private GoogleMap googleMap;
    String URL;
    SharedPreferences sharedPref;
    String email, password;
    View rootView;
    ImageButton gps, changemap, showallfriends;
    ImageButton back, forward;

    float gps_zoomlevel;

    // GPS related variables
    String mprovider;
    Integer code;

    LocationManager locationManager;

    Location oldLocation;

    Boolean live;
    Toast toast;

    Boolean logging = false;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    Integer API;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_map_gps, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);

        return rootView;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {

            API = android.os.Build.VERSION.SDK_INT;

            if (getArguments() != null && getArguments().containsKey("email") && getArguments().containsKey("password")) {
                email = this.getArguments().getString("email").trim();
                password = this.getArguments().getString("password").trim();
            }

            back = (ImageButton) rootView.findViewById(R.id.back);
            forward = (ImageButton) rootView.findViewById(R.id.forward);
            back.setVisibility(View.GONE);
            forward.setVisibility(View.GONE);

            code = Integer.parseInt(getResources().getString(R.string.permissions_code));

            if (getActivity() != null) {
                changemap = (ImageButton) getActivity().findViewById(R.id.changemap);
                gps = (ImageButton) getActivity().findViewById(R.id.gps);
                showallfriends = (ImageButton) getActivity().findViewById(R.id.showallfriends);

                sharedPref = getActivity().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

                if (sharedPref.contains("logging")) {
                    logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
                }

            }

            //locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            // Required to instantiate map
            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();

            if (getActivity() != null) {
                try {
                    if (getActivity() != null) {
                        MapsInitializer.initialize(getActivity());
                    }
                } catch (Exception e) {

                    if (getContext() != null) {
                        ShowErrorDetails(Log.getStackTraceString(e), getContext());
                    }

                        /*
                        toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        */

                    if (logging == true) {
                        Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                    }
                }
            }

            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {

                    googleMap = mMap;

                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                    gps_zoomlevel = Float.parseFloat(getResources().getString(R.string.gps_zoomlevel));

                    // Disables unwanted  google maps features
                    googleMap.getUiSettings().setMapToolbarEnabled(false);
                    googleMap.getUiSettings().setMyLocationButtonEnabled(false);

                    if (getActivity() != null) {
                        if (!((MainActivity) getActivity()).live_location_self) {

                            live = false;
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, code);

                        } else {

                            live = true;
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, code);

                        }

                        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                            @Override
                            public void onCameraMove() {
                                CameraPosition position = googleMap.getCameraPosition();
                                gps_zoomlevel = position.zoom;
                                //System.out.println("map_fragment_gps camera zoom level: " + position.zoom);
                            }
                        });


                    }
                }
            });


            gps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new map_fragment_gps())
                            .commit();

                }
            });

            showallfriends.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new map_fragment_show_all_friends())
                            .commit();

                }
            });

            // This is the map selection type button
            // Changes between Map / Satellite / Hybrid
            changemap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String[] maptype = {"Standard", "Hybrid", "Satellite"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);

                    builder.setTitle("Select Map Type");

                    builder.setSingleChoiceItems(maptype, -1, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int item) {

                            set_map_type(item);

                            //Toast.makeText(getContext(), "Map type selected: " + googleMap.getMapType(), Toast.LENGTH_LONG).show();

                            dialog.dismiss();


                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();

                }

            });


        } catch (Exception e) {

            if (getContext() != null) {

                ShowErrorDetails(Log.getStackTraceString(e), getContext());

                /*
                toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                 */
            }

            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

            return;
        }

    }

    public float getBatteryLevel() {

        try {

            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.get_battery_level));
            }

            // We are registering a broadcast receiver to the ACTION_BATTERY_CHANGED Intent to get
            // the battery levels. With IntentFilter you can filter out intent for a specific event
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = getContext().registerReceiver(null, ifilter);
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = (float) level / (float) scale;
            return batteryPct;


        } catch (Exception e) {

            if (getContext() != null) {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
                            /*
                            toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                             */
            }

            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

            return 0;


        }
    }

    public void saveUserInfo(Location location) {
        // Need to save time / date of last user movement to sharedpreferences
        // For display under You
        SharedPreferences.Editor editor = sharedPref.edit();
        SimpleDateFormat formatter = new SimpleDateFormat("d MMM, h:mm a");
        String value = formatter.format(location.getTime());
        editor.putString("user_time", value);
        editor.putString("user_latitude", String.valueOf(location.getLatitude()));
        editor.putString("user_longitude", String.valueOf(location.getLongitude()));
        editor.commit();

    }

    public void set_map_type(int map_type) {

        if (map_type == 0) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            back.setColorFilter(Color.BLACK);
            forward.setColorFilter(Color.BLACK);
            showallfriends.setColorFilter(Color.BLACK);
            gps.setColorFilter(Color.BLACK);
            changemap.setColorFilter(Color.BLACK);

        } else if (map_type == 1) {

            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            back.setColorFilter(Color.WHITE);
            forward.setColorFilter(Color.WHITE);
            showallfriends.setColorFilter(Color.WHITE);
            gps.setColorFilter(Color.WHITE);
            changemap.setColorFilter(Color.WHITE);

        } else if (map_type == 2) {

            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            back.setColorFilter(Color.WHITE);
            forward.setColorFilter(Color.WHITE);
            showallfriends.setColorFilter(Color.WHITE);
            gps.setColorFilter(Color.WHITE);
            changemap.setColorFilter(Color.WHITE);

        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() != null) {
            toast = Toast.makeText(getActivity(), getResources().getString(R.string.loading_location_data), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

        //System.out.println("Modern phone (API>23)");
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

            /*

         else {

            //System.out.println("Ordinary phone (API<23)");

            /*
            System.out.println("requestCode: " + requestCode);
            System.out.println("permissions: ");
            for(int i=0; i<permissions.length; i++)
            {
                System.out.println(permissions[i]);
            }

            System.out.println("grantResults: ");
            for(int i=0; i<permissions.length; i++)
            {
                System.out.println(grantResults[i]);
            }


            if(grantResults.length == 1)
                    {
                    if(permissions[0] ==  Manifest.permission.ACCESS_COARSE_LOCATION)
                    {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        {
                            live = false;
                            //System.out.print("Permission granted for coarse!");
                            startLocationService();

                        } else
                        {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toast.setDuration(Toast.LENGTH_SHORT);
                                    toast.setText("Permission was denied or request was cancelled");
                                    toast.show();
                                }
                            });
                        }
                    }
                    else if(permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION)
                    {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        {
                            live = true;
                            //System.out.print("Permission granted for fine!");
                            startLocationService();

                        } else
                        {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toast.setDuration(Toast.LENGTH_SHORT);
                                    toast.setText("Permission was denied for fine accuracy, using coarse accuracy instead!");
                                    toast.show();
                                }
                            });

                            live = false;
                            startLocationService();

                        }

                    }


                }

                */


    }

    public void WriteToLog(String URL, Boolean logging) {

        // Using Log.i because Log.i stays after compilation, easy to read using Log Viewer App
        // sdcard0 is the external sd card

        File logFile = new File("sdcard/spatially.log");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {

                if (getContext() != null) {
                    ShowErrorDetails(Log.getStackTraceString(e), getContext());
                }

                if (logging == true) {
                    Log.i(Resources.getSystem().getString(R.string.TAG), Log.getStackTraceString(e));
                }

            }
        }

        try {
            // BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            buf.append("\n" + dateFormat.format(date) + " " + URL);
            buf.close();

        } catch (IOException e) {
            if (getContext() != null) {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
            }

            if (logging == true) {
                Log.i(Resources.getSystem().getString(R.string.TAG), Log.getStackTraceString(e));
            }

        }


    }

    private void sendToSpatiallyServer(String URL) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(URL)
                .build();

        if (logging == true) {
            Log.i(getResources().getString(R.string.TAG), URL);
        }

        // Runs in background thread by default
        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {

                if (getContext() != null) {
                    ShowErrorDetails(Log.getStackTraceString(e), getContext());
                }

                if (logging == true) {
                    Log.i(Resources.getSystem().getString(R.string.TAG), Log.getStackTraceString(e));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String reply = response.body().string().trim();

                if (!((reply.split(",")[0]).trim()).equals("OK")) {

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (getContext() != null) {
                                    toast = Toast.makeText(getContext(), getResources().getString(R.string.error_sending_location_to_spatially), Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }

                                if (logging == true) {
                                    Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.error_sending_location_to_spatially));
                                }

                            }
                        });
                    }

                }


            }
        });
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

        if (getActivity() != null) {
            if (receiver != null) {
                getActivity().unregisterReceiver(receiver);
            }
        }

    }

    private void ShowErrorDetails(String exception_message, Context context) {

        if (context != null) {

            sharedPref = context.getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

            if (sharedPref != null && exception_message != null) {
                if (sharedPref.contains("Errors") && sharedPref.getBoolean("Errors", getResources().getBoolean(R.bool.Errors))) {
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
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

        startLocationService();
    }

    private void startLocationService() {

        //Toast.makeText(getActivity(), "in map_fragment_gps: Permission granted!", Toast.LENGTH_LONG).show();
        //System.out.println("startLocationService");

        // The location tracking service is started in the background
        Intent LocationServiceGPS = new Intent(getContext(), locationservice_fuse.class);
        LocationServiceGPS.putExtra("live", live);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        {
            getActivity().startService(LocationServiceGPS);
        }
        else
        {
            getActivity().startForegroundService(LocationServiceGPS);
        }

        intentFilter = new IntentFilter("LocationUpdates");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //Toast.makeText(getActivity(), "Location Changed!", Toast.LENGTH_SHORT).show();

                Location location = new Location("LocationUpdates");
                location.setLatitude(intent.getFloatExtra("Latitude", 0));
                location.setLongitude(intent.getFloatExtra("Longitude", 0));
                location.setAccuracy(intent.getFloatExtra("Accuracy", 0));
                location.setTime(intent.getLongExtra("Time", 0));

                //System.out.println("time: " + location.getTime() + ", latitude: " + location.getLatitude() + ", longitude: " + location.getLongitude() + ", accuracy: " + location.getAccuracy() + " m");

                if (location != null) {

                    if (oldLocation != null) {

                        float movement = oldLocation.distanceTo(location);

                        LatLngBounds.Builder center_point = new LatLngBounds.Builder();
                        center_point.include(new LatLng(oldLocation.getLatitude(), oldLocation.getLongitude()));
                        center_point.include(new LatLng(location.getLatitude(), location.getLongitude()));

                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(center_point.build().getCenter())
                                .bearing(oldLocation.bearingTo(location))
                                .zoom(gps_zoomlevel)
                                .build();

                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
                            saveUserInfo(location);

                        }

                        if (getContext() != null) {
                            // Data is only sent upon meeting certain criteria, same as in iOS app
                            if (((oldLocation.getAccuracy() - location.getAccuracy()) > Float.parseFloat(getResources().getString(R.string.SIGNIFICANT_ACCURACY_CHANGE))) || movement > (oldLocation.getAccuracy() + location.getAccuracy()) || movement > Float.parseFloat(getResources().getString(R.string.SIGNIFICANT_MOVE))) {

                                // Making sure latitude and longitude have 3 decimal places
                                DecimalFormat df = new DecimalFormat(getResources().getString(R.string.No_of_Decimals_for_gps));
                                df.setRoundingMode(RoundingMode.CEILING);

                                URL = sharedPref.getString("Spatially_Host", String.valueOf(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", String.valueOf(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/update-pos.cgi?email=" + email + "&password=" + password + "&lat=" + df.format(location.getLatitude()) + "&lng=" + df.format(location.getLongitude()) + "&acc=" + df.format(location.getAccuracy()) + "&bat=" + df.format(getBatteryLevel());

                                if (logging == true) {
                                    Log.i(getResources().getString(R.string.TAG), URL);
                                } else {
                                    sendToSpatiallyServer(URL);
                                }

                            }
                        }

                    }

                    //Toast.makeText(getApplicationContext(), "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    oldLocation = location;

                    //System.out.println("time: " + location.getTime() + ", latitude: " + location.getLatitude() + ", longitude: " + location.getLongitude() + ", accuracy: " + location.getAccuracy() + " m");

                }


                //Toast.makeText(getContext(), "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude(), Toast.LENGTH_LONG).show();


            }

        };

        if (getActivity() != null) {
            if (!receiver.isOrderedBroadcast()) {
                getActivity().registerReceiver(receiver, intentFilter);
            }
        }

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setText(getResources().getString(R.string.permission_denied_request_cancelled));
                toast.show();
            }
        });

    }

}