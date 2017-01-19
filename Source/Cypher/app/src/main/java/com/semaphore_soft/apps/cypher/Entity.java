package com.semaphore_soft.apps.cypher;

/**
 * Created by rickm on 1/19/2017.
 */

public class Entity
{
    private long id;
    private long roomID;

    public Entity(long id)
    {
        this.id = id;
        this.roomID = -1;
    }

    public long getID()
    {
        return id;
    }

    public void setRoom(int roomID)
    {
        this.roomID = roomID;
    }

    public long getRoomID()
    {
        return roomID;
    }
}
