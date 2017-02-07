package com.semaphore_soft.apps.cypher.networking;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.semaphore_soft.apps.cypher.MainActivity;
import com.semaphore_soft.apps.cypher.MainApplication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Evan on 2/6/2017.
 * Class to hold client thread and helper methods
 */

public class Client
{
    private Context mContext       = MainApplication.getInstance()
                                                    .getApplicationContext();
    private Intent  mServiceIntent = new Intent(mContext, ClientService.class);


    public Client()
    {
    }

    public ClientThread startClient(InetAddress addr)
    {
        ClientThread clientThread = new ClientThread(addr);
        clientThread.start();
        return clientThread;
    }

    public class ClientThread extends Thread
    {
        Socket mySocket = null;

        public ClientThread(InetAddress address)
        {
            try
            {
                // creates and connects to address at specified port
                mySocket = new Socket(address, MainActivity.SERVER_PORT);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.e("ClientThread", "Failed to start socket");
                mServiceIntent.setData(Uri.parse(ClientService.THREAD_UPDATE));
                mServiceIntent.putExtra("message", "Failed to start socket");
                mContext.startService(mServiceIntent);
            }
        }

        public void run()
        {
            // Connection was accepted
            if (mySocket != null)
            {
                Log.i("ClientThread", "Connection made");
                Boolean running = true;
                while (running)
                {
                    try
                    {
                        DataInputStream in = new DataInputStream(mySocket.getInputStream());
                        processMessage(in.readUTF());
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        running = false;
                    }
                }
            }
        }

        public void write(String str)
        {
            try
            {
                DataOutputStream out = new DataOutputStream(mySocket.getOutputStream());
                out.writeUTF(str);
                // flush after write or inputStream will hang on read
                out.flush();
                Log.d("ClientThread", "sent message");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        private void processMessage(String msg)
        {
            Log.i("ClientThread", msg);
            mServiceIntent.setData(Uri.parse(ClientService.THREAD_READ));
            mServiceIntent.putExtra("message", msg);
            mContext.startService(mServiceIntent);
        }
    }
}
