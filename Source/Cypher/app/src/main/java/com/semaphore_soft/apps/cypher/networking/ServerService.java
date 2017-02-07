package com.semaphore_soft.apps.cypher.networking;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Evan on 2/6/2017.
 * Server service
 */

public class ServerService extends IntentService
{
    private Server serverThread = new Server();

    private static final String TAG = "ServerService";

    public ServerService()
    {
        super("Cypher_server");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // Gets data from the incoming intent
        String dataString = intent.getDataString();
        if (dataString.equals(NetworkConstants.SETUP_SERVER))
        {
            Log.d(TAG, "Starting AcceptorThread");
            serverThread.startAcceptor();
        }
        else if (dataString.equals(NetworkConstants.WRITE_TO_CLIENT))
        {
            Log.d(TAG, "Writing to single client");
            serverThread.writeToClient(intent.getStringExtra(NetworkConstants.MSG_EXTRA),
                                       intent.getIntExtra(NetworkConstants.INDEX_EXTRA, -1));
        }
        else if (dataString.equals(NetworkConstants.WRITE_ALL))
        {
            Log.d(TAG, "Writing to all clients");
            serverThread.writeAll(intent.getStringExtra(NetworkConstants.MSG_EXTRA));
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
    }
}
