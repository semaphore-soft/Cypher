package com.semaphore_soft.apps.cypher.game;

import java.util.Hashtable;

/**
 * Created by rickm on 1/31/2017.
 */

public class StatusLinked extends Status
{
    private long linkID;

    public StatusLinked(long id, E_STATUS_TYPE type, int effectRating, long linkID)
    {
        super(id, type, effectRating);
        this.linkID = linkID;
    }

    public long getLinkID()
    {
        return linkID;
    }

    public boolean checkLink(Hashtable<Long, ?> links)
    {
        return links.keySet().contains(linkID);
    }
}
