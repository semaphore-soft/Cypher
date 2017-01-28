package com.semaphore_soft.apps.cypher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements WiFiServicesList.DeviceClickListener
{

    private final static String TAG = "Main";
    // TXT RECORD properties
    public final static String SERVICE_INSTANCE = "_cypher";
    public final static String SERVICE_REG_TYPE = "_presence._tcp";
    // Port should be between 49152-65535
    public final static int SERVER_PORT = 58008;
    // rebroadcast every 2 minutes
//    private static final long SERVICE_BROADCASTING_INTERVAL = 120000;
    private static final long SERVICE_BROADCASTING_INTERVAL = 100000;
    private static final long SERVICE_DISCOVERING_INTERVAL = 120000;

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver = null;
    private IntentFilter mIntentFiler = new IntentFilter();
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private Handler mServiceBroadcastingHandler = new Handler();
    private Handler mServiceDiscoveringHandler = new Handler();

    public ProgressBar progressBar;
    // hostWillingness will only work if devices have not connected before
    // Devices will use the settings from the first time they were grouped
    private int hostWillingness;
    private final HashMap<String, String> buddies = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Allow network connections
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mIntentFiler.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFiler.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFiler.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFiler.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        WiFiServicesList servicesList = new WiFiServicesList();
        getFragmentManager().beginTransaction().add(R.id.servicesRoot, servicesList, "services").commit();

        Button findGame = (Button) findViewById(R.id.connect);
        findGame.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                hostWillingness = 0;
                // Reset ListView so old items are removed
                reset();
//                mServiceBroadcastingHandler.postDelayed(mServiceBroadcastingRunnable, SERVICE_BROADCASTING_INTERVAL);
                peerDiscovery();
                setupService();
                startDiscovery();
            }
        });

        Button hostGame = (Button) findViewById(R.id.host);
        hostGame.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                hostWillingness = 15;
                // Reset ListView so old items are removed
                reset();
