package com.semaphore_soft.apps.cypher.game;

import java.util.ArrayList;

/**
 * Created by rickm on 1/31/2017.
 */

public abstract class Item
{
    protected long                       id;
    protected String                     name;
    private   int                        effectRating;
    private   ArrayList<Effect.E_EFFECT> effects;

    private Item(long id, String name)
    {
        this.id = id;
        this.name = name;
        effects = new ArrayList<>();
    }

    Item(long id, String name, int effectRating)
    {
        this(id, name);
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

    public int getEffectRating()
    {
        return effectRating;
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
}