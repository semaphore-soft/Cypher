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
    private static Socket clientSocket = null;
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

    public static int clientWrite(String str)
    {
        if (clientSocket != null)
        {
            Log.d("clientWrite", "sending message");
            try
            {
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                dos.writeUTF(str);
                // flush after write or inputStream will hang on read
                dos.flush();
                return 0;
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return 2;
            }
        }
        else
        {
            Log.d("clientWrite", "Socket is null");
            return 1;
        }
    }

    public static String clientRead()
    {
        if (clientSocket != null)
        {
            Log.d("clientRead", "reading message");
            try
            {
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                return dis.readUTF();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return null;
            }
        }
        else
        {
            Log.d("clientRead", "socket is null");
            return null;
        }
    }

    public class ServerThread extends Thread
    {
        // The local server socket
        private ServerSocket startSocket = null;
        private Socket serverSocket;

        public ServerThread()
        {
            try
            {
                startSocket = new ServerSocket(MainActivity.SERVER_PORT);

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
            serverSocket = null;
            try
            {
                serverSocket = startSocket.accept();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.e("ServerThread", "Failed to accept connection");
                makeToast("Failed to accept connection");
            }
            if (serverSocket != null)
            {
                Log.i("ServerConnect", "Connection made");
                makeToast("Connection made");
                try
                {
                    DataOutputStream out = new DataOutputStream(serverSocket.getOutputStream());
                    // This may or may not be needed
                    out.flush();
                    DataInputStream in = new DataInputStream((serverSocket.getInputStream()));
                    out.writeUTF("Hello, World!");
                    // flush after write or inputStream will hang on read
                    out.flush();
                    Log.d("ServerThread", "sent message");
                    makeToast(in.readUTF());
                    Log.d("ServerThread", "received message");
                    out.close();
                    in.close();
                    serverSocket.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public class ClientThread extends Thread
    {
        public ClientThread(InetAddress address)
        {
            try
            {
                // creates and connects to address at specified port
                clientSocket = new Socket(address, MainActivity.SERVER_PORT);
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
        }

    }
}
