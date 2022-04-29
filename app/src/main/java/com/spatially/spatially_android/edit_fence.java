package com.spatially.spatially_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;

public class edit_fence extends AppCompatActivity {

    MapView mMapView;
    private GoogleMap googleMap;
    private String email, password, reply_array_fence_name, fence_id;
    private Double radius, latitude, longitude;
    Bundle extras;
    EditText fence_name;
    TextView title;
    TextView Cancel, Done;
    ImageButton decrease;
    ImageButton increase;
    ImageButton changemap;
    SharedPreferences sharedPref;

    LatLng center;

    String URL;

    Integer map_type;

    float edit_fence_zoom_level;

    Double radius_padding;

    Boolean logging=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            sharedPref = getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

            if(sharedPref.contains("logging"))
            {
                logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
            }

            setTheme(R.style.AppTheme_NoActionBar);

            setContentView(R.layout.edit_fence);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            TextView Title = (TextView) (toolbar.findViewById(R.id.title));
            Title.setText("Edit Fence");
            Title.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.edit_fence_title)/getResources().getDisplayMetrics().density);

            extras = getIntent().getExtras();
            email = extras.getString("email");
            password = extras.getString("password");
            reply_array_fence_name = extras.getString("fence");
            radius = extras.getDouble("radius");
            latitude = extras.getDouble("latitude");
            longitude = extras.getDouble("longitude");
            fence_id = extras.getString("fence_id");
            edit_fence_zoom_level = extras.getFloat("view_fence_zoom");

            center = new LatLng(latitude, longitude);

            decrease = (ImageButton) findViewById(R.id.decrease);
            increase = (ImageButton) findViewById(R.id.increase);

            fence_name = (EditText) findViewById(R.id.fence_name);
            fence_name.setText(reply_array_fence_name);

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


                if(logging==true) {
                    Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                }


            }

            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {

                    googleMap = mMap;

                    if (extras.containsKey("map_type")) {

                        googleMap.setMapType(extras.getInt("map_type"));
                    }
                    else {
                        map_type=0;
                        set_map_type(map_type);

                    }

                    DrawFence();

                    googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                        @Override
                        public void onCameraMove() {

                            googleMap.clear();

                            center = new LatLng(googleMap.getCameraPosition().target.latitude, googleMap.getCameraPosition().target.longitude);

                            Marker marker = true_if_km(center);
                            marker.showInfoWindow();

                            CircleOptions circleOptions = new CircleOptions();
                            circleOptions.center(center);
                            circleOptions.radius(radius);
                            circleOptions.strokeColor(getResources().getColor(R.color.edit_fence_border_colour)).fillColor(getResources().getColor(R.color.edit_fence_fill_colour));
                            circleOptions.strokeWidth(getResources().getDimension(R.dimen.edit_fence_border_width));
                            googleMap.addCircle(circleOptions);


                        }
                    });


                }


            });

            Cancel = (TextView) findViewById(R.id.Cancel);
            Cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(logging==true) {
                        Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.edit_fence_cancelled));
                    }

                    finish();
                }
            });

            fence_name.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    reply_array_fence_name = fence_name.getText().toString();
                }
            });

            Done = (TextView) findViewById(R.id.Done);
            Done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                                        if(logging==true) {
                                            Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.fence_edited));
                                        }


                                        Intent data = new Intent();
                                        data.putExtra("new_latitude", center.latitude);
                                        data.putExtra("new_longitude", center.longitude);
                                        data.putExtra("new_radius", radius);
                                        data.putExtra("new_fence_name", reply_array_fence_name);
                                        setResult(Activity.RESULT_OK, data);
                                        finish();

                                    }

                                });


            title = (TextView) findViewById(R.id.title);

            changemap = (ImageButton) findViewById(R.id.changemap);
            changemap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] maptype = {"Standard", "Hybrid", "Satellite"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(edit_fence.this, R.style.AlertDialogStyle);

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

            increase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    radius = radius * 2;
                    googleMap.clear();

                    Marker marker = true_if_km(center);
                    marker.showInfoWindow();

                    CircleOptions fence_circleoptions = new CircleOptions().radius(radius).center(center).strokeColor(getResources().getColor(R.color.edit_fence_border_colour)).fillColor(getResources().getColor(R.color.edit_fence_fill_colour)).strokeWidth(getResources().getDimension(R.dimen.edit_fence_border_width));
                    googleMap.addCircle(fence_circleoptions);

                    if(logging==true) {
                        Log.i(getResources().getString(R.string.TAG), "Fence increased to: " + radius.toString());
                    }

                    //Toast.makeText(getApplicationContext(), "Radius = " + radius + "m", Toast.LENGTH_SHORT).show();
                }
            });

            decrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    radius = radius / 2;
                    googleMap.clear();

                    Marker marker = true_if_km(center);
                    marker.showInfoWindow();

                    CircleOptions fence_circleoptions = new CircleOptions().radius(radius).center(center).strokeColor(getResources().getColor(R.color.edit_fence_border_colour)).fillColor(getResources().getColor(R.color.edit_fence_fill_colour)).strokeWidth(getResources().getDimension(R.dimen.edit_fence_border_width));
                    googleMap.addCircle(fence_circleoptions);

                    if(logging==true) {
                        Log.i(getResources().getString(R.string.TAG), "Fence decreased to: " + radius.toString());
                    }

                    //Toast.makeText(getApplicationContext(), "Radius = " + radius + "m", Toast.LENGTH_SHORT).show();

                }
            });

        } catch (Exception e) {

            if(getApplicationContext()!=null) {
                ShowErrorDetails(Log.getStackTraceString(e), getApplicationContext());
            }

            /*
            toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
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
            fence_name.setTextColor(Color.BLACK);
            title.setTextColor(Color.BLACK);

        } else if (map_type == 1) {

            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            changemap.setColorFilter(Color.WHITE);
            fence_name.setTextColor(Color.WHITE);
            title.setTextColor(Color.WHITE);

        } else if (map_type == 2) {

            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            changemap.setColorFilter(Color.WHITE);
            fence_name.setTextColor(Color.WHITE);
            title.setTextColor(Color.WHITE);
        }
    }

    private Marker true_if_km(LatLng latLng){

        if(radius < 1000) {
            return (googleMap.addMarker(new MarkerOptions().title("Lat: " + new DecimalFormat("#.##").format(latLng.latitude) + ", Lng: " + new DecimalFormat("#.##").format(latLng.longitude) + ", Radius: " + radius + "m").position(latLng)));
        }

        return (googleMap.addMarker(new MarkerOptions().title("Lat: " + new DecimalFormat("#.##").format(latLng.latitude) + ", Lng: " + new DecimalFormat("#.##").format(latLng.longitude) + ", Radius: " + (radius/1000) + "km").position(latLng)));

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

    private void DrawFence()
    {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(new LatLng(latitude, longitude));
        circleOptions.radius(radius);
        circleOptions.strokeColor(getResources().getColor(R.color.edit_fence_border_colour));
        circleOptions.fillColor(getResources().getColor(R.color.edit_fence_fill_colour));
        circleOptions.strokeWidth(getResources().getDimension(R.dimen.edit_fence_border_width));
        googleMap.addCircle(circleOptions);

        radius_padding = 3*radius;
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        float mapWidth = mMapView.getWidth() / metrics.density;

        final int EQUATOR_LENGTH = 40075004;
        final int TIME_ANIMATION_MILIS = 1500;
        final double latitudinalAdjustment = Math.cos(Math.PI * latitude / 180.0);
        final double arg = EQUATOR_LENGTH * mapWidth * latitudinalAdjustment / (radius_padding * 256.0);
        double valToZoom = Math.log(arg) / Math.log(2.0);

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), Float.valueOf(String.valueOf(valToZoom))), TIME_ANIMATION_MILIS , null);

    }

}
