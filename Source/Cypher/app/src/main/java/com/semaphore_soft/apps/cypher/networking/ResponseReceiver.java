package com.semaphore_soft.apps.cypher.networking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Evan on 2/6/2017.
 */

public class ResponseReceiver extends BroadcastReceiver
{
    public ResponseReceiver()
    {
    }

    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        Log.d("BR", action);
        if (ClientService.BROADCAST_MESSAGE.equals(action))
        {
            // Message from other devices
            String msg = intent.getStringExtra(ClientService.MESSAGE);
            Log.i("BR", msg);
            //            toasts(msg);
        }
        else if (ClientService.BROADCAST_STATUS.equals(action))
        {
            // Thread status updates
            String msg = intent.getStringExtra(ClientService.MESSAGE);
            Log.i("BR", msg);
            //            toasts(msg);
        }
    }

    //    private void toasts(String str)
    //    {
    //        Toast.makeText(ConnectionLobbyActivity.this, str, Toast.LENGTH_SHORT).show();
    //    }
}
