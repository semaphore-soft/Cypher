package com.semaphore_soft.apps.cypher;

import android.content.Intent;
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
//    private final ConnectionLobbyActivity mActivity;
    private ArrayList<ServerThread> clients = new ArrayList<>();
    public Boolean accepting = false;
    private Intent mServiceIntent;

    public DeviceThreads()//ConnectionLobbyActivity activity)
    {
//        mActivity = activity;
    }

    // Handler to get toasts for debugging
//    private final Handler tHandler = new Handler(new Handler.Callback()
//    {
//        @Override
//        public boolean handleMessage(Message msg)
//        {
//            mActivity.toasts(msg.getData().getString("msg"));
//            return true;
//        }
//    });
//    private void makeToast(String str)
//    {
//        Message msg = new Message();
//        Bundle b = new Bundle();
//        b.putString("msg", str);
//        msg.setData(b);
//        tHandler.sendMessage(msg);
//    }

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
                Toast.makeText(MainApplication.getInstance().getApplicationContext(), "Failed to start server", Toast.LENGTH_SHORT).show();
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
//                    makeToast("Waiting on accept");
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
        int id;

        public ServerThread(Socket socket, int id)
        {
            mySocket = socket;
            this.id = id;
        }

        public void run()
        {
            while(true)
            {
                try
                {
                    DataInputStream in = new DataInputStream(mySocket.getInputStream());
                    processMessage(in.readUTF());
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

        private void processMessage(String msg)
        {
//            makeToast(msg);
            Log.i("ServerThread", msg);
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
//                makeToast("Failed to start socket");
            }
        }

        public void run()
        {
            // Connection was accepted
            if (mySocket != null) {
                Log.i("ClientThread", "Connection made");
                while(true)
                {
                    try
                    {
                        DataInputStream in = new DataInputStream(mySocket.getInputStream());
                        processMessage(in.readUTF());
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
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
                Log.d("ClientThread", "sent message");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void processMessage(String msg)
        {
//            makeToast(msg);
            Log.i("ClientThread", msg);
        }
    }
}
