package com.spatially.spatially_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class fences_fragment extends Fragment {

    String email, password;
    View rootView;
    SharedPreferences sharedPref;

    List<String> reply_array = new ArrayList<>();
    List<String> fence_names = new ArrayList<>();
    List<String> fence_latitude = new ArrayList<>();
    List<String> fence_longitude = new ArrayList<>();
    List<String> fence_ids = new ArrayList<>();
    List<String> fence_radius = new ArrayList<>();

    TextView Edit, Done, fences_title_font, add_fence, Add;

    RecyclerView.Adapter mAdapter;
    RecyclerView recyclerView;
    int Current_position;

    SwipeRefreshLayout pulltorefresh;
    float swipeWidthMax;
    Toast toast;
    Boolean live;
    Boolean logging=false;

    spatially_viewmodel model;

    long pull_to_refresh_vibration_time;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getArguments().containsKey("email") && getArguments().containsKey("password")) {
            email = fences_fragment.this.getArguments().getString("email").trim();
            password = fences_fragment.this.getArguments().getString("password").trim();
        }

        rootView = inflater.inflate(R.layout.fragment_fences, null);

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

            ClearAll();

            model = new ViewModelProvider(getActivity()).get(spatially_viewmodel.class);

            getFencesList();
            setupVMObserver();

            pull_to_refresh_vibration_time = Long.parseLong(getResources().getString(R.string.pull_to_refresh_vibration_time));

            recyclerView = (RecyclerView) rootView.findViewById(R.id.fences_recyclerview);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            recyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

            recyclerView.setLayoutManager(layoutManager);

            recyclerView.setItemAnimator(new custom_item_animator(getContext()));
            recyclerView.getItemAnimator().setChangeDuration(Long.parseLong(getResources().getString(R.string.notify_item_changed_duration)));
            //recyclerView.setItemAnimator(itemAnimator);

            Edit = (TextView) rootView.findViewById(R.id.Edit);
            Done = (TextView) rootView.findViewById(R.id.Done);

            //Plus = rootView.findViewById(R.id.addfence);
            //Plus.setTextSize(getResources().getDimension(R.dimen.plus_text_size));

            //Edit.setTextSize(getResources().getDimension(R.dimen.fences_edit));

            // Automatically sets textsize based on density of the device
            Edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.fences_edit)/getResources().getDisplayMetrics().density);
            Edit.setTextColor(getResources().getColor(R.color.Information_Tint));

            Done.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.fences_done)/getResources().getDisplayMetrics().density);
            Done.setTextColor(getResources().getColor(R.color.Information_Tint));
            Done.setVisibility(View.INVISIBLE);

            fences_title_font = rootView.findViewById(R.id.Fences);
            fences_title_font.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.fences_title_font)/getResources().getDisplayMetrics().density);

            Add = (TextView) getActivity().findViewById(R.id.addfence);
            Add.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.fences_add_fence_font)/getResources().getDisplayMetrics().density);
            TooltipCompat.setTooltipText(Add, getResources().getString(R.string.Add_fence_tooltip));

            live = ((MainActivity)getActivity()).live_location_self;
            //ViewCompat.setBackgroundTintList(add_fence, ContextCompat.getColorStateList(getContext(), R.color.colorPrimary));
            //add_fence.setY(add_fence.getY()+10);

            //fences_title_font.setTextSize(getResources().getDimension(R.dimen.fences_title_font));
            //add_fence.setTextSize(getResources().getDimension(R.dimen.add_fence));

            pulltorefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.pullToRefresh);

            //pulltorefresh.setProgressViewEndTarget(false, 0);

            pulltorefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

                @Override
                public void onRefresh() {

                    Edit.setEnabled(false);

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (pulltorefresh.isRefreshing()) {

                                getFencesList();
                                pulltorefresh.setRefreshing(false);
                                Edit.setEnabled(true);
                                Vibrate(pull_to_refresh_vibration_time);
                            }
                        }

                        // Delay is in milliseconds (ms)
                    }, Long.parseLong(getResources().getString(R.string.pull_to_refresh_delay)));

                }
            });

            final Handler handler = new Handler();
            pulltorefresh.setRefreshing(true);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (pulltorefresh.isRefreshing()) {

                        ClearAll();
                        setupVMObserver();
                        getFencesList();
                        pulltorefresh.setRefreshing(false);
                        Vibrate(pull_to_refresh_vibration_time);
                    }
                }

                // Delay is in milliseconds (ms)
            }, Long.parseLong(getResources().getString(R.string.pull_to_refresh_delay)));

            Add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), activity_addfence.class).putExtra("email", email).putExtra("password", password).putExtra("live", live));
                    manualPullToRefresh();

                }
            });

            // android:translationX="-250dp"

            Edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    recyclerView.animate().translationX(50f).setDuration(400);

                    // This holds the position after animation
                    //animation.setFillEnabled(true);
                    //animation.setFillAfter(true);

                    if (recyclerView.getAdapter()!=null && recyclerView.getAdapter().getItemCount() > 0) {

                            for (int i = 0; i < recyclerView.getAdapter().getItemCount(); i++) {

                                Current_position = i;
                                View test = recyclerView.getChildAt(i);

                                ImageView delete_button = (ImageView) test.findViewById(R.id.delete_icon);

                                if (delete_button != null) {

                                    delete_button.animate().alpha(1).translationX(-20f).setDuration(400).withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            delete_button.setVisibility(View.VISIBLE);
                                            delete_button.setEnabled(true);

                                        }
                                    });

                                    delete_button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            AlertDialog.Builder delete = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);
                                            delete.setMessage("Are you sure you want to delete " + fence_names.get(Current_position) + " ?");
                                            delete.setPositiveButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                    dialogInterface.dismiss();
                                                    Done.performClick();

                                                }
                                            });

                                            delete.setNegativeButton("Yes", new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                    RemoveFence(Current_position);
                                                    Vibrate(Long.parseLong(getResources().getString(R.string.fence_delete_vibration_time)));

                                                    if (logging == true)
                                                        Log.i(getResources().getString(R.string.TAG), "Fence " + fence_names.get(Current_position) + getResources().getString(R.string.fence_removed));

                                                    Done.performClick();
                                                }

                                            });

                                            delete.show();

                                        }
                                    });

                                }

                            }

                        }


                    Edit.setVisibility(View.INVISIBLE);
                    Done.setVisibility(View.VISIBLE);

                    // Automatically resets the item's swipe state to normal after an idle of 10 s

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Done.performClick();
                        }
                    }, Long.parseLong(getResources().getString(R.string.edit_fences_restore_time_delay)));


                }

            });

            Done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    recyclerView.animate().translationX(0f).setDuration(400);

                    // This holds the position after animation
                    //animation.setFillEnabled(true);
                    //animation.setFillAfter(true);

                    if(recyclerView.getAdapter()!=null && recyclerView.getAdapter().getItemCount()>0) {
                        for (int i = 0; i < recyclerView.getAdapter().getItemCount(); i++) {

                            final int Current_position = i;

                            if (recyclerView.getChildAt(i) != null) {

                                View test = recyclerView.getChildAt(i);
                                ImageView delete_button = test.findViewById(R.id.delete_icon);

                                if(delete_button!=null)
                                {
                                    delete_button.animate().alpha(0).translationX(-100f).setDuration(400).withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            delete_button.setVisibility(View.GONE);
                                            delete_button.setEnabled(false);
                                        }
                                    });
                                }

                            }

                        }
                    }

                    Edit.setVisibility(View.VISIBLE);
                    Done.setVisibility(View.INVISIBLE);

                    manualPullToRefresh();

                }

            });

        } catch (Exception e) {

            if(getContext()!=null) {
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

    private void setValuesToVM()
    {

        if(fence_names!=null && fence_latitude!=null && fence_longitude!=null && fence_ids!=null && fence_radius!=null)
        {
            model.setFence_names((ArrayList) fence_names);
            model.setFence_center_latitude((ArrayList) fence_latitude);
            model.setFence_center_longitude((ArrayList) fence_longitude);
            model.setFence_ids((ArrayList) fence_ids);
            model.setFence_radius((ArrayList) fence_radius);

        }

    }

    private void setupVMObserver()
    {

        if(getActivity()!=null)
        {
            if(logging==true) {
                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.setting_up_vm_observer));
            }

            model.get_fence_names().observe(getActivity(), new Observer<ArrayList>() {
                @Override
                public void onChanged(ArrayList arrayList) {

                    if(getActivity()!=null)
                    {
                        if(arrayList!=null)
                        {
                            fence_names = arrayList;

                            if(mAdapter!=null)
                            {
                                mAdapter.notifyDataSetChanged();
                            }

                        }
                    }

                }
            });

            model.get_fence_ids().observe(getActivity(), new Observer<ArrayList>() {
                @Override
                public void onChanged(ArrayList arrayList) {

                    if(getActivity()!=null)
                    {
                        if(arrayList!=null)
                        {
                            fence_ids = arrayList;

                            if(mAdapter!=null)
                            {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                }
            });

            model.get_fence_radius().observe(getActivity(), new Observer<ArrayList>() {
                @Override
                public void onChanged(ArrayList arrayList) {

                    if(getActivity()!=null)
                    {
                        if(arrayList!=null)
                        {
                            fence_radius = arrayList;

                            if(mAdapter!=null)
                            {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                }
            });

            model.get_fence_center_latitude().observe(getActivity(), new Observer<ArrayList>() {
                @Override
                public void onChanged(ArrayList arrayList) {

                    if(getActivity()!=null)
                    {
                        if(arrayList!=null)
                        {
                            fence_latitude = arrayList;

                            if(mAdapter!=null)
                            {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                }
            });

            model.get_fence_center_longitude().observe(getActivity(), new Observer<ArrayList>() {
                @Override
                public void onChanged(ArrayList arrayList) {

                    if(getActivity()!=null)
                    {
                        if(arrayList!=null)
                        {
                            fence_longitude = arrayList;

                            if(mAdapter!=null)
                            {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                }
            });
        }

    }

    public void getFencesList() {

        try {

            if(getContext()!=null) {

                if (getActivity() != null) {

                    mAdapter = new fences_recyclerview(fence_names, getContext());

                    recyclerView.setAdapter(mAdapter);

                    ((fences_recyclerview) mAdapter).setViewItemInterface(new fences_recyclerview.RecyclerViewItemInterface() {
                        @Override
                        public void onNameClick(int position, View view) {

                            if ((fence_latitude.get(position) != null) && (fence_longitude.get(position) != null) && (fence_radius.get(position) != null) && (fence_names.get(position) != null) && (fence_ids.get(position) != null)) {
                                Bundle extras = new Bundle();
                                extras.putString("email", email);
                                extras.putString("password", password);
                                extras.putDouble("latitude", Double.parseDouble(fence_latitude.get(position)));
                                extras.putDouble("longitude", Double.parseDouble(fence_longitude.get(position)));
                                extras.putDouble("radius", Double.parseDouble(fence_radius.get(position)));
                                extras.putString("fence", fence_names.get(position));
                                extras.putString("fence_id", fence_ids.get(position));
                                extras.putInt("map_type", 0);
                                startActivityForResult(new Intent(getActivity(), view_fence.class).putExtras(extras), 1);
                            }

                        }
                    });

                    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                        @Override
                        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                            return false;
                        }

                        @Override
                        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                            if (direction == 4) {

                                Vibrate(Long.parseLong(getResources().getString(R.string.vibration_duration_for_swipe)));

                                // Automatically resets the item's swipe state to normal after an idle of 10 s
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        try {
                                            if ((Edit.getVisibility() != View.VISIBLE) && (Done.getVisibility() == View.VISIBLE)) {
                                                Done.performClick();
                                            }

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

                            if (recyclerView.getChildCount() > 0 && viewHolder.getAdapterPosition() < recyclerView.getChildCount()) {

                                View item = viewHolder.itemView;

                                swipeWidthMax = (float) (0.15 * viewHolder.itemView.getWidth());

                                Bitmap iconbmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.trash_can_delete_icon);
                                Paint paint_red = new Paint();
                                ColorFilter filter_red = new LightingColorFilter(1, Color.RED);
                                paint_red.setColorFilter(filter_red);
                                int horizontal_padding = 25;
                                int vertical_padding = 20;

                                Rect delete_bound = new Rect((item.getRight() - (int) ((swipeWidthMax)) + horizontal_padding), item.getTop() + vertical_padding, item.getRight() - horizontal_padding, item.getBottom() - vertical_padding);

                                c.drawBitmap(iconbmp, null, delete_bound, paint_red);

                                recyclerView.setOnTouchListener(new View.OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View view, MotionEvent motionEvent) {

                                        float x = motionEvent.getX();
                                        float y = motionEvent.getY();

                                        if (delete_bound.contains((int) x, (int) y) && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                                            View item = recyclerView.findChildViewUnder(recyclerView.getWidth() / 2, y); //finding the view that clicked , using coordinates X and Y
                                            int position = recyclerView.getChildAdapterPosition(item);
                                            //System.out.println(position);

                                            AlertDialog.Builder delete = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);
                                            delete.setMessage("Are you sure you want to delete " + fence_names.get(position) + " ?");
                                            delete.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                    //System.out.println("Red Button was pressed!");
                                                    String URL = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/remove-fence.cgi?email=" + email + "&password=" + password + "&fenceId=" + fence_ids.get(position);
                                                    OkHttpClient client = new OkHttpClient();

                                                    Request request = new Request.Builder()
                                                            .url(URL)
                                                            .build();

                                                    if (logging == true)
                                                        Log.i(getResources().getString(R.string.TAG), URL);


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

                                                                Vibrate(Long.parseLong(getResources().getString(R.string.fence_delete_vibration_time)));

                                                                if (getActivity() != null) {
                                                                    getActivity().runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {

                                                                            try {

                                                                                //////////////////////////////////////////////
                                                                                //fence_names.clear();
                                                                                //mAdapter.notifyDataSetChanged();
                                                                                //manualPullToRefresh();
                                                                                //////////////////////////////////////////////

                                                                                removeFenceWithIndex(position);
                                                                                setValuesToVM();

                                                                            } catch (Exception e) {

                                                                                if (getContext() != null) {
                                                                                    ShowErrorDetails(Log.getStackTraceString(e), getContext());
                                                                                }

                                                                                if (logging == true) {
                                                                                    Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                                                                                }
                                                                            }
                                                                            return;
                                                                        }
                                                                    });
                                                                }

                                                                dialogInterface.dismiss();


                                                            }
                                                            else {

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

                                            delete.setPositiveButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                    dialogInterface.cancel();

                                                    Handler handler = new Handler(Looper.getMainLooper());
                                                    handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            try {

                                                                if ((Edit.getVisibility() != View.VISIBLE) && (Done.getVisibility() == View.VISIBLE)) {
                                                                    Done.performClick();
                                                                }

                                                                mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());

                                                            } catch (Exception e) {

                                                                if (getContext() != null) {
                                                                    ShowErrorDetails(Log.getStackTraceString(e), getContext());
                                                                }

                                                                if (logging == true) {
                                                                    Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                                                                }
                                                            }

                                                        }
                                                    }, Long.parseLong(getResources().getString(R.string.one_second_to_ms)) * Long.parseLong(getResources().getString(R.string.delete_cancel_delay)));

                                                    return;
                                                }
                                            });

                                            delete.show();


                                        }
                                        return false;
                                    }


                                });



                            }

                                // dX here limits the swipe left movement
                                super.onChildDraw(c, recyclerView, viewHolder, -(Integer.parseInt(getResources().getString(R.string.fenceslist_left_swipe_limit))), dY, actionState, isCurrentlyActive);

                            }


                    };


                    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

                    itemTouchHelper.attachToRecyclerView(recyclerView);

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

            if(logging==true)
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));

            return;
        }

    }

    private void removeFenceWithIndex(int position) {

        if((fence_radius.get(position))!=null && (fence_latitude.get(position))!=null && (fence_longitude.get(position))!=null && (fence_ids.get(position))!=null && (fence_radius.get(position))!=null) {

            fence_names.remove(position);
            fence_latitude.remove(position);
            fence_longitude.remove(position);
            fence_ids.remove(position);
            fence_radius.remove(position);
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == 1)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                manualPullToRefresh();
            }
        }
    }

    void RemoveFence(int index) {

        try {

            String URL = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST)) + ":" + sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT)) + "/cgi-bin/boundaries" + "/remove-fence.cgi?email=" + email + "&password=" + password + "&fenceId=" + fence_ids.get(index);

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(URL)
                    .build();

            if(logging==true)
                Log.i(getResources().getString(R.string.TAG), URL);


            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {

                    if(logging==true)
                        Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String reply = response.body().string().trim();

                    if(getActivity()!=null)
                    {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (reply.equals("OK") && fence_names.get(index)!=null) {
                                    toast = Toast.makeText(getActivity(), fence_names.get(index) + " " + getResources().getString(R.string.fence_removed), Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();

                                    if(logging==true)
                                    {
                                        Log.i(getResources().getString(R.string.TAG), fence_names.get(index) + " " + getResources().getString(R.string.fence_removed));
                                    }

                                    removeFenceWithIndex(index);
                                    setValuesToVM();

                                } else {
                                    toast = Toast.makeText(getActivity(), getResources().getString(R.string.error_occurred), Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();

                                    if(logging==true)
                                    {
                                        Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.error_occurred));
                                    }
                                }
                            }
                        });
                    }

                    }
                    //listViewAdapterDetails.remove(reply_array_fence_name[i]);
                    //listViewAdapterDetails.notifyDataSetChanged();

            });

        } catch (Exception e) {

            if(getContext()!=null) {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
            }

            if(logging==true)
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
        }

    }

    public void ClearAll() {
        try {

            if(logging==true)
                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.clearing_all_data_lists));

            reply_array = new ArrayList<>();
            fence_names = new ArrayList<>();
            fence_latitude = new ArrayList<>();
            fence_longitude = new ArrayList<>();
            fence_ids = new ArrayList<>();
            fence_radius = new ArrayList<>();

            reply_array.clear();
            fence_names.clear();
            fence_latitude.clear();
            fence_longitude.clear();
            fence_ids.clear();
            fence_radius.clear();

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
                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));

            return;
        }
    }

    public void WriteFences() {

        if(logging==true)
            Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.writing_fences_list));

        SharedPreferences.Editor editor = sharedPref.edit();

        StringBuilder temp = new StringBuilder();

        for (int i = 0; i < fence_ids.size(); i++) {
            temp.append(fence_ids.get(i));
            temp.append(",");
        }

        editor.putString("fence_ids", temp.toString());
        editor.commit();

        temp = new StringBuilder();
        for (int i = 0; i < fence_names.size(); i++) {
            temp.append(fence_names.get(i));
            temp.append(",");
        }

        editor.putString("fence_names", temp.toString());
        editor.commit();

    }

    public void manualPullToRefresh(){

        if(getContext()!=null)
        {
            pulltorefresh.setRefreshing(true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    getFencesList();
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

    @Override
    public void onResume() {
        super.onResume();
        onCreate(Bundle.EMPTY);
    }


    public static float spToPx(Context ctx,float sp){
        return sp * ctx.getResources().getDisplayMetrics().scaledDensity;
    }

    public static float pxToDp(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float dpToPx(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
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

