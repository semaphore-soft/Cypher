package com.semaphore_soft.apps.cypher.game;

/**
 * Created by rickm on 1/31/2017.
 */

public abstract class Item
{
    protected long   id;
    protected String name;

    enum E_ITEM_TYPE
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
    private   int         effectRating;

    private Item(long id, String name)
    {
        this.id = id;
        this.name = name;
    }

    private Item(long id, String name, E_ITEM_TYPE type)
    {
        this(id, name);
        this.type = type;
    }

    Item(long id, String name, E_ITEM_TYPE type, int effectRating)
    {
        this(id, name, type);
        this.effectRating = effectRating;
    }

    public long getID()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public E_ITEM_TYPE getType()
    {
        return type;
    }

    int getEffectRating()
    {
        return effectRating;
    }
}