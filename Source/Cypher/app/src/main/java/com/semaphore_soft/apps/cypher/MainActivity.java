package com.semaphore_soft.apps.cypher;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.widget.Toast;

import com.semaphore_soft.apps.cypher.ui.GetNameDialogFragment;

public class MainActivity extends AppCompatActivity implements GetNameDialogFragment.GetNameDialogListener, ConnectFragment.callback
{
    boolean host = false;

    private final static String TAG = "Main";
    // Port should be between 49152-65535
    public final static int SERVER_PORT = 58008;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(getApplicationContext(), "Launching AR Activity", Toast.LENGTH_SHORT)
                        .show();

                Intent intent = new Intent(getBaseContext(), PortalActivity.class);
                intent.putExtra("host", true);
                intent.putExtra("player", 0);
                intent.putExtra("character", 0);
                startActivity(intent);
            }
        });

        // Allow network connections
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);

        final ConnectFragment connectFragment = new ConnectFragment();
        connectFragment.setListener(this);

        Button findGame = (Button) findViewById(R.id.connect);
        findGame.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                connectFragment.show(getFragmentManager(), "dialog");
            }
        });


        Button btnHost = (Button) findViewById(R.id.btnHost);

        btnHost.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                host = true;
                showGetNameDialog();
            }
        });

        Button hostGame = (Button) findViewById(R.id.host);
        hostGame.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String ip = "";
                try
                {
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
                    {
                        NetworkInterface ni = en.nextElement();
                        for (Enumeration<InetAddress> addresses = ni.getInetAddresses(); addresses.hasMoreElements();)
                        {
                            InetAddress inetAddress = addresses.nextElement();
                            // Limit IP addresses shown to IPv4                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address)
                            {
                                ip = inetAddress.getHostAddress();
                                Log.i(TAG, ip);
                            }
                        }
                    }
                }
                catch (SocketException ex)
                {
                    Log.e(TAG, ex.toString());
                }
                // ServerThread is not static so it requires an instance of the outer class
                new Thread(new DeviceThreads(MainActivity.this).new ServerThread()).start();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("IP Address");
                builder.setMessage("Use this address to connect:\n" + ip);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
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
        });

        Button btnJoin = (Button) findViewById(R.id.btnJoin);

        btnJoin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                host = false;
                showGetNameDialog();
            }
        });
    }

    public void showGetNameDialog()
    {
        FragmentManager       fm                    = getSupportFragmentManager();
        GetNameDialogFragment getNameDialogFragment = new GetNameDialogFragment();
        getNameDialogFragment.setListener(this);
        getNameDialogFragment.show(fm, "get_name_dialog");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings)
        {
            return true;
        }
        else if (id == R.id.reset_list)
        {
            TextView tv = (TextView) findViewById(R.id.test);
            tv.setText("Label");
            Log.d(TAG, "Reset textview");
        }
        else if (id == R.id.write_message)
        {
            DeviceThreads.write("Test string");
            Log.d(TAG, "Send message");
        }

        return super.onOptionsItemSelected(item);
    }

    public void setLabel(String str)
    {
        TextView tv = (TextView) findViewById(R.id.test);
        tv.setText(str);
    }

    public void toasts(String str)
    {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void doNetwork(InetAddress addr)
    {
        // ClientThread is not static so it requires an instance of the outer class
        new Thread(new DeviceThreads(this).new ClientThread(addr)).start();
    }

    @Override
    public void onFinishGetName(String name)
    {
        if (host)
        {
            Toast.makeText(getApplicationContext(),
                           "Moving to Connection Lobby",
                           Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getBaseContext(), ConnectionLobbyActivity.class);
            intent.putExtra("host", host);
            intent.putExtra("name", name);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Moving to Join Game Lobby", Toast.LENGTH_SHORT)
                 .show();
            Intent intent = new Intent(getBaseContext(), JoinGameActivity.class);
            intent.putExtra("name", name);
            startActivity(intent);
        }
    }
}
