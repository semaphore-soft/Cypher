package com.semaphore_soft.apps.cypher.networking;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.semaphore_soft.apps.cypher.MainApplication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Evan on 2/6/2017.
 * Class to hold client thread and helper methods
 */

public class Client
{
    private        Context      mContext       = MainApplication.getInstance()
                                                    .getApplicationContext();
    private        Intent       mServiceIntent = new Intent(mContext, ClientService.class);
    private static ClientThread clientThread   = null;


    public Client()
    {
    }

    public ClientThread startClient(InetAddress addr)
    {
        clientThread = new ClientThread(addr);
        clientThread.start();
        return clientThread;
    }

    public ClientThread getClientThread()
    {
        return clientThread;
    }

    public class ClientThread extends Thread
    {
        Socket mySocket = null;
        private boolean running = true;

        public ClientThread(InetAddress address)
        {
            try
            {
                // creates and connects to address at specified port
                mySocket = new Socket(address, NetworkConstants.SERVER_PORT);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.e("ClientThread", "Failed to start socket");
                mServiceIntent.setData(Uri.parse(NetworkConstants.THREAD_ERROR));
                mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA,
                                        NetworkConstants.ERROR_CLIENT_SOCKET);
                mContext.startService(mServiceIntent);
            }
        }

        public void run()
        {
            // Connection was accepted
            if (mySocket != null)
            {
                Log.i("ClientThread", "Connection made");
                mServiceIntent.setData(Uri.parse(NetworkConstants.THREAD_UPDATE));
                mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA, "Connection made");
                mContext.startService(mServiceIntent);
                while (running)
                {
                    String msg = read();
                    if (msg != null)
                    {
                        processMessage(msg);
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
                Log.d("ClientThread", "sent message: " + str);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        private String read()
        {
            try
            {
                DataInputStream in = new DataInputStream(mySocket.getInputStream());
                try
                {
                    return in.readUTF();
                }
                catch (EOFException e)
                {
                    return null;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                running = false;
            }
            return null;
        }

        private void processMessage(String msg)
        {
            Log.i("ClientThread", msg);
            mServiceIntent.setData(Uri.parse(NetworkConstants.THREAD_READ));
            mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA, msg);
            mContext.startService(mServiceIntent);
        }
    }
}
