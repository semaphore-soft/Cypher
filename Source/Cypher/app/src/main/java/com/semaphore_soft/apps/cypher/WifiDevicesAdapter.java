package com.semaphore_soft.apps.cypher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Evan on 10/23/2016.
 * Adapter for selecting services
 */

public class WifiDevicesAdapter extends ArrayAdapter<WiFiItem> {
    public WifiDevicesAdapter(Context context, int textViewResourceId){
        super(context, textViewResourceId);
    }

    public WifiDevicesAdapter(Context context, int resource, List<WiFiItem> items){
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        View v = convertView;

        if(v == null) {
            LayoutInflater li;
            li = LayoutInflater.from(getContext());
            v = li.inflate(R.layout.item_list_row, null);
        }

        WiFiItem item = getItem(position);

        if(item != null) {
            TextView name = (TextView) v.findViewById(R.id.name);

            if(name != null) {
                name.setText(item.address);
            }
        }

        return v;
    }
}
