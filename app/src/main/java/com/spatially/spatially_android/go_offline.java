package com.spatially.spatially_android;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class go_offline extends AppCompatActivity {

    String URL;
    SharedPreferences sharedPref;
    String email, password;
    String live, hide;
    String reply;
    Toast toast;
    Boolean logging=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_offline);

        this.getSupportActionBar().hide();
        setTheme(R.style.AppTheme_NoActionBar);

        sharedPref = getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

        if(sharedPref.contains("logging"))
        {
            logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
        }

        if(logging==true) {
            Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.entering_go_offline));
        }

        TextView txtGoOnline = findViewById(R.id.txtGoOnline);
        txtGoOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email = getIntent().getExtras().getString("email");
                password = getIntent().getExtras().getString("password");
                live = getIntent().getExtras().getString("live");


                URL = sharedPref.getString("Spatially_Host", String.valueOf(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", String.valueOf(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/get-my-attr.cgi?email=" + email + "&password=" + password;
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

                        try {
                            reply = (response.body().string().trim().split("\nOK"))[0];

                            //System.out.println(reply);
                            //hide = ((((reply.split(","))[0])).split("="))[1];

                            live = ((((reply.split(","))[1])).split("="))[1];

                            hide = "NO";

                            URL = sharedPref.getString("Spatially_Host", String.valueOf(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", String.valueOf(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/update-my-attr.cgi?email=" + email + "&password=" + password + "&hide=" + hide + "&live=" + live;
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

                                    String reply = (response.body().string().trim());

                                    if (reply.equals("OK")) {

                                        if(logging==true) {
                                            Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.exiting_go_offline));
                                        }

                                        finish();
                                        overridePendingTransition(0, R.anim.out_from_right);

                                    }
                                }
                            });


                        }
                        catch(Exception e){

                            if(getApplicationContext()!=null) {
                                ShowErrorDetails(Log.getStackTraceString(e), getApplicationContext());
                            }

                            /*
                            toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
                            toast.show();

                             */

                            if(logging==true) {
                                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                            }

                        }

                    }
                });
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            // If back is pressed, nothing should happen. User must choose Go Online to continue
            // Return true means you handle the event
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
