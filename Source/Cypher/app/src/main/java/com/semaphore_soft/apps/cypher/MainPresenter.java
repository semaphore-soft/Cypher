package com.semaphore_soft.apps.cypher;

/**
 * Created by Scorple on 10/27/2016.
 */
class MainPresenter
{
    private MainActivity view;

    MainPresenter()
    {

    }

    void setView(MainActivity view)
    {
        this.view = view;
        //MainActivityFragment portal = new MainActivityFragment();
        //view.setPortal(portal, 0);
    }
}
