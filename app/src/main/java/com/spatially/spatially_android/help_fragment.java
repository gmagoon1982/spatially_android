package com.spatially.spatially_android;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;

public class help_fragment extends Fragment {

    // Had to move files from drawable to drawable-xhdpi for many images to work
    int[] images = new int[]{R.drawable.help_1, R.drawable.help_2, R.drawable.help_3, R.drawable.help_4, R.drawable.help_5, R.drawable.help_6, R.drawable.help_7, R.drawable.help_8, R.drawable.help_9, R.drawable.help_10, R.drawable.help_11, R.drawable.help_12};
    private ViewFlipper v_flipper;
    Boolean logging=false;
    SharedPreferences sharedPref;

    // Distance is in pixels
    // Velocity is in pixels / second
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_help, null);
        v_flipper = view.findViewById(R.id.v_flipper);

        return view;

    }

    // https://developer.android.com/guide/navigation/navigation-swipe-view
    // Can use getView() inside fragments to get view details
    // https://stackoverflow.com/questions/29429556/android-horizontal-scrolling-image-gallery
    // With this you learn how to add views to a layout programmatically

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {

            v_flipper.setAutoStart(false);

            // Adding views to flipper
            for (int i = 0; i < images.length; i++) {
                ImageView imageView = new ImageView(getContext());
                imageView.setBackgroundResource(images[i]);
                v_flipper.addView(imageView);
            }

            v_flipper.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gd.onTouchEvent(event);
                    return true;
                }

                GestureDetector gd = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener()
                {

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
                    {

                        if (((e1.getX() - e2.getX()) > SWIPE_MIN_DISTANCE) && (Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)) {

                            // Right to left
                            v_flipper.showNext();
                            v_flipper.setInAnimation(getContext(), R.anim.in_from_right);
                            v_flipper.setOutAnimation(getContext(), R.anim.out_from_left);

                            return true;
                        } else if (((e2.getX() - e1.getX()) > SWIPE_MIN_DISTANCE) && (Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)) {
                            // Left to right
                            v_flipper.showPrevious();
                            v_flipper.setInAnimation(getContext(), R.anim.in_from_left);
                            v_flipper.setOutAnimation(getContext(), R.anim.out_from_right);

                            return true;
                        }

                        return true;
                    }

                });

            });


     }

        catch(Exception e)
        {
            if(getContext()!=null) {
                ShowErrorDetails(Log.getStackTraceString(e), getContext());
            }

            /*
            toast = Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT);
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








