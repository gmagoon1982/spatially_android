package com.spatially.spatially_android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class friendslist_addnew_lba extends Fragment {

    View rootView;
    String friends_email;
    String lba_id;
    String email, password;
    SharedPreferences sharedPref;
    String URL;
    String selected_fenceId;
    CheckBox Arrives, Departs;

    // Onetime YES means LBA will be sent only once but assuming NO by default for LBA to be continuous
    String Onetime = "NO";

    String Arrives_value = "YES", Departs_value = "YES";
    TextView select_fence;
    OkHttpClient client;
    String reply;
    Toast toast;
    Boolean logging=false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        rootView = inflater.inflate(R.layout.friendslist_addnew_lba, null);


        return (rootView);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getActivity()!=null)
        {
            sharedPref = getActivity().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

            if(sharedPref.contains("logging"))
            {
                logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
            }

        }

            friends_email = getArguments().getString("friends_email");
            email = getArguments().getString("email");
            password = getArguments().getString("password");

            if(getArguments().containsKey("selected_lba_arrives"))
            {
                if (getArguments().getString("selected_lba_arrives").equals("1")) {
                    Arrives_value = "YES";
                } else {
                    Arrives_value = "NO";

                }
            }

            if(getArguments().containsKey("selected_lba_departs"))
            {
                if (getArguments().getString("selected_lba_departs").equals("1")) {
                    Departs_value = "YES";

                } else {
                    Departs_value = "NO";
                }
            }

            selected_fenceId = getArguments().getString("selected_fence_id");

            lba_id = getArguments().getString("lba_selected_id");

            //System.out.println("friendslist_addnew_lba.java:" + email  + " " +  password + " " + friends_email + " " + Arrives_value + " " + Departs_value + " " + lba_id);

            TextView title = rootView.findViewById(R.id.title);
            TextView name = rootView.findViewById(R.id.Name);
            TextView fence = rootView.findViewById(R.id.fence);
            select_fence = rootView.findViewById(R.id.select_fence);
            Arrives = rootView.findViewById(R.id.Arrives);
            Departs = rootView.findViewById(R.id.Departs);
            TextView notify_when_a_friend = rootView.findViewById(R.id.notify_when_a_friend);

            title.setText("Location Alert");
            title.setTextSize(getResources().getDimension(R.dimen.title_text_font));

            fence.setTextSize(getResources().getDimension(R.dimen.info_small_text_font));
            fence.setText("FENCE");

            select_fence.setTextSize(getResources().getDimension(R.dimen.info_labels_text_font));
            if (!(getArguments().containsKey("fence_selected"))) {
                select_fence.setText("Select a Fence");
            } else select_fence.setText(getArguments().getString("fence_selected"));

            select_fence.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(getContext()!=null)
                    {
                        Bundle extras = new Bundle();
                        extras.putString("email", email);
                        extras.putString("password", password);

                        //System.out.println("email: " + email + " password: " + password);

                        Intent i = new Intent(getContext(), add_new_lba_fences_list.class);
                        i.putExtras(extras);
                        startActivityForResult(i, 1);
                    }

                }
            });

            Arrives.setTextSize(getResources().getDimension(R.dimen.info_labels_text_font));
            Arrives.setText("Arrives");
            Arrives.setChecked(true);

            if (getArguments().containsKey("selected_lba_arrives")) {

                if (getArguments().getString("selected_lba_arrives").equals("1")) {
                    Arrives.setChecked(true);
                } else if (getArguments().getString("selected_lba_arrives").equals("0")) {
                    Arrives.setChecked(false);
                }

            } else {
                Arrives_value = "YES";
            }

            //Departs_value = getArguments().getString("selected_lba_departs");

            Departs.setTextSize(getResources().getDimension(R.dimen.info_labels_text_font));
            Departs.setText("Departs");
            Departs.setChecked(true);

            if (getArguments().containsKey("selected_lba_departs")) {

                if (getArguments().getString("selected_lba_departs").equals("1")) {

                    Departs.setChecked(true);
                } else if (getArguments().getString("selected_lba_departs").equals("0")) {
                    Departs.setChecked(false);
                }

            } else {
                Departs_value = "YES";
            }

            notify_when_a_friend.setText("NOTIFY WHEN A FRIEND");
            notify_when_a_friend.setTextSize(getResources().getDimension(R.dimen.info_small_text_font));

            /* Don't need to display user's name
            String user_name = null;
            if (this.getArguments() != null) {
                user_name = this.getArguments().getString("title");
            }
            name.setText(user_name);
            name.setTextColor(getResources().getColor(R.color.BLUE));

            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BackButtonAnimation();
                }
            });

             */

            ImageView Back = rootView.findViewById(R.id.Back);
            Back.setColorFilter(getResources().getColor(R.color.BLUE));
            Back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    BackButtonAnimation();
                }
            });

            Arrives.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    if (b == true) {
                        Arrives_value = "YES";
                    } else {
                        Arrives_value = "NO";
                    }

                    Arrives.setEnabled(false);
                    if (Arrives_value == "NO" && Departs_value == "NO") {
                        RemoveLBA();
                        Arrives.setEnabled(true);
                    } else {
                        RemoveAndAddLBA();
                        Arrives.setEnabled(true);
                    }


                }
            });

            Departs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    if (b == true) {
                        Departs_value = "YES";
                    } else {
                        Departs_value = "NO";
                    }

                    Arrives.setEnabled(false);
                    if (Arrives_value == "NO" && Departs_value == "NO") {
                        RemoveLBA();
                        Arrives.setEnabled(true);
                    } else {
                        RemoveAndAddLBA();
                        Arrives.setEnabled(true);
                    }

                }
            });

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
            toast = Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            */

            if(logging==true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {

                if (data.hasExtra("lba_fence_id")) {

                    selected_fenceId = data.getStringExtra("lba_fence_id");


                    //System.out.println("lba_fence_id: " + data.getStringExtra("lba_fence_id"));
                    //System.out.println("lba_fence_name: " + data.getStringExtra("lba_fence_name"));
                    //System.out.println("lba_id " + lba_id);

                    select_fence.setText(data.getStringExtra("lba_fence_name"));

                    AddLBA();
                    toast = Toast.makeText(getContext(), "LBA Added!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                }
                //System.out.println("selected_fenceId: " + selected_fenceId);

            }
        }
    }

    private void RemoveAndAddLBA() {

        try {

            if(getActivity()!=null) {

                client = new OkHttpClient();
                Request request;

                // If either values are YES
                if ((lba_id != null) && (email != null) && (password != null)) {

                    URL = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/remove-lba.cgi?email=" + email + "&password=" + password + "&lbaId=" + lba_id;

                    request = new Request.Builder()
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

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (reply.equals("OK")) {
                                            AddLBA();
                                        } else if (reply.equals("No such LBA")) {

                                            toast = Toast.makeText(getContext(), getResources().getString(R.string.no_such_lba), Toast.LENGTH_SHORT);

                                            if(logging==true) {
                                                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.no_such_lba));
                                            }

                                            return;
                                        } else {

                                            toast = Toast.makeText(getContext(), getResources().getString(R.string.error_occurred), Toast.LENGTH_SHORT);
                                            toast = Toast.makeText(getContext(), reply, Toast.LENGTH_SHORT);

                                            if(logging==true)
                                            {
                                                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.error_occurred));
                                                Log.i(getResources().getString(R.string.TAG), reply);

                                            }

                                            return;

                                        }


                                    }
                                });
                            }
                        }

                    });


                }
            }

        } catch (Exception e) {

            if (getContext()!=null) {
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

    private void RemoveLBA() {

        try {

            if(getActivity()!=null) {

                client = new OkHttpClient();
                Request request;

                // If either values are YES
                if ((lba_id != null) && (email != null) && (password != null)) {

                    URL = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/remove-lba.cgi?email=" + email + "&password=" + password + "&lbaId=" + lba_id;

                    request = new Request.Builder()
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

                            String reply = response.body().string().trim();

                            if (reply != null) {

                                if (!reply.equals("OK")) {

                                    toast = Toast.makeText(getContext(), reply, Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();

                                    if(logging==true) {
                                        Log.i(getResources().getString(R.string.TAG), reply);
                                    }

                                    return;

                                }

                            }


                        }

                    });


                }
            }


        } catch (Exception e) {

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

    private void AddLBA() {

        try {

            if (getActivity() != null) {

                client = new OkHttpClient();

                if (!Arrives_value.equals(null) && !Departs_value.equals(null)) {
                    if (((Arrives_value.equals("YES")) || (Departs_value.equals("YES")) || (Arrives_value.equals("YES") && Departs_value.equals("YES"))) && (selected_fenceId != null) && (email != null) && (password != null) && (friends_email != null) && (Arrives_value != null) && (Departs_value != null) && (Onetime != null)) {

                        URL = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/add-lba.cgi?email=" + email + "&password=" + password + "&friendemail=" + friends_email + "&fenceId=" + selected_fenceId + "&arrives=" + Arrives_value + "&departs=" + Departs_value + "&onetime=" + Onetime;

                        Request request;
                        request = new Request.Builder()
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

                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            if (!reply.contains("OK")) {
                                                //Toast.makeText(getContext(), reply, Toast.LENGTH_LONG).show();

                                                if (reply.contains("Error adding LBA")) {
                                                    toast = Toast.makeText(getContext(), getResources().getString(R.string.lba_already_exists), Toast.LENGTH_LONG);
                                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                                    toast.show();

                                                    if(logging==true) {
                                                        Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.lba_already_exists));
                                                    }

                                                    return;
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

                                        }


                                    });
                                }
                            }

                        });
                    }

                }
                }

            } catch(Exception e){

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


