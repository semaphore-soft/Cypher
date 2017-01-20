package com.semaphore_soft.apps.cypher;

/**
 * Created by rickm on 1/19/2017.
 */

public class Actor
{
    private long id;
    private int  markerID;
    private int  charID;
    private long roomID;

    public Actor(long id)
    {
        this.id = id;
        markerID = -1;
        charID = -1;
        roomID = -1;
    }

    public Actor(long id, int charID)
    {
        this.id = id;
        markerID = -1;
        this.charID = charID;
        roomID = -1;
    }

    public long getId()
    {
        return id;
    }

    public void setMarker(int tagID)
    {
        this.markerID = tagID;
    }

    public int getMarker()
    {
        return markerID;
    }

    public void setChar(int charID)
    {
        this.charID = charID;
    }

    public int getChar()
    {
        return charID;
    }

    public void setRoom(long roomID)
    {
        this.roomID = roomID;
    }

    public long getRoom()
    {
        return roomID;
    }
}
