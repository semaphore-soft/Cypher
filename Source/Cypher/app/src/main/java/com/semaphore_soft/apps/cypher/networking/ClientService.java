package com.semaphore_soft.apps.cypher.networking;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.semaphore_soft.apps.cypher.utils.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Service to manage {@link com.semaphore_soft.apps.cypher.networking.Client.ClientThread ClientThread}
 * actions across activities
 *
 * @author Evan
 *
 * @see Client
 * @see com.semaphore_soft.apps.cypher.networking.Client.ClientThread
 */

public class ClientService extends Service
{
    private Client client;
    private Client.ClientThread clientThread = null;

    public ClientService()
    {
        super();
    }

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder
    {
        /**
         * Allow clients to call {@link ClientService} methods.
         *
         * @return An instance of {@link ClientService}
         */
        public ClientService getService()
        {
            // Return this instance of ClientService so clients can call public methods
            return ClientService.this;
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
        client = new Client();
    }

    /**
     * Starts the client thread and attempts to connect to {@code addr}.
     *
     * @param addr String representation of the IP address to connect to.
     *
     * @see Client#startClient(InetAddress, ClientService)
     */
    public void startClient(String addr)
    {
        Logger.logD("Staring client thread");
        try
        {
            clientThread = client.startClient(InetAddress.getByName(addr), this);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            Logger.logE("Could not resolve host");
            Intent localIntent = new Intent(NetworkConstants.BROADCAST_ERROR).putExtra(
                NetworkConstants.MESSAGE,
                NetworkConstants.ERROR_CLIENT_SOCKET);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }

    /**
     * Writes a message to the server.
     *
     * @param msg Message to write to server.
     *
     * @see com.semaphore_soft.apps.cypher.networking.Client.ClientThread#write(String)
     */
    public void write(String msg)
    {
        if (clientThread != null)
        {
            Logger.logD("Writing to server");
            clientThread.write(msg);
        }
        else
        {
            Logger.logD("clientThread null");
        }
    }

    /**
     * Returns the formatted IP of the host that the client is connected to.
     *
     * @return IP address of the host.
     */
    public String getHostIP()
    {
        // Format ip address from client thread
        String   str = clientThread.getSocketAddress();
        // Remove port from address
        String[] ip  = str.split(":");
        // Remove leading '/'
        return ip[0].substring(1);
    }

    /**
     * Sends an intent for ResponseReceiver to signal that data has been read from the network.
     *
     * @param msg Message that was read from the network.
     *
     * @see ResponseReceiver
     */
    public void threadRead(String msg)
    {
        Logger.logD("Sending thread read");
        Intent localIntent = new Intent(NetworkConstants.BROADCAST_MESSAGE)
            .putExtra(NetworkConstants.MESSAGE, msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /**
     * Sends an intent for ResponseReceiver to signal that the thread's status has changed.
     *
     * @param msg Message that was read from the network.
     *
     * @see ResponseReceiver
     */
    public void threadUpdate(String msg)
    {
        Logger.logD("Sending thread update");
        Intent localIntent = new Intent(NetworkConstants.BROADCAST_STATUS)
            .putExtra(NetworkConstants.MESSAGE, msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /**
     * Sends an intent for ResponseReceiver to signal that an error has occurred in the thread.
     *
     * @param msg Message that was read from the network.
     *
     * @see ResponseReceiver
     */
    public void threadError(String msg)
    {
        Logger.logD("Sending thread error");
        Intent localIntent = new Intent(NetworkConstants.BROADCAST_ERROR).putExtra(
            NetworkConstants.MESSAGE, msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
