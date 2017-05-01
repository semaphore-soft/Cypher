package com.semaphore_soft.apps.cypher.game;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import static com.semaphore_soft.apps.cypher.game.Room.E_WALL_TYPE.DOOR_UNLOCKED;

/**
 * An instance of {@link Room game.Room} holds and maintains information about
 * the state of one room in the game, including its resident {@link Actor
 * Actors}, its resident {@link Item Items}, and the {@link E_WALL_TYPE
 * types} of its walls.
 *
 * @author scorple
 */
public class Room
{
    public static final short WALL_TOP    = 0;
    public static final short WALL_RIGHT  = 1;
    public static final short WALL_BOTTOM = 2;
    public static final short WALL_LEFT   = 3;

    /**
     * Describes the type of a wall, including whether or not it hosts a door,
     * and, if there is a door, the status of that door.
     */
    @SuppressWarnings("WeakerAccess") //this enum must be public so the
    //renderer can use it, don't believe Android Studio's lies
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
    private ArrayList<Integer> residentItemIDs;
    private E_WALL_TYPE[]      walls;

    /**
     * Logical ID and marker ID constructor.
     * <p>
     * Creates a {@link Room} object representing a game room associated with
     * an AR marker but is not yet considered to be 'placed', accessible, or
     * connected to the rest of game map.
     *
     * @param id       int: The logical reference ID of this {@link Room}.
     * @param markerID int: The marker/graphical reference ID of this {@link
     *                 Room}.
     */
    public Room(int id, int markerID)
    {
        this(id, markerID, false);
    }

    /**
     * Logical ID, marker ID, and placement flag constructor.
     * <p>
     * Creates a {@link Room} object representing a game room associated with
     * an AR marker and a 'placed' flag.
     *
     * @param id       int: The logical reference ID of this {@link Room}.
     * @param markerID int: The marker/graphical reference ID of this {@link
     *                 Room}.
     * @param placed   boolean:
     *                 <ul>
     *                 <li>True if this {@link Room} is to be considered 'placed',
     *                 accessible and in some way connected to the rest of the
     *                 game map.</li>
     *                 <li>False otherwise.</li>
     *                 </ul>
     */
    public Room(int id, int markerID, boolean placed)
    {
        this.id = id;
        this.markerID = markerID;
        this.placed = placed;
        residentActorIDs = new ArrayList<>();
        residentItemIDs = new ArrayList<>();
        walls = new E_WALL_TYPE[]{
            DOOR_UNLOCKED,
            DOOR_UNLOCKED,
            DOOR_UNLOCKED,
            DOOR_UNLOCKED
        };
    }

    /**
     * Get the logical reference ID of this {@link Room}.
     * <p>
     * To be used for referencing this {@link Room} in a game state context
     * (associating with {@link Actor Actors}, checking path validity, etc.)
     * ONLY.
     *
     * @return int: The logical reference ID of this {@link Room}.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Set the marker/graphical reference ID of this {@link Room}.
     * <p>
     * To be used for checking the AR marker this {@link Room} is anchored to
     * and referencing it in a graphical ({@link
     * com.semaphore_soft.apps.cypher.PortalRenderer}) context ONLY.
     *
     * @param markerID int: The marker/graphical reference ID the be used for
     *                 this {@link Room}
     *
     * @see com.semaphore_soft.apps.cypher.PortalRenderer
     */
    public void setMarker(int markerID)
    {
        this.markerID = markerID;
    }

    /**
     * Get the marker/graphical reference ID of this {@link Room}.
     * <p>
     * To be used for checking the AR marker this {@link Room} is anchored to
     * and referencing it in a graphical ({@link
     * com.semaphore_soft.apps.cypher.PortalRenderer}) context ONLY.
     *
     * @return int: The marker/graphical reference ID of this {@link Room}.
     *
     * @see com.semaphore_soft.apps.cypher.PortalRenderer
     */
    public int getMarker()
    {
        return markerID;
    }

    /**
     * Check the status of this {@link Room}'s 'placed' flag.
     *
     * @return boolean:
     * <ul>
     * <li>True if this {@link Room} is to be considered 'placed', accessible
     * and in some way connected to the rest of the game map.</li>
     * <li>False otherwise.</li>
     * </ul>
     *
     * @see GameMaster#moveActor(Model, int, int)
     * @see GameMaster#getValidPath(Model, int, int)
     * @see GameMaster#openDoor(Model, int, int, short, short)
     */
    public boolean isPlaced()
    {
        return placed;
    }

    /**
     * Update this {@link Room}'s 'placed' flag.
     *
     * @param placed boolean:
     *               <ul>
     *               <li>True if this {@link Room} is to be considered
     *               'placed', accessible and in some way connected to the rest
     *               of the game map.</li>
     *               <li>False otherwise.</li>
     *               </ul>
     *
     * @see GameMaster#moveActor(Model, int, int)
     * @see GameMaster#getValidPath(Model, int, int)
     * @see GameMaster#openDoor(Model, int, int, short, short)
     */
    public void setPlaced(boolean placed)
    {
        this.placed = placed;
    }

