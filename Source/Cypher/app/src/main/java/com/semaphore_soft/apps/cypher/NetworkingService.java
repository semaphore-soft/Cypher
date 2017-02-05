package com.semaphore_soft.apps.cypher;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Evan on 2/5/2017.
 */

public class NetworkingService extends IntentService
{
    // Custom Intent action
    public static final String BROADCAST_ACTION = "com.semaphore_soft.apps.cypher.BROADCAST";

    // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_DATA_STATUS = "com.semaphore_soft.apps.cypher.STATUS";

    public NetworkingService()
    {
        super("Cypher");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // Gets data from the incoming intent
        String dataString = intent.getDataString();
        String status = "Hello";
        //TODO
        Intent localIntent = new Intent(BROADCAST_ACTION).putExtra(EXTENDED_DATA_STATUS, status);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        if (dataString.equals("TEST"))
        {
            Log.i("Service", "TEST");
        }
    }
}
