package com.semaphore_soft.apps.cypher.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.semaphore_soft.apps.cypher.R;

/**
 * Created by Scorple on 2/6/2017.
 */

public class UIConnectionLobby extends UIBase
{
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
        makeView(R.layout.connection_lobby);

        //define layout elements
    }
}
