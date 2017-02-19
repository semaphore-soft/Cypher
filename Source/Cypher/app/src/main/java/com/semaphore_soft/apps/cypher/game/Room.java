package com.semaphore_soft.apps.cypher.game;

import java.util.ArrayList;

import static com.semaphore_soft.apps.cypher.game.Room.E_WALL_TYPE.DOOR_UNLOCKED;
import static com.semaphore_soft.apps.cypher.game.Room.E_WALL_TYPE.NO_DOOR;

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

    private int                id;
    private int                markerID;
    private boolean            placed;
    private ArrayList<Integer> residentActorIDs;
    private ArrayList<Integer> residentEntityIDs;
    private E_WALL_TYPE[]      walls;

    public Room(int id)
    {
        this(id, -1);
    }

    public Room(int id, int markerID)
    {
        this(id, markerID, false);
    }

    public Room(int id, int markerID, boolean placed)
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

    public int getId()
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

    public boolean isPlaced()
    {
        return placed;
    }

    public void setPlaced(boolean placed)
    {
        this.placed = placed;
    }

    public void addActor(int actorID)
    {
        if (!residentActorIDs.contains(actorID))
        {
            residentActorIDs.add(actorID);
        }
    }

    public void removeActor(int actorID)
    {
        if (residentActorIDs.contains(actorID))
        {
            residentActorIDs.remove((Integer) actorID);
        }
    }

    public ArrayList<Integer> getResidentActors()
    {
        return residentActorIDs;
    }

    public void addEntity(int entityID)
    {
        if (!residentEntityIDs.contains(entityID))
        {
            residentEntityIDs.add(entityID);
        }
    }

    public void removeEntity(int entityID)
    {
        if (residentEntityIDs.contains(entityID))
        {
            residentEntityIDs.remove((Integer) entityID);
        }
    }

    public ArrayList<Integer> getResidentEntities()
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
