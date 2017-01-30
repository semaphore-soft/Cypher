package com.semaphore_soft.apps.cypher.ui;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.JoinGameActivity;
import com.semaphore_soft.apps.cypher.R;

import java.util.ArrayList;

/**
 * Created by Scorple on 1/12/2017.
 */

public class GameIDAdapter extends RecyclerView.Adapter<GameIDAdapter.GameIDViewHolder>
{
    JoinGameActivity  parent;
    ArrayList<GameID> gamesList;

    public GameIDAdapter(JoinGameActivity parent, ArrayList<GameID> gamesList)
    {
        this.parent = parent;
        this.gamesList = gamesList;
    }

    @Override
    public GameIDViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
    {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                                      .inflate(R.layout.game_card, viewGroup, false);

        return new GameIDViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GameIDViewHolder holder, int position)
    {
        //get specific category entry
        GameID gameID = gamesList.get(position);

        //attach information to display item
        holder.txtGameName.setText(gameID.gameName);
        //attach listeners to display item
        holder.cardView.setOnClickListener(new GameIDClickListener(gameID));
    }

    @Override
    public int getItemCount()
    {
        return gamesList.size();
    }

    /*public void pushGameID(GameID gameID) {
        gamesList.add(gameID);
    }*/

    public class GameIDViewHolder extends RecyclerView.ViewHolder
    {
        protected CardView cardView;
        protected TextView txtGameName;

        public GameIDViewHolder(View itemView)
        {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            txtGameName = (TextView) itemView.findViewById(R.id.txtGameName);
        }
    }

    private class GameIDClickListener implements View.OnClickListener
    {
        GameID gameID;

        GameIDClickListener(GameID gameID)
        {
            this.gameID = gameID;
        }

        @Override
        public void onClick(View v)
        {
            Toast.makeText(parent, "Game " + gameID.gameName + " selected", Toast.LENGTH_SHORT)
                 .show();
            parent.joinGame(gameID);
        }
    }
}
