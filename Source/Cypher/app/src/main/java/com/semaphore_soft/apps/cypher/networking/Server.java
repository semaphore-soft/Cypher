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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by Evan on 2/6/2017.
 * Class to hold server threads and helper methods
 */

public class Server
{
    private static ArrayList<ClientHandler> clients        = new ArrayList<>();
    private static Boolean                  accepting      = false;
    private static ServerSocket             serverSocket   = null;
    private        Context                  mContext       = MainApplication.getInstance()
                                                                            .getApplicationContext();
    private        Intent                   mServiceIntent =
        new Intent(mContext, ServerService.class);
    private        int                      maxPlayers     = 4;


    public Server()
    {
    }

    public static void setAccepting(Boolean bool)
    {
        accepting = bool;
    }

    public void startAcceptor()
    {
        AcceptorThread acceptorThread = new AcceptorThread();
        acceptorThread.start();
    }

    public void writeAll(String str)
    {
        Log.d("Server", "Attempting to write to all clients");
        for (ClientHandler server : clients)
        {
            server.write(str);
        }
    }

    public void writeToClient(String str, int index)
    {
        Log.d("Server", "Attempting to write to client " + String.valueOf(index));
        // Service will default to -1 if no index is given
        if (!clients.isEmpty() && index >= 0 && index <= clients.size())
        {
            clients.get(index).write(str);
        }
        else
        {
            Log.d("Server", "Could not write to client");
        }
    }

    public void reconnectClient()
    {
        // Assume only one client needs to reconnect at a time
        if (serverSocket != null)
        {
            try
            {
                Log.i("ClientHandler", "Waiting on accept");
                mServiceIntent.setData(Uri.parse(NetworkConstants.THREAD_UPDATE));
                mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA,
                                        NetworkConstants.STATUS_SERVER_WAIT);
                mContext.startService(mServiceIntent);

                Socket        mySocket     = serverSocket.accept();
                ClientHandler serverThread = new ClientHandler(mySocket);
                clients.add(serverThread);
                serverThread.start();
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

    private class AcceptorThread extends Thread
    {
        Socket mySocket;

        public AcceptorThread()
        {
            try
            {
                serverSocket = new ServerSocket(NetworkConstants.SERVER_PORT);

            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.e("ClientHandler", "Failed to start server");
                mServiceIntent.setData(Uri.parse(NetworkConstants.THREAD_ERROR));
                mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA,
                                        NetworkConstants.ERROR_SERVER_START);
                mContext.startService(mServiceIntent);
            }
        }

        public void run()
        {
            accepting = true;
            int index = 0;
            while (accepting && index < maxPlayers - 1)
            {
                try
                {
                    Log.i("ClientHandler", "Waiting on accept");
                    mServiceIntent.setData(Uri.parse(NetworkConstants.THREAD_UPDATE));
                    mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA,
                                            NetworkConstants.STATUS_SERVER_WAIT);
                    mContext.startService(mServiceIntent);

                    mySocket = serverSocket.accept();
                    ClientHandler serverThread = new ClientHandler(mySocket);
                    clients.add(serverThread);
                    serverThread.start();
                    index++;
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

    private class ClientHandler extends Thread
    {
        // The local server socket
        Socket mySocket;

        private boolean running = true;

        public ClientHandler(Socket socket)
        {
            mySocket = socket;
            mServiceIntent.setData(Uri.parse(NetworkConstants.THREAD_UPDATE));
            mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA,
                                    NetworkConstants.STATUS_SERVER_START);
            mContext.startService(mServiceIntent);
        }

        public void run()
        {
            while (running)
            {
                String msg = read();
                if (msg != null)
                {
                    processMessage(msg);
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
                Log.d("ClientHandler", "sent message: " + str);
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
                // Remove bad socket
                clients.remove(this);
                if (e instanceof SocketException)
                {
                    mServiceIntent = new Intent(mContext, ServerService.class);
                    mServiceIntent.setData(Uri.parse(NetworkConstants.SERVER_RECONNECT));
                    mContext.startService(mServiceIntent);
                }
                e.printStackTrace();
                running = false;
            }
            return null;
        }

        private void processMessage(String msg)
        {
            Log.i("ClientHandler", msg);
            mServiceIntent.setData(Uri.parse(NetworkConstants.THREAD_READ));
            mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA, msg);
            mContext.startService(mServiceIntent);
        }
    }
}
