package com.semaphore_soft.apps.cypher.networking;

import com.semaphore_soft.apps.cypher.utils.Logger;

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
    private ClientService clientService;

    public Client()
    {
    }

    public ClientThread startClient(InetAddress addr, ClientService client)
    {
        clientService = client;
        ClientThread clientThread = new ClientThread(addr);
        clientThread.start();
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
                inetAddress = address;
                // Creates and connects to address at specified port
                mySocket = new Socket(address, NetworkConstants.SERVER_PORT);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Logger.logE("Failed to start socket");
                clientService.threadError(NetworkConstants.ERROR_CLIENT_SOCKET);
            }
        }

        public void run()
        {
            // Connection was accepted
            if (mySocket != null)
            {
                Logger.logI("Connection made");
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
                Logger.logD("sent message: " + str);
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
                reconnectSocket();
            }
            return null;
        }

        private void processMessage(String msg)
        {
            Logger.logI(msg);
            clientService.threadRead(msg);
        }

        public String getSocketAddress()
        {
            return mySocket.getRemoteSocketAddress().toString();
        }

        public void reconnectSocket()
        {
            try
            {
                // Wait for server to detect that client has disconnected
                Thread.sleep(NetworkConstants.HEARTBEAT_DELAY);
                mySocket = new Socket(inetAddress, NetworkConstants.SERVER_PORT);
                clientService.threadUpdate(NetworkConstants.STATUS_CLIENT_CONNECT);
                running = true;
            }
            catch (InterruptedException e)
            {
                Logger.logI("Thread interrupted");
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Logger.logI("Connection failed, retrying...");
                reconnectSocket();
            }
        }
    }
}
