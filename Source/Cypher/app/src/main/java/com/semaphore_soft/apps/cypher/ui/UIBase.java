package com.semaphore_soft.apps.cypher.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * Created by Scorple on 2/6/2017.
 */

public abstract class UIBase extends FrameLayout
{
    protected View       view;
    private   UIListener listener;

    public UIBase(Context context)
    {
        super(context);
        createUI();
    }

    public UIBase(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        createUI();
    }

    public UIBase(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        createUI();
    }

    public abstract void createUI();

    protected View makeView(int resource)
    {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        view = inflater.inflate(resource, this, true);
        return view;
    }

    public void setUIListener(UIListener listener)
    {
        this.listener = listener;
    }

    protected void notifyListener(String cmd)
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
