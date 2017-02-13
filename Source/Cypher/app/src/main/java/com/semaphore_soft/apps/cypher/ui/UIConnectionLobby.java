package com.semaphore_soft.apps.cypher.ui;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.semaphore_soft.apps.cypher.CharacterSelectActivity;
import com.semaphore_soft.apps.cypher.R;

import java.util.ArrayList;

import static android.R.attr.host;

/**
 * Created by Scorple on 2/6/2017.
 */

public class UIConnectionLobby extends UIBase
{
    Button btnStart;
    TextView ipAddress;
    TextView txtDisplayName;
    ArrayList<PlayerID> playersList;
    PlayerIDAdapter playerIDAdapter;

    public UIConnectionLobby(Context context)
    {
        super(context);
    }

    public UIConnectionLobby(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public UIConnectionLobby(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void createUI()
    {
        //send over via method
        //ArrayList<PlayerID> playersList = new ArrayList<>() ;

        makeView(R.layout.connection_lobby);

        //define layout elements
        btnStart = (Button) view.findViewById(R.id.btnStart);
        ipAddress = (TextView) view.findViewById(R.id.ip_address);
        txtDisplayName = (TextView) view.findViewById(R.id.txtDisplayName);
        playersList = new ArrayList<>();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recPlayerCardList);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        playerIDAdapter = new PlayerIDAdapter(playersList);

        recyclerView.setAdapter(playerIDAdapter);

    }

    public void setHost(boolean host)
    {
        if (host) {

            btnStart.setEnabled(true);
            btnStart.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    notifyListener("cmd_btnStart");
                }
            });
        }
        else {
            btnStart.setEnabled(false);
        }
    }

    public void setPlayersList(ArrayList<PlayerID> list)
    {
        playersList = list;
        playerIDAdapter.notifyDataSetChanged();
    }

    public void setTextIP(String text) { ipAddress.setText(text); }

    public void setTxtDisplayName(String text) { txtDisplayName.setText(text); }
}
