package com.semaphore_soft.apps.cypher;

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
    private static Socket mySocket = null;

    public DeviceThreads()
    {
    }

    private static void makeToast(String str)
    {
        Toast.makeText(MainApplication.getInstance().getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

    public static void write(String str)
    {
        if (mySocket != null)
        {
            Log.d("Write", "sending message");
            try
            {
                DataOutputStream dos = new DataOutputStream(mySocket.getOutputStream());
                dos.writeUTF(str);
                // flush after write or inputStream will hang on read
                dos.flush();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            Log.d("Write", "Socket is null");
        }
    }

    public static class ServerThread extends Thread
    {
        // The local server socket
        private ServerSocket serverSocket = null;
        private Socket my_socket;

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
            my_socket = null;
            try
            {
                my_socket = serverSocket.accept();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.e("ServerThread", "Failed to accept connection");
                makeToast("Failed to accept connection");
            }
            if (my_socket != null)
            {
                Log.i("ServerConnect", "Connection made");
                makeToast("Connection made");
                try
                {
                    DataOutputStream out = new DataOutputStream(my_socket.getOutputStream());
                    // This may or may not be needed
                    out.flush();
                    DataInputStream in = new DataInputStream((my_socket.getInputStream()));
                    out.writeUTF("Hello, World!");
                    // flush after write or inputStream will hang on read
                    out.flush();
                    Log.d("ServerThread", "sent message");
                    makeToast(in.readUTF());
                    Log.d("ServerThread", "received message");
                    out.close();
                    in.close();
                    my_socket.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class ClientThread extends Thread
    {
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
            if (mySocket != null)
            {
                Log.i("ClientThread", "Connection made");
                try
                {
                    DataOutputStream out = new DataOutputStream(mySocket.getOutputStream());
                    out.flush();
                    DataInputStream in = new DataInputStream(mySocket.getInputStream());
                    makeToast(in.readUTF());
                    Log.d("ClientThread", "read message");
                    // Message passing will not work if stream/socket is closed

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

    }
}
