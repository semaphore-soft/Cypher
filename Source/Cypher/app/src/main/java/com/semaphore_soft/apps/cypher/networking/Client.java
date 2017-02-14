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
import java.net.SocketException;

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
        private boolean     running     = true;
        private InetAddress inetAddress = null;

        public ClientThread(InetAddress address)
        {
            try
            {
                // Creates and connects to address at specified port
                mySocket = new Socket(address, NetworkConstants.SERVER_PORT);
                inetAddress = address;
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
                mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA,
                                        NetworkConstants.STATUS_CLIENT_CONNECT);
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
                // Flush after write or inputStream will hang on read
                out.flush();
                Log.d("ClientThread", "sent message: " + str);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                mServiceIntent.setData(Uri.parse(NetworkConstants.THREAD_ERROR));
                mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA, NetworkConstants.ERROR_WRITE);
                mContext.startService(mServiceIntent);
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
                if (e instanceof SocketException)
                {
                    mServiceIntent.setData(Uri.parse(NetworkConstants.THREAD_ERROR));
                    mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA,
                                            NetworkConstants.ERROR_DISCONNECT_CLIENT);
                    mContext.startService(mServiceIntent);
                }
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

        public void reconnectSocket()
        {
            startClient(inetAddress);
        }
    }
}
