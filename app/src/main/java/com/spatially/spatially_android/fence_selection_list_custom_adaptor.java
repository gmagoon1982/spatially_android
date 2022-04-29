package com.spatially.spatially_android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class fence_selection_list_custom_adaptor extends ArrayAdapter{


    private Context mContext;
    private List<String> objects = new ArrayList<>();


    public fence_selection_list_custom_adaptor(@NonNull Context context, @NonNull List objects) {
        super(context, 0);

        this.mContext = context;
        this.objects = objects;


    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.fence_selection_list_custom_adaptor, parent, false);
        }

        if((objects.size()>=1)) {
            TextView textView1 = (TextView) convertView.findViewById(R.id.Fence);
            textView1.setText((String) this.objects.get(position));

        }

        return convertView;

    }

    @Override
    public int getCount() {
        return objects.size();
    }
}
