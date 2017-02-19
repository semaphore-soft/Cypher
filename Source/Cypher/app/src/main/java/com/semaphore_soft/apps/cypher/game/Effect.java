package com.semaphore_soft.apps.cypher.game;

/**
 * Created by rickm on 2/4/2017.
 */

public abstract class Effect
{
    public enum E_EFFECT
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

    public static void applyTemporaryEffect(E_EFFECT effect,
                                            int effectRating,
                                            int duration,
                                            Actor actor)
    {
        switch (effect)
        {
            case HEAL:
            {
                int actorHealthMaximum = actor.getHealthMaximum();
                int actorHealthCurrent = actor.getHealthCurrent();
                actor.setHealthCurrent(((actorHealthCurrent + effectRating) >
                                        actorHealthMaximum) ? actorHealthMaximum :
                                       actorHealthCurrent + effectRating);
                break;
            }
            case ATTACK:
            {
                int damage = effectRating;

                switch (actor.getState())
                {
                    case ATTACK:
                        damage = effectRating * 2;
                        break;
                    case NEUTRAL:
                    case SPECIAL:
                        damage = effectRating;
                        break;
                    case DEFEND:
                        damage = effectRating / 2;
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
                                            effectRating,
                                            duration);
                break;
            }
            case HEALTH_MAXIMUM_DOWN:
            {
                actor.addNewStatusTemporary(Status.E_STATUS_TYPE.HEALTH_MAXIMUM_MODIFIER,
                                            -effectRating,
                                            duration);
                break;
            }
            case ATTACK_RATING_UP:
            {
                actor.addNewStatusTemporary(Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                            effectRating,
                                            duration);
                break;
            }
            case ATTACK_RATING_DOWN:
            {
                actor.addNewStatusTemporary(Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                            -effectRating,
                                            duration);
                break;
            }
            case SPECIAL_MAXIMUM_UP:
            {
                actor.addNewStatusTemporary(Status.E_STATUS_TYPE.SPECIAL_MAXIMUM_MODIFIER,
                                            effectRating,
                                            duration);
                break;
            }
            case SPECIAL_MAXIMUM_DOWN:
            {
                actor.addNewStatusTemporary(Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                            -effectRating,
                                            duration);
                break;
            }
            case SPECIAL_RATING_UP:
            {
                actor.addNewStatusTemporary(Status.E_STATUS_TYPE.SPECIAL_RATING_MODIFIER,
                                            effectRating,
                                            duration);
                break;
            }
            case SPECIAL_RATING_DOWN:
            {
                actor.addNewStatusTemporary(Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                            -effectRating,
                                            duration);
                break;
            }
            case DEFENCE_RATING_UP:
            {
                actor.addNewStatusTemporary(Status.E_STATUS_TYPE.DEFENCE_RATING_MODIFIER,
                                            effectRating,
                                            duration);
                break;
            }
            case DEFENCE_RATING_DOWN:
            {
                actor.addNewStatusTemporary(Status.E_STATUS_TYPE.DEFENCE_RATING_MODIFIER,
                                            -effectRating,
                                            duration);
                break;
            }
        }
    }

    public static void applyLinkedEffect(E_EFFECT effect,
                                         int effectRating,
                                         Actor actor,
                                         int linkId)
    {
        switch (effect)
        {
            case HEAL:
            {
                int actorHealthMaximum = actor.getHealthMaximum();
                int actorHealthCurrent = actor.getHealthCurrent();
                actor.setHealthCurrent(((actorHealthCurrent + effectRating) >
                                        actorHealthMaximum) ? actorHealthMaximum :
                                       actorHealthCurrent + effectRating);
                break;
            }
            case ATTACK:
            {
                int damage = effectRating;

                switch (actor.getState())
                {
                    case ATTACK:
                        damage = effectRating * 2;
                        break;
                    case NEUTRAL:
                    case SPECIAL:
                        damage = effectRating;
                        break;
                    case DEFEND:
                        damage = effectRating / 2;
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
                actor.addNewStatusLinked(Status.E_STATUS_TYPE.HEALTH_MAXIMUM_MODIFIER,
                                         effectRating,
                                         linkId);
                break;
            }
            case HEALTH_MAXIMUM_DOWN:
            {
                actor.addNewStatusLinked(Status.E_STATUS_TYPE.HEALTH_MAXIMUM_MODIFIER,
                                         -effectRating,
                                         linkId);
                break;
            }
            case ATTACK_RATING_UP:
            {
                actor.addNewStatusLinked(Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                         effectRating,
                                         linkId);
                break;
            }
            case ATTACK_RATING_DOWN:
            {
                actor.addNewStatusLinked(Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                         -effectRating,
                                         linkId);
                break;
            }
            case SPECIAL_MAXIMUM_UP:
            {
                actor.addNewStatusLinked(Status.E_STATUS_TYPE.SPECIAL_MAXIMUM_MODIFIER,
                                         effectRating,
                                         linkId);
                break;
            }
            case SPECIAL_MAXIMUM_DOWN:
            {
                actor.addNewStatusLinked(Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                         -effectRating,
                                         linkId);
                break;
            }
            case SPECIAL_RATING_UP:
            {
                actor.addNewStatusLinked(Status.E_STATUS_TYPE.SPECIAL_RATING_MODIFIER,
                                         effectRating,
                                         linkId);
                break;
            }
            case SPECIAL_RATING_DOWN:
            {
                actor.addNewStatusLinked(Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                         -effectRating,
                                         linkId);
                break;
            }
            case DEFENCE_RATING_UP:
            {
                actor.addNewStatusLinked(Status.E_STATUS_TYPE.DEFENCE_RATING_MODIFIER,
                                         effectRating,
                                         linkId);
                break;
            }
            case DEFENCE_RATING_DOWN:
            {
                actor.addNewStatusLinked(Status.E_STATUS_TYPE.DEFENCE_RATING_MODIFIER,
                                         -effectRating,
                                         linkId);
                break;
            }
        }
    }
}
