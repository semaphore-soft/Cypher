package com.semaphore_soft.apps.cypher;

import java.util.ArrayList;

import static com.semaphore_soft.apps.cypher.Room.E_WALL_TYPE.NO_DOOR;
import static com.semaphore_soft.apps.cypher.Room.E_WALL_TYPE.DOOR_UNLOCKED;
import static com.semaphore_soft.apps.cypher.Room.E_WALL_TYPE.DOOR_OPEN;
import static com.semaphore_soft.apps.cypher.Room.E_WALL_TYPE.DOOR_LOCKED;

/**
 * Created by rickm on 1/19/2017.
 */

public class Room
{
    public static final short WALL_TOP    = 0;
    public static final short WALL_RIGHT  = 1;
    public static final short WALL_BOTTOM = 2;
    public static final short WALL_LEFT   = 3;

    public enum E_WALL_TYPE
    {
        NO_DOOR,
        DOOR_UNLOCKED,
        DOOR_OPEN,
        DOOR_LOCKED
    }

    private long            id;
    private int             markerID;
    private boolean         placed;
    private ArrayList<Long> residentActorIDs;
    private ArrayList<Long> residentEntityIDs;
    private E_WALL_TYPE[]   walls;

    public Room(long id)
    {
        this(id, -1);
    }

    public Room(long id, int markerID)
    {
        this(id, markerID, false);
    }

    public Room(long id, int markerID, boolean placed)
    {
        this.id = id;
        this.markerID = markerID;
        this.placed = placed;
        residentActorIDs = new ArrayList<>();
        residentEntityIDs = new ArrayList<>();
        walls = new E_WALL_TYPE[]{
            DOOR_UNLOCKED,
            DOOR_UNLOCKED,
            DOOR_UNLOCKED,
            NO_DOOR
        };
    }

    public long getId()
    {
        return id;
    }

    public void setMarker(int markerID)
    {
        this.markerID = markerID;
    }

    public int getMarker()
    {
        return markerID;
    }

    public boolean getPlaced()
    {
        return placed;
    }

    public void setPlaced(boolean placed)
    {
        this.placed = placed;
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

    public E_WALL_TYPE getWallType(short wall)
    {
        return walls[wall];
    }

    public void setWallType(short wall, E_WALL_TYPE type)
    {
        walls[wall] = type;
    }
}
