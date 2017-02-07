package com.semaphore_soft.apps.cypher.networking;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.MainActivity;
import com.semaphore_soft.apps.cypher.MainApplication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
    private ArrayList<ClientHandler> clients        = new ArrayList<>();
    public  Boolean                  accepting      = false;
    private Context                  mContext       = MainApplication.getInstance()
                                                                     .getApplicationContext();
    private Intent                   mServiceIntent = new Intent(mContext, ServerService.class);


    public Server()
    {
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
                Log.e("ClientHandler", "Failed to start server");
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
                    Log.i("ClientHandler", "Waiting on accept");
                    mServiceIntent.setData(Uri.parse(NetworkConstants.THREAD_UPDATE));
                    mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA, "Waiting on accept");
                    mContext.startService(mServiceIntent);

                    mySocket = serverSocket.accept();
                    ClientHandler serverThread = new ClientHandler(mySocket, id);
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

    private class ClientHandler extends Thread
    {
        // The local server socket
        Socket mySocket;
        int    id;

        public ClientHandler(Socket socket, int id)
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
                Log.d("ClientHandler", "sent message");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
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
