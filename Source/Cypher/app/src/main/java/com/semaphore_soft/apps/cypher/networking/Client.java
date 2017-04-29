package com.semaphore_soft.apps.cypher.networking;

import com.semaphore_soft.apps.cypher.utils.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Class to hold client thread and helper methods.
 *
 * @author Evan
 * @see ClientService
 * @see ClientThread
 */

public class Client
{
    private static Boolean reconnect = true;
    private ClientService clientService;

    public Client()
    {
    }

    /**
     * Sets whether of not the {@link Server.ClientHandler ServerThread} should try to reconnect
     * to a client if it disconnects.
     *
     * @param bool Whether or not to attempt to reconnect to a client
     */
    public static void setReconnect(Boolean bool)
    {
        reconnect = bool;
    }

    /**
     * Starts the {@link ClientThread ClientThread}.
     *
     * @param addr   Address to connect to.
     * @param client An instance of {@link ClientService} that will interact with the thread.
     *
     * @return An instance of {@link ClientThread ClientThread} that is connected to {@code addr}.
     *
     * @see ClientService#startClient(String)
     */
    public ClientThread startClient(InetAddress addr, ClientService client)
    {
        clientService = client;
        ClientThread clientThread = new ClientThread(addr);
        clientThread.start();
        return clientThread;
    }

    /**
     * Thread that connects to the server
     *
     * @see ClientThread#ClientThread(InetAddress)
     * @see com.semaphore_soft.apps.cypher.networking.Server.ClientHandler
     */
    public class ClientThread extends Thread
    {
        Socket mySocket = null;
        private boolean     running     = true;
        private InetAddress inetAddress = null;

        /**
         * Create new thread that is connected to {@code address}.
         *
         * @param address Address to connect to.
         *
         * @see Client#startClient(InetAddress, ClientService)
         */
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

        /**
         * Write message to server.
         *
         * @param str Message to write.
         */
        public void write(String str)
        {
            try
            {
                DataOutputStream out = new DataOutputStream(mySocket.getOutputStream());
                out.writeUTF(str);
                // Flush after write or inputStream will hang on read
                out.flush();
                Logger.logD("sent message: " + str, 5);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                clientService.threadError(NetworkConstants.ERROR_WRITE);
            }
        }

        /**
         * Reads in data from the network.
         * Will attempt to reconnect if {@link Socket} connection is broken.
         *
         * @return Message that was read.
         *
         * @see ClientThread#reconnectSocket()
         */
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
                clientService.threadError(NetworkConstants.ERROR_DISCONNECT_CLIENT);
                e.printStackTrace();
                running = false;
                if (reconnect)
                {
                    reconnectSocket();
                }
            }
            return null;
        }

        /**
         * Sends message that has been read to be processed by other activities.
         *
         * @param msg Message that was read.
         *
         * @see ClientService#threadRead(String)
         */
        private void processMessage(String msg)
        {
            Logger.logI(msg, 5);
            clientService.threadRead(msg);
        }

        /**
         * Get the {@link java.net.SocketAddress SocketAddress} that the client is connected to.
         *
         * @return Host's address as a string
         *
         * @see ClientService#getHostIP()
         */
        public String getSocketAddress()
        {
            return mySocket.getRemoteSocketAddress().toString();
        }

        /**
         * Will try to reconnect to client if {@link Socket} connection is lost.
         * This method will wait some amount of time for the host
         * to notice that it has disconnected before attempting to reconnect.
         */
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
