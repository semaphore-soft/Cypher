package com.semaphore_soft.apps.cypher.game;

/**
 * Created by rickm on 1/31/2017.
 */

public abstract class Item
{
    protected long id;

    public enum E_ITEM_TYPE
    {
        KEY,
        HEALTH_MAXIMUM_MODIFIER,
        HEALTH_RESTORE,
        ATTACK_RATING_MODIFIER,
        SPECIAL_MAXIMUM_MODIFIER,
        SPECIAL_RESTORE,
        SPECIAL_RATING_MODIFIER,
        SPECIAL_COST_MODIFIER,
        DEFENCE_RATING_MODIFIER
    }

    protected E_ITEM_TYPE type;
    protected int         effectRating;

    public Item(long id, E_ITEM_TYPE type)
    {
        this.id = id;
        this.type = type;
    }

    public Item(long id, E_ITEM_TYPE type, int effectRating)
    {
        this(id, type);
        this.effectRating = effectRating;
    }

    public long getID()
    {
        return id;
    }

    public E_ITEM_TYPE getType()
    {
        return type;
    }

    public int getEffectRating()
    {
        return effectRating;
    }
}