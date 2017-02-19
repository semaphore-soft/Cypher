package com.semaphore_soft.apps.cypher.game;

/**
 * Created by rickm on 1/19/2017.
 */

public class Entity
{
    private int id;
    private int type;
    private int roomID;

    public Entity(int id)
    {
        this.id = id;
        type = -1;
        roomID = -1;
    }

    public Entity(int id, int type)
    {
        this.id = id;
        this.type = type;
        roomID = -1;
    }

    public int getID()
    {
        return id;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public int getType()
    {
        return type;
    }

    public void setRoom(int roomID)
    {
        this.roomID = roomID;
    }

    public int getRoomID()
    {
        return roomID;
    }
}
