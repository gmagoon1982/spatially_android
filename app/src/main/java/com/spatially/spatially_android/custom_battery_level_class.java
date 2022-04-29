package com.spatially.spatially_android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class custom_battery_level_class extends AppCompatImageView {

    private double width_attribute;
    private String colour;

    public custom_battery_level_class(Context context)
    {
        super(context);
    }

    public custom_battery_level_class(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

    }

    public custom_battery_level_class(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if((this.width_attribute > 0) && (colour!=null)) {

            Paint paint = new Paint();
            paint.setColor(Color.parseColor(this.colour));
            canvas.drawRect(0, 0, (float)(width_attribute*this.getWidth()), (float)(this.getHeight()), paint);

        }

    }

    public void setWidthPercentage(double widthPercentage) {
        width_attribute = widthPercentage;
    }

    public void setColour(String colour)
    {
        this.colour = colour;
    }
}
