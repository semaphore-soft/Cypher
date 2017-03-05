package com.semaphore_soft.apps.cypher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.networking.ClientService;
import com.semaphore_soft.apps.cypher.networking.NetworkConstants;
import com.semaphore_soft.apps.cypher.networking.ResponseReceiver;
import com.semaphore_soft.apps.cypher.networking.Server;
import com.semaphore_soft.apps.cypher.networking.ServerService;
import com.semaphore_soft.apps.cypher.ui.PlayerID;
import com.semaphore_soft.apps.cypher.ui.UIConnectionLobby;
import com.semaphore_soft.apps.cypher.ui.UIListener;
import com.semaphore_soft.apps.cypher.utils.Logger;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by Scorple on 1/9/2017.
 * Activity where players will start after they have connected to the host and
 * wait for other players to join.
 *
 * @see UIConnectionLobby
 */

public class ConnectionLobbyActivity extends AppCompatActivity implements ResponseReceiver.Receiver,
                                                                          UIListener
{
    private static ResponseReceiver responseReceiver;
    private static ServerService    serverService;
    private static ClientService    clientService;
    private static boolean mServerBound = false;
    private static boolean mClientBound = false;

    private static String              name;
    private static boolean             host;
    private static int                 playerID;
    private static ArrayList<PlayerID> playersList;

    private UIConnectionLobby uiConnectionLobby;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty);

        uiConnectionLobby = new UIConnectionLobby(this);
        ((FrameLayout) findViewById(R.id.empty)).addView(uiConnectionLobby);
        uiConnectionLobby.setUIListener(this);

        responseReceiver = new ResponseReceiver();
        responseReceiver.setListener(this);
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(responseReceiver, NetworkConstants.getFilter());

        host = getIntent().getBooleanExtra("host", false);
        name = getIntent().getStringExtra("name");

        String welcomeText = "Welcome " + name;
        uiConnectionLobby.setTxtDisplayName(welcomeText);

        playersList = new ArrayList<>();
        uiConnectionLobby.setPlayersList(playersList);
        uiConnectionLobby.setHost(host);

        if (host)
        {
            playerID = 0;

            String ip = "";
            try
            {
                // Use a label to break out of a nested for loop
                // Note: probably better to use a method for the inner loop, but this works
                outerloop:
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                     en.hasMoreElements(); )
                {
                    NetworkInterface ni = en.nextElement();
                    for (Enumeration<InetAddress> addresses = ni.getInetAddresses();
                         addresses.hasMoreElements(); )
                    {
                        InetAddress inetAddress = addresses.nextElement();
                        // Limit IP addresses shown to IPv4
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address)
                        {
                            ip = inetAddress.getHostAddress();
                            Logger.logI(ip);
                            break outerloop;
                        }
                    }
                }
            }
            catch (SocketException ex)
            {
                Logger.logE(ex.toString());
            }

            uiConnectionLobby.setTextIP("Your IP Address is: " + ip);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (host)
        {
            // Bind to ServerService
            Intent intent = new Intent(this, ServerService.class);
            bindService(intent, mServerConnection, Context.BIND_AUTO_CREATE);
        }
        else
        {
            // Bind to ClientService
            Intent intent = new Intent(this, ClientService.class);
            bindService(intent, mClientConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        // Unbind from the service
        if (mServerBound)
        {
            unbindService(mServerConnection);
            mServerBound = false;
        }
        else if (mClientBound)
        {
            unbindService(mClientConnection);
            mClientBound = false;
        }
    }

    /**
     * Assign new players an ID and update clients with new players.
     *
     * @param player Name of player
     * @param id     ID to be assigned to player
     */
    private void addPlayer(String player, int id)
    {
        PlayerID playerID = new PlayerID();
        playerID.setID(id);
        playerID.setPlayerName(player);
        playersList.add(playerID);
        uiConnectionLobby.setPlayersList(playersList);
        if (host)
        {
            // Update clients with all connected players
            serverService.writeAll(NetworkConstants.GAME_UPDATE);
            for (PlayerID pid : playersList)
            {
                serverService.writeAll(
                    NetworkConstants.PREFIX_PLAYER + pid.getPlayerName() + ":" + pid.getID());
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param cmd Command from UI interaction
     */
    public void onCommand(String cmd)
    {
        switch (cmd)
        {
            case "cmd_btnStart":
                Server.setAccepting(false);
                serverService.writeAll(NetworkConstants.GAME_START);
                LocalBroadcastManager.getInstance(ConnectionLobbyActivity.this)
                                     .unregisterReceiver(responseReceiver);
                Toast.makeText(ConnectionLobbyActivity.this,
                               "Moving to Character Select",
                               Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), CharacterSelectActivity.class);
                intent.putExtra("host", host);
                intent.putExtra("player", 0);
                intent.putExtra("numClients", playerID);
                startActivity(intent);
                break;
            default:
                Toast.makeText(this, "UI interaction not handled", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param msg Message read from network
     * @param readFrom Device that message was received from
     */
    @Override
    public void handleRead(String msg, int readFrom)
    {
        if (msg.equals(NetworkConstants.GAME_START))
        {
            LocalBroadcastManager.getInstance(ConnectionLobbyActivity.this)
                                 .unregisterReceiver(responseReceiver);
            // Start character select activity after host has started game
            Intent intent = new Intent(getBaseContext(), CharacterSelectActivity.class);
            intent.putExtra("host", host);
            intent.putExtra("player", playerID);
            startActivity(intent);
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_NAME))
        {
            // Add players on server
            // Ignore the attached prefix with substring to get the clients name
            addPlayer(msg.substring(5), ++playerID);
            serverService.addPlayerID(playerID, readFrom);
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_PLAYER))
        {
            // Add players on client
            // Expect the name of the player,
            // and the PlayerID assigned to them
            String args[] = msg.split(":");
            addPlayer(args[1], Integer.valueOf(args[2]));
            // Update client with their playerID
            if (args[1].equals(name))
            {
                playerID = Integer.valueOf(args[2]);
            }
        }
        else if (msg.equals(NetworkConstants.GAME_UPDATE))
        {
            playersList.clear();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param msg Status update
     * @param readFrom Device that update was received from
     */
    @Override
    public void handleStatus(String msg, int readFrom)
    {
        Toast.makeText(this, "Status: " + msg, Toast.LENGTH_SHORT).show();
        if (msg.equals(NetworkConstants.STATUS_SERVER_WAIT) && readFrom != 0)
        {
            // Don't use status updates from the AcceptorThread (uses host id)
            // If we get a disconnect, stop the AcceptorThread
            Server.setAccepting(false);
        }
        else if (msg.equals(NetworkConstants.STATUS_SERVER_START) && readFrom != 0)
        {
            // Don't use status updates from the AcceptorThread (uses host id)
            // Once we a reconnected let the AcceptorThread start again
            Server.setAccepting(true);
            // Resend updates, just in case a client missed it while disconnected
            // This is needed since this is how the client gets its playerID
            serverService.writeAll(NetworkConstants.GAME_UPDATE);
            for (PlayerID pid : playersList)
            {
                serverService.writeAll(
                    NetworkConstants.PREFIX_PLAYER + pid.getPlayerName() + ":" + pid.getID());
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param msg Error message
     * @param readFrom Device that error was received from
     */
    @Override
    public void handleError(String msg, int readFrom)
    {
        Toast.makeText(this, "Error: " + msg, Toast.LENGTH_SHORT).show();
    }

    // Defines callbacks for service binding, passed to bindService()
    private ServiceConnection mServerConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            // We've bound to ServerService, cast the IBinder and get ServerService instance
            ServerService.LocalBinder binder = (ServerService.LocalBinder) iBinder;
            serverService = binder.getService();
            mServerBound = true;
            addPlayer(name, 0);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mServerBound = false;
        }
    };

    private ServiceConnection mClientConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            // We've bound to ServerService, cast the IBinder and get ServerService instance
            ClientService.LocalBinder binder = (ClientService.LocalBinder) iBinder;
            clientService = binder.getService();
            mClientBound = true;
            clientService.write(NetworkConstants.PREFIX_NAME + name);
            uiConnectionLobby.setTextIP("Host IP is: " + clientService.getHostIP());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mClientBound = false;
        }
    };
}
