package com.spatially.spatially_android;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class settings extends AppCompatActivity {

    Toast toast;
    Boolean logging=false;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    Boolean Errors=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = (activeNetwork != null) && activeNetwork.isConnected();

            setContentView(R.layout.settings);

            getSupportActionBar().setDisplayShowTitleEnabled(false);

            // Will load up stored SharedPreferences with IP and PORT information
            sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);
            editor = sharedPref.edit();

            TextView Host = (TextView)findViewById(R.id.txtHost);
            EditText editHost = (EditText) findViewById(R.id.editHost);
            EditText editPort = (EditText) findViewById(R.id.editPort);
            Button Submit = (Button) findViewById(R.id.btnSubmit);
            Button Cancel = (Button) findViewById(R.id.btnCancel);

            Switch loggingSwitch = findViewById(R.id.swLog);
            Switch debugSwitch = findViewById(R.id.swDebug);

            debugSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    Errors=b;
                    editor.putBoolean("Errors", b);
                    editor.commit();
                    editor.apply();

                }
            });

            if(sharedPref.contains("Errors"))
            {

                debugSwitch.setChecked(sharedPref.getBoolean("Errors", getResources().getBoolean(R.bool.Errors)));

            }
            else
            {
                debugSwitch.setChecked(getResources().getBoolean(R.bool.Errors));
            }

            if(sharedPref.contains("logging"))
            {

                //Toast.makeText(getApplicationContext(), String.valueOf(sharedPref.getBoolean("logging", logging)), Toast.LENGTH_LONG).show();
                loggingSwitch.setChecked(sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging)));
                logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
            }

            loggingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    if(b==true)
                    {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.logging_enabled), Toast.LENGTH_SHORT).show();

                        if(logging==true) {
                            Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.logging_enabled));
                        }
                    }
                    else if(b==false)
                    {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.logging_disabled), Toast.LENGTH_SHORT).show();

                        if(logging==true) {
                            Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.logging_disabled));
                        }
                    }

                    editor.putBoolean("logging", b);
                    editor.commit();
                    editor.apply();

                }
            });

            Host.setText(getResources().getString(R.string.SPATIALLY_HOST_TEXT));

            //This sets the focus onto the widget
            editHost.requestFocus();

            // If a key is not found, default will be used. Cannot keep fields empty
            // sharedPref.getString(Key, Default Value)
            editHost.setText(sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST)));
            editPort.setText(sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT)));

            Submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (isConnected) {

                        if (!(editHost.getText().toString() == null) && !(editHost.getText().toString().equals("")) && !(editPort.getText().toString() == null) && !(editPort.getText().toString().equals(""))) {

                            // If all or some of the information has changed, clear old notification_count
                            if ((editHost.getText().toString() != sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST)))
                                    || (editHost.getText().toString() != sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST))
                                    && (editPort.getText().toString() != sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT))))) {

                                // Clear out any old notification history
                                editor.putInt("notification_count", 0);
                                editor.commit();
                                editor.apply();

                                editor.putString("Spatially_Host", String.valueOf(editHost.getText()));
                                editor.putString("Spatially_Port", String.valueOf(editPort.getText()));
                                editor.commit();
                                editor.apply();

                                toast = Toast.makeText(getApplicationContext(), "Saved!", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

                                finish();
                            }
                        } else {

                            toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.please_check_input_try_again), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();

                            if(logging==true) {
                                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.please_check_input_try_again));
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

            });

            Cancel.setOnClickListener(new View.OnClickListener() {
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
            toast = Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
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
