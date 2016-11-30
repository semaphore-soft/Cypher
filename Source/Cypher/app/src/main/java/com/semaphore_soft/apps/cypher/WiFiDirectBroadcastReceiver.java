package com.semaphore_soft.apps.cypher;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * Created by Evan on 9/24/2016.
 * Broadcast Receiver for Wifi P2P connections
 */

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver
{

    private final WifiP2pManager mManager;
    private final WifiP2pManager.Channel mChannel;
    private final MainActivity mActivity;
    //    private final List<WifiP2pDevice> peers = new ArrayList<>();
    private boolean connecting = false;
    private AlertDialog alertDialog = null;

    private final static String TAG = "WifiBR";

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       MainActivity activity)
    {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    private final WifiP2pManager.ConnectionInfoListener connectionListener =
            new WifiP2pManager.ConnectionInfoListener()
            {
                @Override
                public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo)
                {
                    // InetAddress from WifiP2pInfo struct
                    InetAddress groupOwnerAddress;
                    try
                    {
                        groupOwnerAddress = InetAddress.getByName(wifiP2pInfo.groupOwnerAddress.getHostAddress());
                        // After group negotiation, we can determine the group owner
                        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner)
                        {
                            // Tasks specific to group owner
                            // ex. Create server thread and listen for incoming connections
                            Log.d(TAG, "Device is group owner");
                            Toast.makeText(mActivity, "You're the group owner!", Toast.LENGTH_SHORT).show();
                            new Thread(new ServerThread()).start();
                        } else if (wifiP2pInfo.groupFormed)
                        {
                            // Device acts as client
                            // Create client thread that connects to group owner
                            Log.d(TAG, "Device is in group");
                            Toast.makeText(mActivity, "You're in a group!", Toast.LENGTH_SHORT).show();
                            new Thread(new ClientThread(new InetSocketAddress(groupOwnerAddress, MainActivity.SERVER_PORT), groupOwnerAddress)).start();
                        }
                    } catch (UnknownHostException e)
                    {
                        Log.e(TAG, "Host IP: " + wifiP2pInfo.groupOwnerAddress.getHostAddress());
                        e.printStackTrace();
                    }
                }
            };

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action))
        {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
            {
                // Wifi P2P is enabled
                Log.d(TAG, "P2P enabled");
            } else
            {
                // Wifi P2P is not enabled
                Log.d(TAG, "P2P disabled");
                // Check if wifi is disabled and enable it if so
                WifiManager wifi = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
                if (!wifi.isWifiEnabled())
                {
                    wifi.setWifiEnabled(true);
                    //Toast.makeText(mActivity, "Enabled wifi", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Enabled wifi");
                    return;
                }

                // Display an error if wifi P2P is not supported
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle("Wifi P2P disabled");
                builder.setMessage("This device does not support Wifi P2P.\nThe application will now exit.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        // Close dialog first (avoids error on close), then exit activity
                        dialogInterface.dismiss();
                        mActivity.finish();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {
            // Respond to new connection or disconnections
            Log.d(TAG, "Connection changed");

            if (mManager == null)
            {
                return;
            }

            NetworkInfo networkInfo = intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected())
            {
                // We are connected with another device, request connection
                // info to find group owner IP
                Log.d(TAG, "Connected");
                Toast.makeText(mActivity, "Connected!", Toast.LENGTH_SHORT).show();
                mManager.requestConnectionInfo(mChannel, connectionListener);
                mActivity.progressBar.setVisibility(View.INVISIBLE);
                // Close connection dialog if we already have a connection
                if (alertDialog != null)
                {
                    alertDialog.dismiss();
                    alertDialog = null;
                }
            } else
            {
                // It's a disconnect (maybe, or just never connected)
                if (connecting)
                {
                    Log.d(TAG, "disconnected");
                    connecting = false;
                    Toast.makeText(mActivity, "You have disconnected", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action))
        {
            // Respond to this device's wifi state changing
            Log.d(TAG, "This device's state changed");
        }
    }

    private Handler handler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            mActivity.setLabel(msg.getData().getString("msg"));
            return true;
        }
    });
    public void mkmsg(String str)
    {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("msg", str);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    // Handler to get toasts for debugging
    private Handler tHandler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            mActivity.toasts(msg.getData().getString("msg"));
            return true;
        }
    });
    public void makeToast(String str)
    {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("msg", str);
        msg.setData(b);
        tHandler.sendMessage(msg);
    }

    private class ServerThread extends Thread
    {
        // The local server socket
        private ServerSocket serverSocket = null;
        private Socket my_socket;
        public ServerThread()
        {
            try
            {
                serverSocket = new ServerSocket(MainActivity.SERVER_PORT);

            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.e("ServerThread", "Failed to start server");
                Toast.makeText(mActivity, "Failed to start server", Toast.LENGTH_SHORT).show();
            }
        }

        public void run()
        {
            Log.i("ServerConnect", "Waiting on accept");
//            Toast.makeText(mActivity, "Waiting on accept", Toast.LENGTH_SHORT).show();
            makeToast("Waiting on accept");
            my_socket = null;
            try
            {
                my_socket = serverSocket.accept();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.e("ServerThread", "Failed to accept connection");
//                Toast.makeText(mActivity, "Failed to accept connection", Toast.LENGTH_SHORT).show();
                makeToast("Failed to accept connection");
            }
            if (my_socket != null)
            {
                Log.i("ServerConnect", "Connection made");
//                Toast.makeText(mActivity, "Connection made", Toast.LENGTH_SHORT).show();
                makeToast("Connection made");
                try
                {
                    ObjectOutputStream out = new ObjectOutputStream(my_socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream((my_socket.getInputStream()));
                    out.writeChars("Hello, World!");
                    mkmsg(in.readUTF());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ClientThread extends Thread
    {
        private Socket my_socket = null;
        private SocketAddress socketAddress;
        public ClientThread(SocketAddress socketAddress, InetAddress address)
        {
            this.socketAddress = socketAddress;
            try
            {
                // creates and connects to address at specified port
                my_socket = new Socket(address, MainActivity.SERVER_PORT);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.e("ClientThread", "Failed to start socket");
//                Toast.makeText(mActivity, "Failed to start socket", Toast.LENGTH_SHORT).show();
                makeToast("Failed to start socket");
            }
        }

        public void run()
        {
//            try
//            {
//                my_socket.connect(socketAddress);
//            }
//            catch (IOException e)
//            {
//                e.printStackTrace();
//                Log.e("ClientThread", "Connect failed");
////                Toast.makeText(mActivity, "Connect failed", Toast.LENGTH_SHORT).show();
//                makeToast("Connect failed");
//                try {
//                    my_socket.close();
//                    my_socket = null;
//                } catch (IOException e2) {
//                    Log.e("ClientThread", "unable to close() socket during connection failure: " +
//                            e2.getMessage());
//                    my_socket = null;
//                }
//            }

            // Connection was accepted
            if (my_socket != null)
            {
                Log.i("ClientThread", "Connection made");
                try
                {
                    ObjectOutputStream out = new ObjectOutputStream(my_socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(my_socket.getInputStream());
                    mkmsg(in.readUTF());
                    out.writeChars("Hello, World!");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

    }
}


