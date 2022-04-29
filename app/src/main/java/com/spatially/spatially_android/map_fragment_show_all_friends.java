package com.spatially.spatially_android;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pub.devrel.easypermissions.EasyPermissions;

public class map_fragment_show_all_friends extends Fragment implements EasyPermissions.PermissionCallbacks {

    MapView mMapView;
    private GoogleMap googleMap;

    SharedPreferences sharedPref;

    Bundle extras;

    List<Marker> Marker_List;
    List<String> email_info = new ArrayList<>();
    List<String> name = new ArrayList<>();
    List<Double> latitude = new ArrayList<>();
    List<Double> longitude = new ArrayList<>();

    ImageButton gps, changemap, showallfriends, getmovement;
    ImageButton back, forward;

    String URL, reply;
    String email, password, friendsemail;
    View rootView;

    Integer map_type;

    float showall_friends_zoomlevel = 5;

    TextView fourhours, eighthours, now;


    Integer bounds_padding = 150;
    LatLngBounds bounds;

    LocationManager locationManager;

    LatLngBounds.Builder builder;
    Toast toast;

    boolean isGPSon, isNetworkon;

    Boolean logging = false;

    Integer code;
    Location location;

    private spatially_viewmodel model;

    private Marker marker;

    SingleLiveEvent<Location> test = new SingleLiveEvent<Location>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) rootView.findViewById(R.id.mapView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        try {

            if (getActivity() != null) {

                model = new ViewModelProvider(getActivity()).get(spatially_viewmodel.class);

                try {
                    sharedPref = getContext().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

                    if (sharedPref.contains("logging")) {
                        logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
                    }

                } catch (Exception e) {

                    if (getContext() != null) {
                        ShowErrorDetails(Log.getStackTraceString(e), getContext());
                        /*
                        toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                         */

                        if (logging == true) {
                            Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                        }
                    }

                    return;
                }

                toast.setGravity(Gravity.CENTER, 0, 0);

                if (getArguments() != null && getArguments().containsKey("email") && getArguments().containsKey("password")) {
                    email = getArguments().getString("email");
                    password = getArguments().getString("password");
                }

                code = Integer.parseInt(getResources().getString(R.string.permissions_code));

                // Must call these two methods to start the Google Maps object
                mMapView.onCreate(savedInstanceState);
                mMapView.onResume();

                if (getActivity() != null) {
                    gps = (ImageButton) getActivity().findViewById(R.id.gps);
                    changemap = (ImageButton) getActivity().findViewById(R.id.changemap);
                    showallfriends = (ImageButton) getActivity().findViewById(R.id.showallfriends);
                    getmovement = (ImageButton) getActivity().findViewById(R.id.getmovement);
                }

                back = (ImageButton) rootView.findViewById(R.id.back);
                forward = (ImageButton) rootView.findViewById(R.id.forward);

                getmovement.setVisibility(View.GONE);
                back.setVisibility(View.GONE);
                forward.setVisibility(View.GONE);

                fourhours = (TextView) rootView.findViewById(R.id.hr4);
                eighthours = (TextView) rootView.findViewById(R.id.hr8);
                now = (TextView) rootView.findViewById(R.id.now);

                back.setBackground(null);
                forward.setBackground(null);
                fourhours.setBackground(null);
                eighthours.setBackground(null);
                now.setBackground(null);

                fourhours.setVisibility(View.GONE);
                eighthours.setVisibility(View.GONE);
                now.setVisibility(View.GONE);

                //email = this.getArguments().getString("email").trim();
                //password = this.getArguments().getString("password").trim();

                if (getArguments() != null && getArguments().containsKey("map_type")) {

                    map_type = getArguments().getInt("map_type");
                } else {
                    // Initialize map type to NORMAL by default
                    map_type = 0;
                }

                //registerFriendsListBroadCastReceiver();

                try {
                    if (getContext() != null) {
                        MapsInitializer.initialize(getContext());
                    }
                } catch (Exception e) {
                    if (getContext() != null) {
                        ShowErrorDetails(Log.getStackTraceString(e), getContext());
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

                        if (googleMap != null) {
                            set_map_type(map_type);

                            if (getActivity() != null) {

                                if (((MainActivity) getActivity()).offline == false) {

                                    setupVMObserver();

                                }

                            }

                            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker) {

                                    if (!marker.getTitle().equals("You")) {
                                        friendsemail = marker.getTag().toString();
                                        getmovement.setVisibility(View.VISIBLE);
                                        getmovement.setEnabled(true);
                                    }


                                    if (marker.getTitle() != null) {
                                        marker.showInfoWindow();
                                    }

                                    //getZoomForMetersWide(googleMap, mMapView.getWidth(), new LatLng(marker.getPosition().latitude, marker.getPosition().longitude), extra_distance);

                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude), showall_friends_zoomlevel));
                                    return true;
                                }
                            });


                            googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                                @Override
                                public void onCameraMove() {
                                    CameraPosition position = googleMap.getCameraPosition();

                                    showall_friends_zoomlevel = position.zoom;
                                }
                            });

                            googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                                @Override
                                public void onMapLongClick(LatLng latLng) {

                                    //Toast.makeText(getContext(), "Long press detected!", Toast.LENGTH_LONG).show();

                                    for (Marker marker : Marker_List) {

                                        Location marker_location = new Location("marker");
                                        marker_location.setLatitude(marker.getPosition().latitude);
                                        marker_location.setLongitude(marker.getPosition().longitude);

                                        Location latlng = new Location("latlng");
                                        latlng.setLatitude(latLng.latitude);
                                        latlng.setLongitude(latLng.longitude);

                                        // This is the distance in meters between long pressed location on the map and each marker in the list
                                        double distance_in_m = (double) marker_location.distanceTo(latlng);

                                        if (distance_in_m <= Double.parseDouble(getResources().getString(R.string.long_press_marker_add_fence_distance_threshold))) {

                                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    switch (i) {
                                                        case DialogInterface.BUTTON_POSITIVE:

                                                            dialogInterface.dismiss();
                                                            break;

                                                        case DialogInterface.BUTTON_NEGATIVE:

                                                            //System.out.println("Adding fence for: " + marker.getTitle());
                                                            // Need to enter code here that goes to addfence activity with nearest marker location

                                                            extras = new Bundle();
                                                            extras.putInt("map_type", map_type);
                                                            extras.putString("email", email);
                                                            extras.putString("password", password);
                                                            extras.putDouble("latitude", marker.getPosition().latitude);
                                                            extras.putDouble("longitude", marker.getPosition().longitude);
                                                            extras.putInt("map_type", googleMap.getMapType());
                                                            Intent j = new Intent(getContext(), activity_addfence.class);
                                                            j.putExtras(extras);
                                                            startActivity(j);
                                                            dialogInterface.dismiss();
                                                            break;
                                                    }

                                                }
                                            };

                                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);
                                            builder.setMessage("Add a fence at this location?").setPositiveButton("Cancel", dialogClickListener)
                                                    .setNegativeButton("OK", dialogClickListener).show();

                                        }
                                    }

                                }
                            });

                            // Disables unwanted  google maps features
                            googleMap.getUiSettings().setMapToolbarEnabled(false);
                            googleMap.getUiSettings().setMyLocationButtonEnabled(false);

                            //googleMap.setLatLngBoundsForCameraTarget(builder.build());

                        }
                    }

                });

                gps.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        if (email != null && password != null) {
                            Bundle extras = new Bundle();
                            extras.putString("email", email);
                            extras.putString("password", password);

                            Fragment fragment = new map_fragment_gps();
                            fragment.setArguments(extras);
                            if (getActivity() != null) {
                                getActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, fragment)
                                        .addToBackStack(null)
                                        .commit();
                            }

                        }

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

                                map_type = item;
                                set_map_type(map_type);
                                dialog.dismiss();
                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.show();

                    }


                });

                // This is the group button
                // This shows all the users listed in friends list on the same map
                // Each user's data is acquired by accloc
                showallfriends.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        extras = new Bundle();
                        extras.putString("email", email);
                        extras.putString("password", password);
                        extras.putInt("map_type", map_type);
                        Fragment map_fragment_showall = new map_fragment_show_all_friends();
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, map_fragment_showall).commit();

                    }
                });

                // Shows the movements of selected marker
                getmovement.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (friendsemail == null) {
                            toast = Toast.makeText(getContext(), getResources().getString(R.string.click_on_friend_marker_first), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();

                            if (logging == true) {
                                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.click_on_friend_marker_first));
                            }

                        } else {

                            Fragment mapFragmentGetfriendsMovement = new map_fragment_getfriends_movement();

                            extras = new Bundle();
                            extras.putString("friendsemail", friendsemail);
                            extras.putString("email", email);
                            extras.putString("password", password);
                            extras.putInt("map_type", map_type);

                            mapFragmentGetfriendsMovement.setArguments(extras);
                            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, mapFragmentGetfriendsMovement).commit();

                        }


                    }
                });

            }


        } catch (Exception e) {
            if (getContext() != null) {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
                /*
                toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                 */

                if (logging == true) {
                    Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                }

            }
            return;
        }

    }

    public void readDatafromdisk() {

        ClearAll();

        name = (List) (Arrays.asList(sharedPref.getString("friendsfragment_name", String.valueOf(0)).split(",")));
        email_info = (List) (Arrays.asList(sharedPref.getString("friendsfragment_email_info", String.valueOf(0)).split(",")));
        latitude = (List) (Arrays.asList(sharedPref.getString("friendsfragment_latitude", String.valueOf(0)).split(",")));
        longitude = (List) (Arrays.asList(sharedPref.getString("friendsfragment_longitude", String.valueOf(0)).split(",")));

        /*
        System.out.println("Reading name:" + name);
        System.out.println("Reading email_info: " + email_info);
        System.out.println("Reading latitude: " + latitude);
        System.out.println("Reading longitude: " + longitude);
        */


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
            }

                /*
                toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                */


            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

            return 0;

        }


    }

    private void ClearAll() {

        try {

            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.clearing_all_data_lists));
            }

            if (name != null && email_info != null && latitude != null && longitude != null) {
                name.clear();
                email_info.clear();
                latitude.clear();
                longitude.clear();
            }

        } catch (Exception e) {

            if (getContext() != null) {

                ShowErrorDetails(Log.getStackTraceString(e), getContext());
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

    }

    public void set_map_type(int map_type) {

        try {

            if (map_type == 0) {

                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                back.setColorFilter(Color.BLACK);
                forward.setColorFilter(Color.BLACK);
                showallfriends.setColorFilter(Color.BLACK);
                gps.setColorFilter(Color.BLACK);
                changemap.setColorFilter(Color.BLACK);
                getmovement.setColorFilter(Color.BLACK);

            } else if (map_type == 1) {

                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                back.setColorFilter(Color.WHITE);
                forward.setColorFilter(Color.WHITE);
                showallfriends.setColorFilter(Color.WHITE);
                gps.setColorFilter(Color.WHITE);
                changemap.setColorFilter(Color.WHITE);
                getmovement.setColorFilter(Color.WHITE);

            } else if (map_type == 2) {

                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                back.setColorFilter(Color.WHITE);
                forward.setColorFilter(Color.WHITE);
                showallfriends.setColorFilter(Color.WHITE);
                gps.setColorFilter(Color.WHITE);
                changemap.setColorFilter(Color.WHITE);
                getmovement.setColorFilter(Color.WHITE);

            }

        } catch (Exception e) {
            if (getContext() != null) {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
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

    public void sendMyLocation(Location location) {

        // Making sure latitude and longitude have 3 decimal places
        DecimalFormat df = new DecimalFormat(getResources().getString(R.string.No_of_Decimals_for_gps));
        df.setRoundingMode(RoundingMode.CEILING);

        URL = sharedPref.getString("Spatially_Host", String.valueOf(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", String.valueOf(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/update-pos.cgi?email=" + email + "&password=" + password + "&lat=" + df.format(location.getLatitude()) + "&lng=" + df.format(location.getLongitude()) + "&acc=" + df.format(location.getAccuracy()) + "&bat=" + df.format(getBatteryLevel());

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
                    Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                reply = response.body().string().trim();


                if (!((reply.split(",")[0]).trim()).equals("OK")) {

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toast = Toast.makeText(getContext(), getResources().getString(R.string.error_sending_location_to_spatially), Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

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

    public void getMapInfo() {

        try {


            if (getActivity() != null) {

                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    //Toast.makeText(getActivity(),"Requesting permissions!", Toast.LENGTH_LONG).show();

                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, code);

                }



            }

        } catch (Exception e) {

            if (getContext() != null) {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
            }

            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

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
    public void onDestroy() {
        super.onDestroy();

    }

    public void setupVMObserver() {

        try {
            if (getActivity() != null) {

                if (logging == true) {
                    Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.setting_up_vm_observer));
                }

                Observer<ArrayList> name_observer = new Observer<ArrayList>() {
                    @Override
                    public void onChanged(ArrayList arrayList) {
                        if (getActivity() != null && arrayList != null) {

                            name = arrayList;
                        }
                    }
                };

                model.get_name().observeForever(name_observer);

                Observer<ArrayList> email_info_observer = new Observer<ArrayList>() {
                    @Override
                    public void onChanged(ArrayList arrayList) {
                        if (getActivity() != null && arrayList != null) {

                            email_info = arrayList;

                        }
                    }
                };

                model.getEmail_info().observeForever(email_info_observer);

                Observer<ArrayList> latitude_observer = new Observer<ArrayList>() {
                    @Override
                    public void onChanged(ArrayList arrayList) {
                        if (getActivity() != null && arrayList != null) {

                            latitude = arrayList;

                        }
                    }
                };

                model.getLatitude().observeForever(latitude_observer);

                Observer<ArrayList> longitude_observer = new Observer<ArrayList>() {
                    @Override
                    public void onChanged(ArrayList arrayList) {
                        longitude = arrayList;
                    }
                };

                model.getLongitude().observeForever(longitude_observer);

                getMapInfo();

            }
        } catch (Exception e) {

            if (getContext() != null) {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
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
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

        //Toast.makeText(getActivity(), "Permission Granted!", Toast.LENGTH_LONG).show();

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            isGPSon = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkon = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }

        if (isGPSon) {

            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        } else if (isNetworkon) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        }

        if (location != null) {

            googleMap.setMyLocationEnabled(true);
            builder.include(new LatLng(location.getLatitude(), location.getLongitude()));

            toast = Toast.makeText(getActivity(), getResources().getString(R.string.loading_location_data), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            googleMap.clear();

            //System.out.println(name.size() + " " + email_info.size() + " " + latitude.size() + " " + longitude.size());

            builder = new LatLngBounds.Builder();

            Marker_List = new ArrayList<>();

            if (name != null && name.size() > 0) {
                // Index 1 because 0 is "You" in the friendslist
                for (int i = 1; i < name.size(); i++) {

                    //System.out.println("Data: " + name.get(i) + " " + email_info.get(i) + " " + latitude.get(i) + " " + longitude.get(i));

                    if (latitude.get(i) != null && longitude.get(i) != null && name.get(i) != null && email_info.get(i) != null) {

                        marker = googleMap
                                .addMarker(new MarkerOptions()
                                        .position(new LatLng(Double.valueOf(String.valueOf(latitude.get(i))), Double.valueOf(String.valueOf(longitude.get(i)))))
                                        .title(name.get(i)));

                        Marker_List.add(marker);

                        //Choosing colour for all markers
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        marker.setTag(email_info.get(i));
                        marker.setTitle(name.get(i));
                        builder.include(marker.getPosition());

                    }

                }

                bounds = builder.build();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, bounds_padding));

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