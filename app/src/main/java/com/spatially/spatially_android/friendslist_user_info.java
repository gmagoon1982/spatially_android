package com.spatially.spatially_android;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class friendslist_user_info extends Fragment {

    View rootView;
    SharedPreferences sharedPref;
    String email, password;
    String friends_email;
    String reply;

    List<String> reply_array = new ArrayList<>();
    List<Double> latitude = new ArrayList<>();
    List<Double> longitude = new ArrayList<>();
    List<Double> radius = new ArrayList<>();

    List<String> lbas = new ArrayList<>();

    // lba_id, friends_email, lba_onetime, lba_fenceid, lba_arrives, lba_departs
    List<String> lba_id = new ArrayList<>();
    List<String> lba_arrives = new ArrayList<>();
    List<String> lba_departs = new ArrayList<>();
    List<String> lba_fence_id = new ArrayList<>();
    List<String> lba_onetime = new ArrayList<>();
    List<String> lba_friends_emails = new ArrayList<>();

    String move_attribute, near_attribute, hide_attribute;
    String URL;

    List<String> fence_names = new ArrayList<>();
    List<String> fence_ids = new ArrayList<>();

    List<String> Status = new ArrayList<>();

    LinearLayout LBA_list;
    Integer LBA_size;
    Toast toast;
    Boolean logging=false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.friendslist_user_info, null);

        return (rootView);
    }

    CompoundButton.OnCheckedChangeListener is_on_the_move_notification_listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            try {

                if (getActivity() != null) {

                    sharedPref = getActivity().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

                    // Hard coded host and port for the server are in the strings.xml resources file
                    // Have to use getString to get string associated with specific resource id

                    if (b == true) {
                        move_attribute = "YES";
                    } else {
                        move_attribute = "NO";
                    }

                    URL = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/update-friend-attr.cgi?email=" + email + "&password=" + password + "&friendemail=" + friends_email + "&move=" + move_attribute + "&near=" + near_attribute + "&hide=" + hide_attribute;

                    OkHttpClient client = new OkHttpClient();

                    Request request = new Request.Builder()
                            .url(URL)
                            .build();

                    if(logging==true) {
                        Log.i(getResources().getString(R.string.TAG), URL);
                    }

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

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (!reply.equals("OK")) {

                                            toast = Toast.makeText(getContext(), reply, Toast.LENGTH_LONG);
                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                            toast.show();

                                            if(logging==true) {
                                                Log.i(getResources().getString(R.string.TAG), reply);
                                            }
                                        }

                                    }
                                });
                            }


                        }


                    });


                }
            }
            catch(Exception e)
            {
                if(getContext()!=null) {

                    ShowErrorDetails(Log.getStackTraceString(e), getContext());

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

            }

    };

    CompoundButton.OnCheckedChangeListener sharemylocationcheckbox_onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            try {

                if (getActivity() != null) {

                    if (b == true) {
                        hide_attribute = "NO";
                    } else {
                        hide_attribute = "YES";
                    }

                    URL = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/update-friend-attr.cgi?email=" + email + "&password=" + password + "&friendemail=" + friends_email + "&move=" + move_attribute + "&near=" + near_attribute + "&hide=" + hide_attribute;


                    OkHttpClient client = new OkHttpClient();

                    Request request = new Request.Builder()
                            .url(URL)
                            .build();

                    if(logging==true) {
                        Log.i(getResources().getString(R.string.TAG), URL);
                    }

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

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (!reply.equals("OK")) {
                                            toast = Toast.makeText(getContext(), reply, Toast.LENGTH_LONG);toast.setGravity(Gravity.CENTER, 0, 0);
                                            toast.show();

                                            if(logging==true) {
                                                Log.i(getResources().getString(R.string.TAG), reply);
                                            }

                                            return;
                                        }

                                    }
                                });
                            }


                        }

                        // Asynchronous Get
                        // Runs in a background thread (off the main thread)


                    });


                }
            }
            catch(Exception e)
            {
                if(getContext()!=null) {

                    ShowErrorDetails(Log.getStackTraceString(e), getContext());

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
        }

    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            if (getActivity() != null) {
                sharedPref = getActivity().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

                if(sharedPref.contains("logging"))
                {
                    logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
                }

                String Spatially_Host = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST));
                String Spatially_Port = sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT));

                Bundle extras = getArguments();

                email = extras.getString("email");
                password = extras.getString("password");
                friends_email = extras.getString("friends_email");

                //System.out.println("friendslist_user_info.java: " + email +  " " + password + " " + friends_email);

                String title = extras.getString("title");

                String battery_level = extras.getString("battery_level");
                String last_seen_time = extras.getString("last_seen");

                TextView lastseen = rootView.findViewById(R.id.lastseen);

                try {

                    if (battery_level != null && last_seen_time != null) {

                        Double battery_level_double = Double.parseDouble(battery_level);
                        lastseen.setText("Last seen " + last_seen_time + " with " + (String.valueOf(new DecimalFormat(getResources().getString(R.string.No_of_Decimals_for_Last_Seen)).format(battery_level_double * 100) + " % battery")));

                    }

                } catch (Exception e) {

                    if(getContext()!=null)
                    {
                        ShowErrorDetails(Log.getStackTraceString(e), getContext());
                    }

                    if(logging==true) {
                        Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                    }
                }

                LBA_list = (LinearLayout) rootView.findViewById(R.id.lbas_list);
                LBA_list.setOrientation(LinearLayout.VERTICAL);

                lastseen.setTextSize(getResources().getDimension(R.dimen.info_small_text_font));
                TextView info_title = rootView.findViewById(R.id.title);
                info_title.setText(title);

                TextView sharingwith = rootView.findViewById(R.id.sharingwith);
                sharingwith.setText("SHARING WITH " + title);
                sharingwith.setTextSize(getResources().getDimension(R.dimen.info_small_text_font));

                CheckBox sharemylocationcheckbox = rootView.findViewById(R.id.sharemylocationcheckbox);
                sharemylocationcheckbox.setText("Share my location");
                sharemylocationcheckbox.setBackgroundColor(getResources().getColor(R.color.WHITE));
                sharemylocationcheckbox.setTextSize(getResources().getDimension(R.dimen.info_labels_text_font));
                sharemylocationcheckbox.setTypeface(sharemylocationcheckbox.getTypeface(), Typeface.BOLD);

                TextView lbas = rootView.findViewById(R.id.lbas);
                lbas.setText("LOCATION-BASED ALERTS");
                lbas.setTextSize(getResources().getDimension(R.dimen.info_small_text_font));

                getLBAList();

                TextView add_new_lba = rootView.findViewById(R.id.add_new_lba);
                add_new_lba.setText("Add new location alert");
                add_new_lba.setBackgroundResource(R.color.WHITE);
                add_new_lba.setTypeface(null, Typeface.BOLD);
                add_new_lba.setTextSize(getResources().getDimension(R.dimen.info_labels_text_font));
                add_new_lba.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //view.setBackgroundColor(Color.parseColor("#C6D9CD"));

                        Bundle extras = new Bundle();
                        extras.putString("title", title);
                        extras.putString("email", email);
                        extras.putString("password", password);
                        extras.putString("friends_email", friends_email);

                        //System.out.println("friendslist_user_info.java: " + email +  " " + password + " " + friends_email);

                        Fragment add_new_lba = new friendslist_addnew_lba();
                        add_new_lba.setArguments(extras);
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.in_from_right, 0);
                        transaction.replace(R.id.fragment_container, add_new_lba).addToBackStack(null).commit();

                    }
                });

                TextView notify_me_when_text = rootView.findViewById(R.id.notifymewhen);
                notify_me_when_text.setText("NOTIFY ME WHEN");
                notify_me_when_text.setTextSize(getResources().getDimension(R.dimen.info_small_text_font));

                CheckBox is_on_the_move_notification = rootView.findViewById(R.id.notifycheckbox);
                is_on_the_move_notification.setText(title + " is on the move");
                is_on_the_move_notification.setBackgroundColor(getResources().getColor(R.color.WHITE));
                is_on_the_move_notification.setTextSize(getResources().getDimension(R.dimen.info_labels_text_font));
                is_on_the_move_notification.setTypeface(is_on_the_move_notification.getTypeface(), Typeface.BOLD);

                TextView request_accurate_location = rootView.findViewById(R.id.request_accurate_location);
                request_accurate_location.setText("Request accurate location");
                request_accurate_location.setTextColor(getResources().getColor(R.color.RED));
                request_accurate_location.setBackgroundColor(getResources().getColor(R.color.WHITE));
                request_accurate_location.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        URL = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/request-accloc.cgi?email=" + email + "&password=" + password + "&friendemail=" + friends_email;

                        OkHttpClient client = new OkHttpClient();

                        Request request = new Request.Builder()
                                .url(URL)
                                .build();

                        if(logging==true) {
                            Log.i(getResources().getString(R.string.TAG), URL);
                        }

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

                                if (response.isSuccessful()) {

                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                if (reply.equals("OK")) {
                                                    toast = Toast.makeText(getContext(), getResources().getString(R.string.accurate_location_requested), Toast.LENGTH_LONG);
                                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                                    toast.show();

                                                    if(logging==true) {
                                                        Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.accurate_location_requested));
                                                    }

                                                } else {
                                                    toast = Toast.makeText(getContext(), reply, Toast.LENGTH_LONG);
                                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                                    toast.show();

                                                    if(logging==true) {
                                                        Log.i(getResources().getString(R.string.TAG), reply);
                                                    }

                                                    return;
                                                }

                                            }
                                        });
                                    }
                                }

                            }

                            // Asynchronous Get
                            // Runs in a background thread (off the main thread)


                        });
                    }
                });

                TextView request_accurate_location_info = rootView.findViewById(R.id.request_accurate_location_info);
                request_accurate_location_info.setText("This sends a request to your friend. Use sparingly as it may drain their battery.");
                request_accurate_location_info.setTextSize(getResources().getDimension(R.dimen.info_small_text_font));

                TextView Friends = rootView.findViewById(R.id.Friends);
                Friends.setTextColor(getResources().getColor(R.color.BLUE));
                Friends.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        BackButtonAnimation();
                    }
                });

                ImageView Back = rootView.findViewById(R.id.Back);

                Back.setColorFilter(getResources().getColor(R.color.BLUE));
                Back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        BackButtonAnimation();

                    }
                });

                // Gets users' attributes from server
                URL = Spatially_Host + ":" + Spatially_Port + "/cgi-bin/boundaries" + "/get-friend-attr.cgi?email=" + email + "&password=" + password + "&friendemail=" + friends_email;


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

                        if (response != null) {

                            reply = response.body().string().trim();

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        String reply_1 = (reply.split("\n")[0]);

                                        //System.out.println("reply_1: " + reply_1);

                                        if(reply_1.split(",").length==3) {
                                        /*
                                        System.out.println("MOVE:" + ((reply_1.split(",")[0]).split("=")[1]));
                                        System.out.println("NEAR:" + ((reply_1.split(",")[1]).split("=")[1]));
                                        System.out.println("HIDE:" + ((reply_1.split(",")[2]).split("=")[1]));
                                        */

                                            // MOVE=XXX, NEAR=XXX, HIDE=XXX
                                            if ((reply_1.split(",")[0]).split("=")[1] != null)
                                                move_attribute = ((reply_1.split(",")[0]).split("=")[1]).trim();

                                            if ((reply_1.split(",")[1]).split("=")[1] != null)
                                                near_attribute = ((reply_1.split(",")[1]).split("=")[1]).trim();

                                            if ((reply_1.split(",")[2]).split("=")[1] != null)
                                                hide_attribute = ((reply_1.split(",")[2]).split("=")[1]).trim();

                                            // Checking if MOVE attribute is YES or NO
                                            if (move_attribute.equals("YES")) {

                                                is_on_the_move_notification.setOnCheckedChangeListener(null);
                                                is_on_the_move_notification.setChecked(true);
                                                is_on_the_move_notification.setOnCheckedChangeListener(is_on_the_move_notification_listener);

                                            } else if (move_attribute.equals("NO")) {

                                                is_on_the_move_notification.setOnCheckedChangeListener(null);
                                                is_on_the_move_notification.setChecked(false);
                                                is_on_the_move_notification.setOnCheckedChangeListener(is_on_the_move_notification_listener);
                                            }

                                            if (hide_attribute.equals("YES")) {

                                                sharemylocationcheckbox.setOnCheckedChangeListener(null);
                                                sharemylocationcheckbox.setChecked(false);
                                                sharemylocationcheckbox.setOnCheckedChangeListener(sharemylocationcheckbox_onCheckedChangeListener);

                                            } else if (hide_attribute.equals("NO")) {

                                                sharemylocationcheckbox.setOnCheckedChangeListener(null);
                                                sharemylocationcheckbox.setChecked(true);
                                                sharemylocationcheckbox.setOnCheckedChangeListener(sharemylocationcheckbox_onCheckedChangeListener);

                                            }

                                        }
                                    }
                                });
                            }


                        }

                    }

                });

            }
            }
        catch(Exception e){

            if(getContext()!=null)
            {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
            }
            /*
                toast = Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

             */

            if(logging==true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

            }
        }

            public void BackButtonAnimation() {

                try {

                    Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.out_from_right);

                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                            if(getActivity()!=null)
                            {
                                getActivity().getSupportFragmentManager().popBackStack();
                            }

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    getView().startAnimation(animation);

                } catch (Exception e) {

                    if(getContext()!=null)
                    {
                        ShowErrorDetails(Log.getStackTraceString(e), getContext());
                    }

                    /*
                    toast = Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                     */

                    if(logging==true) {
                        Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                    }

                    return;
                }

            }

            //lbas = dbquery("SELECT lbas.lbaId,friendships.targetId,lbas.oneTime,lbas.selected_fenceId,lbas.arrives,lbas.departs FROM lbas,friendships WHERE lbas.friendshipId = friendships.friendshipId AND friendships.followerId ='%s'" % get_userId(follower))
            // lba_id, friends_email, lba_onetime, lba_fenceid, lba_arrives, lba_departs

            public void getLBAList() {

                try {
                    ClearAll();

                    if (getActivity() != null) {
                        sharedPref = getActivity().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);
                        // Hard coded host and port for the server are in the strings.xml resources file
                        // Have to use getString to get string associated with specific resource id
                        URL = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/get-lba-list.cgi?email=" + email + "&password=" + password;

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

                                // onResponse already assures the response is successful, apparently.
                                //if(response.isSuccessful()) {
                                String reply = response.body().string().trim();

                                if (reply != null) {

                                    reply_array = new ArrayList<>(Arrays.asList(reply.split("\n")));

                                    //for(int i = 0; i <= (reply_array.size()-2); i++){

                                    //System.out.println(reply_array[i]);
                                    //id.add((reply_array.get(i).split(",")[0]));
                                    //fence_name.add(reply_array.get(i).split(",")[4]);
                                    //latitude.add(Double.parseDouble(reply_array.get(i).split(",")[1]));
                                    //longitude.add(Double.parseDouble(reply_array.get(i).split(",")[2]));
                                    //radius.add(Double.parseDouble(reply_array.get(i).split(",")[3]));
                                    //}

                                    if (reply_array.size() >= 1) {

                                        if (getActivity() != null) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                public void run() {
                                                    // UI code goes here


                                                    // Have to make sure not to get the last element which is OK

                                                    for (int i = 0; i <= (reply_array.size() - 2); i++) {

                                                        if (((reply_array.get(i)).split(",")).length == 6) {

                                                            lba_id.add(i, (reply_array.get(i).split(","))[0]);
                                                            lba_friends_emails.add(i, (((reply_array.get(i)).split(","))[1]));
                                                            lba_onetime.add(i, (reply_array.get(i).split(","))[2]);
                                                            lba_fence_id.add(i, (reply_array.get(i).split(","))[3]);
                                                            lba_arrives.add(i, (reply_array.get(i).split(","))[4]);
                                                            lba_departs.add(i, (reply_array.get(i).split(","))[5]);

                                                            //System.out.println("LBA info: " + lba_id.size() + " " + lba_id.get(i) + " " + lba_friends_emails.get(i) + " " + lba_onetime.get(i) + " " + lba_fence_id.get(i) + " " + lba_arrives.get(i) + " " + lba_departs.get(i));

                                                        }

                                                    }

                                                    writeLBAs();

                                                    // Using lba_id as a reference as it corresponds to number of lbas
                                                    Status = new ArrayList<>(lba_id.size());

                                                    for (int i = 0; i < lba_id.size(); i++) {

                                                        // Have to check lba_arrives and lba_departs
                                                        if (lba_arrives.get(i).equals("0") && lba_departs.get(i).equals("0")) {
                                                            Status.add(i, "");
                                                        } else if (lba_arrives.get(i).equals("0") && lba_departs.get(i).equals("1")) {
                                                            Status.add(i, "On leaving");
                                                        } else if (lba_arrives.get(i).equals("1") && lba_departs.get(i).equals("0")) {
                                                            Status.add(i, "On arriving");
                                                        } else if (lba_arrives.get(i).equals("1") && lba_departs.get(i).equals("1")) {
                                                            Status.add(i, "On arriving and leaving");
                                                        }

                                                    }

                                                    URL = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/get-fence-list.cgi?email=" + email + "&password=" + password;

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

                                                            // onResponse already assures the response is successful, apparently.
                                                            //if(response.isSuccessful()) {
                                                            final String reply = response.body().string().trim();

                                                            if (getActivity() != null) {

                                                                getActivity().runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {

                                                                        if (reply != null) {

                                                                            reply_array = new ArrayList<>(Arrays.asList(reply.split("\n")));

                                                                            if (reply_array.size() >= 1) {
                                                                                // Have to exclude the OK
                                                                                for (int i = 0; i <= (reply_array.size() - 2); i++) {

                                                                                    //System.out.println(reply_array.get(i));
                                                                                    fence_ids.add((reply_array.get(i).split(",")[0]));
                                                                                    fence_names.add(reply_array.get(i).split(",")[4]);
                                                                                }
                                                                            }

                                                                            LBA_size = lba_id.size();
                                                                            LBA_list.removeAllViews();

                                                                            for (int i = 0; i < LBA_size; i++) {

                                                                                if (lba_friends_emails.get(i).equals(friends_email)) {

                                                                                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                                                                    View rowTextView = inflater.inflate(R.layout.lbas_row, null, false);
                                                                                    TextView lba_name = (TextView) rowTextView.findViewById(R.id.lba_name);
                                                                                    TextView status = (TextView) rowTextView.findViewById(R.id.onstatus);


                                                                                    lba_name.setTextSize(getResources().getDimension(R.dimen.lba_text_size));
                                                                                    status.setTextSize(getResources().getDimension(R.dimen.lba_status_size));
                                                                                    lba_name.setTextColor(Color.BLACK);
                                                                                    status.setTextColor(Color.BLACK);

                                                                                    LBA_list.addView(rowTextView);

                                                                                    for (int j = 0; j < fence_ids.size(); j++) {

                                                                                        if (fence_ids.get(j).equals(lba_fence_id.get(i))) {

                                                                                            lba_name.setText(fence_names.get(j));
                                                                                            status.setText(Status.get(i));

                                                                                            int tempj = j;
                                                                                            int tempi = i;

                                                                                            rowTextView.setOnClickListener(new View.OnClickListener() {
                                                                                                @Override
                                                                                                public void onClick(View view) {

                                                                                                    Bundle extras = new Bundle();
                                                                                                    extras.putString("email", email);
                                                                                                    extras.putString("password", password);
                                                                                                    extras.putString("friends_email", friends_email);
                                                                                                    extras.putString("lba_selected_id", lba_id.get(tempi));
                                                                                                    extras.putString("fence_selected", fence_names.get(tempj));
                                                                                                    extras.putString("selected_fence_id", fence_ids.get(tempj));
                                                                                                    extras.putString("selected_lba_arrives", lba_arrives.get(tempi));
                                                                                                    extras.putString("selected_lba_departs", lba_departs.get(tempi));

                                                                                                    //System.out.println("friendslist_user_info.java: " + email +  " " + password + " " + friends_email);

                                                                                                    Fragment add_new_lba = new friendslist_addnew_lba();
                                                                                                    add_new_lba.setArguments(extras);
                                                                                                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                                                                                                    transaction.replace(R.id.fragment_container, add_new_lba).addToBackStack(null).commit();


                                                                                                }
                                                                                            });


                                                                                        }
                                                                                    }

                                                                                }

                                                                            }


                                                                        }


                                                                    }


                                                                });

                                                            }

                                                        }


                                                    });


                                                }


                                            });

                                        }

                                    }
                                }
                            }

                        });
                        }
                    }
            catch(Exception e)
                    {

                        if(getContext()!=null) {
                            ShowErrorDetails(Log.getStackTraceString(e), getContext());
                        }

                        /*
                        toast = Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                         */

                        if(logging==true) {
                            Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                        }
                    }

                }


            public void ClearAll()
            {

                if(logging==true)
                {
                    Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.clearing_all_data_lists));
                }

                reply_array.clear();
                lbas.clear();
                latitude.clear();
                longitude.clear();
                lba_id.clear();
                radius.clear();
                Status.clear();
                LBA_list.removeAllViews();


            }

            public void writeLBAs()
            {

                if(getActivity()!=null) {

                    sharedPref = getActivity().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();

                    StringBuilder lba_info = new StringBuilder();

                    lba_info.append("");

                    editor.putString("LBA_info", lba_info.toString());
                    editor.commit();
                }


            }

            public void readLBAs()
            {

                if(getActivity()!=null) {

                    sharedPref = getActivity().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

                    lbas = (List) (Arrays.asList(sharedPref.getString("LBA_info", String.valueOf(0)).split(",")));

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

}

