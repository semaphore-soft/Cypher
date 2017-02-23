package com.semaphore_soft.apps.cypher.networking;

import com.semaphore_soft.apps.cypher.utils.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketOptions;
import java.util.ArrayList;

/**
 * Created by Evan on 2/6/2017.
 * Class to hold server threads and helper methods
 */

public class Server
{
    private static ArrayList<ClientHandler> clients      = new ArrayList<>();
    private static Boolean                  accepting    = false;
    private static ServerSocket             serverSocket = null;
    private        int                      maxPlayers   = 4;
    private ServerService serverService;


    public Server()
    {
    }

    public static void setAccepting(Boolean bool)
    {
        accepting = bool;
    }

    public void startAcceptor(ServerService service)
    {
        serverService = service;
        AcceptorThread acceptorThread = new AcceptorThread();
        acceptorThread.start();
    }

    public void writeAll(String str)
    {
        Logger.logD("Attempting to write to all clients");
        for (ClientHandler server : clients)
        {
            server.write(str);
        }
    }

    public void writeToClient(String str, int index)
    {
        Logger.logD("Attempting to write to client " + String.valueOf(index));
        // Service will default to -1 if no index is given
        if (!clients.isEmpty() && index >= 0 && index < clients.size())
        {
            clients.get(index).write(str);
        }
        else
        {
            Logger.logD("Could not write to client");
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
                Logger.logE("Failed to start server");
                // Pass 0 since this does not receive input from client
                serverService.threadError(NetworkConstants.ERROR_SERVER_START, 0);
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
                    Logger.logI("Waiting on accept");
                    // Pass 0 since this does not receive input from client
                    serverService.threadUpdate(NetworkConstants.STATUS_SERVER_WAIT, 0);

                    // Set a timeout for the serverSocket to block
                    serverSocket.setSoTimeout(SocketOptions.SO_TIMEOUT);
                    mySocket = serverSocket.accept();
                    ClientHandler serverThread = new ClientHandler(mySocket);
                    clients.add(serverThread);
                    serverThread.start();
                    index++;
                }
                catch (SocketException e)
                {
                    Logger.logI("Socket closed");
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
            serverService.threadUpdate(NetworkConstants.STATUS_SERVER_START,
                                       clients.indexOf(this) + 1);
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
                Logger.logD("sent message: " + str);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                serverService.threadError(NetworkConstants.ERROR_WRITE, clients.indexOf(this) + 1);
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
                serverService.threadError(NetworkConstants.ERROR_DISCONNECT_SERVER,
                                          clients.indexOf(this) + 1);
                e.printStackTrace();
                running = false;
                reconnectSocket();
            }
            return null;
        }

        private void processMessage(String msg)
        {
            Logger.logI(msg);
            Logger.logD(String.valueOf(clients.indexOf(this) + 1));
            serverService.threadRead(msg, clients.indexOf(this) + 1);
        }

        private void reconnectSocket()
        {
            // Assume only one client needs to reconnect at a time
            if (serverSocket != null)
            {
                try
                {
                    Logger.logI("Waiting on accept");
                    serverService.threadUpdate(NetworkConstants.STATUS_SERVER_WAIT,
                                               clients.indexOf(this) + 1);

                    // Disable timeout
                    serverSocket.setSoTimeout(0);
                    mySocket = serverSocket.accept();
                    running = true;
                    serverService.threadUpdate(NetworkConstants.STATUS_SERVER_START,
                                               clients.indexOf(this) + 1);
                }
                catch (SocketException e)
                {
                    Logger.logI("Socket closed");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
