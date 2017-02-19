package com.semaphore_soft.apps.cypher.game;

/**
 * Created by rickm on 1/31/2017.
 */

public class StatusTemporary extends Status
{
    int duration;

    public StatusTemporary(int id, E_STATUS_TYPE type, int effectRating, int duration)
    {
        super(id, type, effectRating);
        this.duration = duration;
    }

    public int getDuration()
    {
        return duration;
    }

    public boolean tick()
    {
        return (--duration <= 0);
    }
}
