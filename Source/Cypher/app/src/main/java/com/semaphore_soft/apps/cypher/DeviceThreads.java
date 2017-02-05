package com.semaphore_soft.apps.cypher;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Evan on 1/27/2017.
 * Class to hold Server and Client threads
 */

public class DeviceThreads
{
    private final ConnectionLobbyActivity mActivity;

    public DeviceThreads(ConnectionLobbyActivity activity)
    {
        mActivity = activity;
    }

    // Handler to get toasts for debugging
    private final Handler tHandler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            mActivity.toasts(msg.getData().getString("msg"));
            return true;
        }
    });
    private void makeToast(String str)
    {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("msg", str);
        msg.setData(b);
        tHandler.sendMessage(msg);
    }

    public class ServerThread extends Thread
    {
        // The local server socket
        ServerSocket serverSocket = null;
        Socket mySocket;

        public ServerThread()
        {
            try
            {
                serverSocket = new ServerSocket(MainActivity.SERVER_PORT);

            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.e("ServerThread", "Failed to start server");
                Toast.makeText(MainApplication.getInstance().getApplicationContext(), "Failed to start server", Toast.LENGTH_SHORT).show();
            }
        }

        public void run()
        {
            Log.i("ServerThread", "Waiting on accept");
            makeToast("Waiting on accept");
            mySocket = null;
            try
            {
                mySocket = serverSocket.accept();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.e("ServerThread", "Failed to accept connection");
                makeToast("Failed to accept connection");
            }
            if (mySocket != null)
            {
                Log.i("ServerConnect", "Connection made");
                makeToast("Connection made");
                try
                {
                    DataInputStream in = new DataInputStream((mySocket.getInputStream()));
                    makeToast(in.readUTF());
                    Log.d("ServerThread", "received message");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        public void write(String str)
        {
            try {
                DataOutputStream out = new DataOutputStream(mySocket.getOutputStream());
                out.writeUTF(str);
                // flush after write or inputStream will hang on read
                out.flush();
                Log.d("ServerThread", "sent message");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                makeToast("Failed to start socket");
            }
        }

        public void run()
        {
            // Connection was accepted
            if (mySocket != null) {
                Log.i("ClientThread", "Connection made");
                try {
                    DataInputStream in = new DataInputStream(mySocket.getInputStream());
                    makeToast(in.readUTF());
                    Log.d("ClientThread", "read message");
                    // Message passing will not work if stream/socket is closed

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(String str)
        {
            try {
                DataOutputStream out = new DataOutputStream(mySocket.getOutputStream());
                out.writeUTF(str);
                // flush after write or inputStream will hang on read
                out.flush();
                Log.d("ServerThread", "sent message");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
