package com.semaphore_soft.apps.cypher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.networking.ClientService;
import com.semaphore_soft.apps.cypher.networking.NetworkConstants;
import com.semaphore_soft.apps.cypher.networking.ResponseReceiver;
import com.semaphore_soft.apps.cypher.networking.ServerService;

/**
 * Created by Scorple on 1/9/2017.
 */

public class CharacterSelectActivity extends AppCompatActivity implements ResponseReceiver.Receiver
{
    private boolean          host;
    private int              playerID;
    private ResponseReceiver responseReceiver;
    private int              numClients;
    private int playersReady = 0;
    private Button        btnGo;
    private TextView      status;
    private RadioButton   char0;
    private RadioButton   char1;
    private RadioButton   char2;
    private RadioButton   char3;
    private ServerService serverService;
    private ClientService clientService;
    private boolean mServerBound  = false;
    private boolean mClientBound  = false;
    private boolean ready         = false;
    private Handler handler       = new Handler();
    private boolean sendHeartbeat = true;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.character_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        responseReceiver = new ResponseReceiver();
        responseReceiver.setListener(this);
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(responseReceiver, NetworkConstants.getFilter());

        host = getIntent().getBooleanExtra("host", false);

        playerID = getIntent().getIntExtra("player", 0);

        status = (TextView) findViewById(R.id.groupStatus);

        btnGo = (Button) findViewById(R.id.btnGo);
        if (host)
        {
            numClients = getIntent().getIntExtra("numClients", 0);
            // Include host when displaying connected players
            status.setText(1 + "/" + (numClients + 1) + " connected");
            // Make sure host can't start game until everyone has picked a character,
            // unless no clients are connected
            if (numClients != 0)
            {
                btnGo.setEnabled(false);
            }
        }


        char0 = (RadioButton) findViewById(R.id.char0);
        char0.setChecked(true);
        char1 = (RadioButton) findViewById(R.id.char1);
        char2 = (RadioButton) findViewById(R.id.char2);
        char3 = (RadioButton) findViewById(R.id.char3);

        btnGo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (host)
                {
                    serverService.writeAll(NetworkConstants.GAME_AR_START);
                    startAR();
                }
                else
                {
                    if (!ready)
                    {
                        clientService.write(NetworkConstants.GAME_READY);
                        status.setText("Waiting for host...");
                        btnGo.setText("Cancel");
                        ready = true;
                    }
                    else
                    {
                        clientService.write(NetworkConstants.GAME_UNREADY);
                        status.setText("");
                        btnGo.setText("GO!");
                        ready = false;
                    }
                }
            }
        });
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
            handler.postDelayed(heartbeat, NetworkConstants.HEARTBEAT_DELAY);
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
        // Unbind from the services
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
        sendHeartbeat = false;
    }

    private void startAR()
    {
        Toast.makeText(CharacterSelectActivity.this, "Starting AR Activity", Toast.LENGTH_SHORT)
             .show();
        LocalBroadcastManager.getInstance(CharacterSelectActivity.this)
                             .unregisterReceiver(responseReceiver);
        Intent intent = new Intent(getBaseContext(), PortalActivity.class);
        intent.putExtra("host", host);
        intent.putExtra("player", playerID);
        if (char0.isChecked())
        {
            intent.putExtra("character", "knight");
        }
        else if (char1.isChecked())
        {
            intent.putExtra("character", "soldier");
        }
        else if (char2.isChecked())
        {
            intent.putExtra("character", "ranger");
        }
        else if (char3.isChecked())
        {
            intent.putExtra("character", "wizard");
        }
        startActivity(intent);
    }

    Runnable heartbeat = new Runnable()
    {
        @Override
        public void run()
        {
            if (sendHeartbeat)
            {
                serverService.writeAll(NetworkConstants.GAME_HEARTBEAT);
                handler.postDelayed(heartbeat, NetworkConstants.HEARTBEAT_DELAY);
            }
        }
    };

    @Override
    public void handleRead(String msg, int readFrom)
    {
        Toast.makeText(this, "Read: " + msg, Toast.LENGTH_SHORT).show();
        if (msg.equals(NetworkConstants.GAME_READY))
        {
            playersReady++;
            // Since default value is 0, allow host to start game
            // even if numClients == 0 and clients are connected
            if (playersReady >= numClients)
            {
                btnGo.setEnabled(true);
            }
            // Include host when displaying connected players
            status.setText((playersReady + 1) + "/" + (numClients + 1) + " connected");
        }
        else if (msg.equals(NetworkConstants.GAME_UNREADY))
        {
            playersReady--;
            if (playersReady < numClients)
            {
                btnGo.setEnabled(false);
            }
            // Include host when displaying connected players
            status.setText((playersReady + 1) + "/" + (numClients + 1) + " connected");
        }
        else if (msg.equals(NetworkConstants.GAME_AR_START))
        {
            startAR();
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
            clientService.reconnect();
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
