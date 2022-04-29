package com.spatially.spatially_android;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

public class custom_item_animator extends DefaultItemAnimator {

    private Context context;
    public custom_item_animator(Context context) {
        this.context = context;
    }

    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        Vibrate(Long.parseLong(context.getResources().getString(R.string.vibration_duration_for_swipe)));
        return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY);
    }

    public void Vibrate(Long time_ms) {

        if(this.context!=null)
        {
            Vibrator v = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(time_ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(time_ms);
            }
        }

    }
}
