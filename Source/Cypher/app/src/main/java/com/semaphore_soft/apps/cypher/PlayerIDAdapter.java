package com.semaphore_soft.apps.cypher;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Scorple on 1/12/2017.
 */

public class PlayerIDAdapter extends RecyclerView.Adapter<PlayerIDAdapter.PlayerIDViewHolder>
{
    ConnectionLobbyActivity parent;
    ArrayList<PlayerID> playersList;

    PlayerIDAdapter(ConnectionLobbyActivity parent, ArrayList<PlayerID> playersList) {
        this.parent = parent;
        this.playersList = playersList;
    }

    @Override
    public PlayerIDAdapter.PlayerIDViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
    {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.player_card, viewGroup, false);

        return new PlayerIDViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PlayerIDAdapter.PlayerIDViewHolder holder, int position)
    {
        PlayerID playerID = playersList.get(position);

        holder.txtPlayerName.setText(playerID.playerName);
    }

    @Override
    public int getItemCount()
    {
        return playersList.size();
    }

    public class PlayerIDViewHolder extends RecyclerView.ViewHolder {
        protected CardView cardView;
        protected TextView txtPlayerName;

        public PlayerIDViewHolder(View itemView)
        {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            txtPlayerName = (TextView) itemView.findViewById(R.id.txtPlayerName);
        }
    }
}
