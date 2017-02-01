package com.semaphore_soft.apps.cypher.game;

/**
 * Created by rickm on 1/31/2017.
 */

public class ItemConsumable extends Item
{
    private int duration;

    public ItemConsumable(long id, E_ITEM_TYPE type, int effectRating, int duration)
    {
        super(id, type, effectRating);
        this.duration = duration;
    }

    public int getDuration()
    {
        return duration;
    }
}
