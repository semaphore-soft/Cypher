package com.semaphore_soft.apps.cypher;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements WiFiServicesList.DeviceClickListener
{

    private final static String TAG = "Main";
    // TXT RECORD properties
    public final static String SERVICE_INSTANCE = "_cypher";
    public final static String SERVICE_REG_TYPE = "_presence._tcp";

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver = null;
    private IntentFilter mIntentFiler = new IntentFilter();
    private WifiP2pDnsSdServiceRequest serviceRequest;

    //    private ProgressDialog progress;
    public ProgressBar progressBar;
    private int hostWillingness;
    private final int SERVER_PORT = 58008;
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
                discoverService();
            }
        });

        Button hostGame = (Button) findViewById(R.id.host);
        hostGame.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                hostWillingness = 15;
                discoverService();
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
                Log.d("clear", "Cleared local services");
                startRegistration();
            }

            @Override
            public void onFailure(int i)
            {
                Log.d("clear", "Failed to clear local services");
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
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        disconnect();
        super.onStop();
    }

    @Override
    protected void onRestart()
    {
        Fragment frag = getFragmentManager().findFragmentByTag("services");
        if (frag != null)
        {
            getFragmentManager().beginTransaction().remove(frag).commit();
        }
        super.onRestart();
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
                //Toast.makeText(getApplication(), "Failed to disconnect", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Error removing group. Error: " + i);
                // This should cancel service discovery
                // Could also call after successful connection
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

    public void connectP2p(WiFiP2pService service)
    {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = service.device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = hostWillingness;
        if (serviceRequest != null)
        {
            mManager.removeServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener()
            {
                @Override
                public void onSuccess()
                {

                }

                @Override
                public void onFailure(int i)
                {

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
        record.put("buddyname", "NetworkTest" + (int) (Math.random() * 1000));
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
                Log.d("add", "Added local service");
                Toast.makeText(getApplication(), "Added local service", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i)
            {
                // Command failed
                Log.d("add", "Failed to add local service");
                Toast.makeText(getApplication(), "Adding local service failed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void discoverService()
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
                        Toast.makeText(getApplication(), "Service fragment is null", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        mManager.setDnsSdResponseListeners(mChannel, servListener, txtRecordListener);

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
                // Success
                Log.d(TAG, "Added service discovery request");
                Toast.makeText(getApplication(), "Added service discovery request", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i)
            {
                // Command failed
                Log.d(TAG, "Failed adding service discovery request");
                Toast.makeText(getApplication(), "Failed adding service discovery request", Toast.LENGTH_SHORT).show();
            }
        });

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
                // Success
                Log.d(TAG, "Service discovery initiated");
                Toast.makeText(getApplication(), "Service discovery initiated", Toast.LENGTH_SHORT).show();
                // Display progress bar(circle) while waiting for broadcast receiver
                /*progress.setIndeterminate(true);
                progress.setTitle("Looking for players");
                progress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        mManager.stopPeerDiscovery(mChannel, null);
                        Log.d(TAG, "Stopping discovery?");
                    }
                });
                progress.show();*/
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int i)
            {
                // Command failed
                if (i == WifiP2pManager.P2P_UNSUPPORTED)
                {
                    Log.d(TAG, "P2P isn't supported on this device.");
                } else if (i == WifiP2pManager.BUSY)
                {
                    Log.d(TAG, "System is busy");
                } else if (i == WifiP2pManager.ERROR)
                {
                    Log.d(TAG, "There was an error"); // soooo helpful...
                }
            }
        });
    }

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

        return super.onOptionsItemSelected(item);
    }
}
