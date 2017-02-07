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
        if (dataString.equals(NetworkConstants.SETUP_SERVER))
        {
            serverThread.startAcceptor();
        }
        else if (dataString.equals(NetworkConstants.WRITE_TO_CLIENT))
        {
            serverThread.writeToClient(intent.getStringExtra(NetworkConstants.MSG_EXTRA),
                                       intent.getIntExtra(NetworkConstants.INDEX_EXTRA, -1));
        }
        else if (dataString.equals(NetworkConstants.WRITE_ALL))
        {
            serverThread.writeAll(intent.getStringExtra(NetworkConstants.MSG_EXTRA));
        }
        else if (dataString.equals(NetworkConstants.THREAD_READ))
        {
            String msg         = intent.getStringExtra(NetworkConstants.MSG_EXTRA);
            Intent localIntent = new Intent(NetworkConstants.BROADCAST_MESSAGE)
                    .putExtra(NetworkConstants.MESSAGE, msg);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
        else if (dataString.equals(NetworkConstants.THREAD_UPDATE))
        {
            String msg         = intent.getStringExtra(NetworkConstants.MSG_EXTRA);
            Intent localIntent = new Intent(NetworkConstants.BROADCAST_STATUS)
                    .putExtra(NetworkConstants.MESSAGE, msg);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }
}
