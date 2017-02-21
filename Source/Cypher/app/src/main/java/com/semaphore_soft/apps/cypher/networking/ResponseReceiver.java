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
            String msg = intent.getStringExtra(NetworkConstants.MESSAGE);
            Logger.logI(msg);
            listener.handleStatus(msg);
        }
        else if (NetworkConstants.BROADCAST_ERROR.equals(action))
        {
            // Thread errors
            String msg = intent.getStringExtra(NetworkConstants.MESSAGE);
            Logger.logI(msg);
            listener.handleError(msg);
        }
    }

    public interface Receiver
    {
        void handleRead(String msg, int readFrom);
        void handleStatus(String msg);
        void handleError(String msg);
    }

    public void setListener(Receiver r)
    {
        listener = r;
    }
}
