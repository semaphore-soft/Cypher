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
    public static final String MESSAGE = "com.semaphore_soft.apps.cypher.networking.MESSAGE";
    public static final String INDEX   = "com.semaphore_soft.apps.cypher.networking.INDEX";

    // Constants for read codes
    // Constants for game status updates
    public static final String GAME_START     = "GAME_START";
    public static final String GAME_UNREADY   = "GAME_UNREADY";
    public static final String GAME_AR_START  = "GAME_AR_START";
    public static final String GAME_UPDATE    = "GAME_UPDATE";
    public static final String GAME_HEARTBEAT = "GAME_HEARTBEAT";
    public static final String GAME_KNIGHT    = "knight";
    public static final String GAME_SOLDIER   = "soldier";
    public static final String GAME_RANGER    = "ranger";
    public static final String GAME_WIZARD    = "wizard";
    public static final String GAME_TAKEN     = "GAME_TAKEN";
    // Constants to use as prefixes to exchange information with other devices
    public static final String PF_NAME        = "NAME:";
    public static final String PF_PLAYER      = "PLAYER:";
    public static final String PF_LOCK        = "LOCK:";
    public static final String PF_FREE        = "FREE:";
    public static final String PF_READY       = "READY:";

    // Constants for status codes
    public static final String STATUS_SERVER_START   = "Server thread started";
    public static final String STATUS_CLIENT_CONNECT = "Connection made";
    public static final String STATUS_SERVER_WAIT    = "Waiting on accept";

    // Constants for error codes
    public static final String ERROR_CLIENT_SOCKET     = "Failed to start socket";
    public static final String ERROR_SERVER_START      = "Failed to start server";
    public static final String ERROR_WRITE             = "Error writing to socket";
    public static final String ERROR_DISCONNECT_CLIENT = "Socket has been disconnected";
    public static final String ERROR_DISCONNECT_SERVER = "Client had been disconnected";

    // Port should be between 49152-65535
    public static final int SERVER_PORT     = 58008;
    public static final int HEARTBEAT_DELAY = 5000;


    public static IntentFilter getFilter()
    {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BROADCAST_MESSAGE);
        intentFilter.addAction(BROADCAST_STATUS);
        intentFilter.addAction(BROADCAST_ERROR);

        return intentFilter;
    }
}
