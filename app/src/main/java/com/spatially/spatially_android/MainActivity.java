package com.spatially.spatially_android;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    Bundle extras;
    Intent intent;
    String email, password, URL;

    // Global variables
    Integer unread_notifications = 0;
    Boolean live_location_self = false;
    Boolean offline = false;
    SharedPreferences sharedPref;

    BottomNavigationView navView;
    BottomNavigationMenuView menuView;
    BottomNavigationView mBottomNavigationView;
    Menu menu;

    BadgeDrawable notifications_badge;

    Integer notification_count;

    String sharedPref_Spatially_Host, sharedPref_Spatially_Port;
    Toast toast;
    Boolean logging = false;
    private spatially_viewmodel model;
    String reply;

    List<String> reply_array;

    List<String> name;
    List<String> account_creation_time;
    List<String> id_time;
    List<String> email_info;
    List<String> battery_info;
    List<String> accuracy_info;
    List<String> latitude;
    List<String> longitude;
    List<String> id;
    List<String> last_movement_time;

    List<String> fences;

    List<String> fence_names = new ArrayList<>();
    List<String> fence_center_latitude = new ArrayList<>();
    List<String> fence_center_longitude = new ArrayList<>();
    List<String> fence_ids = new ArrayList<>();
    List<String> fence_radius = new ArrayList<>();

    List<String> notifications = new ArrayList<>();
    List<String> notifications_time = new ArrayList<>();

    String notification_friends_email;
    AlertDialog.Builder new_friend_alert;

    String near_attribute = "NO", hide_attribute = "NO";

    String CHANNEL_ID = "Spatially";
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;
    NotificationManagerCompat notificationManagerCompat;
    long[] notification_pattern = {0, 300, 300, 300};

    Integer badge_count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            if (getApplicationContext() != null) {

                model = new ViewModelProvider(this).get(spatially_viewmodel.class);

                sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

                sharedPref_Spatially_Host = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST));
                sharedPref_Spatially_Port = sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT));

                if (sharedPref.contains("logging")) {
                    logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
                }

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

                    toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.must_enable_internet_for_spatially), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                    if (logging == true) {
                        Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.must_enable_internet_for_spatially));
                    }


                    return;
                } else {

                    this.getSupportActionBar().hide();

                    setContentView(R.layout.activity_main);
                    navView = (BottomNavigationView) findViewById(R.id.nav_view);
                    navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

                    menuView = (BottomNavigationMenuView) navView.getChildAt(0);

                    mBottomNavigationView = (BottomNavigationView) findViewById(R.id.nav_view);
                    navView = (BottomNavigationView) findViewById(R.id.nav_view);

                    menu = navView.getMenu();

                    notifications_badge = navView.getOrCreateBadge(menu.getItem(3).getItemId());
                    notifications_badge.setBadgeGravity(BadgeDrawable.TOP_END);
                    notifications_badge.setBackgroundColor(getResources().getColor(R.color.RED));

                    //System.out.println("Number of items in BottomNavigationMenu are: " + menuView.getChildCount());

                    extras = getIntent().getExtras();
                    if (extras != null && extras.containsKey("email") && extras.containsKey("password")) {
                        email = extras.getString("email");
                        password = extras.getString("password");
                    }

                    // Declaring a Bundle which is extracted from the Intent
                    intent = getIntent();
                    extras = intent.getExtras();
                    extras.putString("email", email);
                    extras.putString("password", password);

                    ReadNotificationsFromDisk();

                    //test();

                    //badgeDrawable.setVisible(false);
                    //navView.removeBadge(R.id.notifications);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("live_location", live_location_self);
                    editor.commit();

                    loadFragment(new friends_fragment(), extras);

                    // Heartbeat
                    if (live_location_self == true && offline == false) {
                        //Declare the timer
                        Timer t = new Timer();
                        //Set the schedule function and rate
                        t.scheduleAtFixedRate(new TimerTask() {

                                                  @Override
                                                  public void run() {

                                                      // Have to use getString to get string associated with specific resource id
                                                      URL = sharedPref.getString("Spatially_Host", String.valueOf(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", String.valueOf(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/update-pos.cgi?email=" + email + "&password=" + password + "&bat=" + String.valueOf(getBatteryLevel());

                                                      OkHttpClient client = new OkHttpClient();

                                                      Request request = new Request.Builder()
                                                              .url(URL)
                                                              .build();

                                                      if (logging == true) {
                                                          Log.i(getResources().getString(R.string.TAG), URL);
                                                      }

                                                      // Asynchronous Get
                                                      // Runs in a background thread (off the main thread)
                                                      client.newCall(request).enqueue(new Callback() {

                                                          @Override
                                                          public void onFailure(Call call, IOException e) {
                                                          }

                                                          @Override
                                                          public void onResponse(Call call, Response response) throws IOException {
                                                          }

                                                      });

                                                  }
                                              },
                                0, Long.parseLong(getResources().getString(R.string.one_min_to_ms)) * Long.parseLong(getResources().getString(R.string.mainactivity_heartbeatDelaymin))); // 1/2 hour, starts right away

                        // End of heartbeat

                    }

                    loadFragment(new friends_fragment(), extras);

                    // Timer that gets data from server into view model
                    if (offline == false) {

                        // Causes a problem and adds an extra "You" in friends list sometimes
                        // Need to create a timer that will update friends list every 1/2 hour

                        Timer t = new Timer();

                        //Set the schedule function and rate
                        t.scheduleAtFixedRate(new TimerTask() {

                                                  @Override
                                                  public void run() {

                                                      if (getApplicationContext() != null) {

                                                          runOnUiThread(new Runnable() {
                                                              @Override
                                                              public void run() {

                                                                  if (getApplicationContext() != null) {
                                                                      if (logging == true) {
                                                                          Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.getting_data_from_server));
                                                                      }

                                                                      getDataFromServer();
                                                                      WriteNotificationDatatoDisk();

                                                                      toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.loading_location_data), Toast.LENGTH_SHORT);
                                                                      toast.setGravity(Gravity.CENTER, 0, 0);
                                                                      toast.show();
                                                                  }


                                                              }
                                                          });
                                                      }


                                                  }
                                              },
                                0, Long.parseLong(getResources().getString(R.string.one_min_to_ms)) * Long.parseLong(getResources().getString(R.string.data_update_time)));

                    }

                }

            }


        } catch (Exception e) {
            if (getApplicationContext() != null) {
                ShowErrorDetails(Log.getStackTraceString(e), this);
            }

            /*
            toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            */

            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }
        }

    }

    private boolean loadFragment(Fragment fragment, Bundle extras) {

        try {
            // This replaces the contents of the fragment_container with user's fragment
            if (fragment != null) {

                // This puts in extra bundle to the fragment
                fragment.setArguments(extras);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();

                return true;
            }


        } catch (Exception e) {

            if (this != null) {
                ShowErrorDetails(Log.getStackTraceString(e), this);
            }

        /*
        toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        */

            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

            return false;
        }

        return false;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;

    {

        // Declaring a Bundle which is extracted from the Intent
        extras = new Bundle();
        extras.putString("email", email);
        extras.putString("password", password);

        try {

            mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {

                        case R.id.friends:

                            loadFragment(new friends_fragment(), extras);
                            return true;

                        case R.id.map:

                            loadFragment(new map_fragment_show_all_friends(), extras);
                            return true;

                        case R.id.fences:

                            loadFragment(new fences_fragment(), extras);
                            return true;

                        case R.id.notifications:

                            loadFragment(new notifications_fragment(), extras);
                            return true;

                        case R.id.help:

                            loadFragment(new help_fragment(), extras);
                            return true;

                    }

                    return false;
                }


            };
        } catch (Exception e) {

            if (this != null) {
                ShowErrorDetails(Log.getStackTraceString(e), this);
            }
            /*
            toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

             */

            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }


        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    // The app just came into view again
    @Override
    protected void onResume() {

        if(notifications_badge!=null)
        {
            if(badge_count!=0)
            {
                notifications_badge.setNumber(badge_count);
                notifications_badge.setVisible(true);
            }
            else
            {
                notifications_badge.setVisible(false);
            }
        }

        super.onResume();

    }

    public float getBatteryLevel() {

        try {

            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.get_battery_level));
            }

            // We are registering a broadcast receiver to the ACTION_BATTERY_CHANGED Intent to get
            // the battery levels. With IntentFilter you can filter out intent for a specific event
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = (float) level / (float) scale;
            return batteryPct;

        } catch (Exception e) {

            if (getApplicationContext() != null) {
                ShowErrorDetails(Log.getStackTraceString(e), getApplicationContext());
            }

            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

            return 0;

        }

    }

    @Override
    public void onBackPressed() {

        if(getSupportFragmentManager()!=null)
        {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {

                AlertDialog.Builder log_off = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                log_off.setMessage("Log out of spatially?");
                log_off.setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        // Finish the activity gracefully
                        finish();
                        overridePendingTransition(0, R.anim.out_from_right);
                    }
                });

                log_off.setNegativeButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if(sharedPref.contains("email") && sharedPref.contains("password"))
                        {
                            sharedPref.edit().remove("email");
                            sharedPref.edit().remove("password");
                            sharedPref.edit().commit();
                            sharedPref.edit().apply();
                        }

                        // Finish the activity gracefully
                        finish();
                        overridePendingTransition(0, R.anim.out_from_right);
                    }

                });

                log_off.show();

            }
            else
            {
                getSupportFragmentManager().popBackStack();
            }
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (getApplicationContext() != null) {
            // Stop the LocationServiceGPS service on destroy
            Intent intent = new Intent(getApplicationContext(), locationservice_listener.class);
            getApplicationContext().stopService(intent);
        }

        if (logging == true) {
            Log.i(getResources().getString(R.string.TAG), "Calling onDestroy() in MainActivity");
        }


    }

    public void getDataFromServer() {

        try {

            ClearAll();

            // Hard coded host and port for the server are in the strings.xml resources file
            // Have to use getString to get string associated with specific resource id
            URL = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/get-friends-list.cgi?email=" + email + "&password=" + password;

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(URL)
                    .build();

            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), URL);
            }

            // Asynchronous Get
            // Runs in a background thread (off the main thread)
            client.newCall(request).enqueue(new Callback() {


                @Override
                public void onFailure(Call call, IOException e) {

                    if (logging == true) {
                        Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                    }

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // onResponse already assures the response is successful, apparently.
                    //if(response.isSuccessful()) {
                    reply = response.body().string().trim();

                    if (reply != null) {

                        reply_array = new ArrayList<String>(Arrays.asList(reply.split("\n")));

                        //System.out.println(reply_array);

                        //reply_array = reply.split("\n");

                        //System.out.println("The size of reply_array including OK is: " + reply_array.size());

                        name = new ArrayList<String>((reply_array.size() - 1));
                        account_creation_time = new ArrayList<String>((reply_array.size() - 1));
                        id_time = new ArrayList<String>((reply_array.size() - 1));
                        fences = new ArrayList<String>((reply_array.size() - 1));
                        email_info = new ArrayList<String>((reply_array.size() - 1));
                        battery_info = new ArrayList<String>((reply_array.size() - 1));
                        accuracy_info = new ArrayList<String>((reply_array.size() - 1));
                        latitude = new ArrayList<String>((reply_array.size() - 1));
                        longitude = new ArrayList<String>((reply_array.size() - 1));
                        id = new ArrayList<String>((reply_array.size() - 1));
                        last_movement_time = new ArrayList<String>((reply_array.size() - 1));

                        // Have to exclude the last element which is an OK
                        for (int i = 0; i <= (reply_array.size() - 2); i++) {

                            // If the user is new and only first three fields exist
                            if ((reply_array.get(i).split(",")).length == 3) {

                                email_info.add(reply_array.get(i).split(",")[0]);
                                name.add(reply_array.get(i).split(",")[1]);

                                // seconds to milliseconds
                                long unixseconds = Long.parseLong(reply_array.get(i).split(",")[2]);
                                Date date = new Date(unixseconds * 1000L);
                                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a dd/MM/yyyy");
                                String formattedDate = sdf.format(date);

                                account_creation_time.add(formattedDate);

                                id.add(null);
                                id_time.add(null);
                                latitude.add(null);
                                longitude.add(null);
                                accuracy_info.add(null);
                                battery_info.add(null);
                                last_movement_time.add(null);

                                // Dealing with a different scenario with 8 fields
                            } else if ((reply_array.get(i).split(",")).length == 8) {

                                //System.out.println("reply_array at " + i + " is a normal user!");

                                //System.out.println("TEST: " + (reply_array.get(i).split(","))[0].trim());

                                email_info.add(reply_array.get(i).split(",")[0]);
                                name.add(reply_array.get(i).split(",")[1]);

                                // seconds to milliseconds
                                long unixseconds = Long.parseLong(reply_array.get(i).split(",")[2]);
                                Date date = new Date(unixseconds * 1000L);
                                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a dd/MM/yyyy");
                                String formattedDate = sdf.format(date);
                                account_creation_time.add(formattedDate);

                                id.add(reply_array.get(i).split(",")[3]);

                                long unixseconds2 = Long.parseLong(reply_array.get(i).split(",")[4]);

                                long current_time = System.currentTimeMillis() / 1000;
                                if ((current_time - unixseconds2) < 1) {
                                    last_movement_time.add("Now");
                                } else if ((current_time - unixseconds2) < 60) {
                                    last_movement_time.add((current_time - unixseconds2) + " sec ago");
                                } else if ((current_time - unixseconds2) < 3600) {
                                    last_movement_time.add(((current_time - unixseconds2) / 60) + " min ago");
                                } else {
                                    Date date3 = new Date(unixseconds2 * 1000L);
                                    SimpleDateFormat sdf3 = new SimpleDateFormat("hh:mm a");
                                    String formattedDate3 = sdf3.format(date3);
                                    last_movement_time.add(formattedDate3);
                                }

                                id_time.add(null);

                                latitude.add(reply_array.get(i).split(",")[5]);
                                longitude.add(reply_array.get(i).split(",")[6]);

                                accuracy_info.add(reply_array.get(i).split(",")[7]);

                                battery_info.add(null);

                                // If the user's info has 10 fields
                            } else if (((reply_array.get(i).split(",").length) == 10)) {

                                email_info.add(reply_array.get(i).split(",")[0]);
                                name.add(reply_array.get(i).split(",")[1]);

                                // seconds to milliseconds
                                long unixseconds = Long.parseLong(reply_array.get(i).split(",")[2]);
                                Date date = new Date(unixseconds * 1000L);
                                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a dd/MM/yyyy");
                                String formattedDate = sdf.format(date);
                                account_creation_time.add(formattedDate);

                                id.add(reply_array.get(i).split(",")[3]);

                                long unixseconds2 = Long.parseLong(reply_array.get(i).split(",")[4]);
                                Date date2 = new Date(unixseconds2 * 1000L);
                                SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm a dd/MM/yyyy");
                                String formattedDate2 = sdf2.format(date2);

                                id_time.add(formattedDate2);

                                latitude.add(reply_array.get(i).split(",")[5]);
                                longitude.add(reply_array.get(i).split(",")[6]);
                                accuracy_info.add(reply_array.get(i).split(",")[7]);

                                battery_info.add(reply_array.get(i).split(",")[8]);

                                long unixseconds3 = Long.parseLong(reply_array.get(i).split(",")[9]);

                                long current_time = System.currentTimeMillis() / 1000L;
                                if ((current_time - unixseconds3) < 1) {
                                    last_movement_time.add("Now");
                                } else if ((current_time - unixseconds3) < 60) {
                                    last_movement_time.add((current_time - unixseconds3) + " sec ago");
                                } else if ((current_time - unixseconds3) < 3600) {
                                    last_movement_time.add(((current_time - unixseconds3) / 60) + " min ago");
                                } else {
                                    Date date3 = new Date(unixseconds3 * 1000L);
                                    SimpleDateFormat sdf3 = new SimpleDateFormat("hh:mm a");
                                    String formattedDate3 = sdf3.format(date3);
                                    last_movement_time.add(formattedDate3);
                                }

                            }

                        }

                        /*
                        System.out.println(name);
                        System.out.println(email_info);
                        System.out.println(last_movement_time);
                        System.out.println(latitude);
                        System.out.println(longitude);

                        */

                        // Adding in the info for YOU to the existing friends list

                        email_info.add(0, email);
                        // Need to update later with details
                        // Need to insert "You" into the friends list
                        name.add(0, "You");

                        if (sharedPref.contains("user_reading")) {

                            last_movement_time.add(0, sharedPref.getString("user_reading", null));
                        } else {
                            last_movement_time.add(0, null);
                        }

                        if (sharedPref.contains("user_latitude") && sharedPref.contains("user_longitude")) {
                            latitude.add(0, sharedPref.getString("user_latitude", null));
                            longitude.add(0, sharedPref.getString("user_longitude", null));
                        } else {

                            latitude.add(0, null);
                            longitude.add(0, null);

                        }
                        //latitude.add(0, Double.parseDouble(String.format("%.2f", getUserGPS().getLatitude())));
                        //longitude.add(0, Double.parseDouble(String.format("%.2f", getUserGPS().getLongitude())));

                        battery_info.add(0, String.valueOf(getBatteryLevel()));

                    }

                    /*
                      // Check the data
                      System.out.println("FRIENDS DATA:");
                      System.out.println(name);
                      System.out.println(account_creation_time);
                      System.out.println(id_time);
                      System.out.println(email_info);
                      System.out.println(battery_info);
                      System.out.println(accuracy_info);
                      System.out.println(latitude);
                      System.out.println(longitude);
                      System.out.println(id);
                      System.out.println(last_movement_time);
                     */

                      runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              model.setName((ArrayList) name);
                              model.setAccount_creation_time((ArrayList) account_creation_time);
                              model.setId_time((ArrayList) id_time);
                              model.setFences((ArrayList) fences);
                              model.setEmail_info((ArrayList) email_info);
                              model.setBattery_info((ArrayList) battery_info);
                              model.setAccuracy_info((ArrayList) accuracy_info);
                              model.setLatitude((ArrayList) latitude);
                              model.setLongitude((ArrayList) longitude);
                              model.setId((ArrayList) id);
                              model.setLast_movement_time((ArrayList) last_movement_time);

                          }
                      });


                    // The fence data is acquired here.
                    // Need to read in fences again to update fences related info in friends list
                    URL = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/get-fence-list.cgi?email=" + email + "&password=" + password;

                    OkHttpClient client2 = new OkHttpClient();

                    Request request2 = new Request.Builder()
                            .url(URL)
                            .build();

                    if (logging == true) {
                        Log.i(getResources().getString(R.string.TAG), URL);
                    }

                    // Asynchronous Get
                    // Runs in a background thread (off the main thread)
                    client2.newCall(request2).enqueue(new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {

                            if (logging == true) {
                                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                            }

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            reply = response.body().string().trim();

                            if (reply != null) {

                                //System.out.println("Fences response: " + Arrays.asList(reply.split("\n")));

                                reply_array = new ArrayList<String>(Arrays.asList(reply.split("\n")));

                                // Have to exclude the OK
                                for (int i = 0; i <= (reply_array.size() - 2); i++) {

                                    //System.out.println(reply_array[i]);
                                    fence_ids.add((reply_array.get(i).split(",")[0]));
                                    fence_names.add(reply_array.get(i).split(",")[4]);
                                    fence_center_latitude.add(reply_array.get(i).split(",")[1]);
                                    fence_center_longitude.add(reply_array.get(i).split(",")[2]);
                                    fence_radius.add(reply_array.get(i).split(",")[3]);

                                }

                                /*
                                System.out.println("FENCES DATA:");
                                System.out.println(fence_ids);
                                System.out.println(fence_names);
                                System.out.println(fence_center_latitude);
                                System.out.println(fence_center_longitude);
                                System.out.println(fence_radius);
                                */

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        model.setFence_ids((ArrayList) fence_ids);
                                        model.setFence_names((ArrayList) fence_names);
                                        model.setFence_center_latitude((ArrayList) fence_center_latitude);
                                        model.setFence_center_longitude((ArrayList) fence_center_longitude);
                                        model.setFence_radius((ArrayList) fence_radius);

                                    }
                                });
                            }
                        }

                    });

                    URL = sharedPref.getString("Spatially_Host", String.valueOf(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", String.valueOf(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/get-notifications.cgi?email=" + email + "&password=" + password;

                    OkHttpClient client = new OkHttpClient();

                    Request request = new Request.Builder()
                            .url(URL)
                            .build();

                    if (logging == true) {
                        Log.i(getResources().getString(R.string.TAG), URL);
                    }

                    // Asynchronous Get
                    // Runs in a background thread (off the main thread)
                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {

                            if (logging == true) {
                                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                            }
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            // onResponse already assures the response is successful, apparently.
                            //if(response.isSuccessful()) {

                            String reply1 = response.body().string().trim();

                            //String reply1 = "one\ntwo\nthree\nfour\nfive";

                            List<String> reply_array1 = Arrays.asList(reply1.split("\n"));

                            //System.out.println("reply_array in notifications: " + reply_array1);

                            if (reply1 != null) {

                                runOnUiThread(new Runnable() {
                                    public void run() {

                                        if ((reply1 != null) && (reply_array1.size() >= 1)) {

                                            // Count downwards
                                            for (int i = (reply_array1.size() - 1); i >= 0; i--) {

                                                if ((reply_array1.get(i) != null) && (reply_array1.get(i) != "OK")) {

                                                    // id, unix time, email, notification_type, details
                                                    if ((reply_array1.get(i)).split(",").length == 5) {

                                                        String notification_details = (((reply_array1.get(i)).split(","))[4]).trim();
                                                        String notification_time = ((reply_array1.get(i)).split(","))[1].trim();

                                                                                        /*
                                                                                        // notification is at 4th index, time is at 1st index
                                                                                        long unixseconds = Long.parseLong(notification_time);
                                                                                        Date date = new Date(unixseconds * 1000L);
                                                                                        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                                                                                        String formattedDate = sdf.format(date);
                                                                                        */

                                                        String notification_type = (((reply_array1.get(i)).split(","))[3]).trim();

                                                        //System.out.println("notification: " + notification_type + " " + notification_time  + " " + notification_details);

                                                        // If notification is of accepted friend request, notify user
                                                        if (notification_type.equals("FRIEND_REQUEST")) {

                                                            notification_friends_email = (((reply_array1.get(i)).split(","))[2]).trim();

                                                            if (searchNameforEmail(notification_friends_email) != null) {

                                                                new_friend_alert = new AlertDialog.Builder(getApplicationContext(), R.style.AlertDialogStyle);
                                                                new_friend_alert.setMessage(searchNameforEmail(notification_friends_email) + " wants to share location and view yours. Add friend ?");

                                                                new_friend_alert.setPositiveButton("No", new DialogInterface.OnClickListener() {

                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                                        dialogInterface.dismiss();

                                                                    }

                                                                });

                                                                new_friend_alert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {

                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                                        MainActivity.this.URL = sharedPref_Spatially_Host + ":" + sharedPref_Spatially_Port + "/cgi-bin/boundaries" + "/accept-friend.cgi?email=" + email + "&password=" + password + "&friendemail=" + notification_friends_email;

                                                                        OkHttpClient client = new OkHttpClient();

                                                                        Request request = new Request.Builder()

                                                                                .url(MainActivity.this.URL)
                                                                                .build();

                                                                        if (logging == true) {
                                                                            Log.i(getResources().getString(R.string.TAG), MainActivity.this.URL);
                                                                        }

                                                                        client = new OkHttpClient.Builder()
                                                                                .connectTimeout(Long.parseLong(getResources().getString(R.string.login_timeout_seconds)), TimeUnit.SECONDS)
                                                                                .build();

                                                                        // Asynchronous Get
                                                                        // Runs in a background thread (off the main thread)
                                                                        client.newCall(request).enqueue(new Callback() {

                                                                            @Override
                                                                            public void onFailure(Call call, IOException e) {

                                                                                if (logging == true) {
                                                                                    Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onResponse(Call call, Response response) throws IOException {

                                                                                if (response.isSuccessful()) {
                                                                                    dialogInterface.dismiss();
                                                                                    reply = response.body().string().trim();

                                                                                    runOnUiThread(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {

                                                                                            if (reply.equals("OK")) {

                                                                                                if (searchNameforEmail(notification_friends_email) != null) {
                                                                                                    toast = Toast.makeText(getApplicationContext(), "New friend added!", Toast.LENGTH_LONG);
                                                                                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                                                                                    toast.show();

                                                                                                    new_friend_alert = new AlertDialog.Builder(getApplicationContext(), R.style.AlertDialogStyle);
                                                                                                    new_friend_alert.setMessage("Set a on-the-move alert for " + searchNameforEmail(notification_friends_email) + " ?");
                                                                                                    new_friend_alert.setPositiveButton("No", new DialogInterface.OnClickListener() {

                                                                                                        @Override
                                                                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                                                                            dialogInterface.dismiss();

                                                                                                        }

                                                                                                    });

                                                                                                    new_friend_alert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {

                                                                                                        @Override
                                                                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                                                                            dialogInterface.dismiss();
                                                                                                            String move_attribute = "YES";

                                                                                                            MainActivity.this.URL = sharedPref_Spatially_Host + ":" + sharedPref_Spatially_Port + "/cgi-bin/boundaries" + "/update-friend-attr.cgi?email=" + email + "&password=" + password + "&friendemail=" + notification_friends_email + "&move=" + move_attribute + "&near=" + near_attribute + "&hide=" + hide_attribute;


                                                                                                            OkHttpClient client = new OkHttpClient();

                                                                                                            Request request = new Request.Builder()
                                                                                                                    .url(MainActivity.this.URL)
                                                                                                                    .build();

                                                                                                            if (logging == true) {
                                                                                                                Log.i(getResources().getString(R.string.TAG), MainActivity.this.URL);
                                                                                                            }

                                                                                                            client.newCall(request).enqueue(new Callback() {
                                                                                                                @Override
                                                                                                                public void onFailure(Call call, IOException e) {

                                                                                                                    if (logging == true) {
                                                                                                                        Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                                                                                                                    }
                                                                                                                }

                                                                                                                @Override
                                                                                                                public void onResponse(Call call, Response response) throws IOException {

                                                                                                                    reply = response.body().string().trim();

                                                                                                                    if (getApplicationContext() != null) {
                                                                                                                        runOnUiThread(new Runnable() {
                                                                                                                            @Override
                                                                                                                            public void run() {

                                                                                                                                if (!reply.equals("OK")) {

                                                                                                                                    toast = Toast.makeText(getApplicationContext(), reply, Toast.LENGTH_SHORT);
                                                                                                                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                                                                                                                    toast.show();
                                                                                                                                }

                                                                                                                            }
                                                                                                                        });
                                                                                                                    }


                                                                                                                }


                                                                                                            });


                                                                                                        }
                                                                                                    });

                                                                                                }

                                                                                            } else {
                                                                                                toast = Toast.makeText(getApplicationContext(), reply, Toast.LENGTH_LONG);
                                                                                                toast.setGravity(Gravity.CENTER, 0, 0);
                                                                                                toast.show();
                                                                                            }
                                                                                        }

                                                                                    });
                                                                                }
                                                                            }
                                                                        });

                                                                    }
                                                                });

                                                                new_friend_alert.show();

                                                            }

                                                        } else if (notification_type.equals("REMOVED_FRIEND")) {

                                                            notification_friends_email = (((reply_array.get(i)).split(","))[2]).trim();

                                                            if (searchNameforEmail(notification_friends_email) != null) {
                                                                toast = Toast.makeText(getApplicationContext(), searchNameforEmail(notification_friends_email) + " has removed you as a friend!", Toast.LENGTH_LONG);
                                                                toast.setGravity(Gravity.CENTER, 0, 0);
                                                                toast.show();

                                                            }

                                                            // If notification is normal
                                                        } else {

                                                            Log.i(getResources().getString(R.string.TAG), notification_details + " " + notification_time + " " + notification_type);

                                                            // Just insert new data on top of list
                                                            notifications.add(0, notification_details);
                                                            notifications_time.add(0, notification_time);

                                                            long unixseconds = Long.parseLong(notification_time);
                                                            Date date = new Date(unixseconds * 1000L);
                                                            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                                                            String formattedDate = sdf.format(date);

                                                            Notify(notification_details + " " + formattedDate, (int) unixseconds);

                                                            badge_count++;

                                                            UpdateBadgeAndWriteToDisk(badge_count);

                                                        }

                                                    }
                                                }

                                            }

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    model.setNotifications((ArrayList) notifications);
                                                    model.setNotifications_time((ArrayList) notifications_time);
                                                }
                                            });

                                        }

                                    }

                                });

                            }

                        }
                    });

                }

            });


        } catch (Exception e) {

            if (this != null) {
                ShowErrorDetails(Log.getStackTraceString(e), this);
            }

            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

        }

    }

    private String searchNameforEmail(String email) {

        if (getApplicationContext() != null) {

            if (name != null && email_info != null && email != null) {
                for (int i = 0; i < email_info.size(); i++) {
                    if (email_info.get(i).equals(email)) {
                        return (name.get(i));
                    }
                }
            }
        }

        return null;

    }

    void ClearAll() {

        try {

            if (logging == true)
                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.clearing_all_data_lists));

            reply_array = new ArrayList<>();
            account_creation_time = new ArrayList<>();
            id_time = new ArrayList<>();
            name = new ArrayList<>();
            fences = new ArrayList<>();
            email_info = new ArrayList<>();
            battery_info = new ArrayList<>();
            accuracy_info = new ArrayList<>();
            latitude = new ArrayList<>();
            longitude = new ArrayList<>();
            id = new ArrayList<>();
            last_movement_time = new ArrayList<>();

            fence_names = new ArrayList<>();
            fence_center_latitude = new ArrayList<>();
            fence_center_longitude = new ArrayList<>();
            fence_ids = new ArrayList<>();
            fence_radius = new ArrayList<>();

            notifications = new ArrayList<>();
            notifications_time = new ArrayList<>();

        } catch (Exception e) {

            if (this != null) {
                ShowErrorDetails(Log.getStackTraceString(e), this);
            }

            /*
            toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

             */

            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

            return;
        }

    }

    public void Notify(String textContent, Integer notificationId) {

        try {

            // Creating sound for notification
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification_icons)
                    .setContentTitle(textContent)
                    .setContentText("Spatially Notification")
                    .setSound(defaultSoundUri)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            Notification notification;

            // For >= Android 8.0
            // Must create channels etc
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                CharSequence name = "spatially";
                String description = "spatially android notifications";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);

                builder.setChannelId(CHANNEL_ID);

                notification = builder.build();

                // Notification is automatically closed upon swipe or click
                notification.flags = Notification.FLAG_AUTO_CANCEL;

                if (getApplicationContext() != null) {
                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                }
                notificationManager.createNotificationChannel(channel);
                notificationManager.notify(notificationId, notification);

            } else {

                notification = builder.build();

                // Notification is automatically closed upon swipe or click
                notification.flags = Notification.FLAG_AUTO_CANCEL;

                notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
                notificationManagerCompat.notify(notificationId, notification);

            }

            Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(notification_pattern, -1);

        } catch (Exception e) {

            if (this != null) {
                ShowErrorDetails(Log.getStackTraceString(e), this);
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

    public void ReadNotificationsFromDisk() {

        try {
            List<String> temp;

            if (sharedPref.contains("notifications") && !sharedPref.getString("notifications", "0").equals(null)) {

                notifications.clear();

                temp = Arrays.asList(sharedPref.getString("notifications", String.valueOf(0)).split(","));

                for (int i = 0; i < temp.size(); i++) {
                    notifications.add(temp.get(i));
                }

                badge_count = notifications.size();

                if (sharedPref.contains("notifications_timestamp") && !sharedPref.getString("notifications_timestamp", "0").equals(null)) {

                    notifications_time.clear();
                    temp = Arrays.asList(sharedPref.getString("notifications_timestamp", String.valueOf(0)).split(","));
                    for (int i = 0; i < temp.size(); i++) {
                        notifications_time.add(temp.get(i));
                    }
                    //System.out.println("time: " + time + " " + time.size());

                }

                model.setNotifications((ArrayList) notifications);
                model.setNotifications_time((ArrayList) notifications_time);

                UpdateBadgeAndWriteToDisk(badge_count);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.remove("notifications_timestamp");
                editor.remove("notifications");
                editor.commit();
                editor.apply();

            } else {
                badge_count = 0;
                UpdateBadgeAndWriteToDisk(badge_count);

            }
        } catch (Exception e) {

            if (this != null) {
                ShowErrorDetails(Log.getStackTraceString(e), this);
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

    public void UpdateBadgeAndWriteToDisk(int notification_count) {

        try {

            unread_notifications = notification_count;

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("notification_count", notification_count);
            editor.commit();
            editor.apply();

            if (notification_count != 0 && notification_count > 0) {

                if(notifications_badge!=null)
                {
                    notifications_badge.setNumber(notification_count);
                    notifications_badge.setVisible(true);

                }

            } else if (notification_count == 0) {

                if(notifications_badge!=null)
                {
                    //notifications_badge.clearNumber();
                    notifications_badge.setVisible(false);

                }

            }

        } catch (Exception e) {

            if (this != null) {
                ShowErrorDetails(Log.getStackTraceString(e), this);
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

    public void WriteNotificationDatatoDisk() {

        try {
            SharedPreferences.Editor editor = sharedPref.edit();

            if (notifications.size() > 0) {

                if (sharedPref.contains("notifications") && !sharedPref.getString("notifications", "0").equals(null)) {
                    editor.remove("notifications");
                    editor.commit();
                }

                StringBuilder notifications_string = new StringBuilder();

                for (int i = 0; i <= (notifications.size() - 1); i++) {
                    notifications_string.append(notifications.get(i)).append(",");
                }

                editor.putString("notifications", notifications_string.toString());
                editor.commit();

                if (sharedPref.contains("notifications_timestamp") && !sharedPref.getString("notifications_timestamp", "0").equals(null)) {
                    editor.remove("notifications_timestamp");
                    editor.commit();

                }

                StringBuilder notifications_timestamp = new StringBuilder();

                for (int i = 0; i <= (notifications_time.size() - 1); i++) {
                    notifications_timestamp.append(notifications_time.get(i)).append(",");
                }

                editor.putString("notifications_timestamp", notifications_timestamp.toString());
                editor.commit();


            }
        } catch (Exception e) {

            if (this != null) {
                ShowErrorDetails(Log.getStackTraceString(e), this);
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

    private void ShowErrorDetails(String exception_message, Context context) {

        if (context != null) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

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
            });

        }

    }

    // A method created to test notifications.
    public void test() {

        int test = 50;
        // testing
        for (int i = 0; i < test; i++) {

            int id = (int) System.currentTimeMillis();

            Notify("test " + String.valueOf(i), id);

        }

        UpdateBadgeAndWriteToDisk(test);
    }

}