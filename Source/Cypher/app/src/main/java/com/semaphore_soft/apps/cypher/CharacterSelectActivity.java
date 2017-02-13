package com.semaphore_soft.apps.cypher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
    boolean host;
    long    playerID;
    private ResponseReceiver responseReceiver;
    private int              numPlayers;
    private int playersReady = 0;
    private Button      btnGo;
    private TextView    status;
    private RadioButton char0;
    private RadioButton char1;
    private RadioButton char2;
    private RadioButton char3;

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

        playerID = getIntent().getLongExtra("player", 0);

        status = (TextView) findViewById(R.id.groupStatus);

        btnGo = (Button) findViewById(R.id.btnGo);
        if (host)
        {
            // Make sure host can't start game until everyone has picked a character
            btnGo.setEnabled(false);
            numPlayers = getIntent().getIntExtra("numPlayers", 0);
            // Include host when displaying connected players
            status.setText(1 + "/" + (numPlayers + 1) + " connected");
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
                    Intent mServiceIntent =
                        new Intent(CharacterSelectActivity.this, ServerService.class);
                    mServiceIntent.setData(Uri.parse(NetworkConstants.WRITE_ALL));
                    mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA,
                                            NetworkConstants.GAME_AR_START);
                    startService(mServiceIntent);
                    startAR();
                }
                else
                {
                    Intent mServiceIntent =
                        new Intent(CharacterSelectActivity.this, ClientService.class);
                    mServiceIntent.setData(Uri.parse(NetworkConstants.CLIENT_WRITE));
                    mServiceIntent.putExtra(NetworkConstants.MSG_EXTRA,
                                            NetworkConstants.GAME_READY);
                    startService(mServiceIntent);
                    status.setText("Waiting for host...");
                }
            }
        });
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

    @Override
    public void handleRead(String msg)
    {
        Toast.makeText(this, "Read: " + msg, Toast.LENGTH_SHORT).show();
        if (msg.equals(NetworkConstants.GAME_READY))
        {
            playersReady++;
            // Since default value is 0, allow host to start game even if numPlayers == 0
            if (playersReady >= numPlayers)
            {
                btnGo.setEnabled(true);
            }
            // Include host when displaying connected players
            status.setText((playersReady + 1) + "/" + (numPlayers + 1) + " connected");
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
    }
}
