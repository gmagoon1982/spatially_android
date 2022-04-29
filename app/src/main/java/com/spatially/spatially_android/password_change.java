package com.spatially.spatially_android;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class password_change extends AppCompatActivity {

    Toast toast;
    Boolean logging=false;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.activity_generic_titlebar);

        Intent extras = getIntent();
        String email = extras.getExtras().getString("email");
        String password = extras.getExtras().getString("password");

        //System.out.println(email);
        //System.out.println(password);

        EditText old_password = findViewById(R.id.old_password);
        EditText new_password = findViewById(R.id.new_password);
        EditText new_password2 = findViewById(R.id.new_password2);

        Button Submit = findViewById(R.id.Submit);
        Button Cancel = findViewById(R.id.Cancel);

        sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

        if(sharedPref.contains("logging"))
        {
            logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
        }

        String sharedPref_Spatially_Host = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST));
        String sharedPref_Spatially_Port = sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT));

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            //System.out.println(old_password.getText());
            //System.out.println(new_password.getText());
            //System.out.println(new_password2.getText());


                if(!old_password.getText().toString().equals("") && !old_password.getText().equals(null) && !new_password.getText().toString().equals("") && !new_password.getText().equals(null) && !new_password2.getText().toString().equals("") && !new_password2.getText().equals(null)) {

                    if (((new_password.getText().toString()).equals(new_password2.getText().toString())) && ((old_password.getText().toString()).equals(password))) {

                        String URL = sharedPref_Spatially_Host + ":" + sharedPref_Spatially_Port + "/cgi-bin/boundaries" + "/change-password.cgi?email=" + email + "&password=" + password + "&newpass=" + (new_password2.getText().toString());

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

                                String reply = response.body().string().trim();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        //System.out.println(reply);

                                        if (reply.equals("OK")) {

                                            toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.password_is_changed), Toast.LENGTH_LONG);
                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                            toast.show();
                                            finish();

                                        }
                                    }
                                });

                            }
                        });

                    }

                    else
                    {
                        toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.please_check_input_try_again) , Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        if(logging==true) {
                            Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.please_check_input_try_again));
                        }
                    }

                    }
                    else {

                        toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.please_check_input_try_again), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        if(logging==true) {
                            Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.please_check_input_try_again));
                        }

                    }

            }




        });


        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

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
