package com.semaphore_soft.apps.cypher;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.networking.ClientService;
import com.semaphore_soft.apps.cypher.networking.NetworkConstants;
import com.semaphore_soft.apps.cypher.networking.ResponseReceiver;
import com.semaphore_soft.apps.cypher.networking.ServerService;
import com.semaphore_soft.apps.cypher.ui.ConnectFragment;
import com.semaphore_soft.apps.cypher.ui.GetNameDialogFragment;
import com.semaphore_soft.apps.cypher.ui.UIListener;
import com.semaphore_soft.apps.cypher.ui.UIMainActivity;

public class MainActivity extends AppCompatActivity implements GetNameDialogFragment.GetNameDialogListener,
                                                               ConnectFragment.Callback,
                                                               ResponseReceiver.Receiver,
                                                               UIListener
{
    boolean host = false;
    private String name = "";
    private ResponseReceiver responseReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty);

        UIMainActivity UIMainActivity = new UIMainActivity(this);
        ((FrameLayout) findViewById(R.id.empty)).addView(UIMainActivity);
        UIMainActivity.setUIListener(this);

        setSupportActionBar(UIMainActivity.getToolbar());

        // Allow network connections
        StrictMode.ThreadPolicy policy =
            new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);

        responseReceiver = new ResponseReceiver();
        responseReceiver.setListener(this);
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(responseReceiver, NetworkConstants.getFilter());
    }

    public void showGetNameDialog()
    {
        FragmentManager       fm                    = getSupportFragmentManager();
        GetNameDialogFragment getNameDialogFragment = new GetNameDialogFragment();
        getNameDialogFragment.setListener(this);
        getNameDialogFragment.show(fm, "get_name_dialog");
    }

    public void showConnectDialog()
    {
        FragmentManager fm              = getSupportFragmentManager();
        ConnectFragment connectFragment = new ConnectFragment();
        connectFragment.setListener(this);
        connectFragment.show(fm, "connect_dialog");
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void startClient(String addr, String name)
    {
        this.name = name;
        // Try to connect to the socket before moving to connection lobby
        Intent mServiceIntent = new Intent(this, ClientService.class);
        mServiceIntent.setData(Uri.parse(NetworkConstants.SETUP_CLIENT));
        mServiceIntent.putExtra(NetworkConstants.ADDR_EXTRA, addr);
        startService(mServiceIntent);
    }

    @Override
    public void onFinishGetName(String name)
    {
        this.name = name;
        // Start the server service, which will start
        // the connection lobby when it starts accepting connections
        Intent mServiceIntent = new Intent(this, ServerService.class);
        mServiceIntent.setData(Uri.parse(NetworkConstants.SETUP_SERVER));
        startService(mServiceIntent);
    }

    @Override
    public void handleRead(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handleStatus(String msg)
    {
        if (msg.equals(NetworkConstants.STATUS_CLIENT_CONNECT) ||
            msg.equals(NetworkConstants.STATUS_SERVER_WAIT))
        {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(responseReceiver);
            Toast.makeText(getApplicationContext(),
                           "Moving to Connection Lobby",
                           Toast.LENGTH_SHORT)
                 .show();
            Intent intent = new Intent(getBaseContext(), ConnectionLobbyActivity.class);
            intent.putExtra("host", host);
            intent.putExtra("name", name);
            startActivity(intent);
        }
    }

    @Override
    public void handleError(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        if (msg.equals(NetworkConstants.ERROR_CLIENT_SOCKET))
        {
            // Show a dialog to inform the user that the connection couldn't be made
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error");
            builder.setMessage("Unable to connect to host \nPlease try again");
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
    }

    @Override
    public void onCommand(String cmd)
    {
        switch (cmd)
        {
            case "cmd_btnHost":
                host = true;
                showGetNameDialog();
                break;
            case "cmd_btnJoin":
                host = false;
                showGetNameDialog();
                break;
            case "cmd_btnLaunch":
                Toast.makeText(getApplicationContext(), "Launching AR Activity", Toast.LENGTH_SHORT)
                     .show();

                Intent intent = new Intent(getBaseContext(), PortalActivity.class);
                intent.putExtra("host", true);
                intent.putExtra("player", 0);
                intent.putExtra("character", 0);
                startActivity(intent);
                break;
            default:
                Toast.makeText(this, "UI interaction not handled", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
