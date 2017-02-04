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

    public enum E_SPECIAL_EFFECT
    {
        HEAL,
        ATTACK,
        HEALTH_MAXIMUM_UP,
        HEALTH_MAXIMUM_DOWN,
        ATTACK_RATING_UP,
        ATTACK_RATING_DOWN,
        SPECIAL_MAXIMUM_UP,
        SPECIAL_MAXIMUM_DOWN,
        SPECIAL_RATING_UP,
        SPECIAL_RATING_DOWN,
        DEFENCE_RATING_UP,
        DEFENCE_RATING_DOWN
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
                case HEAL:
                {
                    int actorHealthMaximum = actor.getHealthMaximum();
                    int actorHealthCurrent = actor.getHealthCurrent();
                    actor.setHealthCurrent(((actorHealthCurrent + specialRating) >
                                            actorHealthMaximum) ? actorHealthMaximum :
                                           actorHealthCurrent + specialRating);
                    break;
                }
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
                case HEALTH_MAXIMUM_UP:
                {
                    actor.addNewStatusTemporary(Status.E_STATUS_TYPE.HEALTH_MAXIMUM_MODIFIER,
                                                specialRating,
                                                duration);
                    break;
                }
                case HEALTH_MAXIMUM_DOWN:
                {
                    actor.addNewStatusTemporary(Status.E_STATUS_TYPE.HEALTH_MAXIMUM_MODIFIER,
                                                -specialRating,
                                                duration);
                    break;
                }
                case ATTACK_RATING_UP:
                {
                    actor.addNewStatusTemporary(Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                                specialRating,
                                                duration);
                    break;
                }
                case ATTACK_RATING_DOWN:
                {
                    actor.addNewStatusTemporary(Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                                -specialRating,
                                                duration);
                    break;
                }
                case SPECIAL_MAXIMUM_UP:
                {
                    actor.addNewStatusTemporary(Status.E_STATUS_TYPE.SPECIAL_MAXIMUM_MODIFIER,
                                                specialRating,
                                                duration);
                    break;
                }
                case SPECIAL_MAXIMUM_DOWN:
                {
                    actor.addNewStatusTemporary(Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                                -specialRating,
                                                duration);
                    break;
                }
                case SPECIAL_RATING_UP:
                {
                    actor.addNewStatusTemporary(Status.E_STATUS_TYPE.SPECIAL_RATING_MODIFIER,
                                                specialRating,
                                                duration);
                    break;
                }
                case SPECIAL_RATING_DOWN:
                {
                    actor.addNewStatusTemporary(Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                                -specialRating,
                                                duration);
                    break;
                }
                case DEFENCE_RATING_UP:
                {
                    actor.addNewStatusTemporary(Status.E_STATUS_TYPE.DEFENCE_RATING_MODIFIER,
                                                specialRating,
                                                duration);
                    break;
                }
                case DEFENCE_RATING_DOWN:
                {
                    actor.addNewStatusTemporary(Status.E_STATUS_TYPE.DEFENCE_RATING_MODIFIER,
                                                -specialRating,
                                                duration);
                    break;
                }
            }
        }
    }
}
