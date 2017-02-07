package com.semaphore_soft.apps.cypher.networking;

import android.content.IntentFilter;

/**
 * Created by Evan on 2/6/2017.
 * Class to hold networking specific constants
 */

public class NetworkConstants
{
    // Custom Intent action
    public static final String BROADCAST_MESSAGE =
        "com.semaphore_soft.apps.cypher.networking.BROADCAST";
    public static final String BROADCAST_STATUS  =
        "com.semaphore_soft.apps.cypher.networking.STATUS";

    // Defines the key for the status "extra" in an Intent
    public static final String MESSAGE = "com.semaphore_soft.apps.cypher.MESSAGE";

    public static final String SETUP_SERVER    = "SERVER_SETUP";
    public static final String SETUP_CLIENT    = "CLIENT_SETUP";
    public static final String WRITE_TO_CLIENT = "SINGLE_WRITE";
    public static final String WRITE_ALL       = "WRITE_ALL";
    public static final String CLIENT_WRITE    = "CLIENT_WRITE";
    public static final String THREAD_READ     = "THREAD_READ";
    public static final String THREAD_UPDATE   = "THREAD_UPDATE";

    public static final String MSG_EXTRA   = "message";
    public static final String INDEX_EXTRA = "index";
    public static final String ADDR_EXTRA  = "address";


    public static IntentFilter getFilter()
    {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BROADCAST_MESSAGE);
        intentFilter.addAction(BROADCAST_STATUS);

        return intentFilter;
    }
}
