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

    public static final String SETUP_CLIENT  = "CLIENT_SETUP";
    public static final String CLIENT_WRITE  = "CLIENT_WRITE";
    public static final String THREAD_READ   = "THREAD_READ";
    public static final String THREAD_UPDATE = "THREAD_UPDATE";

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
        if (dataString.equals(SETUP_CLIENT))
        {
            try
            {
                clientThread = client.startClient(
                        InetAddress.getByName(intent.getStringExtra("address")));
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
                // TODO sent intent for error?
            }
        }
        else if (dataString.equals(CLIENT_WRITE))
        {
            if (clientThread != null)
            {
                clientThread.write(intent.getStringExtra("message"));
            }
        }
        else if (dataString.equals(THREAD_READ))
        {
            String msg         = intent.getStringExtra("message");
            Intent localIntent = new Intent(BROADCAST_MESSAGE).putExtra(MESSAGE, msg);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
        else if (dataString.equals(THREAD_UPDATE))
        {
            String msg         = intent.getStringExtra("message");
            Intent localIntent = new Intent(BROADCAST_STATUS).putExtra(MESSAGE, msg);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }
}
