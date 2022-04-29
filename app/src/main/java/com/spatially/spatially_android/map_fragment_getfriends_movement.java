package com.spatially.spatially_android;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pub.devrel.easypermissions.EasyPermissions;

public class map_fragment_getfriends_movement extends Fragment implements EasyPermissions.PermissionCallbacks{

    MapView mMapView;
    private GoogleMap googleMap;
    String email, password, friendsemail;
    List<String> reply_array;
    SharedPreferences sharedPref;
    String reply;
    View rootView;
    Integer currentLocationInList = 0;
    ImageButton back, forward, showallfriends;
    ImageButton getmovement, gps, changemap;
    Bundle extras;

    LatLngBounds.Builder movement = new LatLngBounds.Builder();

    List<Double> Latitude = new ArrayList<>();
    List<Double> Longitude= new ArrayList<>();
    List<Long> Time= new ArrayList<>();
    List<Float> Accuracy= new ArrayList<>();
    List<Integer> Id= new ArrayList<>();

    TextView fourhours, eighthours, now;
    Integer map_type;

    LatLngBounds bounds;
    Boolean logging=false;

    float getfriends_movement_zoomlevel = 6;

    Integer bounds_padding_in_px;

    float extra_distance = 5000;

    Marker marker;

    Integer gps_zoomlevel;

    Integer code = 1;
    boolean permission_granted=false;

    List<Location> full_movement_markers = new ArrayList<>();
    List<Location> fourhour_locations = new ArrayList();
    List<Location> eighthour_locations = new ArrayList();

    Boolean fourhour=false, eighthour=false;
    int max=0;

