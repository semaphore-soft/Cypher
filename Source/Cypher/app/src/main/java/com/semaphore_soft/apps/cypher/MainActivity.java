package com.semaphore_soft.apps.cypher;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

public class MainActivity extends AppCompatActivity implements ConnectFragment.callback
{

    private final static String TAG = "Main";
    // Port should be between 49152-65535
    public final static int SERVER_PORT = 58008;

    @Override
    protected void onCreate(Bundle savedInstanceState)
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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
}
