package com.spatially.spatially_android;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class friends_recyclerview extends RecyclerView.Adapter<friends_recyclerview.MyViewHolder>  {


    List<String> longitudeH;
    List<String> latitudeH;
    List<String> battery_infoH;
    List<String> nameH;
    List<String> last_movement_timeH;

    List<String> fence_namesH;
    List<String> fence_center_latitudeH;
    List<String> fence_center_longitudeH;
    List<String> fence_radiusH;

    List<String> fences_user_is_part_of_list = new ArrayList<String>();

    private ImageView batteryImage;
    private Context context;

    public interface RecyclerViewItemInterface {

        void onNameClick(int position, View view);

        void onInfoClick(int position, View view);

    }

    private RecyclerViewItemInterface viewItemInterface;

    public void setViewItemInterface(RecyclerViewItemInterface viewItemInterface) {
        this.viewItemInterface = viewItemInterface;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        public TextView nameView, timeView, fencesView, batteryView, distance_from_you;
        public ImageView info_icon;

        public MyViewHolder(View v) {
            super(v);

            nameView = v.findViewById(R.id.name);
            timeView = v.findViewById(R.id.time);
            fencesView = v.findViewById(R.id.fences);
            batteryView = v.findViewById(R.id.batterylvl);
            batteryImage = v.findViewById(R.id.battery_image);
            info_icon = v.findViewById(R.id.information);
            distance_from_you = v.findViewById(R.id.distance_from_you);

        }

    }

    // Constructor
    public friends_recyclerview(Context context, List<String> name, List<String> last_movement_time, List<String> latitude, List<String> longitude, List<String> battery_info, List<String> fence_names, List<String> fence_center_latitude, List<String> fence_center_longitude, List<String> fence_radius) {

        this.nameH = name;
        this.last_movement_timeH = last_movement_time;
        this.latitudeH = latitude;
        this.longitudeH = longitude;
        this.battery_infoH = battery_info;
        this.context = context;

        this.fence_namesH = fence_names;
        this.fence_center_latitudeH = fence_center_latitude;
        this.fence_center_longitudeH = fence_center_longitude;
        this.fence_radiusH = fence_radius;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new viewecyclerViewClickListener listener
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friends_row, parent, false);

        MyViewHolder vh = new MyViewHolder(v);

        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    // Holder contains all the view details for the items in recyclerview
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        SharedPreferences sharedPreferences =  (context.getApplicationContext().getSharedPreferences(context.getString(R.string.spatially_shared_pref_file), Context.MODE_PRIVATE));


            // In this method we look at different cases and appropriately assign values

            if (position!=0  && (nameH.get(position) != null) && (last_movement_timeH.get(position) != null)) {

                holder.nameView.setText(nameH.get(position));
                holder.timeView.setText(last_movement_timeH.get(position));
            }
            else if (position!=0 && (nameH.get(position) != null) && (last_movement_timeH.get(position) == null)){

                holder.nameView.setText(nameH.get(position));

            }
            else if (position == 0 && (nameH.get(position) == "You")) {

                holder.nameView.setText(nameH.get(position));

                if(sharedPreferences.getString("user_time", null)!=null)
                {
                    holder.timeView.setText(sharedPreferences.getString("user_time", null));
                }

            }

            if (position != 0 && !(battery_infoH.get(position) == null)) {

                double level = Double.valueOf(battery_infoH.get(position));

                custom_battery_level_class custom_battery_level_class = holder.itemView.findViewById(R.id.custom_battery_view);
                custom_battery_level_class.setVisibility(View.GONE);

                // Setting battery level based on value of battery_infoR[]
                if ((level > 0) && (level < 0.20))
                {

                    custom_battery_level_class.setColour("#" + Integer.toHexString(Color.RED));
                    custom_battery_level_class.setWidthPercentage(0.10);
                    custom_battery_level_class.setVisibility(View.VISIBLE);
                    batteryImage.setImageResource(R.drawable.battery_outline);
                    batteryImage.setAlpha((float) 1);
                    holder.batteryView.setTextColor(R.string.batteryView);
                    holder.batteryView.setTypeface(holder.batteryView.getTypeface(), Typeface.BOLD);
                    holder.batteryView.setText(String.valueOf(new DecimalFormat("#.#").format(Double.valueOf(battery_infoH.get(position)) * 100)) + "%");

                }
                else if ((level >= 0.20 && level < 0.40))
                {
                    custom_battery_level_class.setColour("#" + Integer.toHexString(Color.RED));
                    custom_battery_level_class.setWidthPercentage(0.30);
                    custom_battery_level_class.setVisibility(View.VISIBLE);
                    batteryImage.setImageResource(R.drawable.battery_outline);
                    batteryImage.setAlpha((float) 1);
                    holder.batteryView.setTextColor(R.string.batteryView);
                    holder.batteryView.setTypeface(holder.batteryView.getTypeface(), Typeface.BOLD);
                    holder.batteryView.setText(String.valueOf(new DecimalFormat("#.#").format(Double.valueOf(battery_infoH.get(position)) * 100)) + "%");
                }
                else if ((level >= 0.40 && level < 0.60))
                {

                    custom_battery_level_class.setColour("#" + Integer.toHexString(Color.YELLOW));
                    custom_battery_level_class.setWidthPercentage(0.50);
                    custom_battery_level_class.setVisibility(View.VISIBLE);
                    batteryImage.setImageResource(R.drawable.battery_outline);
                    batteryImage.setAlpha((float) 1);
                    holder.batteryView.setTextColor(R.string.batteryView);
                    holder.batteryView.setTypeface(holder.batteryView.getTypeface(), Typeface.BOLD);
                    holder.batteryView.setText(String.valueOf(new DecimalFormat("#.#").format(Double.valueOf(battery_infoH.get(position)) * 100)) + "%");
                }
                else if ((level >= 0.60 && level < 0.80))
                {

                    custom_battery_level_class.setColour("#" + Integer.toHexString(Color.GREEN));
                    custom_battery_level_class.setWidthPercentage(0.70);
                    custom_battery_level_class.setVisibility(View.VISIBLE);
                    batteryImage.setImageResource(R.drawable.battery_outline);
                    batteryImage.setAlpha((float) 1);
                    holder.batteryView.setTextColor(R.string.batteryView);
                    holder.batteryView.setTypeface(holder.batteryView.getTypeface(), Typeface.BOLD);
                    holder.batteryView.setText(String.valueOf(new DecimalFormat("#.#").format(Double.valueOf(battery_infoH.get(position)) * 100)) + "%");

                }
                else if ((level >= 0.80) && (level < 1))
                {

                    custom_battery_level_class.setColour("#" + Integer.toHexString(Color.GREEN));
                    custom_battery_level_class.setWidthPercentage(0.90);
                    custom_battery_level_class.setVisibility(View.VISIBLE);
                    batteryImage.setImageResource(R.drawable.battery_outline);
                    batteryImage.setAlpha((float) 1);
                    holder.batteryView.setTextColor(R.string.batteryView);
                    holder.batteryView.setTypeface(holder.batteryView.getTypeface(), Typeface.BOLD);
                    holder.batteryView.setText(String.valueOf(new DecimalFormat("#.#").format(Double.valueOf(battery_infoH.get(position)) * 100)) + "%");

                }
                else if (level == 1)
                {

                    custom_battery_level_class.setColour("#" + Integer.toHexString(Color.GREEN));
                    custom_battery_level_class.setWidthPercentage(1.0);
                    custom_battery_level_class.setVisibility(View.VISIBLE);
                    batteryImage.setImageResource(R.drawable.battery_outline);
                    batteryImage.setAlpha((float) 1);
                    holder.batteryView.setTextColor(R.string.batteryView);
                    holder.batteryView.setTypeface(holder.batteryView.getTypeface(), Typeface.BOLD);
                    holder.batteryView.setText(String.valueOf(new DecimalFormat("#.#").format(Double.valueOf(battery_infoH.get(position)) * 100)) + "%");

                }

            }


            // Displays distance of each user from YOU
            if (position != 0 && (latitudeH.get(position) != null) && (longitudeH.get(position) != null)) {

                boolean check_lat = sharedPreferences.contains("user_latitude");
                boolean check_long = sharedPreferences.contains("user_longitude");

                if ((check_lat == true) && (check_long == true)) {

                    holder.distance_from_you.setTextColor(Color.BLACK);

                    Double You_latitude = Double.valueOf(sharedPreferences.getString("user_latitude", String.valueOf(0)));
                    Double You_longitude = Double.valueOf(sharedPreferences.getString("user_longitude", String.valueOf(0)));

                    Location You = new Location("You");
                    You.setLatitude(You_latitude);
                    You.setLongitude(You_longitude);


                    Location Data = new Location("Data");
                    Data.setLatitude(Double.valueOf(latitudeH.get(position)));
                    Data.setLongitude(Double.valueOf(longitudeH.get(position)));

                    // Gives result in meters
                    double distance = You.distanceTo(Data);

                    if (distance < 100) {
                        holder.distance_from_you.setText("near you");
                    }
                    else if (distance < 1000){
                        holder.distance_from_you.setText(new DecimalFormat("#").format(distance) + " m");
                    }
                    else {
                        holder.distance_from_you.setText((new DecimalFormat("#").format(distance/1000)) + " km");
                    }

                }

            }

            // Check if the friends's location is within a any fence. If so include the fence name in friends list
            if (position != 0 && (latitudeH.get(position) != null) && (longitudeH.get(position) != null)) {

                Location friends_center = new Location("friends_center");
                friends_center.setLatitude(Float.parseFloat(latitudeH.get(position)));
                friends_center.setLongitude(Float.parseFloat(longitudeH.get(position)));

                holder.fencesView.setTextColor(Color.BLACK);

                fences_user_is_part_of_list.clear();

                // Check through list of all fences
                for (int i = 0; i < fence_namesH.size(); i++){

                    // Using Locations method
                    Location fences_center = new Location("fences_center");
                    fences_center.setLatitude(Double.valueOf(fence_center_latitudeH.get(i)));
                    fences_center.setLongitude(Double.valueOf(fence_center_longitudeH.get(i)));

                    if(friends_center.distanceTo(fences_center)<=Double.valueOf(fence_radiusH.get(i))){

                        if(!fences_user_is_part_of_list.contains(fence_namesH.get(i)))
                        {
                            fences_user_is_part_of_list.add(fence_namesH.get(i));
                        }
                    }

                }

                if(!fences_user_is_part_of_list.isEmpty()) {
                    holder.fencesView.setText(TextUtils.join(",", fences_user_is_part_of_list));
                }

            }

            holder.info_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (viewItemInterface != null) {
                        viewItemInterface.onInfoClick(holder.getAdapterPosition(), view);
                    }
                }
            });

            holder.nameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (viewItemInterface != null) {
                        viewItemInterface.onNameClick(holder.getAdapterPosition(), view);
                    }
                }
            });

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View information_icon = inflater.inflate(R.layout.friends_list_information_icon_layout_file, null, false);

        information_icon.setDrawingCacheEnabled(true);
        information_icon.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        information_icon.layout(0, 0, information_icon.getMeasuredWidth(), information_icon.getMeasuredHeight());
        information_icon.buildDrawingCache(true);

        Bitmap bitmap = Bitmap.createBitmap(information_icon.getDrawingCache());
        information_icon.setDrawingCacheEnabled(false);

        // Using default android icons
        holder.info_icon.setImageBitmap(bitmap);
        //holder.info_icon.setColorFilter(ContextCompat.getColor(context, R.color.Information_Tint));
        holder.info_icon.setScaleType(ImageView.ScaleType.FIT_XY);
        holder.info_icon.setMaxHeight(R.dimen.info_height);
        holder.info_icon.setMaxWidth(R.dimen.info_width);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return nameH.size();

    }


}
