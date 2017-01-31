package com.semaphore_soft.apps.cypher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.ui.GameID;
import com.semaphore_soft.apps.cypher.ui.GameIDAdapter;

import java.util.ArrayList;

/**
 * Created by Scorple on 1/12/2017.
 */

public class JoinGameActivity extends AppCompatActivity
{
    String            name;
    ArrayList<GameID> gamesList;

    private GameIDAdapter gameIDAdapter;

    RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView txtDisplayName = (TextView) findViewById(R.id.txtDisplayName);

        name = getIntent().getStringExtra("name");

        String welcomeText = "Welcome " + name;
        txtDisplayName.setText(welcomeText);

        recyclerView = (RecyclerView) findViewById(R.id.recGameCardList);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        gamesList = new ArrayList<>();
        addTestGames();

        gameIDAdapter = new GameIDAdapter(this, gamesList);

        recyclerView.setAdapter(gameIDAdapter);
    }

    public void joinGame(GameID gameID)
    {
        Toast.makeText(getApplicationContext(), "Moving to Connection Lobby", Toast.LENGTH_SHORT)
             .show();
        Intent intent = new Intent(getBaseContext(), ConnectionLobbyActivity.class);
        intent.putExtra("name", name);
        startActivity(intent);
    }

    private void addTestGames()
    {
        for (int i = 0; i < 3; ++i)
        {
            GameID gameID = new GameID();
            gameID.setID(i);
            gameID.setGameName("game" + i);
            //gameIDAdapter.pushGameID(gameID);
            gamesList.add(gameID);
        }
    }
}
