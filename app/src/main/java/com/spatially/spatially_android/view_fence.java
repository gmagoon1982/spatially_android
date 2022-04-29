package com.spatially.spatially_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DecimalFormat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class view_fence extends AppCompatActivity {

    MapView mMapView;
    GoogleMap googleMap;
    String email, password, reply_array_fence_name, fence_id;
    Double radius, latitude, longitude;
    Bundle extras;
    float view_fence_zoom_level;
    TextView fence_name;
    ImageButton changemap;

    Integer map_type = 0;

    Double radius_padding;
    SharedPreferences sharedPref;
    String reply;

    Toast toast;
    Boolean logging=false;

    Integer code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            if(getIntent().hasExtra("email") && getIntent().hasExtra("password") && getIntent().hasExtra("fence") && getIntent().hasExtra("latitude")&& getIntent().hasExtra("longitude")) {

                extras = getIntent().getExtras();
                email = extras.getString("email");
                password = extras.getString("password");
                reply_array_fence_name = extras.getString("fence");
                radius = extras.getDouble("radius");
                latitude = extras.getDouble("latitude");
                longitude = extras.getDouble("longitude");
                fence_id = extras.getString("fence_id");
            }
            //System.out.println(" radius is: " + radius);
            code = Integer.parseInt(getResources().getString(R.string.permissions_code));

            sharedPref = getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

            if(sharedPref.contains("logging"))
            {
                logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
            }

            setTheme(R.style.AppTheme_NoActionBar);

            setContentView(R.layout.view_fence);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            fence_name =  (TextView) (toolbar.findViewById(R.id.title));
            fence_name.setText(getIntent().getExtras().getString("fence"));
            fence_name.setTextSize(getResources().getDimension(R.dimen.view_fence_title));

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
                toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                 */

                if(logging==true) {
                    Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                }

            }

            TextView Back = (TextView) findViewById(R.id.Back);
            TextView Edit = (TextView) findViewById(R.id.Edit);
            changemap = (ImageButton) findViewById(R.id.changemap);

            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {

                    googleMap = mMap;

                    // Default map type
                    set_map_type(map_type);

                    DrawFence();

                    view_fence_zoom_level = Float.parseFloat(getResources().getString(R.string.view_fence_zoom_level));

                    googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                        @Override
                        public void onCameraMove() {

                            CameraPosition position = googleMap.getCameraPosition();
                            view_fence_zoom_level = position.zoom;
                        }
                    });

                }

            });

            Back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            });

            Edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    extras = new Bundle();
                    extras.putString("email", email);
                    extras.putString("password", password);
                    extras.putString("fence", reply_array_fence_name);
                    extras.putDouble("radius", radius);
                    extras.putDouble("latitude", latitude);
                    extras.putDouble("longitude", longitude);
                    extras.putString("fence_id", fence_id);
                    extras.putInt("map_type", googleMap.getMapType());
                    extras.putFloat("view_fence_zoom", view_fence_zoom_level);
                    Intent i = new Intent(view_fence.this, edit_fence.class);
                    i.putExtras(extras);
                    startActivityForResult(i, 1);

                }
            });

            changemap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] maptype = {"Standard", "Hybrid", "Satellite"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(view_fence.this, R.style.AlertDialogStyle);

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

    public void set_map_type(int map_type){

        if(map_type==0) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            changemap.setColorFilter(Color.BLACK);
            fence_name.setTextColor(Color.BLACK);

        }
        else if(map_type==1) {

            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            changemap.setColorFilter(Color.WHITE);
            fence_name.setTextColor(Color.WHITE);
        }
        else if(map_type==2){

            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            changemap.setColorFilter(Color.WHITE);
            fence_name.setTextColor(Color.WHITE);

        }

    }

    private Marker true_if_km(){

        if(radius < 1000) {
            return (googleMap.addMarker(new MarkerOptions().title("Lat: " + new DecimalFormat("#.##").format(latitude) + ", Lng: " + new DecimalFormat("#.##").format(longitude) + ", Radius: " + radius + "m").position(new LatLng(latitude, longitude))));
        }

        return (googleMap.addMarker(new MarkerOptions().title("Lat: " + new DecimalFormat("#.##").format(latitude) + ", Lng: " + new DecimalFormat("#.##").format(longitude) + ", Radius: " + (radius/1000) + "km").position(new LatLng(latitude, longitude))));

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == 1) {
                if (resultCode == Activity.RESULT_OK) {

                    latitude = data.getDoubleExtra("new_latitude", 0);
                    longitude = data.getDoubleExtra("new_longitude", 0);
                    radius = data.getDoubleExtra("new_radius", 0);
                    reply_array_fence_name = data.getStringExtra("new_fence_name");

                    fence_name.setText(reply_array_fence_name);

                    //System.out.println("Data from edit_fence.java: " + latitude + " " + longitude + " " + radius);
                    // First need to delete old fence, then add new one with location
                    String URL = sharedPref.getString("Spatially_Host", getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/remove-fence.cgi?email=" + email + "&password=" + password + "&fenceId=" + fence_id;

                    OkHttpClient client = new OkHttpClient();

                    Request request = new Request.Builder()
                            .url(URL)
                            .build();

                    if(logging==true)
                    {
                        Log.i(getResources().getString(R.string.TAG), URL);
                    }

                    // Asynchronous Get
                    // Runs in a background thread (off the main thread)
                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_occurred), Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();

                                    if(logging==true)
                                    {
                                        Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.error_occurred));
                                        Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                                    }

                                    return;
                                }
                            });

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            //System.out.println("Response code: " + response.code());
                            reply = response.body().string().trim().toString();

                            String URL = sharedPref.getString("Spatially_Host", getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/add-fence.cgi?email=" + email + "&password=" + password + "&fence_name=" + reply_array_fence_name + "&lat=" + new DecimalFormat(getResources().getString(R.string.No_of_Decimals_for_edit_fence)).format(latitude) + "&lng=" + new DecimalFormat(getResources().getString(R.string.No_of_Decimals_for_edit_fence)).format(longitude) + "&radius=" + radius;

                            OkHttpClient client = new OkHttpClient();

                            Request request = new Request.Builder()
                                    .url(URL)
                                    .build();

                            if(logging==true)
                            {
                                Log.i(getResources().getString(R.string.TAG), URL);
                            }

                            // Asynchronous Get
                            // Runs in a background thread (off the main thread)
                            client.newCall(request).enqueue(new Callback() {


                                @Override
                                public void onFailure(Call call, IOException e) {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_occurred), Toast.LENGTH_SHORT);
                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                            toast.show();

                                            if(logging==true)
                                            {
                                                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.error_occurred));
                                                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                                            }

                                            return;
                                        }
                                    });

                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {

                                    String reply = response.body().string().trim();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            toast.setDuration(Toast.LENGTH_LONG);
                                            toast.setText(getResources().getString(R.string.fence_updated));
                                            toast.show();

                                            if(logging==true)
                                            {
                                                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.fence_updated));
                                            }

                                            DrawFence();

                                        }
                                    });

                                }
                            });
                        }
                    });
                }
            }
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

        }

    }

    private void DrawFence()
    {
        googleMap.clear();

        Marker marker = true_if_km();
        marker.showInfoWindow();

        radius_padding = 3*radius;

        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        float mapWidth = mMapView.getWidth() / metrics.density;

        final int EQUATOR_LENGTH = 40075004;
        //final int TIME_ANIMATION_MILIS = 1500;
        final double latitudinalAdjustment = Math.cos(Math.PI * marker.getPosition().latitude / 180.0);
        final double arg = EQUATOR_LENGTH * mapWidth * latitudinalAdjustment / (radius_padding * 256.0);
        double valToZoom = Math.log(arg) / Math.log(2.0);

        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(new LatLng(latitude, longitude));
        circleOptions.radius(radius);
        circleOptions.strokeColor(getResources().getColor(R.color.view_fence_border_colour));
        circleOptions.fillColor(getResources().getColor(R.color.view_fence_fill_colour));
        circleOptions.strokeWidth(getResources().getDimension(R.dimen.view_fence_border_width));
        googleMap.addCircle(circleOptions);

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), Float.valueOf(String.valueOf(valToZoom))));

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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        //System.out.println("onRequestPermissionsResult was called");
        if (requestCode == code) {

            if (grantResults.length == permissions.length) {

                for (int i = 0; i < permissions.length; i++) {

                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {

                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setText("Permission Denied for: " + permissions[i]);
                        toast.show();
                    }
                }

            }

        }

    }

}






