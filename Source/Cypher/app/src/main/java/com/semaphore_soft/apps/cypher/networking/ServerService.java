package com.semaphore_soft.apps.cypher.networking;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.semaphore_soft.apps.cypher.utils.Logger;

/**
 * Created by Evan on 2/6/2017.
 * Server service
 */

public class ServerService extends Service
{
    private Server serverThread;

    public ServerService()
    {
        super();
    }

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder
    {
        public ServerService getService()
        {
            // Return this instance of ServerService so clients can call public methods
            return ServerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @Override
    public void onCreate()
    {
        serverThread = new Server();
    }

    /**
     * Starts server listening for connecting clients.
     *
     * @see Server#startAcceptor(ServerService)
     */
    public void startServer()
    {
        Logger.logD("Starting AcceptorThread");
        serverThread.startAcceptor(this);
    }

    /**
     * Writes a message to a specific client.
     *
     * @param msg Message to write.
     * @param client Client to write to.
     *
     * @see Server#writeToClient(String, int)
     */
    public void writeToClient(String msg, int client)
    {
        Logger.logD("Writing to single client");
        serverThread.writeToClient(msg, client);
    }

    /**
     * Writes a message to all connected clients.
     *
     * @param msg Message to write.
     *
     * @see Server#writeAll(String)
     */
    public void writeAll(String msg)
    {
        Logger.logD("Writing to all clients");
        serverThread.writeAll(msg);
    }

    /**
     * Sends an intent for {@link ResponseReceiver} to signal that data has been read from the network.
     *
     * @param msg Message that was read from the network.
     * @param readFrom Client that the message was received from.
     *                 This is in relation to an array of connected clients
     *                 and is not related to playerID.
     */
    public void threadRead(String msg, int readFrom)
    {
        Logger.logD("Sending thread read");
        Intent localIntent = new Intent(NetworkConstants.BROADCAST_MESSAGE)
            .putExtra(NetworkConstants.MESSAGE, msg)
            .putExtra(NetworkConstants.INDEX, readFrom);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /**
     * Sends an intent for {@link ResponseReceiver} to signal that the thread's status has changed.
     *
     * @param msg Message that was sent from thread.
     * @param readFrom Client that the message was received from.
     *                 This is in relation to an array of connected clients
     *                 and is not related to playerID
     */
    public void threadUpdate(String msg, int readFrom)
    {
        Logger.logD("Sending thread update");
        Intent localIntent = new Intent(NetworkConstants.BROADCAST_STATUS)
            .putExtra(NetworkConstants.MESSAGE, msg)
            .putExtra(NetworkConstants.INDEX, readFrom);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /**
     * Sends an intent for {@link ResponseReceiver} to signal that an error has occurred in the thread.
     *
     * @param msg Error message from the thread.
     * @param readFrom Client that the message was received from.
     *                 This is in relation to an array of connected clients
     *                 and is not related to playerID.
     */
    public void threadError(String msg, int readFrom)
    {
        Logger.logD("Sending thread error");
        Intent localIntent = new Intent(NetworkConstants.BROADCAST_ERROR)
            .putExtra(NetworkConstants.MESSAGE, msg)
            .putExtra(NetworkConstants.INDEX, readFrom);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
