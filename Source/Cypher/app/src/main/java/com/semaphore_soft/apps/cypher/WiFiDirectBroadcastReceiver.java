package com.semaphore_soft.apps.cypher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evan on 9/24/2016.
 * Broadcast Receiver for Wifi P2P connections
 */

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;
    private List<WifiP2pDevice> peers = new ArrayList<>();

    private final static String TAG = "WifiBR";


    public WiFiDirectBroadcastReceiver (WifiP2pManager manager, WifiP2pManager.Channel channel,
                                        MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            // Clear old list and add in new peers
            peers.clear();
            peers.addAll(peerList.getDeviceList());

            //if using an AdapterView, notify it of the change (ex. ListView)
            //https://developer.android.com/training/connect-devices-wirelessly/wifi-direct.html
            if (peers.size() == 0) {
                Log.d(TAG, "No devices found");
                return;
            }

            Log.d(TAG, "Peers found: " + peers.size());
            for (WifiP2pDevice device : peers) {
                Log.i(TAG, device.toString());
            }
        }
    };

    @Override
    public void onReceive (Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                Log.d(TAG, "P2P enabled");
            } else {
                // Wifi P2P is not enabled
                Log.d(TAG, "P2P disabled, please fix");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Request available peers. This is an asynchronous call. The calling activity
            // is notified with a callback on PeerListListener.onPeerAvailable()
            if (mManager != null) {
                mManager.requestPeers(mChannel, peerListListener);
            }
            Log.d(TAG, "peer list changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            Log.d(TAG, "connection changed");
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            Log.d(TAG, "this device's state changed");
        }
    }
}
