package com.semaphore_soft.apps.cypher.game;

import java.util.ArrayList;

/**
 * Created by Scorple on 2/3/2017.
 */

public class Special
{
    private long   id;
    private String name;
    private int    cost;
    private int    duration;

    public enum E_TARGETING_TYPE
    {
        SINGLE_PLAYER,
        SINGLE_NON_PLAYER,
        AOE_PLAYER,
        AOE_NON_PLAYER
    }

    private enum E_SPECIAL_EFFECT
    {
        ATTACK,
        DEFEND,
        HEAL
    }

    private E_TARGETING_TYPE            targetingType;
    private ArrayList<E_SPECIAL_EFFECT> effects;

    public Special(long id, String name, int cost, int duration, E_TARGETING_TYPE targetingType)
    {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.duration = duration;
        this.targetingType = targetingType;
        effects = new ArrayList<>();
    }

    public long getId()
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

    public E_TARGETING_TYPE getTargetingType()
    {
        return targetingType;
    }

    public void addEffect(E_SPECIAL_EFFECT effect)
    {
        if (!effects.contains(effect))
        {
            effects.add(effect);
        }
    }

    public void applySpecial(int specialRating, Actor actor)
    {
        for (E_SPECIAL_EFFECT effect : effects)
        {
            switch (effect)
            {
                case ATTACK:
                {
                    int damage = specialRating;

                    switch (actor.getState())
                    {
                        case ATTACK:
                            damage = specialRating * 2;
                            break;
                        case SPECIAL:
                            damage = specialRating;
                            break;
                        case DEFEND:
                            damage = specialRating / 2;
                            break;
                        default:
                            break;
                    }

                    damage -= actor.getRealDefenceRating();
                    damage = (damage < 0) ? 0 : damage;

                    int actorHealth = actor.getHealthCurrent();
                    actor.setHealthCurrent((damage > actorHealth) ? 0 : actorHealth - damage);
                    break;
                }
                case DEFEND:
                {
                    actor.addNewStatusTemporary(Status.E_STATUS_TYPE.DEFENCE_RATING_MODIFIER,
                                                specialRating,
                                                duration);
                    break;
                }
                case HEAL:
                {
                    int actorHealthMaximum = actor.getHealthMaximum();
                    int actorHealthCurrent = actor.getHealthCurrent();
                    actor.setHealthCurrent(((actorHealthCurrent + specialRating) >
                                            actorHealthMaximum) ? actorHealthMaximum :
                                           actorHealthCurrent + specialRating);
                    break;
                }
            }
        }
    }
}