//                mServiceBroadcastingHandler.postDelayed(mServiceBroadcastingRunnable, SERVICE_BROADCASTING_INTERVAL);
                peerDiscovery();
                setupService();
                startDiscovery();
            }
        });

        Button disconnect = (Button) findViewById((R.id.disconnect));
        disconnect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                disconnect();
            }
        });

        // Make sure we're using the newest service and it's the only one
        mManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
                Log.d(TAG, "Cleared local services");
                startRegistration();
            }

            @Override
            public void onFailure(int i)
            {
                // TODO retry clearing local services
                Log.d(TAG, "Failed to clear local services");
                Toast.makeText(getApplication(), "Failed to add local service",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Register the broadcast receiver with the intent values to be matched
    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(mReceiver, mIntentFiler);
    }

    // Unregister the broadcast receiver
    @Override
    protected void onPause()
    {
        unregisterReceiver(mReceiver);
        stopServices();
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        disconnect();
        super.onStop();
    }

    private void disconnect()
    {
        progressBar.setVisibility(View.INVISIBLE);
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
                // Will be handled by Broadcast Receiver
                Log.d(TAG, "Removing group");
                Toast.makeText(getApplicationContext(), "Disconnecting", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i)
            {
                Log.d(TAG, "Error removing group. Error: " + i);
                // This should cancel service discovery
                // NOTE: Could also call after successful connection
                mManager.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener()
                {
                    @Override
                    public void onSuccess()
                    {
                        Log.d(TAG, "Removed service request");
                        Toast.makeText(getApplication(), "Removed service request", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i)
                    {
                        Log.d(TAG, "Failed to remove service request");
                        Toast.makeText(getApplication(), "Action failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void connectP2p(WiFiP2pService service)
    {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = service.device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = hostWillingness;
        if (serviceRequest != null)
        {
            mManager.removeServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Removed service request");
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG, "Failed to remove service request");
                }
            });
            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener()
            {
                @Override
                public void onSuccess()
                {
                    Log.d(TAG, "Connecting to service");
                    Toast.makeText(getApplication(), "Connecting to service", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int i)
                {
                    Log.d(TAG, "Failed connecting to service");
                    Toast.makeText(getApplication(), "Failed connecting to service", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // register a local service for discovery
    private void startRegistration()
    {
        // Create a string map containing information about the service
        Map<String, String> record = new HashMap<>();
        record.put("listenport", String.valueOf(SERVER_PORT));
        record.put("buddyname", Build.MODEL);
        record.put("available", "visible");

        // Service information
        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE, record);

        // Add the local service
        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
                // Command successful. Code not needed here
                Log.d("Add", "Added local service");
                Toast.makeText(getApplication(), "Added local service", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i)
            {
                // Command failed
                Log.d("Add", "Failed to add local service");
                Toast.makeText(getApplication(), "Adding local service failed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void peerDiscovery()
    {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int error) {
            }
        });
        mServiceBroadcastingHandler
                .postDelayed(mServiceBroadcastingRunnable, SERVICE_BROADCASTING_INTERVAL);
    }

    // Force rebroadcast of service information
    private Runnable mServiceBroadcastingRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("Thread", "Broadcasting peers");
            peerDiscovery();
        }
    };

    private void setupService()
    {
        WifiP2pManager.DnsSdTxtRecordListener txtRecordListener = new WifiP2pManager.DnsSdTxtRecordListener()
        {
            @Override
            /* Callback includes:
             * fullDomain: Full domain name: e.g "printer._ipp._tcp.local".
             * record: TXT record data as a map of key/value pairs.
             * device: The device running the advertised service.
             */
            public void onDnsSdTxtRecordAvailable(String s, Map<String, String> map, WifiP2pDevice device)
            {
                Log.d(TAG, "DnsSdTxtRecord available = " + map.toString());
                buddies.put(device.deviceAddress, map.get("buddyname"));
            }
        };

        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener()
        {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice device)
            {
                if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE))
                {
                    // Update the device name with the human-friendly version from
                    // the DnsTxtRecord, assuming one arrived
                    device.deviceName = buddies.containsKey(device.deviceAddress) ? buddies.get(device.deviceAddress) : device.deviceName;

                    // Update the UI and add the discovered device
                    WiFiServicesList fragment = (WiFiServicesList) getFragmentManager().findFragmentByTag("services");
                    if (fragment != null)
                    {
                        WiFiServicesList.WiFiDevicesAdapter adapter =
                                ((WiFiServicesList.WiFiDevicesAdapter) fragment.getListAdapter());
                        WiFiP2pService service = new WiFiP2pService();
                        service.device = device;
                        service.instanceName = instanceName;
                        service.serviceRegistrationType = registrationType;
                        progressBar.setVisibility(View.INVISIBLE);
                        adapter.add(service);
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "Service available " + instanceName);
                        Toast.makeText(getApplication(), "Service available " + instanceName, Toast.LENGTH_SHORT).show();
                    } else
                    {
                        Log.d("TAG", "fragment is null");
                    }
                }
            }
        };

        mManager.setDnsSdResponseListeners(mChannel, servListener, txtRecordListener);
    }

    private void startDiscovery()
    {
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.removeServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
                Log.d(TAG, "Removed service request");
                mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener()
                {
                    @Override
                    public void onSuccess()
                    {
                        // Success
                        Log.d(TAG, "Added service discovery request");
                        Toast.makeText(getApplication(), "Added service discovery request", Toast.LENGTH_SHORT).show();
                        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener()
                        {
                            @Override
                            public void onSuccess()
                            {
                                // Success
                                Log.d(TAG, "Service discovery initiated");
                                Toast.makeText(getApplication(), "Service discovery initiated", Toast.LENGTH_SHORT).show();
                                // Display progress bar(circle) while waiting for broadcast receiver
                                progressBar.setVisibility(View.VISIBLE);
                                mServiceDiscoveringHandler
                                        .postDelayed(mServiceDiscoveringRunnable, SERVICE_DISCOVERING_INTERVAL);
                            }

                            @Override
                            public void onFailure(int i)
                            {
                                // Command failed
                                if (i == WifiP2pManager.P2P_UNSUPPORTED)
                                {
                                    Log.d(TAG, "P2P isn't supported on this device.");
                                    Toast.makeText(getApplication(), "P2P not supported", Toast.LENGTH_SHORT).show();
                                } else if (i == WifiP2pManager.BUSY)
                                {
                                    Log.d(TAG, "System is busy");
                                } else if (i == WifiP2pManager.ERROR)
                                {
                                    // soooo helpful...
                                    Log.d(TAG, "There was an error");
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(int i)
                    {
                        // Command failed
                        Log.d(TAG, "Failed adding service discovery request");
                        Toast.makeText(getApplication(), "Failed adding service discovery request", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(int i)
            {
                Log.d(TAG, "Failed to remove service");
            }
        });
    }

    private Runnable mServiceDiscoveringRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            startDiscovery();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        } else if (id == R.id.reset_list)
        {
            reset();
            TextView tv = (TextView) findViewById(R.id.test);
            tv.setText("Label");
            Log.d(TAG, "Reset textview");
        }

        return super.onOptionsItemSelected(item);
    }

    private void reset()
    {
        // clear list of available devices
        WiFiServicesList fragment = (WiFiServicesList) getFragmentManager().findFragmentByTag("services");
        if (fragment != null)
        {
            WiFiServicesList.WiFiDevicesAdapter adapter =
                    ((WiFiServicesList.WiFiDevicesAdapter) fragment.getListAdapter());
            adapter.clear();
            adapter.notifyDataSetChanged();
            Log.d(TAG, "Reset listFragment");
        }
    }

    public void setLabel(String str)
    {
        TextView tv = (TextView) findViewById(R.id.test);
        tv.setText(str);
    }

    public void toasts(String str)
    {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    // Stop these threads from running after they are no longer needed
    public void stopServices()
    {
        Log.i(TAG, "Removing service callbacks");
        mServiceDiscoveringHandler.removeCallbacks(mServiceDiscoveringRunnable);
        mServiceBroadcastingHandler.removeCallbacks(mServiceBroadcastingRunnable);
    }
}
