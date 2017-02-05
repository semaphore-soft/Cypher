package com.semaphore_soft.apps.cypher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.TextView;
import android.widget.Toast;

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

public class ConnectionLobbyActivity extends AppCompatActivity
{
    String              name;
    boolean             host;
    long                playerID;
    ArrayList<PlayerID> playersList;

    private PlayerIDAdapter playerIDAdapter;

    RecyclerView recyclerView;
    private Intent mServiceIntent;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_lobby);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        IntentFilter statusIntentFilter = new IntentFilter(NetworkingService.BROADCAST_MESSAGE);
        // TODO register and unregister in OnResume and OnPause
        LocalBroadcastManager.getInstance(this).registerReceiver(new ResponseReceiver(), statusIntentFilter);

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
        addTestPlayers();

        playerIDAdapter = new PlayerIDAdapter(this, playersList);

        recyclerView.setAdapter(playerIDAdapter);

        Button btnStart = (Button) findViewById(R.id.btnStart);

        if (host)
        {
            playerID = 0;

            String ip = "";
            try
            {
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
                        }
                    }
                }
            }
            catch (SocketException ex)
            {
                Log.e("Lobby", ex.toString());
            }

//            threads.startAcceptor();
//            threads.writeToClient("Hello", 0);
            
            mServiceIntent = new Intent(this, NetworkingService.class);
            mServiceIntent.setData(Uri.parse(NetworkingService.SETUP_SERVER));
            startService(mServiceIntent);

            mServiceIntent.setData(Uri.parse(NetworkingService.WRITE_TO_CLIENT));
            mServiceIntent.putExtra("message", "Hello, World!");
            mServiceIntent.putExtra("index", 0);
            startService(mServiceIntent);

            TextView ipAddress = (TextView) findViewById(R.id.ip_address);
            ipAddress.setText("Your IP Address is: " + ip);

            btnStart.setEnabled(true);

            btnStart.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Snackbar.make(view, "Moving to Character Select", Snackbar.LENGTH_LONG).show();
                    Intent intent = new Intent(getBaseContext(), CharacterSelectActivity.class);
                    intent.putExtra("host", host);
                    intent.putExtra("player", playerID);
                    startActivity(intent);
                }
            });
        }
        else
        {
            btnStart.setEnabled(false);

//            try
//            {
//                InetAddress addr = InetAddress.getByName(getIntent().getStringExtra("address"));
            mServiceIntent = new Intent(this, NetworkingService.class);
            mServiceIntent.setData(Uri.parse(NetworkingService.SETUP_CLIENT));
            mServiceIntent.putExtra("address", getIntent().getStringExtra("address"));
            startService(mServiceIntent);

            mServiceIntent.setData(Uri.parse(NetworkingService.CLIENT_WRITE));
            mServiceIntent.putExtra("message", "Hello, World!");
            startService(mServiceIntent);
//                DeviceThreads.ClientThread client = threads.startClient(addr);
//                client.write("Hello");

//            }
//            catch (UnknownHostException e)
//            {
//                e.printStackTrace();
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setTitle("Error");
//                builder.setMessage("Unable to connect to host \nPlease try again");
//                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
//                {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i)
//                    {
//                        dialogInterface.dismiss();
//                    }
//                });
//                AlertDialog alert = builder.create();
//                alert.show();
//            }
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

    public void toasts(String str)
    {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private class ResponseReceiver extends BroadcastReceiver
    {
        // Prevents instantiation
        private ResponseReceiver()
        {
        }

        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            Log.d("BR", action);
            if (NetworkingService.BROADCAST_MESSAGE.equals(action))
            {
                Log.i("BR", intent.getStringExtra(NetworkingService.MESSAGE));
            }
        }
    }
}
