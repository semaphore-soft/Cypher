package com.semaphore_soft.apps.cypher.networking;

import android.util.Log;

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
    private static ClientThread clientThread   = null;
    private ClientService clientService;

    public Client()
    {
    }

    public ClientThread startClient(InetAddress addr, ClientService client, boolean reconnect)
    {
        clientService = client;
        clientThread = new ClientThread(addr, reconnect);
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

        public ClientThread(InetAddress address, boolean reconnect)
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
                clientService.threadError(NetworkConstants.ERROR_CLIENT_SOCKET);
                if (reconnect)
                {
                    startClient(address, clientService, true);
                }
            }
        }

        public void run()
        {
            // Connection was accepted
            if (mySocket != null)
            {
                Log.i("ClientThread", "Connection made");
                clientService.threadUpdate(NetworkConstants.STATUS_CLIENT_CONNECT);
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
                clientService.threadError(NetworkConstants.ERROR_WRITE);
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
                    clientService.threadError(NetworkConstants.ERROR_DISCONNECT_CLIENT);
                }
                e.printStackTrace();
                running = false;
            }
            return null;
        }

        private void processMessage(String msg)
        {
            Log.i("ClientThread", msg);
            clientService.threadRead(msg);
        }

        public void reconnectSocket()
        {
            startClient(inetAddress, clientService, true);
        }
    }
}
