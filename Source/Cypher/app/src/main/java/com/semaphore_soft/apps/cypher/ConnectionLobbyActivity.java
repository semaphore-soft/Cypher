package com.semaphore_soft.apps.cypher;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.semaphore_soft.apps.cypher.ui.PlayerID;
import com.semaphore_soft.apps.cypher.ui.PlayerIDAdapter;

import java.util.ArrayList;

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

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_lobby);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        host = getIntent().getBooleanExtra("host", false);

        TextView txtDisplayName = (TextView) findViewById(R.id.txtDisplayName);

        name = getIntent().getStringExtra("name");

        String welcomeText = "Welcome" + name;
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
}
