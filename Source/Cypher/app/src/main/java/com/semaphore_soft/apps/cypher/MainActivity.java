package com.semaphore_soft.apps.cypher;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFiler;

    private ProgressDialog peerProgress;
    private int hostWillingness;
    private int SERVER_PORT = 58008;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        peerProgress = new ProgressDialog(this);

        mIntentFiler = new IntentFilter();
        mIntentFiler.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFiler.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFiler.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFiler.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        Button findGame = (Button) findViewById(R.id.connect);
        findGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hostWillingness = 0;
                discoverPeers();
            }
        });

        Button hostGame = (Button) findViewById(R.id.host);
        hostGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hostWillingness = 15;
                discoverPeers();
            }
        });

        Button disconnect = (Button) findViewById((R.id.disconnect));
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnect();
            }
        });

        // Make sure we're using the newest service and it's the only one
        mManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("clear", "Cleared local services");
                startRegistration();
            }

            @Override
            public void onFailure(int i) {
                Log.d("clear", "Failed to clear local services");
                Toast.makeText(getApplication(), "Failed to add local service",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Register the broadcast receiver with the intent values to be matched
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFiler);
    }

    // Unregister the broadcast receiver
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private void discoverPeers() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Broadcast Receiver will be notified if successful
                Log.d("main", "Discovering peers");
                // Display progress bar(circle) while waiting for broadcast receiver
                peerProgress.setIndeterminate(true);
                peerProgress.setTitle("Looking for peers");
                peerProgress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        mManager.stopPeerDiscovery(mChannel, null);
                        Log.d("main", "Stopping peer discovery");
                    }
                });
                peerProgress.show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d("main", "Peer discovery failed, Error:" + reasonCode);
            }
        });
    }

    private void disconnect() {
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Will be handled by Broadcast Receiver
                Log.d("main", "Removing group");
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(getApplication(), "Failed to disconnect", Toast.LENGTH_SHORT).show();
                Log.d("main", "Error removing group. Error: " + i);
            }
        });
    }

    // register a local service for discovery
    private void startRegistration() {
        // Create a string map containing information about the service
        Map<String, String> record = new HashMap<>();
        record.put("listenport", String.valueOf(SERVER_PORT));
        record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
        record.put("available", "visible");

        // Service information
        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("cypher", "_presence._tcp", record);

        // Add the local service
        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Command successful. Code not needed here
                Log.d("add", "Added local service");
            }

            @Override
            public void onFailure(int i) {
                // Command failed
                Log.d("add", "Failed to add local service");
                Toast.makeText(getApplication(), "Adding local service failed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public ProgressDialog getPeerProgress() {
        return peerProgress;
    }

    public int getHostWillingness() {
        return hostWillingness;
    }
}
