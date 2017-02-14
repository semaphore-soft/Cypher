package com.semaphore_soft.apps.cypher;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
    private Intent           mServiceIntent;
    private ResponseReceiver responseReceiver;

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

            addPlayer(name, 0);

            btnStart.setEnabled(true);

            btnStart.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Server.setAccepting(false);
                    // Service intent initialized by addPlayer
                    mServiceIntent.setData(Uri.parse(NetworkConstants.WRITE_ALL));
                    mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA,
                                            NetworkConstants.GAME_START);
                    startService(mServiceIntent);
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
        playerIDAdapter.notifyDataSetChanged();
        if (host)
        {
            mServiceIntent = new Intent(this, ServerService.class);
            mServiceIntent.setData(Uri.parse(NetworkConstants.WRITE_ALL));
            mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA,
                                    NetworkConstants.PF_PLAYER + player + ":" + id);
            startService(mServiceIntent);
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
                    mServiceIntent = new Intent(ConnectionLobbyActivity.this, ClientService.class);
                    mServiceIntent.setData(Uri.parse(NetworkConstants.CLIENT_RECONNECT));
                    startService(mServiceIntent);
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
        // Currently unused
        else if (msg.equals(NetworkConstants.ERROR_DISCONNECT_SERVER))
        {
            mServiceIntent = new Intent(ConnectionLobbyActivity.this, ServerService.class);
            mServiceIntent.setData(Uri.parse(NetworkConstants.SERVER_RECONNECT));
            startService(mServiceIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reconnect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_reconnect)
        {
            if (host)
            {
                mServiceIntent = new Intent(ConnectionLobbyActivity.this, ServerService.class);
                mServiceIntent.setData(Uri.parse(NetworkConstants.SERVER_RECONNECT));
                startService(mServiceIntent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
