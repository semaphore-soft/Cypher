package com.semaphore_soft.apps.cypher.ui;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.semaphore_soft.apps.cypher.R;

import java.util.ArrayList;

/**
 * Created by Scorple on 2/6/2017.
 */

public class UIConnectionLobby extends UIBase
{
    Button              btnStart;
    TextView            ipAddress;
    TextView            txtDisplayName;
    ArrayList<PlayerID> playersList;
    PlayerIDAdapter     playerIDAdapter;

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
        btnStart = (Button) findViewById(R.id.btnStart);
        ipAddress = (TextView) findViewById(R.id.ip_address);
        txtDisplayName = (TextView) findViewById(R.id.txtDisplayName);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recPlayerCardList);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        playersList = new ArrayList<>();
        playerIDAdapter = new PlayerIDAdapter(playersList);
        recyclerView.setAdapter(playerIDAdapter);
    }

    public void setHost(boolean host)
    {
        if (host)
        {

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
        else
        {
            btnStart.setEnabled(false);
        }
    }

    public void setPlayersList(ArrayList<PlayerID> list)
    {
        for (PlayerID playerID : playersList)
        {
            playersList.remove(playerID);
        }
        for (PlayerID playerID : list)
        {
            playersList.add(playerID);
        }
        playerIDAdapter.notifyDataSetChanged();
    }

    public void setTextIP(String text)
    {
        ipAddress.setText(text);
    }

    public void setTxtDisplayName(String text)
    {
        txtDisplayName.setText(text);
    }
}
