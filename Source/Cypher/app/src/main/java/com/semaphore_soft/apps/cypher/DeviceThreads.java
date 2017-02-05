package com.semaphore_soft.apps.cypher;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by Evan on 1/27/2017.
 * Class to hold Server and Client threads
 */

public class DeviceThreads
{
    private ArrayList<ServerThread> clients        = new ArrayList<>();
    public  Boolean                 accepting      = false;
    private Context                 mContext       = MainApplication.getInstance()
                                                                    .getApplicationContext();
    private Intent                  mServiceIntent = new Intent(mContext, NetworkingService.class);


    public DeviceThreads()
    {
    }

    public ClientThread startClient(InetAddress addr)
    {
        ClientThread clientThread = new ClientThread(addr);
        clientThread.start();
        return clientThread;
    }

    public void startAcceptor()
    {
        AcceptorThread acceptorThread = new AcceptorThread();
        acceptorThread.start();
    }

    public void writeAll(String str)
    {
        Log.d("Threads", "Attempting to write to all clients");
        for (ServerThread server : clients)
        {
            server.write(str);
        }
    }

    public void writeToClient(String str, int index)
    {
        Log.d("Threads", "Attempting to write to client " + String.valueOf(index));
        // TODO call writeALL if index invalid?
        // Service will default to -1 if no index is given
        if (!clients.isEmpty() && index > 0)
        {
            clients.get(index).write(str);
        }
    }

    private class AcceptorThread extends Thread
    {
        // The local server socket
        ServerSocket serverSocket = null;
        Socket mySocket;

        public AcceptorThread()
        {
            try
            {
                serverSocket = new ServerSocket(MainActivity.SERVER_PORT);

            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.e("ServerThread", "Failed to start server");
                Toast.makeText(mContext, "Failed to start server", Toast.LENGTH_SHORT).show();
            }
        }

        public void run()
        {
            accepting = true;
            int id = 0;
            while (accepting)
            {
                try
                {
                    Log.i("ServerThread", "Waiting on accept");
                    mServiceIntent.setData(Uri.parse(NetworkingService.THREAD_UPDATE));
                    mServiceIntent.putExtra("message", "Waiting on accept");
                    mContext.startService(mServiceIntent);

                    mySocket = serverSocket.accept();
                    ServerThread serverThread = new ServerThread(mySocket, id);
                    clients.add(serverThread);
                    serverThread.start();
                    id++;
                    accepting = false;
                }
                catch (SocketException e)
                {
                    Log.i("AcceptorThread", "Socket closed");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ServerThread extends Thread
    {
        // The local server socket
        Socket mySocket;
        int    id;

        public ServerThread(Socket socket, int id)
        {
            mySocket = socket;
            this.id = id;
        }

        public void run()
        {
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

        public void write(String str)
        {
            try
            {
                DataOutputStream out = new DataOutputStream(mySocket.getOutputStream());
                out.writeUTF(str);
                // flush after write or inputStream will hang on read
                out.flush();
                Log.d("ServerThread", "sent message");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        private void processMessage(String msg)
        {
            Log.i("ServerThread", msg);
            mServiceIntent.setData(Uri.parse(NetworkingService.THREAD_READ));
            mServiceIntent.putExtra("message", msg);
            mContext.startService(mServiceIntent);
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
                mServiceIntent.setData(Uri.parse(NetworkingService.THREAD_UPDATE));
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
            mServiceIntent.setData(Uri.parse(NetworkingService.THREAD_READ));
            mServiceIntent.putExtra("message", msg);
            mContext.startService(mServiceIntent);
        }
    }
}
