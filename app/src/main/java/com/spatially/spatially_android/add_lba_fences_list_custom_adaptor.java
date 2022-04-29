package com.spatially.spatially_android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class add_lba_fences_list_custom_adaptor extends ArrayAdapter {

    private Context mContext;
    private List<String> objects1;
    private List<String> objects2;

    public add_lba_fences_list_custom_adaptor(@NonNull Context context, @NonNull List<String> objects1, @NonNull List<String> objects2) {

        super(context, R.layout.add_lba_fences_list_custom_adaptor);

        this.mContext = context;
        this.objects1 = objects1;
        this.objects2 = objects2;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            if (convertView == null) {

                if (position < this.objects1.size() && position < this.objects2.size()) {

                    LayoutInflater inflater = (LayoutInflater) (mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
                    convertView = inflater.inflate(R.layout.add_lba_fences_list_custom_adaptor, parent, false);


                    TextView textView1 = (TextView) (convertView.findViewById(R.id.Fence));
                    textView1.setText(objects1.get(position));
                    TextView textView2 = (TextView) (convertView.findViewById(R.id.Status));
                    textView2.setText(objects2.get(position));
                    return convertView;

                }

            }





    return convertView;
    }

    @Override
    public int getCount() {
        return objects1.size();
    }
}
