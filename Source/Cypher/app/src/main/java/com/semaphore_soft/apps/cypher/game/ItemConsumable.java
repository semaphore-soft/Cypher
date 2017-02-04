package com.semaphore_soft.apps.cypher.game;

/**
 * Created by rickm on 1/31/2017.
 */

public class ItemConsumable extends Item
{
    private int duration;

    public enum E_TARGETING_TYPE
    {
        SINGLE_PLAYER,
        SINGLE_NON_PLAYER,
        AOE_PLAYER,
        AOE_NON_PLAYER
    }

    private E_TARGETING_TYPE targetingType;

    public ItemConsumable(long id,
                          String name,
                          E_ITEM_TYPE type,
                          int effectRating,
                          int duration,
                          E_TARGETING_TYPE targetingType)
    {
        super(id, name, type, effectRating);
        this.duration = duration;
        this.targetingType = targetingType;
    }

    int getDuration()
    {
        return duration;
    }

    public E_TARGETING_TYPE getTargetingType()
    {
        return targetingType;
    }
}
