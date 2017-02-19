package com.semaphore_soft.apps.cypher.game;

import java.util.Hashtable;

/**
 * Created by rickm on 1/31/2017.
 */

public class StatusLinked extends Status
{
    private int linkID;

    public StatusLinked(int id, E_STATUS_TYPE type, int effectRating, int linkID)
    {
        super(id, type, effectRating);
        this.linkID = linkID;
    }

    public int getLinkID()
    {
        return linkID;
    }

    public boolean checkLink(Hashtable<Integer, ?> links)
    {
        return links.keySet().contains(linkID);
    }
}
