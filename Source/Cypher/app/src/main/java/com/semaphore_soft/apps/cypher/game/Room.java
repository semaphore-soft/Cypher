package com.semaphore_soft.apps.cypher.game;

import java.util.ArrayList;

import static com.semaphore_soft.apps.cypher.game.Room.E_WALL_TYPE.DOOR_UNLOCKED;

/**
 * An instance of game.Room holds and maintains information about the state of
 * one room in the game, including its resident Actors, its resident Entities,
 * and the types of its walls.
 *
 * @author scorple
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

    /**
     * Logical ID and marker ID constructor.
     * <p>
     * Creates a Room object representing a game room associated with an AR
     * marker but is not yet considered to be 'placed', accessible, or
     * connected to the rest of game map.
     *
     * @param id       int: The logical reference ID of this Room.
     * @param markerID int: The marker/graphical reference ID of this Room.
     */
    public Room(int id, int markerID)
    {
        this(id, markerID, false);
    }

    /**
     * Logical ID, marker ID, and placement flag constructor.
     * <p>
     * Creates a Room object representing a game room associated with an AR
     * marker and a 'placed' flag.
     *
     * @param id       int: The logical reference ID of this room.
     * @param markerID int: The marker/graphical reference ID of this Room.
     * @param placed   boolean: True if this Room is to be considered 'placed',
     *                 accessible and in some way connected to the rest of the
     *                 game map. False otherwise.
     */
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
            DOOR_UNLOCKED
        };
    }

    /**
     * Get the logical reference ID of this Room.
     * <p>
     * To be used for referencing this Room in a game state context
     * (associating with Actors, checking path validity, etc.) ONLY.
     *
     * @return int: The logical reference ID of this Room.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Set the marker/graphical reference ID of this Room.
     * <p>
     * To be used for checking the AR marker this Room is anchored to and
     * referencing it in a graphical (renderer) context ONLY.
     *
     * @param markerID int: The marker/graphical reference ID the be used for
     *                 this Room.
     */
    public void setMarker(int markerID)
    {
        this.markerID = markerID;
    }

    /**
     * Get the marker/graphical reference ID of this Room.
     * <p>
     * To be used for checking the AR marker this Room is anchored to and
     * referencing it in a graphical (renderer) context ONLY.
     *
     * @return int: The marker/graphical reference ID of this Room.
     */
    public int getMarker()
    {
        return markerID;
    }

    /**
     * Check the status of this Room's 'placed' flag.
     *
     * @return boolean:
     * <ul>
     * <li>True if this Room is to be considered 'placed', accessible and
     * in some way connected to the rest of the game map.</li>
     * <li>False otherwise.</li>
     * </ul>
     */
    public boolean isPlaced()
    {
        return placed;
    }

    /**
     * Update this Room's 'placed' flag.
     *
     * @param placed boolean:
     *               <ul>
     *               <li>True if this Room is to be considered 'placed', accessible
     *               and in some way connected to the rest of the game map.</li>
     *               <li>False otherwise.</li>
     *               </ul>
     */
    public void setPlaced(boolean placed)
    {
        this.placed = placed;
    }

    /**
     * Add a single Actor ID to be associated with this Room. This Room will
     * consider that Actor to be a resident of, or located within, this Room,
     * until the Actor's ID is removed.
     *
     * @param actorID int: The logical reference ID of the Actor to be
     *                associated with, or a resident of, this Room.
     */
    public void addActor(int actorID)
    {
        if (!residentActorIDs.contains(actorID))
        {
            residentActorIDs.add(actorID);
        }
    }

    /**
     * Dissociate a single Actor ID from this Room. This Room will no longer
     * consider that Actor to be a resident of, or located within, this Room,
     * unless that Actor's ID is added again.
     *
     * @param actorID int: The logical reference ID of the Actor to be
     *                dissociated with, or no longer a resident of, this Room.
     */
    public void removeActor(int actorID)
    {
        if (residentActorIDs.contains(actorID))
        {
            residentActorIDs.remove((Integer) actorID);
        }
    }

    /**
     * Get the list of the IDs of all Actors associated with, or considered to
     * be residents of, this Room.
     *
     * @return ArrayList: A list of IDs of all the Actors associated with, or
     * considered to be residents of, this Room.
     */
    public ArrayList<Integer> getResidentActors()
    {
        return residentActorIDs;
    }

    /**
     * Add a single Entity ID to be associated with this Room. This Room will
     * consider that Entity to be located within this Room, until that Entity's
     * ID is removed.
     *
     * @param entityID int: The logical reference ID of the Entity to be
     *                 associated with, or located within, this Room.
     */
    public void addEntity(int entityID)
    {
        if (!residentEntityIDs.contains(entityID))
        {
            residentEntityIDs.add(entityID);
        }
    }

    /**
     * Dissociate a single Entity ID from this Room. This Room will no longer
     * consider that Entity to be located within this Room, unless that
     * Entity's ID is added again.
     *
     * @param entityID int: The logical reference ID of the Entity to be
     *                 dissociated with, or no longer located within, this
     *                 Room.
     */
    public void removeEntity(int entityID)
    {
        if (residentEntityIDs.contains(entityID))
        {
            residentEntityIDs.remove((Integer) entityID);
        }
    }

    /**
     * Get the list of the IDs of all Entities associated with, or considered
     * to be located within, this Room.
     *
     * @return ArrayList: A list of IDs of all the Entities associated with, or
     * considered to be located within, this Room.
     */
    public ArrayList<Integer> getResidentEntities()
    {
        return residentEntityIDs;
    }

    /**
     * Get the type of one of this Room's four walls.
     *
     * @param wall short: The position reference ID of the wall to check.
     *             <ul>
     *             <li>0: The top/north wall.</li>
     *             <li>1: The right/east wall.</li>
     *             <li>2: The bottom/south wall.</li>
     *             <li>3: The left/west wall.</li>
     *             </ul>
     *             Note: The wall position is relative to the default
     *             orientation of the room. If the room is rotated, the wall
     *             position references will also be rotated.
     *
     * @return E_WALL_TYPE: The type of the wall at the given wall position
     * reference ID.
     */
    public E_WALL_TYPE getWallType(short wall)
    {
        return walls[wall];
    }

    /**
     * Set the type of one of this Room's four walls.
     *
     * @param wall short: The position reference ID of the wall to check.
     *             <ul>
     *             <li>0: The top/north wall.</li>
     *             <li>1: The right/east wall.</li>
     *             <li>2: The bottom/south wall.</li>
     *             <li>3: The left/west wall.</li>
     *             </ul>
     *             Note: The wall position is relative to the default
     *             orientation of the room. If the room is rotated, the wall
     *             position references will also be rotated.
     * @param type E_WALL_TYPE: The new wall type to associated with the wall
     *             at the given wall position reference ID.
     */
    public void setWallType(short wall, E_WALL_TYPE type)
    {
        walls[wall] = type;
    }
}
