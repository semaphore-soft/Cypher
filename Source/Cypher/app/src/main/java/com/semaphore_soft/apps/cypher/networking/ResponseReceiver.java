package com.semaphore_soft.apps.cypher.networking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.semaphore_soft.apps.cypher.utils.Logger;

/**
 * Created by Evan on 2/6/2017.
 * Broadcast Receiver for network events
 */

public class ResponseReceiver extends BroadcastReceiver
{
    private Receiver listener;

    public ResponseReceiver()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        Logger.logD(action);
        if (NetworkConstants.BROADCAST_MESSAGE.equals(action))
        {
            // Message from other devices
            String msg  = intent.getStringExtra(NetworkConstants.MESSAGE);
            int    from = intent.getIntExtra(NetworkConstants.INDEX, -1);
            Logger.logI(msg + " - " + from);
            listener.handleRead(msg, from);
        }
        else if (NetworkConstants.BROADCAST_STATUS.equals(action))
        {
            // Thread status updates
            String msg  = intent.getStringExtra(NetworkConstants.MESSAGE);
            int    from = intent.getIntExtra(NetworkConstants.INDEX, -1);
            Logger.logI(msg + " - " + from);
            listener.handleStatus(msg, from);
        }
        else if (NetworkConstants.BROADCAST_ERROR.equals(action))
        {
            // Thread errors
            String msg  = intent.getStringExtra(NetworkConstants.MESSAGE);
            int    from = intent.getIntExtra(NetworkConstants.INDEX, -1);
            Logger.logI(msg + " - " + from);
            listener.handleError(msg, from);
        }
    }

    public interface Receiver
    {
        /**
         * Handle messages that have been read from the network.
         *
         * @param msg      Message read from network
         * @param readFrom Device that message was received from
         */
        void handleRead(String msg, int readFrom);

        /**
         * Handle status updates.
         * @param msg Status update
         * @param readFrom Device that update was received from
         */
        void handleStatus(String msg, int readFrom);

        /**
         * Handle error messages.
         * @param msg Error message
         * @param readFrom Device that error was received from
         */
        void handleError(String msg, int readFrom);
    }

    /**
     * Used by ResponseReceiver to call interface methods.
     * @param r Instance of ResponseReceiver
     */
    public void setListener(Receiver r)
    {
        listener = r;
    }
}
