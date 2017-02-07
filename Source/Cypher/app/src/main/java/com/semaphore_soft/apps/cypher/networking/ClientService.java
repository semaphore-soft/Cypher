package com.semaphore_soft.apps.cypher.networking;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Evan on 2/6/2017.
 * Client service
 */

public class ClientService extends IntentService
{
    // Custom Intent action
    public static final String BROADCAST_MESSAGE = "com.semaphore_soft.apps.cypher.networking.BROADCAST";
    public static final String BROADCAST_STATUS  = "com.semaphore_soft.apps.cypher.networking.STATUS";

    // Defines the key for the status "extra" in an Intent
    public static final String MESSAGE = "com.semaphore_soft.apps.cypher.MESSAGE";



    private Client              client       = new Client();
    private Client.ClientThread clientThread = null;

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
            try
            {
                clientThread = client.startClient(
                        InetAddress.getByName(intent.getStringExtra(NetworkConstants.ADDR_EXTRA)));
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
                // TODO sent intent for error?
            }
        }
        else if (dataString.equals(NetworkConstants.CLIENT_WRITE))
        {
            if (clientThread != null)
            {
                clientThread.write(intent.getStringExtra(NetworkConstants.MSG_EXTRA));
            }
        }
        else if (dataString.equals(NetworkConstants.THREAD_READ))
        {
            String msg         = intent.getStringExtra(NetworkConstants.MSG_EXTRA);
            Intent localIntent = new Intent(BROADCAST_MESSAGE).putExtra(MESSAGE, msg);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
        else if (dataString.equals(NetworkConstants.THREAD_UPDATE))
        {
            String msg         = intent.getStringExtra(NetworkConstants.MSG_EXTRA);
            Intent localIntent = new Intent(BROADCAST_STATUS).putExtra(MESSAGE, msg);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }
}
