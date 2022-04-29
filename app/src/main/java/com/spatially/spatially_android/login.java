package com.spatially.spatially_android;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class login extends AppCompatActivity {

    Dialog dialog;
    String reply="";
    String URL;
    String sharedPref_Spatially_Host, sharedPref_Spatially_Port;
    Integer response_code;
    Toast toast;
    Boolean logging=false;
    SharedPreferences sharedPref;
    Boolean Errors=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setContentView(R.layout.activity_login);
        Button btnSubmit = (Button) findViewById(R.id.btnSubmit);
        TextView editPassword = (TextView) findViewById(R.id.editPassword);
        TextView editEmail = (TextView) findViewById(R.id.editEmail);
        Button btnCancel = (Button)findViewById(R.id.btnCancel);

        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = (activeNetwork != null) && activeNetwork.isConnected();

        //This sets the focus onto the widget
        editEmail.requestFocus();

        sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

        if(sharedPref.contains("logging"))
        {
            logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
        }

        if (sharedPref.contains("email") && sharedPref.contains("password"))
        {
            Bundle extras = new Bundle();
            extras.putString("email", sharedPref.getString("email", ""));
            extras.putString("password", sharedPref.getString("password", ""));
            Intent i = new Intent(login.this, MainActivity.class);
            i.putExtras(extras);
            startActivity(i);

        }
        if (sharedPref.contains("Spatially_Host") && sharedPref.contains("Spatially_Port")) {

            sharedPref_Spatially_Host = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST));
            sharedPref_Spatially_Port = sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT));

        }
        else
        {
            toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.host_and_port_not_found_using_default), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0,  0);
            toast.show();

            if(logging==true)
            {
                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.host_and_port_not_found_using_default));
            }

            sharedPref_Spatially_Host = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST));
            sharedPref_Spatially_Port = sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT));

        }

        if(getIntent().getExtras()!=null)
        {

            if (getIntent().getExtras().containsKey("email") && getIntent().getExtras().containsKey("password") && !sharedPref.contains("email") && !sharedPref.contains("password"))
            {
                if (!getIntent().getExtras().getString("email").equals("") && !getIntent().getExtras().getString("password").equals("")) {

                    editEmail.setText(getIntent().getExtras().getString("email"));
                    editPassword.setText(getIntent().getExtras().getString("password"));

                }

            }
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    if (isConnected) {
                        if ((editEmail.getText() != null) && (editPassword.getText() != null) && !(editEmail.getText().toString().equals("")) && !(editPassword.getText().toString().equals(""))) {

                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("email", editEmail.getText().toString());
                            editor.putString("password", editPassword.getText().toString());
                            editor.commit();

                            dialog = new Dialog(login.this);
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.setContentView(R.layout.custom_login_dialog);
                            dialog.show();

                            URL = sharedPref_Spatially_Host + ":" + sharedPref_Spatially_Port + "/cgi-bin/boundaries" + "/auth.cgi?email=" + editEmail.getText().toString() + "&password=" + editPassword.getText().toString();

                            if(logging==true)
                            {
                                Log.i(getResources().getString(R.string.TAG), URL);
                            }

                            OkHttpClient client = new OkHttpClient();

                            Request request = new Request.Builder()

                                    .url(URL)
                                    .build();

                            if(logging==true)
                            {
                                Log.i(getResources().getString(R.string.TAG), URL);
                            }

                            client = new OkHttpClient.Builder()
                                    .connectTimeout(Long.parseLong(getResources().getString(R.string.login_timeout_seconds)), TimeUnit.SECONDS)
                                    .build();

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

                                    if (response.isSuccessful()) {

                                        reply = response.body().string().trim();

                                        //System.out.println("Response code is: " + response.code());
                                        response_code = response.code();

                                        if (response_code == 200) {

                                            if (reply != null) {

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        // Response 4
                                                        if (reply.equals("OK")) {

                                                            dialog.dismiss();
                                                            //Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_LONG).show();
                                                            // Can pass in the user login and password via a bundle
                                                            Bundle extras = new Bundle();
                                                            extras.putString("email", editEmail.getText().toString().trim());
                                                            extras.putString("password", editPassword.getText().toString().trim());
                                                            Intent i = new Intent(login.this, MainActivity.class);
                                                            i.putExtras(extras);
                                                            startActivity(i);


                                                        } else {

                                                            dialog.dismiss();

                                                            toast = Toast.makeText(getApplicationContext(), reply, Toast.LENGTH_LONG);
                                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                                            toast.show();

                                                            return;


                                                        }

                                                    }
                                                });
                                            }
                                        } else {

                                            dialog.dismiss();

                                            toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.unable_to_login), Toast.LENGTH_LONG);
                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                            toast.show();

                                            if(logging==true)
                                            {
                                                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.unable_to_login));
                                            }

                                            return;
                                        }

                                    }
                                }
                            });


                        } else {

                            toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.check_credentials_try_again), Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();

                            if(logging==true)
                            {
                                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.check_credentials_try_again));
                            }
                        }

                    }
                    else
                    {

                        toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.spatially_requires_internet), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        if(logging==true) {
                            Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.spatially_requires_internet));
                        }

                    }

                    }

                catch(Exception e){

                    if(getApplicationContext()!=null) {
                        ShowErrorDetails(Log.getStackTraceString(e), getApplicationContext());
                    }
                        /*
                        // Can toast the exception to user or ignore it and return
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

        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();

        // Finish the activity gracefully
        finish();
        overridePendingTransition(0, R.anim.out_from_right);

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
