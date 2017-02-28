package com.semaphore_soft.apps.cypher.game;

/**
 * An instance of {@link Entity game.Entity} holds and maintains information
 * about the state of one entity in the game, including its type identifier
 * and the logical reference ID of the {@link Room} in which it is located.
 * <p>
 * An {@link Entity} is any object in the game world which is not a {@link
 * Room}, {@link Actor}, or {@link Item}.
 *
 * @author scorple
 * @see Model
 * @see Room
 * @see Actor
 * @see Item
 */
public class Entity
{
    private int id;
    private int type;
    private int roomID;

    /**
     * Logical reference ID constructor
     * <p>
     * Creates an {@link Entity} object with no type ID and which is not
     * associated with, or located within, a {@link Room}.
     *
     * @param id int: The logical reference ID of this {@link Entity}.
     *
     * @see Room
     */
    public Entity(int id)
    {
        this.id = id;
        type = -1;
        roomID = -1;
    }

    /**
     * Logical reference ID and type ID constructor.
     * <p>
     * Creates an {@link Entity} object with a type ID which is not
     * associated with, or located within, a {@link Room}.
     *
     * @param id   int: The logical reference ID of this {@link Entity}.
     * @param type int: The type ID of this {@link Entity}.
     *
     * @see Room
     */
    public Entity(int id, int type)
    {
        this.id = id;
        this.type = type;
        roomID = -1;
    }

    /**
     * Get the logical reference ID of this {@link Entity}.
     *
     * @return int: The logical reference ID of this {@link Entity}.
     */
    public int getID()
    {
        return id;
    }

    /**
     * Set the type ID of this {@link Entity}.
     *
     * @param type int: The type ID to be used for this {@link Entity}.
     */
    public void setType(int type)
    {
        this.type = type;
    }

    /**
     * Get the type ID of this {@link Entity}.
     *
     * @return int: The type ID of this {@link Entity}.
     */
    public int getType()
    {
        return type;
    }

    /**
     * Set the logical reference ID of the {@link Room} this {@link Entity}
     * associates with, or considers itself to be located within.
     *
     * @param roomID int: The logical reference ID of the {@link Room} this
     *               {@link Entity} will associate with.
     *
     * @see Room
     */
    public void setRoom(int roomID)
    {
        this.roomID = roomID;
    }

    /**
     * Get the logical reference ID of the {@link Room} this {@link Entity}
     * associates with, or considers itself to be located within.
     *
     * @return int: The logical reference ID of the {@link Room} this {@link
     * Entity} associates with.
     *
     * @see Room
     */
    public int getRoomID()
    {
        return roomID;
    }
}
