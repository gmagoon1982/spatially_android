package com.spatially.spatially_android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class add_new_lba_fences_list extends AppCompatActivity {

    List<String> reply_array;
    List<String> fence_names;
    List<String> fence_ids;
    List<Double> latitude;
    List<Double> longitude;
    List<Double> radius;
    SharedPreferences sharedPref;
    String email, password;
    String URL;
    Toast toast;
    Boolean logging=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setTheme(R.style.AppTheme_NoActionBar);
            getSupportActionBar().hide();

            setContentView(R.layout.activity_add_new_lba_fences_list);

            reply_array = new ArrayList<>();
            fence_names = new ArrayList<>();
            fence_ids = new ArrayList<>();
            latitude = new ArrayList<>();
            longitude = new ArrayList<>();
            radius = new ArrayList<>();

            email = getIntent().getExtras().getString("email");
            password = getIntent().getExtras().getString("password");

            sharedPref = getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

            if(email!=null && password!=null) {

                URL = sharedPref.getString("Spatially_Host", null) + ":" + sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/get-fence-list.cgi?email=" + email + "&password=" + password;

            }

            if(sharedPref.contains("logging"))
            {
                logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
            }

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


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                reply_array = (Arrays.asList(reply.split("\n")));

                                // Have to exclude the OK
                                for (int i = 0; i <= (reply_array.size() - 2); i++) {

                                    if (reply_array.get(i).split(",").length == 5) {

                                        fence_names.add(String.valueOf(reply_array.get(i).split(",")[4]));
                                        fence_ids.add(String.valueOf((reply_array.get(i).split(","))[0]));
                                        latitude.add(Double.parseDouble((reply_array.get(i).split(","))[1]));
                                        longitude.add(Double.parseDouble((reply_array.get(i).split(","))[2]));
                                        radius.add(Double.parseDouble((reply_array.get(i).split(","))[3]));


                                    }
                                }
                                //System.out.println(reply_array);

                                //System.out.println("email: " + email + " password: " + password);

                                TextView select = findViewById(R.id.txtSelect);
                                select.setText("SELECT A PLACE");
                                select.setTextSize(getResources().getDimension(R.dimen.title_text_font));

                                ListView fences_list = findViewById(R.id.fences_list);

                                Paint textPaint = select.getPaint();
                                float textPaint_width = textPaint.measureText(select.getText().toString());
                                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) select.getLayoutParams();


                                double width = getResources().getDisplayMetrics().widthPixels;
                                double height = getResources().getDisplayMetrics().heightPixels;

                                params.setMarginStart((int) ((width / 2) - (textPaint_width / 2)));

                                //System.out.println("width: " + width + " width_of_fences_widget: " + width_of_fences_widget + " width_of_title: " + textPaint_width);
                                //System.out.println("width: " + width + " height: " + height);

                                ArrayAdapter<String> fences_ids_list;

                                fences_ids_list = new fence_selection_list_custom_adaptor(getBaseContext(), fence_names);

                                fences_list.setAdapter(fences_ids_list);

                                fences_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                        //System.out.println(fences_list.getItemAtPosition(i));
                                        Intent data = new Intent();
                                        data.putExtra("lba_fence_id", fence_ids.get(i));
                                        data.putExtra("lba_fence_name", fence_names.get(i));
                                        setResult(Activity.RESULT_OK, data);
                                        finish();
                                    }
                                });


                            }


                            //System.out.println("add_new_lba_fences_list: " + fence_names.get(i) + " " + fence_ids.get(i));


                        });


                    }


                }

            });
        }
        catch(Exception e){

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

        if(sharedPref!=null && exception_message!=null && context!=null)
        {

            sharedPref = context.getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

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
