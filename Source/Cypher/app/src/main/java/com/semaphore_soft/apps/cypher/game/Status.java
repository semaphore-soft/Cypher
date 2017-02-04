package com.semaphore_soft.apps.cypher.game;

/**
 * Created by rickm on 1/31/2017.
 */

public abstract class Status
{
    protected long id;

    public enum E_STATUS_TYPE
    {
        HEALTH_MAXIMUM_MODIFIER,
        ATTACK_RATING_MODIFIER,
        SPECIAL_MAXIMUM_MODIFIER,
        SPECIAL_RATING_MODIFIER,
        SPECIAL_COST_MODIFIER,
        DEFENCE_RATING_MODIFIER
    }

    protected E_STATUS_TYPE type;
    protected int           effectRating;

    public Status(long id, E_STATUS_TYPE type)
    {
        this.id = id;
        this.type = type;
    }

    public Status(long id, E_STATUS_TYPE type, int effectRating)
    {
        this(id, type);
        this.effectRating = effectRating;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public E_STATUS_TYPE getType()
    {
        return type;
    }

    int getEffectRating()
    {
        return effectRating;
    }
}