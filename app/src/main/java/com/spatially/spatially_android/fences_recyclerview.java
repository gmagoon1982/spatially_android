package com.spatially.spatially_android;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class fences_recyclerview extends RecyclerView.Adapter<fences_recyclerview.MyViewHolder> {

    List<String> fences;
    Context context;

    public interface RecyclerViewItemInterface {

        void onNameClick(int position, View view);

    }

    private fences_recyclerview.RecyclerViewItemInterface viewItemInterface;

    public void setViewItemInterface(fences_recyclerview.RecyclerViewItemInterface viewItemInterface) {
        this.viewItemInterface = viewItemInterface;
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView text;
        public ImageView delete;

        public MyViewHolder(View v) {
            super(v);

            text = v.findViewById(R.id.text);
            delete = v.findViewById(R.id.delete_icon);

        }

    }

    // Constructor
    public fences_recyclerview(List<String> fences, Context context) {

        this.fences = fences;
        this.context = context;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fences_row, parent, false);

        MyViewHolder vh = new MyViewHolder(v);

        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    // Holder contains all the view details for the items in recyclerview
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.text.setText(fences.get(position));
        holder.text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewItemInterface != null) {
                    viewItemInterface.onNameClick(holder.getAdapterPosition(), view);
                }
            }
        });


        // Can convert any view into a bitmap using this code
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View delete_button_temp = inflater.inflate(R.layout.fences_list_delete_button_layout, null);

        delete_button_temp.setDrawingCacheEnabled(true);
        delete_button_temp.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        delete_button_temp.layout(0, 0, delete_button_temp.getMeasuredWidth(), delete_button_temp.getMeasuredHeight());
        delete_button_temp.buildDrawingCache(true);

        Bitmap bitmap = Bitmap.createBitmap(delete_button_temp.getDrawingCache());
        delete_button_temp.setDrawingCacheEnabled(false);

        // The image is set here for the holder
        holder.delete.setImageBitmap(bitmap);

        // Old Code
        // The image is set here for the holder
        //holder.delete.setImageDrawable(context.getResources().getDrawable(R.drawable.delete));


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return fences.size();

    }


}