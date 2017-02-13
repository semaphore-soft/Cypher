package com.semaphore_soft.apps.cypher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.networking.ClientService;
import com.semaphore_soft.apps.cypher.networking.NetworkConstants;
import com.semaphore_soft.apps.cypher.networking.ResponseReceiver;
import com.semaphore_soft.apps.cypher.networking.ServerService;
import com.semaphore_soft.apps.cypher.ui.PlayerID;
import com.semaphore_soft.apps.cypher.ui.PlayerIDAdapter;
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

public class ConnectionLobbyActivity extends AppCompatActivity implements UIListener
{
    String              name;
    boolean             host;
    long                playerID;
    ArrayList<PlayerID> playersList;

    //private PlayerIDAdapter playerIDAdapter;

    RecyclerView recyclerView;
    private Intent mServiceIntent;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        UIConnectionLobby uiConnectionLobby = new UIConnectionLobby(this);
        ((FrameLayout) findViewById(R.id.empty)).addView(uiConnectionLobby);
        uiConnectionLobby.setUIListener(this);

        // TODO register and unregister in OnResume and OnPause
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(new ResponseReceiver(),
                                               NetworkConstants.getFilter());


        host = getIntent().getBooleanExtra("host", false);

        //TextView txtDisplayName = (TextView) findViewById(R.id.txtDisplayName);

        name = getIntent().getStringExtra("name");

        String welcomeText = "Welcome " + name;
        //txtDisplayName.setText(welcomeText);
        uiConnectionLobby.setTxtDisplayName(welcomeText);

        playersList = new ArrayList<>();
        addTestPlayers();
        uiConnectionLobby.setPlayersList(playersList);

        //playerIDAdapter = new PlayerIDAdapter(this, playersList);
        //recyclerView.setAdapter(playerIDAdapter);
        //Button btnStart = (Button) findViewById(R.id.btnStart);

        if (host)
        {
            playerID = 0;

            String ip = "";
            try
            {
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
                            Log.i("Lobby", ip);
                        }
                    }
                }
            }
            catch (SocketException ex)
            {
                Log.e("Lobby", ex.toString());
            }

            mServiceIntent = new Intent(this, ServerService.class);
            mServiceIntent.setData(Uri.parse(NetworkConstants.SETUP_SERVER));
            startService(mServiceIntent);

            mServiceIntent.setData(Uri.parse(NetworkConstants.WRITE_TO_CLIENT));
            mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA, "Hello, World!");
            mServiceIntent.putExtra(NetworkConstants.INDEX_EXTRA, 0);
            startService(mServiceIntent);

            //TextView ipAddress = (TextView) findViewById(R.id.ip_address);
            //ipAddress.setText("Your IP Address is: " + ip);
            uiConnectionLobby.setTextIP("Your IP Address is: " + ip);

        }
        else
        {
            uiConnectionLobby.setHost(false);

            mServiceIntent = new Intent(this, ClientService.class);
            mServiceIntent.setData(Uri.parse(NetworkConstants.SETUP_CLIENT));
            mServiceIntent
                .putExtra(NetworkConstants.ADDR_EXTRA, getIntent().getStringExtra("address"));
            startService(mServiceIntent);

            mServiceIntent.setData(Uri.parse(NetworkConstants.CLIENT_WRITE));
            mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA, "Hello, World!");
            startService(mServiceIntent);
        }
    }

    private void addTestPlayers()
    {
        for (int i = 0; i < 3; ++i)
        {
            PlayerID playerID = new PlayerID();
            playerID.setID(i);
            playerID.setPlayerName("player" + i);
            //gameIDAdapter.pushGameID(gameID);
            playersList.add(playerID);
        }
    }

    public void onCommand(String cmd)
    {
        switch (cmd) {
            case "cmd_btnStart":
                Toast.makeText(getApplicationContext(), "Moving to Character Select", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getBaseContext(), CharacterSelectActivity.class);
                intent.putExtra("host", host);
                intent.putExtra("player", playerID);
                startActivity(intent);
                break;
            default:
                Toast.makeText(this, "UI interaction not handled", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
