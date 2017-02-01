package com.semaphore_soft.apps.cypher.ui;

/**
 * Created by Scorple on 1/12/2017.
 */

public class PlayerID
{
    protected int    _id;
    protected String playerName;

    public int getID()
    {
        return _id;
    }

    public void setID(int id)
    {
        this._id = id;
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public void setPlayerName(String playerName)
    {
        this.playerName = playerName;
    }
}