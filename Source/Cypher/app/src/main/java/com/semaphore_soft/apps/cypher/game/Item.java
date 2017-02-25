package com.semaphore_soft.apps.cypher.game;

import java.util.ArrayList;

/**
 * Created by rickm on 1/31/2017.
 */

public abstract class Item
{
    protected int                        id;
    protected String                     name;
    private   int                        effectRating;
    private   ArrayList<Effect.E_EFFECT> effects;

    private String displayName;

    private Item(int id, String name)
    {
        this.id = id;
        this.name = name;
        effects = new ArrayList<>();
    }

    Item(int id, String name, int effectRating)
    {
        this(id, name);
        this.effectRating = effectRating;
    }

    public int getID()
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

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
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