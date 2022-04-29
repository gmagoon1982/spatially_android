package com.spatially.spatially_android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
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

public class friendslist_self_info extends Fragment {

    View rootView;
    SharedPreferences sharedPref;
    String email, password;
    String reply;
    String URL;
    String hide, live;
    Switch live_location;
    CompoundButton.OnCheckedChangeListener onCheckedChangeListener;
    Toast toast;
    Boolean logging = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.friendslist_self_info, null);
        return (rootView);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {

            if (getActivity() != null) {

                sharedPref = getActivity().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

                if (sharedPref.contains("logging")) {
                    logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
                }

                email = this.getArguments().getString("email").trim();
                password = this.getArguments().getString("password").trim();


                URL = sharedPref.getString("Spatially_Host", String.valueOf(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", String.valueOf(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/get-my-attr.cgi?email=" + email + "&password=" + password;
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

                        try {
                            reply = (response.body().string().trim().split("\nOK"))[0];
                            //System.out.println(reply);
                            hide = ((((reply.split(","))[0])).split("="))[1];
                            live = ((((reply.split(","))[1])).split("="))[1];

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (live.equals("NO")) {

                                            if (((MainActivity) getActivity()).live_location_self != null) {
                                                ((MainActivity) getActivity()).live_location_self = false;
                                                live_location.setOnCheckedChangeListener(null);
                                                live_location.setChecked(false);
                                                live_location.setOnCheckedChangeListener(onCheckedChangeListener);
                                            }

                                        } else {
                                            if (((MainActivity) getActivity()).live_location_self != null) {
                                                ((MainActivity) getActivity()).live_location_self = true;
                                                live_location.setOnCheckedChangeListener(null);
                                                live_location.setChecked(true);
                                                live_location.setOnCheckedChangeListener(onCheckedChangeListener);
                                            }
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {

                            if (getContext() != null) {
                                ShowErrorDetails(Log.getStackTraceString(e), getContext());
                            }

                            /*
                            toast = Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();

                             */

                            if (logging == true) {
                                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                            }
                        }

                        //System.out.println("hide: " + hide);
                        //System.out.println("live: " + live);


                    }
                });

                TextView connection = rootView.findViewById(R.id.connection);
                connection.setTextSize(getResources().getDimension(R.dimen.info_small_text_font));
                connection.setText("CONNECTION");

                TextView go_offline = rootView.findViewById(R.id.go_offline);
                go_offline.setBackgroundResource(R.color.WHITE);
                go_offline.setTextSize(getResources().getDimension(R.dimen.info_labels_text_font));
                go_offline.setTextColor(getResources().getColor(R.color.BLACK));
                go_offline.setText("Go Offline");
                go_offline.setTypeface(go_offline.getTypeface(), Typeface.BOLD);
                go_offline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        SendHide(true);
                        SendLive(false);

                        ((MainActivity) getActivity()).live_location_self = false;
                        ((MainActivity) getActivity()).offline = true;

                        if (getContext() != null) {
                            Intent i = new Intent(getContext(), go_offline.class);
                            i.putExtra("email", email);
                            i.putExtra("password", password);
                            i.putExtra("live", live);
                            startActivity(i);
                        }

                    }
                });

                live_location = rootView.findViewById(R.id.live_location);
                live_location.setBackgroundResource(R.color.WHITE);
                live_location.setTextSize(getResources().getDimension(R.dimen.info_labels_text_font));
                live_location.setTypeface(live_location.getTypeface(), Typeface.BOLD);
                live_location.setTextColor(getResources().getColor(R.color.BLACK));
                live_location.setText("Live Location");

                onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                        SendLive(b);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("live_location", b);
                        editor.commit();

                    }

                };

                TextView go_offline_info = rootView.findViewById(R.id.go_offline_info);
                go_offline_info.setText(getResources().getString(R.string.go_offline_info));
                go_offline_info.setTextSize(getResources().getDimension(R.dimen.info_small_text_font));
                go_offline_info.setBackgroundColor(getResources().getColor(R.color.self_info_grey_item));

                TextView account = rootView.findViewById(R.id.account);
                account.setTextSize(getResources().getDimension(R.dimen.info_small_text_font));
                account.setText("ACCOUNT");

                TextView reset_password = rootView.findViewById(R.id.reset_password);
                reset_password.setBackgroundResource(R.color.WHITE);
                reset_password.setText("Reset Password");
                reset_password.setTextSize(getResources().getDimension(R.dimen.info_labels_text_font));
                reset_password.setTypeface(reset_password.getTypeface(), Typeface.BOLD);
                reset_password.setTextColor(getResources().getColor(R.color.BLACK));
                reset_password.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent i = new Intent(getActivity(), password_change.class);
                        i.putExtra("email", email);
                        i.putExtra("password", password);
                        startActivity(i);

                    }
                });

                TextView delete_account = rootView.findViewById(R.id.delete_account);
                delete_account.setTextColor(getResources().getColor(R.color.RED));
                delete_account.setBackgroundResource(R.color.WHITE);
                delete_account.setText("Delete Account");
                delete_account.setTextSize(getResources().getDimension(R.dimen.info_labels_text_font));

                delete_account.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (getContext() != null) {
                            AlertDialog.Builder delete = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);
                            delete.setMessage("Are you sure you want to delete your account?");

                            delete.setPositiveButton("No", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    dialogInterface.cancel();
                                    return;


                                }
                            });

                            delete.setNegativeButton("Yes", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    dialogInterface.dismiss();

                                    URL = sharedPref.getString("Spatially_Host", String.valueOf(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", String.valueOf(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/delete-act.cgi?email=" + email + "&password=" + password;
                                    OkHttpClient client = new OkHttpClient();

                                    Request request = new Request.Builder()
                                            .url(URL)
                                            .build();

                                    if (logging == true)
                                        Log.i(getResources().getString(R.string.TAG), URL);

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

                                            return;

                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {

                                            reply = response.body().string().trim();

                                            if (getActivity() != null) {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        if (reply.equals("OK")) {

                                                            if (getContext() != null) {
                                                                Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                                                                v.vibrate(Long.parseLong(getResources().getString(R.string.fence_delete_vibration_time)));
                                                                toast = Toast.makeText(getContext(), getResources().getString(R.string.your_account_has_been_deleted), Toast.LENGTH_LONG);
                                                                toast.setGravity(Gravity.CENTER, 0, 0);
                                                                toast.show();

                                                                if (logging == true) {
                                                                    Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.your_account_has_been_deleted));
                                                                }

                                                                getActivity().finish();
                                                            }
                                                        }

                                                    }
                                                });
                                            }


                                        }
                                    });


                                }

                            });

                            delete.show();


                        }
                    }
                });

                TextView delete_info = rootView.findViewById(R.id.delete_info);
                delete_info.setText("Delete Account removes your data permanently from our servers");
                delete_info.setTextSize(getResources().getDimension(R.dimen.info_small_text_font));
                delete_info.setBackgroundColor(getResources().getColor(R.color.self_info_grey_item));

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

            }

        } catch (Exception e) {

            if (getContext() != null) {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
            }
            /*
            toast = Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

             */

            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

            return;
        }
    }

    public void BackButtonAnimation() {

        try {

            if (getContext() != null) {

                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.out_from_right);

                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        getActivity().getSupportFragmentManager().popBackStack();

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                getView().startAnimation(animation);

            }
        } catch (
                Exception e) {

            if (getContext() != null) {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
            }

            /*
            toast = Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            */

            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

            return;
        }

    }

    public void SendLive(Boolean live_value) {

        try {
            if (live_value) {
                live = "YES";
            } else if (!live_value) {
                live = "NO";
            }

            URL = sharedPref.getString("Spatially_Host", String.valueOf(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", String.valueOf(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/update-my-attr.cgi?email=" + email + "&password=" + password + "&hide=" + hide + "&live=" + live;
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


                }
            });

        } catch (Exception e) {

            if (getContext() != null) {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
            }

            /*
            toast = Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            */

            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

            return;
        }
    }

    public void SendHide(Boolean hide_value) {

        try {
            if (hide_value) {
                hide = "YES";
            } else if (!hide_value) {
                hide = "NO";
            }

            URL = sharedPref.getString("Spatially_Host", String.valueOf(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", String.valueOf(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/update-my-attr.cgi?email=" + email + "&password=" + password + "&hide=" + hide + "&live=" + live;
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


                }
            });

        } catch (Exception e) {

            if (getContext() != null) {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
            }

            /*
            toast = Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            */

            if (logging == true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

            return;
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

}
