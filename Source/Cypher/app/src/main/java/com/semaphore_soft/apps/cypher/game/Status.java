package com.semaphore_soft.apps.cypher.game;

/**
 * An instance of game.Status holds and maintains information about the state
 * of one status in the game, including the type of modifier it applied to an
 * Actor and the rating of that modifier's effect.
 * <p>
 * Must be extended and given qualifications describing under what conditions
 * the Status will apply and cease to apply.
 *
 * @see StatusLinked
 * @see StatusTemporary
 * @see Actor
 * @see Special
 * @see ItemConsumable
 * @see ItemDurable
 * @see Item
 *
 * @author scorple
 */
abstract class Status
{
    protected int id;

    /**
     * Describes the type of modifier applied to an Actor which associates with
     * this Status. Used in determining the Status modified stats of an Actor.
     */
    enum E_STATUS_TYPE
    {
        HEALTH_MAXIMUM_MODIFIER,
        ATTACK_RATING_MODIFIER,
        SPECIAL_MAXIMUM_MODIFIER,
        SPECIAL_RATING_MODIFIER,
        SPECIAL_COST_MODIFIER,
        DEFENCE_RATING_MODIFIER
    }

    private   E_STATUS_TYPE type;
    protected int           effectRating;

    /**
     * Logical ID and Status type constructor.
     * <p>
     * Creates a Status object representing a status to be applied to an Actor
     * with a Status modifier type and no specified modifier effect rating.
     * <p>
     * WARNING: A Status without a modifier effect rating cannot be properly
     * applied to an Actor.
     *
     * @param id   int: The logical reference ID of this Status.
     * @param type E_STATUS_TYPE: The type of modifier applied to an
     *             Actor by this Status.
     *
     * @see E_STATUS_TYPE
     * @see Actor
     */
    private Status(int id, E_STATUS_TYPE type)
    {
        this.id = id;
        this.type = type;
    }

    /**
     * Logical ID, Status type, and modifier effect rating constructor.
     * <p>
     * Creates a Status object representing a status to be applied to an Actor
     * with a Status modifier type and an modifier effect rating.
     *
     * @param id           int: The logical reference ID of this Status.
     * @param type         E_STATUS_TYPE: The type of modifier applied to an
     *                     Actor by this Status.
     * @param effectRating int: The rating of the modifier applied to an Actor
     *                     by this Status.
     *
     * @see E_STATUS_TYPE
     * @see Actor
     */
    Status(int id, E_STATUS_TYPE type, int effectRating)
    {
        this(id, type);
        this.effectRating = effectRating;
    }

    /**
     * Get the logical reference ID of this Status.
     *
     * @return int: The logical reference ID of this Status.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Set the logical reference ID of this Status.
     *
     * @param id int: The new logical reference ID of this Status.
     */
    public void setId(int id)
    {
        this.id = id;
    }

    /**
     * Get the type of modifier applied to an Actor by this Status.
     *
     * @return E_STATUS_TYPE: The type of modified applied by this Status.
     *
     * @see E_STATUS_TYPE
     * @see Actor
     */
    E_STATUS_TYPE getType()
    {
        return type;
    }

    /**
     * Get the rating of the modifier effect applied to an Actor by this
     * Status.
     *
     * @return int: The rating of the modifier applied by this Status.
     *
     * @see Actor
     */
    int getEffectRating()
    {
        return effectRating;
    }
}