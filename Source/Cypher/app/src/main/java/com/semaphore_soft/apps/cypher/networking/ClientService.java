package com.semaphore_soft.apps.cypher.networking;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Evan on 2/6/2017.
 * Client service
 */

public class ClientService extends IntentService
{
    private Client              client       = new Client();

    private static final String TAG = "ClientService";

    public ClientService()
    {
        super("Cypher_client");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // Gets data from the incoming intent
        String dataString = intent.getDataString();
        if (dataString.equals(NetworkConstants.SETUP_CLIENT))
        {
            Log.d(TAG, "Staring client thread");
            try
            {
                client.startClient(InetAddress.getByName(intent.getStringExtra(NetworkConstants.ADDR_EXTRA)));
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
                // TODO sent intent for error?
            }
        }
        else if (dataString.equals(NetworkConstants.CLIENT_WRITE))
        {
            Client.ClientThread clientThread = client.getClientThread();
            if (clientThread != null)
            {
                Log.d(TAG, "Writing to server");
                clientThread.write(intent.getStringExtra(NetworkConstants.MSG_EXTRA));
            }
            else
            {
                Log.d(TAG, "clientThread null");
            }
        }
        else if (dataString.equals(NetworkConstants.THREAD_READ))
        {
            Log.d(TAG, "Sending thread read");
            String msg = intent.getStringExtra(NetworkConstants.MSG_EXTRA);
            Intent localIntent = new Intent(NetworkConstants.BROADCAST_MESSAGE)
                .putExtra(NetworkConstants.MESSAGE, msg);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
        else if (dataString.equals(NetworkConstants.THREAD_UPDATE))
        {
            Log.d(TAG, "Sending thread update");
            String msg = intent.getStringExtra(NetworkConstants.MSG_EXTRA);
            Intent localIntent = new Intent(NetworkConstants.BROADCAST_STATUS)
                .putExtra(NetworkConstants.MESSAGE, msg);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
        else if (dataString.equals(NetworkConstants.THREAD_ERROR))
        {
            Log.d(TAG, "Sending thread error");
            String msg = intent.getStringExtra(NetworkConstants.MSG_EXTRA);
            Intent localIntent = new Intent(NetworkConstants.BROADCAST_ERROR).putExtra(
                NetworkConstants.MESSAGE,
                msg);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }
}
