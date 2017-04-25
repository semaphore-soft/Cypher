package com.semaphore_soft.apps.cypher.game;

/**
 * An instance of {@link Status game.Status} holds and maintains information
 * about the state of one status in the game, including the {@link
 * E_STATUS_TYPE type} of modifier it applies to an {@link Actor} and the
 * rating, or strength, of that modifier.
 * <p>
 * Must be extended and given qualifications describing under what conditions
 * the {@link Status} will apply and cease to apply.
 *
 * @author scorple
 * @see StatusLinked
 * @see StatusTemporary
 * @see Actor
 * @see Special
 * @see ItemConsumable
 * @see ItemDurable
 * @see Item
 */
abstract class Status
{
    protected int id;

    /**
     * Describes the type of modifier applied to an {@link Actor} which
     * associates with this {@link Status}. Used in determining the {@link Status} modified
     * stats of an {@link Actor}.
     */
    enum E_STATUS_TYPE
    {
        RECURRING_HEAL,
        RECURRING_ENERGY_RESTORE,
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
     * Logical ID and {@link E_STATUS_TYPE Status type} constructor.
     * <p>
     * Creates a {@link Status} object representing a status to be applied to an {@link
     * Actor} with a {@link E_STATUS_TYPE Status modifier type} and no
     * specified modifier rating.
     * <p>
     * WARNING: A Status without a modifier rating cannot be properly applied
     * to an {@link Actor}.
     *
     * @param id   int: The logical reference ID of this {@link Status}.
     * @param type {@link E_STATUS_TYPE}: The {@link E_STATUS_TYPE} of modifier
     *             applied to an {@link Actor} by this {@link Status}.
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
     * Logical ID, {@link E_STATUS_TYPE Status type}, and modifier rating
     * constructor.
     * <p>
     * Creates a {@link Status} object representing a status to be applied to an {@link
     * Actor} with a {@link E_STATUS_TYPE Status modifier type} and a modifier
     * effect rating.
     *
     * @param id           int: The logical reference ID of this {@link Status}.
     * @param type         {@link E_STATUS_TYPE}: The type of modifier applied
     *                     to an {@link Actor} by this {@link Status}.
     * @param effectRating int: The rating of the modifier applied to an {@link
     *                     Actor} by this {@link Status}.
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
     * Get the logical reference ID of this {@link Status}.
     *
     * @return int: The logical reference ID of this {@link Status}.
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
     * Get the type of modifier applied to an {@link Actor} by this {@link Status}.
     *
     * @return {@link E_STATUS_TYPE}: The type of modified applied by this
     * {@link Status}.
     *
     * @see E_STATUS_TYPE
     * @see Actor
     */
    E_STATUS_TYPE getType()
    {
        return type;
    }

    /**
     * Get the rating of the modifier effect applied to an {@link Actor} by
     * this {@link Status}.
     *
     * @return int: The rating of the modifier applied by this {@link Status}.
     *
     * @see Actor
     */
    int getEffectRating()
    {
        return effectRating;
    }
}