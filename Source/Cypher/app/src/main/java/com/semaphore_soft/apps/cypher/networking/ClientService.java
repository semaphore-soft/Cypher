package com.semaphore_soft.apps.cypher.networking;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Evan on 2/6/2017.
 * Client service
 */

public class ClientService extends Service
{
    private Client client;

    private static final String TAG = "ClientService";

    public ClientService()
    {
        super();
    }

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder
    {
        public ClientService getService()
        {
            // Return this instance of ServerService so clients can call public methods
            return ClientService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @Override
    public void onCreate()
    {
        client = new Client();
    }

    public void startClient(String addr)
    {
        Log.d(TAG, "Staring client thread");
        try
        {
            client.startClient(InetAddress.getByName(addr), this);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            Log.e(TAG, "Could not resolve host");
            Intent localIntent = new Intent(NetworkConstants.BROADCAST_ERROR).putExtra(
                NetworkConstants.MESSAGE,
                NetworkConstants.ERROR_CLIENT_SOCKET);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }

    public void clientWrite(String msg)
    {
        Client.ClientThread clientThread = client.getClientThread();
        if (clientThread != null)
        {
            Log.d(TAG, "Writing to server");
            clientThread.write(msg);
        }
        else
        {
            Log.d(TAG, "clientThread null");
        }
    }

    public void reconnect()
    {
        Client.ClientThread clientThread = client.getClientThread();
        if (clientThread != null)
        {
            Log.d(TAG, "reconnecting...");
            clientThread.reconnectSocket();
        }
        else
        {
            Log.d(TAG, "clientThread null");
        }
    }

    public void threadRead(String msg)
    {
        Log.d(TAG, "Sending thread read");
        Intent localIntent = new Intent(NetworkConstants.BROADCAST_MESSAGE)
            .putExtra(NetworkConstants.MESSAGE, msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    public void threadUpdate(String msg)
    {
        Log.d(TAG, "Sending thread update");
        Intent localIntent = new Intent(NetworkConstants.BROADCAST_STATUS)
            .putExtra(NetworkConstants.MESSAGE, msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    public void threadError(String msg)
    {
        Log.d(TAG, "Sending thread error");
        Intent localIntent = new Intent(NetworkConstants.BROADCAST_ERROR).putExtra(
            NetworkConstants.MESSAGE, msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
