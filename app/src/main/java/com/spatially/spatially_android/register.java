package com.spatially.spatially_android;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class register extends AppCompatActivity {

    Dialog dialog;
    String reply = "";
    Button btnSubmit, btnLogin;
    ImageButton btnSpatiallySettings;
    EditText editName, editPassword, editEmail;
    Toast toast;
    Boolean logging = false;
    SharedPreferences sharedPref;

    boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        try {

            // Gets the API level for the phone
            Integer API;
            API = android.os.Build.VERSION.SDK_INT;
            //System.out.println("The API level for this phone is:" + API);

            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            if(Build.VERSION.SDK_INT<Build.VERSION_CODES.P) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                isConnected = (activeNetwork != null) && activeNetwork.isConnected();
            }

            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setCustomView(R.layout.activity_signup_titlebar);

            btnSubmit = (Button) findViewById(R.id.btnSubmit);
            editName = (EditText) findViewById(R.id.editName);
            editPassword = (EditText) findViewById(R.id.editPassword);
            editEmail = (EditText) findViewById(R.id.editEmail);
            btnLogin = (Button) findViewById(R.id.btnLogin);
            btnSpatiallySettings = (ImageButton) findViewById(R.id.imageSettings);

            // This sets the focus onto the widget
            editEmail.requestFocus();

            // Using sharedpreferences to store/read global data
            sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

            if (sharedPref.contains("logging")) {
                logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
            }

            if (sharedPref.contains("email") && sharedPref.contains("password")) {
                Intent login = new Intent(register.this, login.class);
                Bundle extras = new Bundle();
                extras.putString("email", sharedPref.getString("email", ""));
                extras.putString("password", sharedPref.getString("password", ""));
                login.putExtras(extras);
                startActivity(login);
                overridePendingTransition(R.anim.in_from_right, 0);
            }

            // Removing any stored location of user via GPS
            SharedPreferences.Editor editor = sharedPref.edit();

            if (sharedPref.contains("user_time")) {
                editor.remove("user_time");
            }
            if (sharedPref.contains("user_latitude")) {
                editor.remove("user_latitude");
            }
            if (sharedPref.contains("user_longitude")) {
                editor.remove("user_longitude");
            }
            editor.commit();

            btnSpatiallySettings.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    try {
                        startActivity(new Intent(register.this, settings.class));

                    } catch (Exception e) {

                        if(getApplicationContext()!=null)
                        {
                            ShowErrorDetails(Log.getStackTraceString(e), getApplicationContext());
                        }
                            /*
                            toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            */

                        if (logging == true) {
                            Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                        }

                        return;
                    }

                }
            });

            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    try {

                        if (isConnected) {

                            if (editEmail.getText() != null && editPassword.getText() != null && editName.getText() != null && !editEmail.getText().toString().equals("") && !editName.getText().toString().equals("") && !editPassword.getText().toString().equals("")) {

                                dialog = new Dialog(register.this);
                                dialog.setCanceledOnTouchOutside(false);
                                dialog.setContentView(R.layout.custom_register_dialog);
                                dialog.show();

                                // Hard coded host and port for the server are in the strings.xml resources file
                                String URL = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/register.cgi?email=" + editEmail.getText().toString() + "&password=" + editPassword.getText().toString() + "&name=" + editName.getText().toString();

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

                                        if (response.isSuccessful()) {

                                            if (reply != null) {

                                                reply = response.body().string().trim();

                                                runOnUiThread(new Runnable() {

                                                    @Override
                                                    public void run() {

                                                        // Checks what the response from server is
                                                        // Response 4
                                                        if (reply.equals("OK")) {

                                                            dialog.dismiss();

                                                            toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.check_email_to_activate), Toast.LENGTH_LONG);
                                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                                            toast.show();

                                                            if (logging == true) {
                                                                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.check_email_to_activate));
                                                            }

                                                            return;

                                                        }
                                                        // Response 21
                                                        else if (reply.equals("User already exists")) {

                                                            dialog.dismiss();

                                                            toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.user_already_exists), Toast.LENGTH_LONG);
                                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                                            toast.show();

                                                            if (logging == true) {
                                                                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.user_already_exists));
                                                            }


                                                            return;

                                                        } else if (reply.length() > 1) {

                                                            dialog.dismiss();

                                                            toast = Toast.makeText(getApplicationContext(), reply, Toast.LENGTH_LONG);
                                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                                            toast.show();

                                                            if (logging == true) {
                                                                Log.i(getResources().getString(R.string.TAG), reply);
                                                            }


                                                            return;
                                                        }

                                                    }


                                                });

                                            }

                                        }
                                    }

                                });
                            } else {
                                toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.please_check_input_try_again), Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

                                if (logging == true) {
                                    Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.please_check_input_try_again));
                                }

                            }

                        } else {
                            toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.spatially_requires_internet), Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();

                            if (logging == true) {
                                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.spatially_requires_internet));
                            }

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

                        if (logging == true) {
                            Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                        }


                    }

                }


            });

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(register.this, login.class));
                    overridePendingTransition(R.anim.in_from_right, 0);
                }
            });


        } catch (Exception e) {

            if(getApplicationContext()!=null) {
                ShowErrorDetails(Log.getStackTraceString(e), getApplicationContext());
            }
                /*
                toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                */

            if (logging == true) {
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


