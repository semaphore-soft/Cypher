package com.semaphore_soft.apps.cypher.networking;

import android.content.IntentFilter;

/**
 * Class to hold networking specific constants
 *
 * @author Evan
 * @see ResponseReceiver
 * @see ServerService
 * @see ClientService
 * @see Client
 * @see Server
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
    public static final String GAME_START                       = "GAME_START";
    public static final String GAME_UNREADY                     = "GAME_UNREADY";
    public static final String GAME_AR_START                    = "GAME_AR_START";
    public static final String GAME_UPDATE                      = "GAME_UPDATE";
    public static final String GAME_HEARTBEAT                   = "GAME_HEARTBEAT";
    public static final String GAME_KNIGHT                      = "knight";
    public static final String GAME_SOLDIER                     = "soldier";
    public static final String GAME_RANGER                      = "ranger";
    public static final String GAME_WIZARD                      = "wizard";
    public static final String GAME_TAKEN                       = "GAME_TAKEN";
    public static final String GAME_WAIT                        = "GAME_WAIT";
    public static final String GAME_TURN                        = "GAME_TURN";
    public static final String GAME_TURN_OVER                   = "TURN_OVER";
    // Constants to use as prefixes to exchange information with other devices
    public static final String PREFIX_NAME                      = "NAME:";
    public static final String PREFIX_PLAYER                    = "PLAYER:";
    public static final String PREFIX_LOCK                      = "LOCK:";
    public static final String PREFIX_FREE                      = "FREE:";
    public static final String PREFIX_READY                     = "READY:";
    public static final String PREFIX_MARK_REQUEST              = "MARK_REQUEST:";
    public static final String PREFIX_ASSIGN_MARK               = "ASSIGN_MARK:";
    public static final String PREFIX_ASSIGN_ROOM_MARK          = "ROOM_MARK:";
    public static final String PREFIX_RESERVE_PLAYER            = "RESERVE_PLAYER:";
    public static final String PREFIX_RESERVE_ROOM_MARKER       = "RESERVE_ROOM:";
    public static final String PREFIX_PLACE_ROOM                = "PLACE_ROOM:";
    public static final String PREFIX_ATTACH                    = "ATTACH:";
    public static final String PREFIX_CREATE_ROOM               = "CREATE_ROOM:";
    public static final String PREFIX_MOVE_REQUEST              = "MOVE_REQUEST:";
    public static final String PREFIX_GENERATE_ROOM_REQUEST     = "GENERATE_ROOM_REQUEST:";
    public static final String PREFIX_OPEN_DOOR_REQUEST         = "OPEN_DOOR_REQUEST:";
    public static final String PREFIX_ACTION_REQUEST            = "ACTION_REQUEST;";
    // Use a different delimiter, because of how residents are stored
    public static final String PREFIX_UPDATE_ROOM_RESIDENTS     = "UPDATE_ROOM_RESIDENTS~";
    public static final String PREFIX_SHOW_ACTION               = "SHOW_ACTION:";
    public static final String PREFIX_UPDATE_ROOM_WALLS         = "UPDATE_ROOM_WALLS:";
    public static final String PREFIX_UPDATE_ROOM_ALIGNMENT     = "UPDATE_ROOM_ALIGNMENT:";
    public static final String PREFIX_UPDATE_NON_PLAYER_TARGETS = "UPDATE_NON_PLAYER_TARGETS:";
    public static final String PREFIX_UPDATE_PLAYER_TARGETS     = "UPDATE_PLAYER_TARGETS:";
    public static final String PREFIX_UPDATE_PLAYER_SPECIALS    = "UPDATE_PLAYER_SPECIALS:";

    // Constants for status codes
    public static final String STATUS_SERVER_START   = "Server thread started";
    public static final String STATUS_CLIENT_CONNECT = "Connection made";
    public static final String STATUS_SERVER_WAIT    = "Waiting on accept";

    // Constants for error codes
    public static final String ERROR_CLIENT_SOCKET     = "Failed to start socket";
    public static final String ERROR_SERVER_START      = "Failed to start server";
    public static final String ERROR_WRITE             = "Error writing to socket";
    public static final String ERROR_DISCONNECT_CLIENT = "Socket has been disconnected";
    public static final String ERROR_DISCONNECT_SERVER = "Client has been disconnected";

    // Port should be between 49152-65535
    public static final int SERVER_PORT     = 58008;
    public static final int HEARTBEAT_DELAY = 5000;


    /**
     * Method to get {@link IntentFilter IntentFilters} for registering a broadcast receiver
     *
     * @return An IntentFilter for ResponseReceiver
     *
     * @see ResponseReceiver
     */
    public static IntentFilter getFilter()
    {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BROADCAST_MESSAGE);
        intentFilter.addAction(BROADCAST_STATUS);
        intentFilter.addAction(BROADCAST_ERROR);

        return intentFilter;
    }
}
