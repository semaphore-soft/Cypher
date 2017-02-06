package com.semaphore_soft.apps.cypher.ui;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.R;

/**
 * Created by Crud Stuntley on 2/5/2017.
 */

public class MainActivityView extends RelativeLayout
{
    private UIListener listener;

    public MainActivityView(Context context)
    {
        super(context);
        init();
    }

    public MainActivityView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        init();
    }

    public MainActivityView(Context context, AttributeSet attributeSet, int style)
    {
        super(context, attributeSet, style);
        init();
    }

    public void setUIListener(UIListener listener)
    {
        this.listener = listener;
    }

    public Toolbar getToolbar()
    {
        return (Toolbar) findViewById(R.id.toolbar);
    }

    public void init()
    {
        //all buttons live here
        //define on click
        //call UIListener method
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View           view     = inflater.inflate(R.layout.activity_main, this, true);
        //inflate(getContext(), R.layout.activity_main, null);

        //Host Button
        Button btnHost = (Button) view.findViewById(R.id.btnHost);
        btnHost.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                sendCommand("cmd_btnHost");
            }
        });

        //Join Button
        Button btnJoin = (Button) view.findViewById(R.id.btnJoin);
        btnJoin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                sendCommand("cmd_btnJoin");
            }
        });

        //Launch Button
        Button btnLaunch = (Button) view.findViewById(R.id.btnLaunch);
        btnLaunch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                sendCommand("cmd_btnLaunch");
            }
        });
    }

    private void sendCommand(String cmd)
    {
        if (listener != null)
        {
            listener.onCommand(cmd);
        }
        else
        {
            Toast.makeText(getContext(), "UI listener is null", Toast.LENGTH_SHORT).show();
        }
    }
}
