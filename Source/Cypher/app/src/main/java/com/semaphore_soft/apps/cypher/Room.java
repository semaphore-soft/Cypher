package com.semaphore_soft.apps.cypher;

import java.util.ArrayList;

/**
 * Created by rickm on 1/19/2017.
 */

public class Room
{
    private long            id;
    private int             tagID;
    private ArrayList<Long> residentActorIDs;
    private ArrayList<Long> residentEntityIDs;

    public Room(long id)
    {
        this.id = id;
        tagID = -1;
        residentActorIDs = new ArrayList<>();
        residentEntityIDs = new ArrayList<>();
    }

    public long getId()
    {
        return id;
    }

    public void setTag(int tagID)
    {
        this.tagID = tagID;
    }

    public int getTag()
    {
        return tagID;
    }

    public void addActor(long actorID)
    {
        if (!residentActorIDs.contains(actorID))
        {
            residentActorIDs.add(actorID);
        }
    }

    public void removeActor(long actorID)
    {
        if (residentActorIDs.contains(actorID))
        {
            residentActorIDs.remove(actorID);
        }
    }

    public ArrayList<Long> getResidentActors()
    {
        return residentActorIDs;
    }

    public void addEntity(long entityID)
    {
        if (!residentEntityIDs.contains(entityID))
        {
            residentEntityIDs.add(entityID);
        }
    }

    public void removeEntity(long entityID)
    {
        if (residentEntityIDs.contains(entityID))
        {
            residentEntityIDs.remove(entityID);
        }
    }

    public ArrayList<Long> getResidentEntities()
    {
        return residentEntityIDs;
    }
}
