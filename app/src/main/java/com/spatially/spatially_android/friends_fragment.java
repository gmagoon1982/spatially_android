package com.spatially.spatially_android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// Notice the extends Fragment part
public class friends_fragment extends Fragment {

    Bundle extras;
    String email, password;

    List<String> name = new ArrayList<>();
    List<String> account_creation_time = new ArrayList<>();
    List<String> id_time = new ArrayList<>();
    List<String> fences = new ArrayList<>();
    List<String> email_info = new ArrayList<>();
    List<String> battery_info = new ArrayList<>();
    List<String> accuracy_info = new ArrayList<>();
    List<String> latitude = new ArrayList<>();
    List<String> longitude = new ArrayList<>();
    List<String> id = new ArrayList<>();
    List<String> last_movement_time = new ArrayList<>();

    View rootView;
    SharedPreferences sharedPref;
    RecyclerView recyclerView;

    RecyclerView.Adapter mAdapter;
    BottomNavigationView navView;

    TextView name_friendsrow_font, time_friendsrow_font, battery_lvl_friendsrow_font;
    TextView friends_title_font, add_friend;

    // Fence data
    List<String> fence_names = new ArrayList<>();
    List<String> fence_center_latitude = new ArrayList<>();
    List<String> fence_center_longitude = new ArrayList<>();
    List<String> fence_ids = new ArrayList<>();
    List<String> fence_radius = new ArrayList<>();

    SwipeRefreshLayout pulltorefresh;

    float swipeWidthMax;
    Toast toast;

    Boolean logging=false;

    private spatially_viewmodel model;

