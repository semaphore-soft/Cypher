package com.semaphore_soft.apps.cypher;

import android.app.ListFragment;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evan on 10/23/2016.
 * Simple ListFragment to show available services as published by peers
 */

public class WiFiServicesList extends ListFragment
{
    WiFiDevicesAdapter listAdapter = null;

    interface DeviceClickListener
    {
        void connectP2p(WiFiP2pService service);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstaceState)
    {
        return inflater.inflate(R.layout.devices_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        listAdapter = new WiFiDevicesAdapter(this.getActivity(), android.R.layout.simple_expandable_list_item_2,
                android.R.id.text1, new ArrayList<WiFiP2pService>());
        setListAdapter(listAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        ((DeviceClickListener) getActivity()).connectP2p((WiFiP2pService) l.getItemAtPosition(position));
        ((TextView) v.findViewById(android.R.id.text2)).setText("Connecting");
    }

    public class WiFiDevicesAdapter extends ArrayAdapter<WiFiP2pService>
    {
        private List<WiFiP2pService> items;

        public WiFiDevicesAdapter(Context context, int resource, int textViewResourceId,
                                  List<WiFiP2pService> items)
        {
            super(context, resource, textViewResourceId, items);
            this.items = items;
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            View v = convertView;
            if (v == null)
            {
                LayoutInflater vi = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_2, null);
            }

            WiFiP2pService service = items.get(position);
            if (service != null)
            {
                TextView nameText = (TextView) v.findViewById(android.R.id.text1);
                if (nameText != null)
                {
                    nameText.setText(service.device.deviceName + " - " + service.instanceName);
                }
                TextView statusText = (TextView) v.findViewById(android.R.id.text2);
                statusText.setText(getDeviceStatus(service.device.status));
            }

            return v;
        }

        @Override
        public void clear()
        {
            Log.d("adapter", "Called clear()");
            items.clear();
            notifyDataSetChanged();
            super.clear();
        }
    }

    public static String getDeviceStatus(int statusCode)
    {
        switch (statusCode)
        {
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }
}