    String name;
    Toast toast;


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
            if(getActivity()!=null) {

                bounds_padding_in_px = Integer.parseInt(getResources().getString(R.string.bounds_padding_in_px));

                fourhours = (TextView) rootView.findViewById(R.id.hr4);
                eighthours = (TextView) rootView.findViewById(R.id.hr8);
                now = (TextView) rootView.findViewById(R.id.now);

                fourhours.setVisibility(View.VISIBLE);
                eighthours.setVisibility(View.VISIBLE);
                now.setVisibility(View.VISIBLE);

                back = (ImageButton) rootView.findViewById(R.id.back);
                forward = (ImageButton) rootView.findViewById(R.id.forward);

                back.setBackground(null);
                forward.setBackground(null);
                fourhours.setBackground(null);
                eighthours.setBackground(null);
                now.setBackground(null);

                showallfriends = (ImageButton) getActivity().findViewById(R.id.showallfriends);
                showallfriends.setColorFilter(Color.BLACK);
                getmovement = (ImageButton) getActivity().findViewById(R.id.getmovement);
                gps = (ImageButton) rootView.findViewById(R.id.gps);
                changemap = (ImageButton) getActivity().findViewById(R.id.changemap);

                back.setEnabled(true);
                forward.setEnabled(true);

                extras = getArguments();

                gps_zoomlevel = Integer.parseInt(getResources().getString(R.string.gps_zoomlevel));

                if(getActivity()!=null)
                {
                    sharedPref = getActivity().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

                    if (sharedPref.contains("logging")) {
                        logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
                    }
                }

                if (getArguments() != null && getArguments().containsKey("email") & getArguments().containsKey("password") && getArguments().containsKey("friendsemail")) {
                    // Can recover any extras
                    email = extras.getString("email");
                    password = extras.getString("password");
                    friendsemail = extras.getString("friendsemail");
                }

                if (getArguments() != null && getArguments().containsKey("name"))
                {
                    name = extras.getString("name");
                }

                if (getArguments() != null && getArguments().containsKey("map_type")) {

                    map_type = getArguments().getInt("map_type");
                } else {
                    // Initialize map type to NORMAL by default
                    map_type = 0;
                }

                movement = new LatLngBounds.Builder();

                /*
                System.out.println(email);
                System.out.println(password);
                System.out.println(friendsemail);
                */


                mMapView.onCreate(savedInstanceState);
                mMapView.onResume();

                try {
                    if(getActivity()!=null)
                    {
                        MapsInitializer.initialize(getActivity());
                    }
                } catch (Exception e) {

                    if(getContext()!=null)
                    {
                        ShowErrorDetails(Log.getStackTraceString(e), getContext());
                    }

                    /*
                    toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    */

                    if(logging==true) {
                        Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                    }
                }

                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap mMap) {

                        googleMap = mMap;

                            set_map_type(map_type);

                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.


                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

                                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, code);

                                } else {

                                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, code);

                                }

                            }

                        // Disables unwanted  google maps features
                        googleMap.getUiSettings().setMapToolbarEnabled(false);
                        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

                        //getMapInfo();

                        Timer t = new Timer();

                        if (getActivity() != null) {

                            t.scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {

                                    if (getActivity() != null) {

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                getMapInfo();

                                                if(getContext()!=null)
                                                {
                                                    toast = Toast.makeText(getContext(), getResources().getString(R.string.loading_movement_data), Toast.LENGTH_SHORT);
                                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                                    toast.show();
                                                }
                                            }
                                        });

                                    }
                                }
                            }, 0, Long.parseLong(getResources().getString(R.string.one_min_to_ms)) * Long.parseLong(getResources().getString(R.string.friends_movement_check_rate_in_min)));


                        }

                        // Goes back in time
                        back.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {

                                if ((fourhour == true) && (eighthour == false)) {

                                    if (fourhour_locations.size() >= 1) {
                                        max = fourhour_locations.size();
                                    }

                                } else if ((fourhour == false) && (eighthour == true)) {

                                    if (eighthour_locations.size() >= 1) {
                                        max = eighthour_locations.size();
                                    }

                                } else if (fourhour == false && eighthour == false) {

                                    if (full_movement_markers.size() >= 1) {
                                        max = full_movement_markers.size();
                                    }

                                }

                                if (currentLocationInList == (max - 1)) {

                                    back.setEnabled(false);
                                    back.setColorFilter(R.color.Information_Tint_Disabled);

                                    movement = new LatLngBounds.Builder();

                                    if (fourhour == false && eighthour == false) {
                                        if(full_movement_markers.get(currentLocationInList)!=null)
                                        {
                                            Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(full_movement_markers.get(currentLocationInList).getLatitude(), full_movement_markers.get(currentLocationInList).getLongitude())).title(name).snippet("Started from here at: " + UnixToString(Time.get(currentLocationInList))));
                                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(full_movement_markers.get(currentLocationInList).getLatitude(), full_movement_markers.get(currentLocationInList).getLongitude()), getfriends_movement_zoomlevel));
                                            marker.showInfoWindow();
                                        }

                                    } else if (fourhour = true && eighthour == false) {
                                        if(fourhour_locations.get(currentLocationInList)!=null)
                                        {
                                            Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(fourhour_locations.get(currentLocationInList).getLatitude(), fourhour_locations.get(currentLocationInList).getLongitude())).title(name).snippet("Started from here at: " + UnixToString(fourhour_locations.get(currentLocationInList).getTime())));
                                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(fourhour_locations.get(currentLocationInList).getLatitude(), fourhour_locations.get(currentLocationInList).getLongitude()), getfriends_movement_zoomlevel));
                                            marker.showInfoWindow();
                                        }

                                    } else if (fourhour == false && eighthour == true) {
                                        if(eighthour_locations.get(currentLocationInList)!=null)
                                        {
                                            Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(eighthour_locations.get(currentLocationInList).getLatitude(), eighthour_locations.get(currentLocationInList).getLongitude())).title(name).snippet("Started from here at: " + UnixToString(eighthour_locations.get(currentLocationInList).getTime())));
                                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(eighthour_locations.get(currentLocationInList).getLatitude(), eighthour_locations.get(currentLocationInList).getLongitude()), getfriends_movement_zoomlevel));
                                            marker.showInfoWindow();
                                        }

                                    }


                                } else {

                                    back.setEnabled(true);
                                    forward.setEnabled(true);

                                    currentLocationInList++;

                                    if (fourhour == false && eighthour == false) {

                                        if (full_movement_markers.size() > 1) {
                                            if (currentLocationInList < max) {

                                                if (full_movement_markers.get(currentLocationInList) != null) {
                                                    Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(full_movement_markers.get(currentLocationInList).getLatitude(), full_movement_markers.get(currentLocationInList).getLongitude())).title(UnixToString(full_movement_markers.get(currentLocationInList).getTime())));
                                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(full_movement_markers.get(currentLocationInList).getLatitude(), full_movement_markers.get(currentLocationInList).getLongitude()), getfriends_movement_zoomlevel));
                                                    marker.showInfoWindow();

                                                    showAccuracyCircle(currentLocationInList, googleMap, full_movement_markers);
                                                    //movement.include(marker.getPosition());
                                                    //googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(movement.build(), bounds_padding));
                                                }
                                            }
                                        }


                                    } else if (fourhour == true && eighthour == false) {

                                        if (fourhour_locations.size() > 1) {
                                            if (currentLocationInList < max) {
                                                if (fourhour_locations.get(currentLocationInList) != null) {

                                                    Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(fourhour_locations.get(currentLocationInList).getLatitude(), fourhour_locations.get(currentLocationInList).getLongitude())).title(UnixToString(fourhour_locations.get(currentLocationInList).getTime())));
                                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(fourhour_locations.get(currentLocationInList).getLatitude(), fourhour_locations.get(currentLocationInList).getLongitude()), getfriends_movement_zoomlevel));
                                                    marker.showInfoWindow();

                                                    showAccuracyCircle(currentLocationInList, googleMap, fourhour_locations);
                                                    //movement.include(marker.getPosition());
                                                    //googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(movement.build(), bounds_padding));
                                                }
                                            }
                                        }

                                    } else if (fourhour == false && eighthour == true) {

                                        if (eighthour_locations.size() > 1) {
                                            if (currentLocationInList < max) {
                                                if (eighthour_locations.get(currentLocationInList) != null) {

                                                    Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(eighthour_locations.get(currentLocationInList).getLatitude(), eighthour_locations.get(currentLocationInList).getLongitude())).title(UnixToString(eighthour_locations.get(currentLocationInList).getTime())));
                                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(eighthour_locations.get(currentLocationInList).getLatitude(), eighthour_locations.get(currentLocationInList).getLongitude()), getfriends_movement_zoomlevel));
                                                    marker.showInfoWindow();

                                                    showAccuracyCircle(currentLocationInList, googleMap, eighthour_locations);
                                                    //movement.include(marker.getPosition());
                                                    //googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(movement.build(), bounds_padding));
                                                }
                                            }
                                        }

                                    }

                                }

                            }

                        });

                        // Goes forward in time
                        forward.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if (currentLocationInList == 0) {

                                    forward.setEnabled(false);
                                    forward.setColorFilter(R.color.Information_Tint_Disabled);

                                    if (fourhour == false && eighthour == false) {
                                        if(full_movement_markers.get(currentLocationInList)!=null)
                                        {
                                            Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(full_movement_markers.get(0).getLatitude(), full_movement_markers.get(0).getLongitude())).title(name).snippet("Reached here at: " + UnixToString(full_movement_markers.get(0).getTime())));
                                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(full_movement_markers.get(0).getLatitude(), full_movement_markers.get(0).getLongitude()), getfriends_movement_zoomlevel));
                                            marker.showInfoWindow();
                                        }
                                    } else if (fourhour == true && eighthour == false) {
                                        if(fourhour_locations.get(currentLocationInList)!=null)
                                        {
                                            Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(fourhour_locations.get(0).getLatitude(), fourhour_locations.get(0).getLongitude())).title(name).snippet("Reached here at: " + UnixToString(fourhour_locations.get(0).getTime())));
                                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(full_movement_markers.get(0).getLatitude(), full_movement_markers.get(0).getLongitude()), getfriends_movement_zoomlevel));
                                            marker.showInfoWindow();
                                        }
                                    } else if (fourhour == false && eighthour == true) {
                                        if(eighthour_locations.get(currentLocationInList)!=null)
                                        {
                                            Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(eighthour_locations.get(0).getLatitude(), eighthour_locations.get(0).getLongitude())).title(name).snippet("Reached here at: " + UnixToString(eighthour_locations.get(0).getTime())));
                                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(eighthour_locations.get(0).getLatitude(), eighthour_locations.get(0).getLongitude()), getfriends_movement_zoomlevel));
                                            marker.showInfoWindow();
                                        }
                                    }

                                } else {

                                    back.setEnabled(true);
                                    forward.setEnabled(true);

                                    currentLocationInList--;

                                    //marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                    //builder.include(marker.getPosition());
                                    //bounds.including();
                                    //bounds = builder.build();
                                    //LatLng center = builder.build().getCenter();
                                    //googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, bounds_padding));

                                    if (fourhour == false && eighthour == false) {

                                        if (full_movement_markers.size() > 1) {
                                            max = full_movement_markers.size();

                                            if (currentLocationInList < max) {
                                                if (full_movement_markers.get(currentLocationInList) != null) {
                                                    //getZoomForMetersWide(googleMap, mMapView.getWidth(), new LatLng(Latitude.get(currentLocationInList), Longitude.get(currentLocationInList)), Accuracy.get(currentLocationInList) + extra_distance);

                                                    Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(full_movement_markers.get(currentLocationInList).getLatitude(), full_movement_markers.get(currentLocationInList).getLongitude())).title(name).snippet(UnixToString(full_movement_markers.get(currentLocationInList).getTime())));
                                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(full_movement_markers.get(currentLocationInList).getLatitude(), full_movement_markers.get(currentLocationInList).getLongitude()), getfriends_movement_zoomlevel));
                                                    showAccuracyCircle(currentLocationInList, googleMap, full_movement_markers);
                                                    marker.showInfoWindow();

                                                    movement.include(marker.getPosition());
                                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(movement.build(), bounds_padding_in_px));

                                                }
                                            }
                                        }


                                    } else if (fourhour == true && eighthour == false) {

                                        if (fourhour_locations.size() > 1) {

                                            max = fourhour_locations.size();

                                            if (currentLocationInList < max) {

                                                if (fourhour_locations.get(currentLocationInList) != null) {

                                                    Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(fourhour_locations.get(currentLocationInList).getLatitude(), fourhour_locations.get(currentLocationInList).getLongitude())).title(UnixToString(fourhour_locations.get(currentLocationInList).getTime())));
                                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(fourhour_locations.get(currentLocationInList).getLatitude(), fourhour_locations.get(currentLocationInList).getLongitude()), getfriends_movement_zoomlevel));
                                                    showAccuracyCircle(currentLocationInList, googleMap, fourhour_locations);
                                                    marker.showInfoWindow();

                                                    //getZoomForMetersWide(googleMap, mMapView.getWidth(), new LatLng(Latitude.get(currentLocationInList), Longitude.get(currentLocationInList)), Accuracy.get(currentLocationInList) + extra_distance);

                                                    movement.include(marker.getPosition());
                                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(movement.build(), bounds_padding_in_px));


                                                }
                                            }
                                        }

                                    } else if (fourhour == false && eighthour == true) {

                                        if (eighthour_locations.size() > 1) {
                                            max = eighthour_locations.size();

                                            if (currentLocationInList < max) {
                                                if (eighthour_locations.get(currentLocationInList) != null) {
                                                    //getZoomForMetersWide(googleMap, mMapView.getWidth(), new LatLng(Latitude.get(currentLocationInList), Longitude.get(currentLocationInList)), Accuracy.get(currentLocationInList) + extra_distance);

                                                    Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(eighthour_locations.get(currentLocationInList).getLatitude(), eighthour_locations.get(currentLocationInList).getLongitude())).title(UnixToString(eighthour_locations.get(currentLocationInList).getTime())));
                                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(eighthour_locations.get(currentLocationInList).getLatitude(), eighthour_locations.get(currentLocationInList).getLongitude()), getfriends_movement_zoomlevel));
                                                    showAccuracyCircle(currentLocationInList, googleMap, eighthour_locations);
                                                    marker.showInfoWindow();

                                                    movement.include(marker.getPosition());
                                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(movement.build(), bounds_padding_in_px));


                                                }
                                            }
                                        }

                                    }

                                }

                            }

                        });


                        now.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                currentLocationInList = 0;
                                getfriends_movement_zoomlevel = 6;

                                if ((Latitude.get(currentLocationInList) != null) && (Longitude.get(currentLocationInList) != null) && (Time.get(currentLocationInList) != null)) {

                                    back.setEnabled(true);
                                    forward.setEnabled(true);

                                    // Current accuracy circle
                                    CircleOptions circleOptions = new CircleOptions();
                                    circleOptions.center(new LatLng(Latitude.get(0), Longitude.get(0)));
                                    circleOptions.radius(Accuracy.get(0));
                                    circleOptions.strokeColor(getResources().getColor(R.color.friends_movement_fence_stroke_colour));
                                    circleOptions.fillColor(getResources().getColor(R.color.friends_movement_fence_fill_colour));
                                    circleOptions.strokeWidth(getResources().getDimension(R.dimen.friends_movement_border_width));
                                    googleMap.addCircle(circleOptions);

                                    Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(Latitude.get(currentLocationInList), Longitude.get(currentLocationInList))).title(name).snippet("Reached here at " + UnixToString(Time.get(currentLocationInList))));
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), getfriends_movement_zoomlevel));
                                    marker.showInfoWindow();

                                }


                            }
                        });


                        fourhours.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                fourhour = true;
                                fourhour_locations.clear();
                                googleMap.clear();
                                movement = new LatLngBounds.Builder();
                                getfriends_movement_zoomlevel = 8;

                                // four hour history
                                long four_hours_unix_time = (Long.valueOf(Time.get(0)) - Long.parseLong(getResources().getString(R.string.four_hour_offset)));

                                //System.out.println("Starting time + 4 hours in unix time: " + four_hours_unix_time);

                                for (int i = 0; i < (Time.size()); i++) {

                                    //System.out.println("Time.get(i): " + Time.get(i));

                                    if (Time.get(i) >= four_hours_unix_time) {

                                        Location temp = new Location(String.valueOf(i));
                                        temp.setLatitude(Latitude.get(i));
                                        temp.setLongitude(Longitude.get(i));
                                        temp.setTime(Time.get(i));

                                        fourhour_locations.add(temp);


                                    }
                                }

                                //showAccuracyCircle(i, googleMap, fourhour_locations);

                                // To center the movement
                                //builder.include(marker.getPosition());

                                //bounds = builder.build();

                                //LatLng center = builder.build().getCenter();

                                // Radius of 10km around cluster
                                //getZoomForMetersWide(googleMap, mMapView.getWidth(), new LatLng(center.latitude, center.longitude), 10000);

                                //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, getfriends_movement_zoomlevel));
                                //googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, bounds_padding));
                                if(getContext()!=null)
                                {
                                    toast = Toast.makeText(getContext(), getResources().getString(R.string.showing_4_hour_history), Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }

                            }
                        });

                        eighthours.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                eighthour = true;
                                eighthour_locations.clear();
                                googleMap.clear();
                                movement = new LatLngBounds.Builder();
                                getfriends_movement_zoomlevel = 8;

                                // eight hour history
                                long eight_hours_unix_time = (Long.valueOf(Time.get(0)) - Long.parseLong(getResources().getString(R.string.eight_hour_offset)));

                                //System.out.println("Starting time + 8 hours in unix time: " + eight_hours_unix_time);

                                for (int i = 0; i < (Time.size()); i++) {

                                    if (Time.get(i) >= eight_hours_unix_time) {

                                        Location temp = new Location(String.valueOf(i));
                                        temp.setLatitude(Latitude.get(i));
                                        temp.setLongitude(Longitude.get(i));
                                        temp.setTime(Time.get(i));

                                        eighthour_locations.add(temp);

                                        //marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(Latitude.get(i), Longitude.get(i))).title(UnixToString(Time.get(i))));
                                        //marker.showInfoWindow();
                                        //showAccuracyCircle(i, googleMap, eighthour_locations);

                                        //builder.include(marker.getPosition());

                                    }

                                }


                                //bounds = builder.build();

                                //LatLng center = builder.build().getCenter();

                                // Radius of 10km around cluster
                                //getZoomForMetersWide(googleMap, mMapView.getWidth(), new LatLng(center.latitude, center.longitude), 10000);
                                //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, getfriends_movement_zoomlevel));

                                //googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, bounds_padding));
                                if(getContext()!=null)
                                {
                                    toast = Toast.makeText(getContext(), getResources().getString(R.string.showing_8_hour_history), Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }

                            }
                        });

                        showallfriends.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Bundle extras = new Bundle();
                                extras.putString("email", email);
                                extras.putString("password", password);
                                Fragment map_fragment_showall = new map_fragment_show_all_friends();
                                map_fragment_showall.setArguments(extras);
                                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, map_fragment_showall).commit();


                            }
                        });

                        // Need to implement the logic and map fragments for different map options
                        // This is the map movement button
                        // This shows movement of the selected user over time
                        // This is acquired by getting data by getloc
                        // This is acquired by getting data by getloc
                        getmovement.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if (friendsemail != null) {
                                    String URL = sharedPref.getString("Spatially_Host", String.valueOf(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", String.valueOf(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/get-locations-for-friend.cgi?email=" + email + "&password=" + password + "&friendemail=" + friendsemail;

                                    OkHttpClient client = new OkHttpClient();

                                    Request request = new Request.Builder()
                                            .url(URL)
                                            .build();

                                    if(logging==true) {
                                        Log.i(getResources().getString(R.string.TAG), URL);
                                    }

                                    // Runs in background thread by default
                                    client.newCall(request).enqueue(new Callback() {


                                        @Override
                                        public void onFailure(Call call, IOException e) {

                                            if(logging==true) {
                                                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                                            }

                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {


                                            // onResponse already assures the response is successful, apparently.
                                            //if(response.isSuccessful()) {

                                            reply = response.body().string().trim();

                                            Fragment map_fragment_getfriendslocations = new map_fragment_getfriends_movement();
                                            extras = new Bundle();
                                            extras.putString("email", email);
                                            extras.putString("password", password);
                                            extras.putString("friendsemail", friendsemail);

                                            map_fragment_getfriendslocations.setArguments(extras);
                                            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                                            transaction.replace(R.id.fragment_container, map_fragment_getfriendslocations).commit();

                                            BottomNavigationView mBottomNavigationView;
                                            mBottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
                                            MenuItem menuItem = mBottomNavigationView.getMenu().getItem(1);
                                            menuItem.setChecked(true);


                                        }
                                    });
                                } else {
                                    if(getContext()!=null)
                                    {
                                        toast = Toast.makeText(getContext(), getResources().getString(R.string.click_on_friend_marker_first), Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();
                                    }
                                }
                            }

                        });

                        // This is the gps location button
                        // This shows the user their current location
                        // https://www.thecrazyprogrammer.com/2017/01/how-to-get-current-location-in-android.html
                        // https://stackoverflow.com/questions/22471100/how-to-show-current-position-marker-on-map-in-android
                        gps.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {

                                Bundle extras = new Bundle();
                                extras.putString("email", email);
                                extras.putString("password", password);

                                Fragment fragment = new map_fragment_gps();
                                fragment.setArguments(extras);

                                getActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.fragment_container, fragment)
                                        .addToBackStack(null)
                                        .commit();

                            }
                        });

                        // This is the map selection type button
                        // Changes between Map / Satellite / Hybrid
                        changemap.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if(getContext()!=null) {
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
                            }


                        });

                        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {

                                //Toast.makeText(getContext(), "marker clicked", Toast.LENGTH_SHORT).show();

                                //getZoomForMetersWide(googleMap, mMapView.getWidth(), new LatLng(marker.getPosition().latitude, marker.getPosition().longitude), extra_distance);

                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude), getfriends_movement_zoomlevel));
                                marker.showInfoWindow();

                                forward.setEnabled(true);
                                back.setEnabled(true);

                                // If listener has consumed event, return true else false;
                                return true;
                            }
                        });

                        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                            @Override
                            public void onCameraMove() {
                                CameraPosition position = googleMap.getCameraPosition();

                                getfriends_movement_zoomlevel = position.zoom;
                            }
                        });

                    }


                });
            }

        } catch (Exception e) {

            if(getContext()!=null)
            {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
            }

            /*
            toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            */

            if(logging==true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

            return;
        }

    }

    public void getZoomForMetersWide(GoogleMap googleMap, int mapViewWidth, LatLng latLngPoint, double desiredMeters) {

        if(getContext()!=null)
        {
            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            float mapWidth = mapViewWidth / metrics.density;

            final int EQUATOR_LENGTH = 40075004;
            final int TIME_ANIMATION_MILIS = 1500;
            final double latitudinalAdjustment = Math.cos(Math.PI * latLngPoint.latitude / 180.0);
            final double arg = EQUATOR_LENGTH * mapWidth * latitudinalAdjustment / (desiredMeters * 256.0);
            double valToZoom = Math.log(arg) / Math.log(2.0);

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngPoint, Float.valueOf(String.valueOf(valToZoom))), TIME_ANIMATION_MILIS, null);
        }
    }

    public void showAccuracyCircle(int i, GoogleMap googleMap, List<Location> locations){

        if((i<locations.size()) && (locations.get(i)!=null)) {

            CircleOptions circleOptions = new CircleOptions();
            //circleOptions.center(new LatLng(Latitude.get(i), Longitude.get(i)));
            circleOptions.center(new LatLng(locations.get(i).getLatitude(), locations.get(i).getLongitude()));
            circleOptions.radius(locations.get(i).getAccuracy());
            circleOptions.strokeColor(getResources().getColor(R.color.friends_movement_fence_stroke_colour));
            circleOptions.fillColor(getResources().getColor(R.color.friends_movement_fence_fill_colour));
            circleOptions.strokeWidth(getResources().getDimension(R.dimen.friends_movement_border_width));
            googleMap.addCircle(circleOptions);

        }


    }

    public void set_map_type(int map_type){

        try {

            if (map_type == 0) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                back.setColorFilter(Color.BLACK);
                forward.setColorFilter(Color.BLACK);
                showallfriends.setColorFilter(Color.BLACK);
                gps.setColorFilter(Color.BLACK);
                changemap.setColorFilter(Color.BLACK);
                getmovement.setColorFilter(Color.BLACK);

                fourhours.setTextColor(Color.BLACK);
                eighthours.setTextColor(Color.BLACK);
                now.setTextColor(Color.BLACK);

            } else if (map_type == 1) {


                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                back.setColorFilter(Color.WHITE);
                forward.setColorFilter(Color.WHITE);
                showallfriends.setColorFilter(Color.WHITE);
                gps.setColorFilter(Color.WHITE);
                changemap.setColorFilter(Color.WHITE);
                getmovement.setColorFilter(Color.WHITE);

                fourhours.setTextColor(Color.WHITE);
                eighthours.setTextColor(Color.WHITE);
                now.setTextColor(Color.WHITE);

            } else if (map_type == 2) {

                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                back.setColorFilter(Color.WHITE);
                forward.setColorFilter(Color.WHITE);
                showallfriends.setColorFilter(Color.WHITE);
                gps.setColorFilter(Color.WHITE);
                changemap.setColorFilter(Color.WHITE);
                getmovement.setColorFilter(Color.WHITE);

                fourhours.setTextColor(Color.WHITE);
                eighthours.setTextColor(Color.WHITE);
                now.setTextColor(Color.WHITE);

            }
        }
        catch(Exception e){

            if(getContext()!=null)
            {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
            }
            /*
            toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            */

            if(logging==true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toast = Toast.makeText(getActivity(), getResources().getString(R.string.loading_location_data), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

    }

    public void getMapInfo(){

        googleMap.clear();

        String URL = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/get-locations-for-friend.cgi?email=" + email + "&password=" + password + "&friendemail=" + friendsemail;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(URL)
                .build();

        if(logging==true) {
            Log.i(getResources().getString(R.string.TAG), URL);
        }

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

                reply = response.body().string().trim();

                // reply_list stores the movement information
                // Fields are: Id, Time, Latitude, Longitude, Accuracy
                // 0 is id, 1 is time in unix time, 2 is latitude, 3 is longitude and 4 is accuracy in meters
                reply_array = (List) Arrays.asList(reply.split("\n"));

                if (reply_array.size() > 0) {

                    //System.out.println(reply_array);

                    // Don't forget OK is extra
                    for (int i = 0; i < reply_array.size(); i++) {

                        // This ensures that the movement data exists and is of right size
                        if ((reply_array.get(i).split(",")).length == 5) {

                            if (((reply_array.get(i).split(",")[0])!=null) && (reply_array.get(i).split(",")[1]!=null) && (reply_array.get(i).split(",")[2]) != null && ((reply_array.get(i).split(",")[3]) != null) && ((reply_array.get(i).split(",")[4]) != null)) {

                                Id.add(Integer.valueOf(reply_array.get(i).split(",")[0]));
                                Time.add(Long.parseLong((reply_array.get(i).split(","))[1]));
                                Latitude.add(Double.valueOf((reply_array.get(i).split(",")[2])));
                                Longitude.add(Double.valueOf((reply_array.get(i).split(",")[3])));
                                Accuracy.add(Float.valueOf((reply_array.get(i).split(",")[4])));

                                //builder.include(marker.getPosition());
                                //LatLng center =  builder.build().getCenter();


                            }
                        }
                    }


                    //LatLng current = googleMap.getCameraPosition().target;
                    if(getActivity()!=null) {
                        // Goes to the first point
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //System.out.println(Time + " " + Latitude + " " + Longitude);

                                // If multiple locations
                                if ((Latitude.size() > 1) && (Longitude.size() > 1) && (Time.size() > 1)) {

                                    // Add all movement markers to the map
                                    movement = new LatLngBounds.Builder();
                                    for (int i = 0; i < reply_array.size(); i++) {

                                        String title;

                                        if (i == 0) {
                                            title = "Reached here at " + UnixToString(Time.get(i));
                                        } else if (i == (reply_array.size() - 1)) {
                                            title = "Started from here at " + UnixToString(Time.get(i));
                                        } else {
                                            title = UnixToString(Time.get(i));
                                        }

                                        Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(Latitude.get(i), Longitude.get(i))).title(title));

                                        Location temp = new Location(String.valueOf(i));
                                        temp.setLatitude(Latitude.get(i));
                                        temp.setLongitude(Longitude.get(i));
                                        temp.setTime(Time.get(i));
                                        temp.setAccuracy(Accuracy.get(i));
                                        full_movement_markers.add(temp);

                                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                        //marker.showInfoWindow();

                                        movement.include(marker.getPosition());
                                        showAccuracyCircle(i, googleMap, full_movement_markers);

                                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(movement.build(), bounds_padding_in_px));

                                        currentLocationInList = 0;

                                        back.setEnabled(true);
                                        forward.setEnabled(true);
                                    }

                                }


                                // If only one location
                                else if (Latitude.size() == 1 && Longitude.size() == 1 && Time.size() == 1) {

                                    Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(Latitude.get(0), Longitude.get(0))).title("Reached here at " + UnixToString(Time.get(0))));
                                    showAccuracyCircle(0, googleMap, full_movement_markers);
                                    getZoomForMetersWide(googleMap, mMapView.getWidth(), new LatLng(Latitude.get(0), Longitude.get(0)), extra_distance);
                                    back.setEnabled(false);
                                    forward.setEnabled(false);
                                    marker.showInfoWindow();

                                }


                            }
                        });
                    }




                }


            }


        });
    }

    public String UnixToString(Long unixseconds) {
        Date date = new Date(unixseconds * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        String formattedTime = sdf.format(date);
        return(formattedTime);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

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
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {


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