    long pull_to_refresh_vibration_time;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_friends, null);

        if(getActivity()!=null)
        {
            toast = Toast.makeText(getActivity(), getResources().getString(R.string.loading_location_data), Toast.LENGTH_SHORT);
        }
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        return (rootView);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {

            if(getActivity()!=null)
            {
                sharedPref = getActivity().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);
                if(sharedPref.contains("logging"))
                {
                    logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
                }

            }

            model = new ViewModelProvider(getActivity()).get(spatially_viewmodel.class);

            getFriendsList();
            setupVMObserver();

            recyclerView = (RecyclerView) rootView.findViewById(R.id.friends_recyclerview);
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            recyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new custom_item_animator(getContext()));
            recyclerView.getItemAnimator().setChangeDuration(Long.parseLong(getResources().getString(R.string.notify_item_changed_duration)));

            pull_to_refresh_vibration_time = Long.parseLong(getResources().getString(R.string.pull_to_refresh_vibration_time));

            // Setting font sizes for the friends list row
            View friends_row = getLayoutInflater().inflate(R.layout.friends_row, null);
            name_friendsrow_font = friends_row.findViewById(R.id.name);
            time_friendsrow_font = friends_row.findViewById(R.id.time);
            battery_lvl_friendsrow_font = friends_row.findViewById(R.id.batterylvl);

            // Text has to be in sp
            name_friendsrow_font.setTextSize(getResources().getDimension(R.dimen.name_text_font));
            time_friendsrow_font.setTextSize(getResources().getDimension(R.dimen.time_text_font));
            battery_lvl_friendsrow_font.setTextSize(getResources().getDimension(R.dimen.battery_lvl_text_font));

            friends_title_font = rootView.findViewById(R.id.Friends);
            friends_title_font.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.friends_title_font)/getResources().getDisplayMetrics().density);

            add_friend = rootView.findViewById(R.id.addfriend);
            add_friend.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.friends_add_friend_font)/getResources().getDisplayMetrics().density);
            //add_friend.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

            add_friend.setTextColor(getResources().getColor(R.color.Information_Tint));
            add_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getActivity()!=null)
                    {
                        startActivity(new Intent(getActivity(), activity_addfriend.class).putExtra("email", email).putExtra("password", password));
                    }

                }
            });
            TooltipCompat.setTooltipText(add_friend, getResources().getString(R.string.Add_friend_tooltip));

            //ViewCompat.setBackgroundTintList(add_friend, ContextCompat.getColorStateList(getContext(), R.color.colorPrimary));

            //View line_separator = rootView.findViewById(R.id.vertical_divider);
            //ViewGroup.MarginLayoutParams line_separator_margin = (ViewGroup.MarginLayoutParams)line_separator.getLayoutParams();

            //ViewGroup.LayoutParams add_friend_layout_params = (ViewGroup.LayoutParams)add_friend.getLayoutParams();
            //add_friend.setY(add_friend.getY()+10);

            //friends_title_font.setTextSize(getResources().getDimension(R.dimen.friends_title_font));
            //add_friend.setTextSize(getResources().getDimension(R.dimen.add_friend));

            // this object is important!
            // getArguments is used to get information from the bundle
            if (getArguments().containsKey("email") && getArguments().containsKey("password")) {
                email = friends_fragment.this.getArguments().getString("email").trim();
                password = friends_fragment.this.getArguments().getString("password").trim();
            }

            //System.out.println(email);
            //System.out.println(password);

            pulltorefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.pullToRefresh);

            // Disables the spinning circle graphic
            //pulltorefresh.setProgressViewEndTarget(false, 0);

            pulltorefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

                @Override
                public void onRefresh() {

                    //Toast.makeText(getContext(), "Refreshing...", Toast.LENGTH_SHORT).show();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (pulltorefresh.isRefreshing()) {

                                getFriendsList();
                                pulltorefresh.setRefreshing(false);
                                Vibrate(pull_to_refresh_vibration_time);

                            }
                        }

                        // Delay is in milliseconds (ms)
                    }, Long.parseLong(getResources().getString(R.string.pull_to_refresh_delay)));

                }
            });

            pulltorefresh.setRefreshing(true);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (pulltorefresh.isRefreshing()) {

                        ClearAll();
                        setupVMObserver();
                        getFriendsList();
                        pulltorefresh.setRefreshing(false);
                        Vibrate(pull_to_refresh_vibration_time);

                    }
                }

                // Delay is in milliseconds (ms)
            }, Long.parseLong(getResources().getString(R.string.pull_to_refresh_delay)));

        } catch (Exception e) {

            if(getContext()!=null)
            {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
            }

            /*
            toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

             */

            if(logging==true) {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

            return;
        }

    }


    public void setupVMObserver() {

        try {
            if (getActivity() != null) {

                if(logging==true) {
                    Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.setting_up_vm_observer));
                }

                model.get_name().observe(getActivity(), new Observer<ArrayList>() {
                    @Override
                    public void onChanged(ArrayList arrayList) {

                        if(getActivity()!=null && arrayList!=null)
                        {

                            name=arrayList;

                            if(mAdapter!=null)
                            {
                                mAdapter.notifyDataSetChanged();
                            }

                        }
                    }
                });

                model.getLast_movement_time().observe(getActivity(), new Observer<ArrayList>() {
                    @Override
                    public void onChanged(ArrayList arrayList) {

                        if(getActivity()!=null && arrayList!=null)
                        {

                            last_movement_time=arrayList;

                            if(mAdapter!=null)
                            {
                                mAdapter.notifyDataSetChanged();
                            }

                        }
                    }
                });

                model.getLatitude().observe(getActivity(), new Observer<ArrayList>() {
                    @Override
                    public void onChanged(ArrayList arrayList) {

                        if(getActivity()!=null && arrayList!=null)
                        {

                            latitude=arrayList;

                            if(mAdapter!=null)
                            {
                                mAdapter.notifyDataSetChanged();
                            }

                        }
                    }
                });

                model.getLongitude().observe(getActivity(), new Observer<ArrayList>() {
                    @Override
                    public void onChanged(ArrayList arrayList) {

                        if(getActivity()!=null && arrayList!=null)
                        {

                            longitude=arrayList;

                            if(mAdapter!=null)
                            {
                                mAdapter.notifyDataSetChanged();
                            }

                        }
                    }
                });

                model.getBattery_info().observe(getActivity(), new Observer<ArrayList>() {
                    @Override
                    public void onChanged(ArrayList arrayList) {

                        if(getActivity()!=null && arrayList!=null)
                        {

                            battery_info=arrayList;

                            if(mAdapter!=null)
                            {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });

                model.get_fence_names().observe(getActivity(), new Observer<ArrayList>() {
                    @Override
                    public void onChanged(ArrayList arrayList) {

                        if(getActivity()!=null && arrayList!=null)
                        {

                            fence_names=arrayList;

                            if(mAdapter!=null)
                            {
                                mAdapter.notifyDataSetChanged();
                            }

                        }
                    }
                });

                model.get_fence_center_longitude().observe(getActivity(), new Observer<ArrayList>() {
                    @Override
                    public void onChanged(ArrayList arrayList) {

                        if(getActivity()!=null && arrayList!=null)
                        {

                            fence_center_longitude=arrayList;

                            if(mAdapter!=null)
                            {
                                mAdapter.notifyDataSetChanged();
                            }

                        }

                    }
                });

                model.get_fence_center_latitude().observe(getActivity(), new Observer<ArrayList>() {
                    @Override
                    public void onChanged(ArrayList arrayList) {

                        if(getActivity()!=null && arrayList!=null)
                        {

                            fence_center_latitude=arrayList;

                            if(mAdapter!=null)
                            {
                                mAdapter.notifyDataSetChanged();
                            }

                        }

                    }
                });
                model.get_fence_radius().observe(getActivity(), new Observer<ArrayList>() {
                    @Override
                    public void onChanged(ArrayList arrayList) {

                        if(getActivity()!=null && arrayList!=null)
                        {

                            fence_radius=arrayList;

                            if(mAdapter!=null)
                            {
                                mAdapter.notifyDataSetChanged();
                            }
                        }

                    }
                });

                model.getEmail_info().observe(getActivity(), new Observer<ArrayList>() {
                    @Override
                    public void onChanged(ArrayList arrayList) {

                        if(getActivity()!=null && arrayList!=null)
                        {

                            email_info=arrayList;

                        }

                    }
                });

            }

                /*
                System.out.println(name);
                System.out.println(last_movement_time);
                System.out.println(latitude);
                System.out.println(longitude);
                System.out.println(battery_info);
                */

                /*
                sharedPref = getActivity().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                StringBuilder sb_email_info = new StringBuilder();

                for (int i = 0; i <= (email_info.size() - 1); i++) {
                    sb_email_info.append(email_info.get(i)).append(",");
                }

                editor.putString("friendsfragment_email_info", sb_email_info.toString());
                editor.commit();

                StringBuilder sb_name = new StringBuilder();

                for (int i = 0; i <= (name.size() - 1); i++) {
                    sb_name.append(name.get(i)).append(",");
                }

                editor.putString("friendsfragment_name", sb_name.toString());
                editor.commit();

                StringBuilder sb_latitude = new StringBuilder();

                for (int i = 0; i <= (latitude.size() - 1); i++) {
                    sb_latitude.append(latitude.get(i)).append(",");
                }

                editor.putString("friendsfragment_latitude", sb_latitude.toString());
                editor.commit();

                StringBuilder sb_longitude = new StringBuilder();

                for (int i = 0; i <= (longitude.size() - 1); i++) {
                    sb_longitude.append(longitude.get(i)).append(",");
                }

                editor.putString("friendsfragment_longitude", sb_longitude.toString());
                editor.commit();

                 */

            /*
            System.out.println("Written name: " + name);
            System.out.println("Written email_info: " + email_info);
            System.out.println("Written latitude: " + latitude);
            System.out.println("Written longitude: " + longitude);
            */

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

            return;
        }

    }

    public void getFriendsList() {

        try {

            if (getContext() != null) {

                if (getActivity() != null) {

                    if(logging==true) {
                        Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.getting_friends_list));
                    }
                                            if (getActivity() != null) {

                                                /*
                                                System.out.println("friends_fragment.java: ");
                                                System.out.println(name);
                                                System.out.println(last_movement_time);
                                                System.out.println(latitude);
                                                System.out.println(longitude);
                                                System.out.println(battery_info);
                                                 */

                                                // specify an adapter (see also forward example)
                                                mAdapter = new friends_recyclerview(getContext(), name, last_movement_time, latitude, longitude, battery_info, fence_names, fence_center_latitude, fence_center_longitude, fence_radius);

                                                recyclerView.setAdapter(mAdapter);

                                                        // info_icon and name
                                                        ((friends_recyclerview) mAdapter).setViewItemInterface(new friends_recyclerview.RecyclerViewItemInterface() {
                                                            @Override
                                                            public void onInfoClick(int position, View view) {

                                                                //System.out.println("Opening friendslist_user_info for: " + position);
                                                                OpenInfo(position);

                                                            }

                                                            @Override
                                                            public void onNameClick(int position, View view) {

                                                                //System.out.println("Opening fragment_map for: " + position);
                                                                OpenMap(position);

                                                            }
                                                        });

                                                        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                                                            @Override
                                                            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

                                                                return false;
                                                            }

                                                            @Override
                                                            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                                                                if (viewHolder.getAdapterPosition()!=0 && direction == 4) {

                                                                    Vibrate(Long.parseLong(getResources().getString(R.string.vibration_duration_for_swipe)));

                                                                    // Automatically resets the item's swipe state to normal after an idle of 10 s
                                                                    Handler handler = new Handler(Looper.getMainLooper());
                                                                    handler.postDelayed(new Runnable() {
                                                                        @Override
                                                                        public void run() {

                                                                            try {

                                                                                mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());

                                                                            } catch (Exception e) {

                                                                                if (getContext() != null) {
                                                                                    ShowErrorDetails(Log.getStackTraceString(e), getContext());
                                                                                }

                                                                                if (logging == true)
                                                                                    Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                                                                            }

                                                                        }
                                                                    }, Long.parseLong(getResources().getString(R.string.one_second_to_ms)) * Long.parseLong(getResources().getString(R.string.normal_location_delay)));


                                                                }

                                                            }

                                                            @Override
                                                            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                                                                if(recyclerView.getChildCount()>0) {

                                                                if (viewHolder.getAdapterPosition() != 0 && viewHolder.getAdapterPosition() < recyclerView.getChildCount()) {

                                                                    View item = viewHolder.itemView;

                                                                    swipeWidthMax = (float) (0.15 * viewHolder.itemView.getWidth());

                                                                    Bitmap iconbmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.trash_can_delete_icon);

                                                                    Paint paint_white = new Paint();
                                                                    ColorFilter filter_white = new LightingColorFilter(1, Color.WHITE);
                                                                    paint_white.setColorFilter(filter_white);

                                                                    Paint paint_red = new Paint();
                                                                    ColorFilter filter_red = new LightingColorFilter(1, Color.RED);
                                                                    paint_red.setColorFilter(filter_red);

                                                                    Rect red_box = new Rect(item.getRight() - (int) (swipeWidthMax), item.getTop(), item.getRight(), item.getBottom());
                                                                    c.drawRect(red_box, paint_red);

                                                                    int horizontal_padding = 25;
                                                                    int vertical_padding = 30;
                                                                    Rect delete_bound = new Rect(item.getRight() - (int) (swipeWidthMax) + horizontal_padding, item.getTop() + vertical_padding, item.getRight() - horizontal_padding, item.getBottom() - vertical_padding);
                                                                    c.drawBitmap(iconbmp, null, delete_bound, paint_white);

                                                                    // Don't need this, the item view at position automatically shifts
                                                                    //item.setTranslationX(dX/5);

                                                                    recyclerView.setOnTouchListener(new View.OnTouchListener() {
                                                                        @Override
                                                                        public boolean onTouch(View view, MotionEvent motionEvent) {

                                                                            float x = motionEvent.getX();
                                                                            float y = motionEvent.getY();

                                                                            if (delete_bound.contains((int) x, (int) y) && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                                                                                View item = recyclerView.findChildViewUnder(recyclerView.getWidth() / 2, y); //finding the view that clicked , using coordinates X and Y
                                                                                int position = recyclerView.getChildAdapterPosition(item);
                                                                                //System.out.println(position);


                                                                                if (name.get(position) != null && email_info.get(position) != null) {

                                                                                    AlertDialog.Builder delete = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);

                                                                                    delete.setMessage("Are you sure you want to delete " + name.get(position) + " ?");

                                                                                    delete.setPositiveButton("No", new DialogInterface.OnClickListener() {

                                                                                        @Override
                                                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                                                            dialogInterface.cancel();

                                                                                            Handler handler = new Handler(Looper.getMainLooper());
                                                                                            handler.postDelayed(new Runnable() {
                                                                                                @Override
                                                                                                public void run() {

                                                                                                    mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());

                                                                                                }
                                                                                            }, Long.parseLong(getResources().getString(R.string.one_second_to_ms)) * Long.parseLong(getResources().getString(R.string.delete_cancel_delay)));

                                                                                            return;
                                                                                        }

                                                                                    });

                                                                                    delete.setNegativeButton("Yes", new DialogInterface.OnClickListener() {

                                                                                        @Override
                                                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                                                            //System.out.println("Red Button was pressed!");
                                                                                            String URL = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/remove-friend.cgi?email=" + email + "&password=" + password + "&friendemail=" + email_info.get(position);
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

                                                                                                    String reply = response.body().string().trim();
                                                                                                    // If delete has been accepted, vibrate the phone and refresh the list
                                                                                                    if (reply.equals("OK")) {

                                                                                                        if (getActivity() != null) {
                                                                                                            getActivity().runOnUiThread(new Runnable() {
                                                                                                                @Override
                                                                                                                public void run() {

                                                                                                                    Vibrate(Long.parseLong(getResources().getString(R.string.friend_delete_vibration_time)));

                                                                                                                    try {

                                                                                                                        dialogInterface.dismiss();

                                                                                                                        //////////////////////////////////////////////
                                                                                                                        // Need to include code here that updates the observer with new values
                                                                                                                        name.remove(position);
                                                                                                                        account_creation_time.remove(position);
                                                                                                                        id_time.remove(position);
                                                                                                                        email_info.remove(position);
                                                                                                                        battery_info.remove(position);
                                                                                                                        accuracy_info.remove(position);
                                                                                                                        latitude.remove(position);
                                                                                                                        longitude.remove(position);
                                                                                                                        id.remove(position);
                                                                                                                        last_movement_time.remove(position);

                                                                                                                        model.setName((ArrayList) name);
                                                                                                                        model.setAccount_creation_time((ArrayList) account_creation_time);
                                                                                                                        model.setId_time((ArrayList) id_time);
                                                                                                                        model.setEmail_info((ArrayList) email_info);
                                                                                                                        model.setBattery_info((ArrayList) battery_info);
                                                                                                                        model.setAccuracy_info((ArrayList) accuracy_info);
                                                                                                                        model.setLatitude((ArrayList) latitude);
                                                                                                                        model.setLongitude((ArrayList) longitude);
                                                                                                                        model.setId((ArrayList) id);
                                                                                                                        model.setLast_movement_time((ArrayList) last_movement_time);
                                                                                                                        //////////////////////////////////////////////

                                                                                                                        //mAdapter.notifyDataSetChanged();
                                                                                                                        //manualPullToRefresh();

                                                                                                                        toast = Toast.makeText(getContext(), name.get(position) + " " + getResources().getString(R.string.friend_removed), Toast.LENGTH_LONG);
                                                                                                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                                                                                                        toast.show();

                                                                                                                        if (logging == true) {
                                                                                                                            Log.i(getResources().getString(R.string.TAG), name.get(position) + " " + getResources().getString(R.string.friend_removed));
                                                                                                                        }

                                                                                                                    } catch (Exception e) {

                                                                                                                        if (getContext() != null) {
                                                                                                                            ShowErrorDetails(Log.getStackTraceString(e), getContext());
                                                                                                                        }

                                                                                                                        if (logging == true) {
                                                                                                                            Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                                                                                                                        }

                                                                                                                    }

                                                                                                                }
                                                                                                            });
                                                                                                        }

                                                                                                    } else {

                                                                                                        if (getActivity() != null) {
                                                                                                            getActivity().runOnUiThread(new Runnable() {
                                                                                                                @Override
                                                                                                                public void run() {

                                                                                                                    dialogInterface.dismiss();

                                                                                                                    toast = Toast.makeText(getContext(), getResources().getString(R.string.error_occurred) + " " + reply, Toast.LENGTH_LONG);
                                                                                                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                                                                                                    toast.show();

                                                                                                                    if (logging == true) {
                                                                                                                        Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.error_occurred) + " " + reply);
                                                                                                                    }

                                                                                                                }
                                                                                                            });
                                                                                                        }

                                                                                                    }
                                                                                                }

                                                                                            });


                                                                                        }

                                                                                    });

                                                                                    delete.show();

                                                                                }


                                                                            }
                                                                            return true;
                                                                        }


                                                                    });

                                                                    // dX here limits the swipe left movement
                                                                    super.onChildDraw(c, recyclerView, viewHolder, -swipeWidthMax, dY, actionState, isCurrentlyActive);
                                                                }

                                                            }
                                                            }

                                                        };

                                                        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

                                                        itemTouchHelper.attachToRecyclerView(recyclerView);

                                                    }




                                        }

            }
            } catch(Exception e){

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

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
                });
                return;
            }

        }

    private void ClearAll()
    {
        try {

            if (logging == true)
            {
                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.clearing_all_data_lists));
            }

            name = new ArrayList<>();
            account_creation_time = new ArrayList<>();
            id_time = new ArrayList<>();
            fences = new ArrayList<>();
            email_info = new ArrayList<>();
            battery_info = new ArrayList<>();
            accuracy_info = new ArrayList<>();
            latitude = new ArrayList<>();
            longitude = new ArrayList<>();
            id = new ArrayList<>();
            last_movement_time = new ArrayList<>();

            if(recyclerView!=null)
            {
                recyclerView.removeAllViews();
            }


        } catch (Exception e) {

            if(getContext()!=null) {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
            }

            /*
            toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

             */

            if(logging==true)
            {
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
            }

            return;
        }

    }

    public void OpenMap(int position) {


            if (position == 0) {

                Fragment map_fragment_gps = new map_fragment_gps();

                if (map_fragment_gps != null) {

                    navView = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
                    navView.setSelectedItemId(R.id.map);

                    Bundle extras = new Bundle();
                    extras.putString("email", email);
                    extras.putString("password", password);
                    map_fragment_gps.setArguments(extras);
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, map_fragment_gps)
                            .commit();

                }

            } else if (position<email_info.size() && position>0){

                navView = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
                navView.setSelectedItemId(R.id.map);

                //System.out.println(String.valueOf(email_info.get(position)));

                Fragment getfriendsMovement = new map_fragment_getfriends_movement();
                extras = new Bundle();
                extras.putString("friendsemail", String.valueOf(email_info.get(position)));
                extras.putString("password", password);
                extras.putString("email", email);
                extras.putString("name", name.get(position));

                getfriendsMovement.setArguments(extras);
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, getfriendsMovement).commit();

            }

    }

    private void OpenInfo(int position) {


            // Open self info
            if (position == 0) {

                Fragment selfinfo = new friendslist_self_info();

                Bundle extra = new Bundle();
                extra.putString("email", email);
                extra.putString("password", password);
                selfinfo.setArguments(extra);
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.in_from_right, 0);

                // null means name. You can give it a name too when adding to BackStack
                transaction.replace(R.id.fragment_container, selfinfo).addToBackStack(null).commit();

            }

            // Else Open user info
            else if (position<email_info.size() && position>0){

                /*
                    System.out.println("position: " + position);
                    System.out.println("email_info: " + email_info.get(position));
                    System.out.println("title: " + name.get(position));
                    System.out.println("battery_level: " + battery_info.get(position));
                    System.out.println("last_movement_time: " + last_movement_time.get(position));
                */

                    Fragment userinfo = new friendslist_user_info();
                    extras = new Bundle();
                    extras.putString("email", email);
                    extras.putString("password", password);
                    extras.putString("friends_email", (email_info.get(position)));
                    extras.putString("title", name.get(position));

                    extras.putString("battery_level", String.valueOf(battery_info.get(position)));
                    extras.putString("last_seen", last_movement_time.get(position));

                    userinfo.setArguments(extras);
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(R.anim.in_from_right, 0);

                    // null means name. You can give it a name too when adding to BackStack
                    transaction.replace(R.id.fragment_container, userinfo).addToBackStack(null).commit();

            }



    }


    public float getBatteryLevel() {

        try {

            if(logging==true) {
                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.get_battery_level));
            }

            // We are registering a broadcast receiver to the ACTION_BATTERY_CHANGED Intent to get
            // the battery levels. With IntentFilter you can filter out intent for a specific event
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = getContext().registerReceiver(null, ifilter);
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = (float) level / (float) scale;
            return batteryPct;
        }
        catch(Exception e)
        {
            if(getActivity()!=null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

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
                });
            }

        }

        return 0;
    }

    public void manualPullToRefresh(){

        if(getContext()!=null)
        {
            pulltorefresh.setRefreshing(true);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    pulltorefresh.setRefreshing(false);
                }
            }, Long.parseLong(getResources().getString(R.string.pull_to_refresh_delay)));

        }

    }

    public void Vibrate(Long time_ms) {

        if(getContext()!=null)
        {
            Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(time_ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(time_ms);
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