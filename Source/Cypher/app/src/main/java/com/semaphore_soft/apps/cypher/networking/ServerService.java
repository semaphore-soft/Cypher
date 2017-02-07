package com.semaphore_soft.apps.cypher.networking;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Evan on 2/6/2017.
 * Server service
 */

public class ServerService extends IntentService
{
    // Custom Intent action
    public static final String BROADCAST_MESSAGE = "com.semaphore_soft.apps.cypher.networking.BROADCAST";
    public static final String BROADCAST_STATUS  = "com.semaphore_soft.apps.cypher.networking.STATUS";

    // Defines the key for the status "extra" in an Intent
    public static final String MESSAGE = "com.semaphore_soft.apps.cypher.MESSAGE";

    public static final String SETUP_SERVER    = "SERVER_SETUP";
    public static final String WRITE_TO_CLIENT = "SINGLE_WRITE";
    public static final String WRITE_ALL       = "WRITE_ALL";
    public static final String THREAD_READ     = "THREAD_READ";
    public static final String THREAD_UPDATE   = "THREAD_UPDATE";

    private Server serverThread = new Server();

    public ServerService()
    {
        super("Cypher_server");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // Gets data from the incoming intent
        String dataString = intent.getDataString();
        if (dataString.equals(SETUP_SERVER))
        {
            serverThread.startAcceptor();
        }
        else if (dataString.equals(WRITE_TO_CLIENT))
        {
            serverThread.writeToClient(intent.getStringExtra("message"),
                                       intent.getIntExtra("index", -1));
        }
        else if (dataString.equals(WRITE_ALL))
        {
            serverThread.writeAll(intent.getStringExtra("message"));
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
