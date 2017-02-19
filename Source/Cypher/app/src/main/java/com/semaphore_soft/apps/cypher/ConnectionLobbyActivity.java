package com.semaphore_soft.apps.cypher;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.networking.ClientService;
import com.semaphore_soft.apps.cypher.networking.NetworkConstants;
import com.semaphore_soft.apps.cypher.networking.ResponseReceiver;
import com.semaphore_soft.apps.cypher.networking.Server;
import com.semaphore_soft.apps.cypher.networking.ServerService;
import com.semaphore_soft.apps.cypher.ui.PlayerID;
import com.semaphore_soft.apps.cypher.ui.PlayerIDAdapter;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by Scorple on 1/9/2017.
 */

public class ConnectionLobbyActivity extends AppCompatActivity implements ResponseReceiver.Receiver
{
    String              name;
    boolean             host;
    long                playerID;
    ArrayList<PlayerID> playersList;

    private PlayerIDAdapter playerIDAdapter;

    RecyclerView recyclerView;
    private ResponseReceiver responseReceiver;
    private ServerService    serverService;
    private ClientService    clientService;
    private boolean mServerBound = false;
    private boolean mClientBound = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_lobby);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        responseReceiver = new ResponseReceiver();
        responseReceiver.setListener(this);
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(responseReceiver, NetworkConstants.getFilter());


        host = getIntent().getBooleanExtra("host", false);

        TextView txtDisplayName = (TextView) findViewById(R.id.txtDisplayName);

        name = getIntent().getStringExtra("name");

        String welcomeText = "Welcome " + name;
        txtDisplayName.setText(welcomeText);

        recyclerView = (RecyclerView) findViewById(R.id.recPlayerCardList);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        playersList = new ArrayList<>();

        playerIDAdapter = new PlayerIDAdapter(this, playersList);

        recyclerView.setAdapter(playerIDAdapter);

        Button btnStart = (Button) findViewById(R.id.btnStart);

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

            TextView ipAddress = (TextView) findViewById(R.id.ip_address);
            ipAddress.setText("Your IP Address is: " + ip);

            btnStart.setEnabled(true);

            btnStart.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Server.setAccepting(false);
                    serverService.writeAll(NetworkConstants.GAME_START);
                    LocalBroadcastManager.getInstance(ConnectionLobbyActivity.this).unregisterReceiver(responseReceiver);
                    Toast.makeText(ConnectionLobbyActivity.this,
                                   "Moving to Character Select",
                                   Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getBaseContext(), CharacterSelectActivity.class);
                    intent.putExtra("host", host);
                    intent.putExtra("player", (long) 0);
                    intent.putExtra("numClients", (int) playerID);
                    startActivity(intent);
                }
            });
        }
        else
        {
            btnStart.setEnabled(false);

            TextView ipAddress = (TextView) findViewById(R.id.ip_address);
            ipAddress.setVisibility(View.GONE);
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

    private void addPlayer(String player, int id)
    {
        PlayerID playerID = new PlayerID();
        playerID.setID(id);
        playerID.setPlayerName(player);
        playersList.add(playerID);
        playerIDAdapter.notifyDataSetChanged();
        if (host)
        {
            // Update clients with all connected players
            serverService.writeAll(NetworkConstants.GAME_UPDATE);
            for (PlayerID pid : playersList)
            {
                serverService.writeAll(
                    NetworkConstants.PF_PLAYER + pid.getPlayerName() + ":" + pid.getID());
            }
        }
    }

    @Override
    public void handleRead(String msg, int readFrom)
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
        else if (msg.equals(NetworkConstants.GAME_UPDATE))
        {
            playersList.clear();
        }
    }

    @Override
    public void handleStatus(String msg)
    {
        Toast.makeText(this, "Status: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handleError(String msg)
    {
        Toast.makeText(this, "Error: " + msg, Toast.LENGTH_SHORT).show();
        if (msg.equals(NetworkConstants.ERROR_DISCONNECT_CLIENT))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error");
            builder.setMessage("Connection lost. Retry?");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    clientService.reconnect();
                    playersList.clear();
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        else if (msg.equals(NetworkConstants.ERROR_DISCONNECT_SERVER))
        {
            serverService.reconnect();
        }
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
            clientService.write(NetworkConstants.PF_NAME + name);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mClientBound = false;
        }
    };
}
