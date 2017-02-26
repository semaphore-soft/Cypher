package com.semaphore_soft.apps.cypher;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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

import org.artoolkit.ar.base.camera.CameraPreferencesActivity;

public class MainActivity extends AppCompatActivity implements GetNameDialogFragment.GetNameDialogListener,
                                                               ConnectFragment.Callback,
                                                               ResponseReceiver.Receiver,
                                                               UIListener
{
    private static ResponseReceiver responseReceiver;
    private static ServerService    serverService;
    private static ClientService    clientService;
    private static boolean mServerBound = false;
    private static boolean mClientBound = false;

    private static boolean host = false;
    private static String  name = "";

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
     * Creates an instance of {@code GetNameDialogFragment}.
     *
     * @see GetNameDialogFragment
     */
    private void showGetNameDialog()
    {
        FragmentManager       fm                    = getSupportFragmentManager();
        GetNameDialogFragment getNameDialogFragment = new GetNameDialogFragment();
        getNameDialogFragment.setListener(this);
        getNameDialogFragment.show(fm, "get_name_dialog");
    }

    /**
     * Creates an instance of {@code ConnectFragment}.
     * @see ConnectFragment
     */
    private void showConnectDialog()
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
            startActivity(new Intent(this, CameraPreferencesActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     * Starts {@code Client}.
     * @see ClientService
     * @see com.semaphore_soft.apps.cypher.networking.Client
     * @param addr Address to connect to
     * @param name Name of the player
     */
    @Override
    public void startClient(String addr, String name)
    {
        MainActivity.name = name;
        // Try to connect to the socket before moving to connection lobby
        clientService.startClient(addr);
    }

    /**
     * {@inheritDoc}
     * Starts {@code Server}.
     * @see ServerService
     * @see com.semaphore_soft.apps.cypher.networking.Server
     * @param name Name of the player
     */
    @Override
    public void onFinishGetName(String name)
    {
        MainActivity.name = name;
        // Start the server thread, which will start
        // the connection lobby when it starts accepting connections
        serverService.startServer();
    }

    /**
     * {@inheritDoc}
     * @param msg Message read from network
     * @param readFrom Device that message was received from
     */
    @Override
    public void handleRead(String msg, int readFrom)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * {@inheritDoc}
     * Moves players to {@code ConnectionLobbyActivity} when client or server
     * notify that they have successfully started a connection.
     * @see ConnectionLobbyActivity
     * @param msg Status update
     * @param readFrom Device that update was received from
     */
    @Override
    public void handleStatus(String msg, int readFrom)
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

    /**
     * {@inheritDoc}
     * Shows alert if client was unable to successfully start a connection.
     * @param msg Error message
     * @param readFrom Device that error was received from
     */
    @Override
    public void handleError(String msg, int readFrom)
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

    /**
     * {@inheritDoc}
     * @param cmd Command from UI interaction
     */
    @Override
    public void onCommand(String cmd)
    {
        switch (cmd)
        {
            case "cmd_btnHost":
            {
                host = true;
                // Bind to ServerService
                Intent intent = new Intent(MainActivity.this, ServerService.class);
                bindService(intent, mServerConnection, Context.BIND_AUTO_CREATE);
                showGetNameDialog();
                break;
            }
            case "cmd_btnJoin":
            {
                host = false;
                // Bind to ClientService
                Intent intent = new Intent(MainActivity.this, ClientService.class);
                bindService(intent, mClientConnection, Context.BIND_AUTO_CREATE);
                showConnectDialog();
                break;
            }
            case "cmd_btnLaunch":
            {
                Toast.makeText(getApplicationContext(), "Launching AR Activity", Toast.LENGTH_SHORT)
                     .show();

                Intent intent = new Intent(getBaseContext(), PortalActivity.class);
                intent.putExtra("host", true);
                intent.putExtra("player", 0);
                intent.putExtra("character", 0);
                startActivity(intent);
                break;
            }
            default:
                Toast.makeText(this, "UI interaction not handled", Toast.LENGTH_SHORT).show();
                break;
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
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mClientBound = false;
        }
    };
}
