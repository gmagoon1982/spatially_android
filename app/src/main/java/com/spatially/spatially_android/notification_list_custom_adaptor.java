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

public class notification_list_custom_adaptor extends ArrayAdapter{


    private Context mContext;
    private List<String> notifications = new ArrayList<>();
    private List<String> time = new ArrayList<>();

    public notification_list_custom_adaptor(@NonNull Context context, @NonNull List notifications, @NonNull List time) {
        super(context, 0);

        this.mContext = context;
        this.notifications = notifications;
        this.time = time;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.notifications_row, parent, false);
        }

        if((notifications.size()>=1)) {
            TextView notification = (TextView) convertView.findViewById(R.id.notification);
            notification.setText((String) this.notifications.get(position));

        }
        if(time.size()>=1){
            TextView time = (TextView) convertView.findViewById(R.id.time);
            time.setText((String) this.time.get(position));
        }

        return convertView;

    }

    @Override
    public int getCount() {
        return notifications.size();
    }

}
