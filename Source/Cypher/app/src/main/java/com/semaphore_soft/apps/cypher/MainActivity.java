package com.semaphore_soft.apps.cypher;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
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

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFiler;

    private ProgressDialog peerProgress;
    private int hostWillingness;
    private final int SERVER_PORT = 58008;
    private final HashMap<String, String> buddies = new HashMap<>();

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
//                discoverService();
            }
        });

        Button hostGame = (Button) findViewById(R.id.host);
        hostGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hostWillingness = 15;
                discoverPeers();
//                discoverService();
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

    @Override
    protected void onStop() {
        disconnect();
        super.onStop();
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

    private void discoverService() {
        WifiP2pManager.DnsSdTxtRecordListener txtRecordListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            /* Callback includes:
             * fullDomain: Full domain name: e.g "printer._ipp._tcp.local".
             * record: TXT record data as a map of key/value pairs.
             * device: The device running the advertised service.
             */
            public void onDnsSdTxtRecordAvailable(String s, Map<String, String> map, WifiP2pDevice wifiP2pDevice) {
                Log.d("main", "DnsSdTxtRecord available = " + map.toString());
                buddies.put(wifiP2pDevice.deviceAddress, map.get("buddyname"));
            }
        };

        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice wifiP2pDevice) {
                // Update the device name with the human-friendly version from
                // the DnsTxtRecord, assuming one arrived
                wifiP2pDevice.deviceName = buddies.containsKey(wifiP2pDevice.deviceAddress) ? buddies.get(wifiP2pDevice.deviceAddress) : wifiP2pDevice.deviceName;

                // Add to adapter to show wifi devices
                //TODO create custom adapter for wifi devices
            }
        };

        mManager.setDnsSdResponseListeners(mChannel, servListener, txtRecordListener);

        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Success
            }

            @Override
            public void onFailure(int i) {
                // Command failed
            }
        });

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Success
            }

            @Override
            public void onFailure(int i) {
                // Command failed
                if (i == WifiP2pManager.P2P_UNSUPPORTED) {
                    Log.d("main", "P2P isn't supported on this device.");
                } else if (i == WifiP2pManager.BUSY) {
                    Log.d("main", "System is busy");
                } else if (i == WifiP2pManager.ERROR) {
                    Log.d("main", "There was an error"); // soooo helpful...
                }
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
