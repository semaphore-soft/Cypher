package com.semaphore_soft.apps.cypher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by Scorple on 1/9/2017.
 */

public class ConnectionLobbyActivity extends AppCompatActivity implements ResponseReceiver.Receiver,
                                                                          UIListener
{
    String              name;
    boolean             host;
    long                playerID;
    ArrayList<PlayerID> playersList;

    private Intent           mServiceIntent;
    private ResponseReceiver responseReceiver;

    private UIConnectionLobby uiConnectionLobby;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty);

        uiConnectionLobby = new UIConnectionLobby(this);
        ((FrameLayout) findViewById(R.id.empty)).addView(uiConnectionLobby);
        uiConnectionLobby.setUIListener(this);

        // TODO register and unregister in OnResume and OnPause
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
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
                {
                    NetworkInterface ni = en.nextElement();
                    for (Enumeration<InetAddress> addresses = ni.getInetAddresses(); addresses.hasMoreElements();)
                    {
                        InetAddress inetAddress = addresses.nextElement();
                        // Limit IP addresses shown to IPv4
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address)
                        {
                            ip = inetAddress.getHostAddress();
                            Log.i("Lobby", ip);
                            break outerloop;
                        }
                    }
                }
            }
            catch (SocketException ex)
            {
                Log.e("Lobby", ex.toString());
            }

            uiConnectionLobby.setTextIP("Your IP Address is: " + ip);

            addPlayer(name, 0);
        }
        else
        {
            mServiceIntent = new Intent(this, ClientService.class);
            mServiceIntent.setData(Uri.parse(NetworkConstants.CLIENT_WRITE));
            mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA, NetworkConstants.PF_NAME + name);
            startService(mServiceIntent);
        }
    }

    private void addPlayer(String player, int id)
    {
        PlayerID playerID = new PlayerID();
        playerID.setID(id);
        playerID.setPlayerName(player);
        playersList.add(playerID);
        uiConnectionLobby.setPlayersList(playersList);
        if (host)
        {
            mServiceIntent = new Intent(this, ServerService.class);
            mServiceIntent.setData(Uri.parse(NetworkConstants.WRITE_ALL));
            mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA,
                                    NetworkConstants.PF_PLAYER + player + ":" + id);
            startService(mServiceIntent);
        }
    }

    public void onCommand(String cmd)
    {
        switch (cmd) {
            case "cmd_btnStart":
                Server.setAccepting(false);
                mServiceIntent.setData(Uri.parse(NetworkConstants.WRITE_ALL));
                mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA,
                                        NetworkConstants.GAME_START);
                startService(mServiceIntent);
                LocalBroadcastManager.getInstance(ConnectionLobbyActivity.this)
                                     .unregisterReceiver(responseReceiver);
                Toast.makeText(ConnectionLobbyActivity.this,
                               "Moving to Character Select",
                               Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), CharacterSelectActivity.class);
                intent.putExtra("host", host);
                intent.putExtra("player", (long) 0);
                intent.putExtra("numPlayers", (int) playerID);
                startActivity(intent);
                break;
            default:
                Toast.makeText(this, "UI interaction not handled", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void handleRead(String msg)
    {
        Toast.makeText(this, "Read: " + msg, Toast.LENGTH_SHORT).show();
        if (msg.equals(NetworkConstants.GAME_START))
        {
            // Start character select activity after host has started game
            Intent intent = new Intent(getBaseContext(), CharacterSelectActivity.class);
            intent.putExtra("host", host);
            intent.putExtra("player", playerID);
            startActivity(intent);
        }
        else if (msg.startsWith(NetworkConstants.PF_NAME))
        {
            // add players on server
            addPlayer(msg.substring(5), (int) ++playerID);
        }
        else if (msg.startsWith(NetworkConstants.PF_PLAYER))
        {
            // add players on client
            String args[] = msg.split(":");
            addPlayer(args[1], Integer.valueOf(args[2]));
        }
    }

    @Override
    public void handleStatus(String msg)
    {
        Toast.makeText(this, "Status: " + msg, Toast.LENGTH_SHORT).show();
        if (msg.equals(NetworkConstants.STATUS_SERVER_START))
        {
            // Update client with all connected players
            mServiceIntent = new Intent(this, ServerService.class);
            mServiceIntent.setData(Uri.parse(NetworkConstants.WRITE_TO_CLIENT));
            // this is probably a terrible way to get the client, but it works
            mServiceIntent.putExtra(NetworkConstants.INDEX_EXTRA, (int) playerID);
            for (PlayerID pid : playersList)
            {
                mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA,
                                        NetworkConstants.PF_PLAYER + pid.getPlayerName() + ":" +
                                        pid.getID());
                startService(mServiceIntent);
            }
        }
    }

    @Override
    public void handleError(String msg)
    {
        Toast.makeText(this, "Error: " + msg, Toast.LENGTH_SHORT).show();
    }
}
