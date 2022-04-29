package com.spatially.spatially_android;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class activity_addfriend extends AppCompatActivity {

    Button btnSubmit;
    Button btnCancel;
    EditText editEmail;
    String email, password;
    String reply;
    Toast toast;
    Boolean logging=false;
    SharedPreferences sharedPref;
    spatially_viewmodel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            setContentView(R.layout.activity_addfriend);

            this.getSupportActionBar().hide();
            setTheme(R.style.AppTheme_NoActionBar);

            model = new ViewModelProvider(this).get(spatially_viewmodel.class);

            btnSubmit = (Button) findViewById(R.id.btnSubmit);
            btnCancel = (Button) findViewById(R.id.btnCancel);
            editEmail = (EditText) findViewById(R.id.editEmail);

            sharedPref = getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

            if(getIntent().hasExtra("email")) {
                email = getIntent().getExtras().getString("email");
            }

            if(getIntent().hasExtra("password")) {
                password = getIntent().getExtras().getString("password");
            }

            if(sharedPref.contains("logging"))
            {
                logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
            }

            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    // Hard coded host and port for the server are in the strings.xml resources file
                    // Have to use getString to get string associated with specific resource id

                    if((editEmail.getText()!=null) && !(editEmail.getText().toString().equals(""))) {

                        String URL = sharedPref.getString("Spatially_Host", String.valueOf(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", String.valueOf(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/add-friend.cgi?email=" + email + "&password=" + password + "&friendemail=" + editEmail.getText().toString();

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

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_try_again), Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();

                                    if(logging==true)
                                    {
                                        Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                                        Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.error_try_again));
                                    }



                                    return;
                                }
                            });

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            // onResponse already assures the response is successful, apparently.
                            //if(response.isSuccessful()) {

                            reply = response.body().string().trim();

                            runOnUiThread(new Runnable() {
                                public void run() {

                                    toast = Toast.makeText(getApplicationContext(), reply, Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                    finish();
                                }
                            });

                        }

                    });


                }
                    else {
                        toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.check_credentials_try_again), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        if(logging==true) {
                            Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.check_credentials_try_again));
                        }


                    }
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
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

            if(logging==true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

            return;
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




