package com.semaphore_soft.apps.cypher.game;

import java.util.ArrayList;

/**
 * Created by Scorple on 2/3/2017.
 */

public class Special
{
    private int    id;
    private String name;
    private int    cost;
    private int    duration;
    private float  scalar;

    private String displayName;

    public enum E_TARGETING_TYPE
    {
        SINGLE_PLAYER,
        SINGLE_NON_PLAYER,
        AOE_PLAYER,
        AOE_NON_PLAYER
    }

    private E_TARGETING_TYPE           targetingType;
    private ArrayList<Effect.E_EFFECT> effects;

    public Special(int id,
                   String name,
                   int cost,
                   int duration,
                   float scalar,
                   E_TARGETING_TYPE targetingType)
    {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.duration = duration;
        this.scalar = scalar;
        this.targetingType = targetingType;
        effects = new ArrayList<>();
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public int getCost()
    {
        return cost;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public E_TARGETING_TYPE getTargetingType()
    {
        return targetingType;
    }

    public void addEffect(Effect.E_EFFECT effect)
    {
        if (!effects.contains(effect))
        {
            effects.add(effect);
        }
    }

    public ArrayList<Effect.E_EFFECT> getEffects()
    {
        return effects;
    }

    public void incrementDuration()
    {
        ++duration;
    }

    public void applySpecial(int specialRating, Actor sourceActor, Actor targetActor)
    {
        int effectRating = (int) Math.ceil(((float) specialRating) * scalar);

        for (Effect.E_EFFECT effect : effects)
        {
            Effect.applyTemporaryEffect(effect,
                                        effectRating,
                                        duration,
                                        sourceActor,
                                        targetActor);
        }
    }
}