    /**
     * Add a single {@link Actor} ID to be associated with this {@link Room}.
     * This {@link Room} will consider that {@link Actor} to be a resident of,
     * or located within, this {@link Room}, until the {@link Actor Actor's} ID
     * is removed.
     *
     * @param actorID int: The logical reference ID of the {@link Actor} to be
     *                associated with, or a resident of, this {@link Room}.
     *
     * @see Actor
     * @see GameMaster#getPlayerTargets(Model, int)
     * @see GameMaster#getNonPlayerTargets(Model, int)
     * @see GameMaster#getPlayerTargetIds(Model, int)
     * @see GameMaster#getNonPlayerTargetIds(Model, int)
     */
    public void addActor(int actorID)
    {
        if (!residentActorIDs.contains(actorID))
        {
            residentActorIDs.add(actorID);
        }
    }

    /**
     * Dissociate a single {@link Actor} ID from this {@link Room}. This {@link
     * Room} will no longer consider that {@link Actor} to be a resident of, or
     * located within, this {@link Room}, unless that {@link Actor Actor's} ID
     * is added again.
     *
     * @param actorID int: The logical reference ID of the Ac{@link Actor}tor
     *                to be dissociated with, or no longer a resident of, this
     *                {@link Room}.
     *
     * @see Actor
     * @see GameMaster#getPlayerTargets(Model, int)
     * @see GameMaster#getNonPlayerTargets(Model, int)
     * @see GameMaster#getPlayerTargetIds(Model, int)
     * @see GameMaster#getNonPlayerTargetIds(Model, int)
     * @see GameMaster#removeDeadActors(Model)
     */
    public void removeActor(int actorID)
    {
        if (residentActorIDs.contains(actorID))
        {
            residentActorIDs.remove((Integer) actorID);
        }
    }

    /**
     * Get the list of the IDs of all {@link Actor Actors} associated with, or
     * considered to be residents of, this {@link Room}.
     *
     * @return ArrayList: A list of IDs of all the {@link Actor Actors}
     * associated with, or considered to be residents of, this {@link Room}.
     *
     * @see Actor
     * @see GameMaster#getPlayerTargets(Model, int)
     * @see GameMaster#getNonPlayerTargets(Model, int)
     * @see GameMaster#getPlayerTargetIds(Model, int)
     * @see GameMaster#getNonPlayerTargetIds(Model, int)
     */
    @NonNull
    public ArrayList<Integer> getResidentActors()
    {
        return residentActorIDs;
    }

    /**
     * Add a single {@link Item} ID to be associated with this {@link Room}.
     * This {@link Room} will consider that {@link Item} to be located within
     * this {@link Room}, until that {@link Item Item's} ID is removed.
     *
     * @param itemId int: The logical reference ID of the {@link Item} to be
     *                 associated with, or located within, this {@link Room}.
     *
     * @see Item
     */
    public void addItem(int itemId)
    {
        if (!residentItemIDs.contains(itemId))
        {
            residentItemIDs.add(itemId);
        }
    }

    /**
     * Dissociate a single {@link Item} ID from this {@link Room}. This
     * {@link Room} will no longer consider that {@link Item} to be located
     * within this {@link Room}, unless that {@link Item Item's} ID is
     * added again.
     *
     * @param itemId int: The logical reference ID of the {@link Item} to
     *                 be dissociated with, or no longer located within, this
     *                 {@link Room}.
     *
     * @see Item
     */
    public void removeItem(int itemId)
    {
        if (residentItemIDs.contains(itemId))
        {
            residentItemIDs.remove((Integer) itemId);
        }
    }

    /**
     * Get the list of the IDs of all {@link Item Items} associated with,
     * or considered to be located within, this {@link Room}.
     *
     * @return ArrayList: A list of IDs of all the {@link Item Items}
     * associated with, or considered to be located within, this {@link Room}.
     *
     * @see Item
     */
    @NonNull
    public ArrayList<Integer> getResidentItems()
    {
        return residentItemIDs;
    }

    /**
     * Get the {@link E_WALL_TYPE type} of one of this {@link Room Room's} four
     * walls.
     *
     * @param wall short: The position reference ID of the wall to check.
     *             <ul>
     *             <li>0: The top/north wall.</li>
     *             <li>1: The right/east wall.</li>
     *             <li>2: The bottom/south wall.</li>
     *             <li>3: The left/west wall.</li>
     *             </ul>
     *             Note: The wall position is relative to the default
     *             orientation of the {@link Room}. If the {@link Room} is
     *             rotated, the wall position references will also be rotated.
     *
     * @return {@link E_WALL_TYPE}: The {@link E_WALL_TYPE type} of the wall at
     * the given wall position reference ID.
     *
     * @see GameMaster#getValidAdjacency(Model, int, int, short, short)
     */
    public E_WALL_TYPE getWallType(short wall)
    {
        return walls[wall];
    }

    /**
     * Set the {@link E_WALL_TYPE type} of one of this {@link Room Room's} four
     * walls.
     *
     * @param wall short: The position reference ID of the wall to check.
     *             <ul>
     *             <li>0: The top/north wall.</li>
     *             <li>1: The right/east wall.</li>
     *             <li>2: The bottom/south wall.</li>
     *             <li>3: The left/west wall.</li>
     *             </ul>
     *             Note: The wall position is relative to the default
     *             orientation of the {@link Room}. If the {@link Room} is
     *             rotated, the wall position references will also be rotated.
     * @param type {@link E_WALL_TYPE}: The new wall {@link E_WALL_TYPE type}
     *             to associated with the wall at the given wall position
     *             reference ID.
     *
     * @see GameMaster#getValidAdjacency(Model, int, int, short, short)
     */
    public void setWallType(short wall, E_WALL_TYPE type)
    {
        walls[wall] = type;
    }
}
