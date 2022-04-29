package com.spatially.spatially_android;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class notifications_fragment extends Fragment {

    View rootView;
    String email, password;
    List<String> notifications = new ArrayList<>();
    List<String> notifications_time = new ArrayList<>();
    TextView clear;
    SharedPreferences sharedPref;
    ListView notifications_list;
    notification_list_custom_adaptor notifications_list_adaptor;
    Integer badge_count;
    TextView notifications_title_font, notifications_clear_title_font;
    SwipeRefreshLayout pulltorefresh;
    BottomNavigationView navView;
    BottomNavigationView mBottomNavigationView;
    Menu menu;
    MenuItem notifications_icon;

    spatially_viewmodel model;

    String sharedPref_Spatially_Host, sharedPref_Spatially_Port;

    long pull_to_refresh_vibration_time;

    Boolean logging=false;

    NotificationManagerCompat notificationManagerCompat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_notifications, null);
        return (rootView);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        try {

            if (getContext() != null) {

                model = new ViewModelProvider(getActivity()).get(spatially_viewmodel.class);

                sharedPref = getContext().getSharedPreferences(getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE);

                if (sharedPref.contains("logging")) {
                    logging = sharedPref.getBoolean("logging", getResources().getBoolean(R.bool.logging));
                }

                clear = rootView.findViewById(R.id.Clear);
                notifications_list = rootView.findViewById(R.id.notification_list);

                notifications_title_font = rootView.findViewById(R.id.Notifications);
                notifications_clear_title_font = rootView.findViewById(R.id.Clear);

                notifications_clear_title_font.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.notifications_clear_title_font)/getResources().getDisplayMetrics().density);
                notifications_title_font.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.notifications_title_font)/getResources().getDisplayMetrics().density);

                notificationManagerCompat = NotificationManagerCompat.from(getContext());

                if (getArguments() != null && getArguments().containsKey("email") && getArguments().containsKey("password")) {
                    email = getArguments().getString("email").trim();
                    password = getArguments().getString("password").trim();
                }

                if (sharedPref.contains("Spatially_Host") && sharedPref.contains("Spatially_Port")) {

                    sharedPref_Spatially_Host = sharedPref.getString("Spatially_Host", getResources().getString(R.string.SPATIALLY_HOST));
                    sharedPref_Spatially_Port = sharedPref.getString("Spatially_Port", getResources().getString(R.string.SPATIALLY_PORT));

                }

                pull_to_refresh_vibration_time = Long.parseLong(getResources().getString(R.string.pull_to_refresh_vibration_time));

                if (getActivity() != null) {
                    notifications_list_adaptor = new notification_list_custom_adaptor(getActivity(), notifications, notifications_time);
                }
                notifications_list.setAdapter(notifications_list_adaptor);

                setupVMObserver();

                //notifications_title_font.setTextSize(getResources().getDimension(R.dimen.notifications_title_font));
                //notifications_clear_title_font.setTextSize(getResources().getDimension(R.dimen.notifications_clear_title_font));

                pulltorefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.pullToRefresh);
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

                                    setupVMObserver();
                                    pulltorefresh.setRefreshing(false);
                                    Vibrate(pull_to_refresh_vibration_time);

                                }
                            }

                            // Delay is in milliseconds (ms)
                        }, Long.parseLong(getResources().getString(R.string.pull_to_refresh_delay)));


                    }
                });

                clear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {

                            notifications.clear();
                            notifications_time.clear();
                            writeDataToViewModel();
                            notifications_list_adaptor.clear();
                            DeleteNotificationsFromDisk();
                            badge_count = 0;
                            UpdateBadgeAndWriteToDisk(badge_count);
                            notificationManagerCompat.cancelAll();

                        } catch (Exception e) {

                            if (getContext() != null) {
                                ShowErrorDetails(Log.getStackTraceString(e), getContext());
                            }

                            if (logging == true) {
                                Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                            }
                            return;
                        }
                    }
                });

                pulltorefresh.setRefreshing(true);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (pulltorefresh.isRefreshing()) {

                            setupVMObserver();

                            Vibrate(pull_to_refresh_vibration_time);
                            pulltorefresh.setRefreshing(false);

                        }
                    }

                    // Delay is in milliseconds (ms)
                }, Long.parseLong(getResources().getString(R.string.pull_to_refresh_delay)));

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

        if(logging==true) {
            Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
        }

        return;
    }

    }


    private void writeDataToViewModel()
    {

        if(getActivity()!=null)
        {
            model = new ViewModelProvider(getActivity()).get(spatially_viewmodel.class);

            if(notifications!=null && notifications_time!=null)
            {
                model.setNotifications((ArrayList) notifications);
                model.setNotifications_time((ArrayList) notifications_time);
            }


        }


    }

    private void setupVMObserver() {

        if(getActivity()!=null)
        {
            if(logging==true) {
                Log.i(getResources().getString(R.string.TAG), getResources().getString(R.string.setting_up_vm_observer));
            }

                model.getNotifications().observe(getActivity(), new Observer<ArrayList>() {
                    @Override
                    public void onChanged(ArrayList arrayList) {

                        if(getActivity()!=null && arrayList!=null)
                        {

                            notifications = arrayList;

                            if(notifications_list_adaptor!=null)
                                notifications_list_adaptor.notifyDataSetChanged();

                        }


                    }
                });

            model.getNotifications_time().observe(getActivity(), new Observer<ArrayList>() {
                @Override
                public void onChanged(ArrayList arrayList) {

                    if(getActivity()!=null && arrayList!=null)
                    {
                       notifications_time = arrayList;

                       if(notifications_list_adaptor!=null)
                           notifications_list_adaptor.notifyDataSetChanged();
                    }

                }
            });


        }

    }

    public void UpdateBadgeAndWriteToDisk(int notification_count) {

        try {

            if(getActivity()!=null)
            {
                ((MainActivity) getActivity()).unread_notifications = notification_count;
            }

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("notification_count", notification_count);
            editor.commit();
            editor.apply();

            if(getActivity()!=null)
            {
                mBottomNavigationView = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
                navView = (BottomNavigationView) getActivity().findViewById(R.id.nav_view);
            }
            menu = navView.getMenu();

            // Get the fourth item in list, index starts from 0 from left in menu
            BadgeDrawable badgeDrawable = navView.getOrCreateBadge(menu.getItem(3).getItemId());

            if(notification_count!=0 && notification_count>0) {

                badgeDrawable.setBackgroundColor(getResources().getColor(R.color.RED));
                badgeDrawable.setNumber(notification_count);
                badgeDrawable.setBadgeGravity(BadgeDrawable.TOP_END);
                badgeDrawable.setVisible(true);
            }
            else if(notification_count==0)
            {
                //badgeDrawable.clearNumber();
                badgeDrawable.setVisible(false);
            }


        } catch (Exception e) {

            if(getContext()!=null)
            {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
                /*
                toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                */

                if(logging==true) {
                    Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                }
            }

        }

    }

    public void manualPullToRefresh() {

        pulltorefresh.setRefreshing(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pulltorefresh.setRefreshing(false);
            }
        }, Long.parseLong(getResources().getString(R.string.pull_to_refresh_delay)));


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void DeleteNotificationsFromDisk() {

        try
        {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove("notifications");
            editor.remove("notifications_count");
            editor.remove("notifications_timestamp");
            editor.commit();
            editor.apply();

        } catch (Exception e) {

            if(getContext()!=null)
            {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
                /*
                toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                */

                if(logging==true) {
                    Log.i(getResources().getString(R.string.TAG), Log.getStackTraceString(e));
                }
            }

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

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}