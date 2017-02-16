package com.semaphore_soft.apps.cypher.networking;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Evan on 2/6/2017.
 * Server service
 */

public class ServerService extends Service
{
    private Server serverThread;

    private static final String TAG = "ServerService";

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

    public void startServer()
    {
        Log.d(TAG, "Starting AcceptorThread");
        serverThread.startAcceptor(this);
    }

    public void writeToClient(String msg, int client)
    {
        Log.d(TAG, "Writing to single client");
        serverThread.writeToClient(msg, client);
    }

    public void writeAll(String msg)
    {
        Log.d(TAG, "Writing to all clients");
        serverThread.writeAll(msg);
    }

    public void reconnect()
    {
        Log.d(TAG, "reconnecting...");
        serverThread.reconnectClient();
    }

    public void threadRead(String msg)
    {
        Log.d(TAG, "Sending thread read");
        Intent localIntent = new Intent(NetworkConstants.BROADCAST_MESSAGE)
            .putExtra(NetworkConstants.MESSAGE, msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    public void threadUpdate(String msg)
    {
        Log.d(TAG, "Sending thread update");
        Intent localIntent = new Intent(NetworkConstants.BROADCAST_STATUS)
            .putExtra(NetworkConstants.MESSAGE, msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    public void threadError(String msg)
    {
        Log.d(TAG, "Sending thread error");
        Intent localIntent = new Intent(NetworkConstants.BROADCAST_ERROR).putExtra(
            NetworkConstants.MESSAGE,
            msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
