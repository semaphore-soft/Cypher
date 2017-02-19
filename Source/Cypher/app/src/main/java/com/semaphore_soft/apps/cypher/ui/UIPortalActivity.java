package com.semaphore_soft.apps.cypher.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.semaphore_soft.apps.cypher.R;

/**
 * Created by Scorple on 2/17/2017.
 */

public class UIPortalActivity extends UIBase
{
    public UIPortalActivity(Context context)
    {
        super(context);
    }

    public UIPortalActivity(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public UIPortalActivity(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void createUI()
    {
        makeView(R.layout.main_portal);
    }
}
