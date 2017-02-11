package com.semaphore_soft.apps.cypher.networking;

import android.content.IntentFilter;

/**
 * Created by Evan on 2/6/2017.
 * Class to hold networking specific constants
 */

public class NetworkConstants
{
    // Custom Intent actions
    public static final String BROADCAST_MESSAGE =
        "com.semaphore_soft.apps.cypher.networking.BROADCAST";
    public static final String BROADCAST_STATUS  =
        "com.semaphore_soft.apps.cypher.networking.STATUS";
    public static final String BROADCAST_ERROR   =
        "com.semaphore_soft.apps.cypher.networking.ERROR";

    // Constant for message extra used by broadcast receiver
    public static final String MESSAGE = "com.semaphore_soft.apps.cypher.MESSAGE";

    // Constants for client and server services - used like methods by activities
    public static final String SETUP_SERVER    = "SERVER_SETUP";
    public static final String SETUP_CLIENT    = "CLIENT_SETUP";
    public static final String WRITE_TO_CLIENT = "SINGLE_WRITE";
    public static final String WRITE_ALL       = "WRITE_ALL";
    public static final String CLIENT_WRITE    = "CLIENT_WRITE";
    public static final String THREAD_READ     = "THREAD_READ";
    public static final String THREAD_UPDATE   = "THREAD_UPDATE";
    public static final String THREAD_ERROR    = "THREAD_ERROR";

    // Constants for intent extras - used like parameters by activities
    public static final String MSG_EXTRA   = "message";
    public static final String INDEX_EXTRA = "index";
    public static final String ADDR_EXTRA  = "address";

    // Constants for read codes
    // Constants for game status updates
    public static final String GAME_START = "GAME_START";
    // Constants to use as prefixes to exchange information with other devices
    public static final String PF_NAME   = "NAME:";
    public static final String PF_PLAYER = "PLAYER:";

    // Constants for status codes
    public static final String STATUS_SERVER_START = "Server thread started";

    // Constants for error codes
    public static final String ERROR_CLIENT_SOCKET = "Failed to start socket";

    // Port should be between 49152-65535
    public final static int SERVER_PORT = 58008;


    public static IntentFilter getFilter()
    {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BROADCAST_MESSAGE);
        intentFilter.addAction(BROADCAST_STATUS);
        intentFilter.addAction(BROADCAST_ERROR);

        return intentFilter;
    }
}
