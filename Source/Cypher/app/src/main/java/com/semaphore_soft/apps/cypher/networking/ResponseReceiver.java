package com.semaphore_soft.apps.cypher.networking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
        Log.d("BR", action);
        if (NetworkConstants.BROADCAST_MESSAGE.equals(action))
        {
            // Message from other devices
            String msg = intent.getStringExtra(NetworkConstants.MESSAGE);
            Log.i("BR", msg);
            listener.handleRead(msg);
        }
        else if (NetworkConstants.BROADCAST_STATUS.equals(action))
        {
            // Thread status updates
            String msg = intent.getStringExtra(NetworkConstants.MESSAGE);
            Log.i("BR", msg);
            listener.handleStatus(msg);
        }
        else if (NetworkConstants.BROADCAST_ERROR.equals(action))
        {
            // Thread errors
            String msg = intent.getStringExtra(NetworkConstants.MESSAGE);
            Log.i("BR", msg);
            listener.handleError(msg);
        }
    }

    public interface Receiver
    {
        void handleRead(String msg);
        void handleStatus(String msg);
        void handleError(String msg);
    }

    public void setListener(Receiver r)
    {
        listener = r;
    }
}
