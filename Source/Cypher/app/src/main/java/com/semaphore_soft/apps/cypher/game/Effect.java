package com.semaphore_soft.apps.cypher.game;

/**
 * {@link Effect game.Effect} provides a list of effect types for {@link Item
 * Items} and {@link Special Specials} to associate with, and the methods
 * necessary to apply an effect to an {@link Actor}, directly modifying {@link
 * Actor} stats or applying new {@link Status Statuses} to {@link Actor Actors}
 * as necessary.
 *
 * @author scorple
 * @see Item
 * @see ItemConsumable
 * @see ItemDurable
 * @see Special
 * @see Actor
 * @see Status
 * @see StatusLinked
 * @see StatusTemporary
 */
public abstract class Effect
{
    /**
     * Describes a stat effect applied by an {@link Item} or {@link Special}.
     *
     * @see Item
     * @see ItemConsumable
     * @see ItemDurable
     * @see Special
     * @see Item#addEffect(E_EFFECT)
     * @see Item#getEffects()
     * @see Special#addEffect(E_EFFECT)
     * @see Special#getEffects()
     */
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

    /**
     * Applies an {@link Effect} temporarily to an {@link Actor}, either performing
     * an instantaneous change of one of the {@link Actor Actor's} stats, or
     * creating and applying a new {@link StatusTemporary} to that {@link
     * Actor}, based on the provided {@link Effect} rating and duration.
     * <p>
     * Invoked by the use of an {@link ItemConsumable} or a {@link Special}.
     *
     * @param effect       {@link E_EFFECT}: The type of effect to apply to the
     *                     target {@link Actor}.
     * @param effectRating int: The rating of the {@link Effect} modifier to apply to
     *                     the target {@link Actor}.
     * @param duration     int: The duration of any {@link StatusTemporary} to
     *                     be applied to the target {@link Actor}.
     * @param actor        {@link Actor}: The target to apply the given {@link Effect}
     *                     to.
     *
     * @see E_EFFECT
     * @see Actor
     * @see StatusTemporary
     * @see Status
     * @see ItemConsumable
     * @see Item
     * @see Special
     * @see Special#applySpecial(int, Actor)
     * @see Actor#useItem(int)
     * @see Actor#useItem(Item)
     * @see Actor#addNewStatusTemporary(Status.E_STATUS_TYPE, int, int)
     */
    static void applyTemporaryEffect(E_EFFECT effect,
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

    /**
     * Applies an {@link Effect} linked to an {@link ItemDurable} to an {@link Actor},
     * either performing an instantaneous change of one of the {@link Actor
     * Actor's} stats, or creating and applying a new {@link StatusLinked} to
     * that {@link Actor}, based on the provided {@link Effect} rating and associated
     * {@link ItemDurable} ID.
     *
     * @param effect       {@link E_EFFECT}: The type of effect to apply to the
     *                     target {@link Actor}.
     * @param effectRating int: The rating of the {@link Effect} modifier to apply to
     *                     the target {@link Actor}.
     * @param actor        {@link Actor}: The target to apply the given {@link Effect}
     *                     to.
     * @param linkId       int: The logical reference ID of {@link ItemDurable}
     *                     this {@link Effect} and any resulting {@link StatusLinked}
     *                     is tied to.
     *
     * @see E_EFFECT
     * @see Actor
     * @see StatusLinked
     * @see Status
     * @see ItemDurable
     * @see Item
     * @see Actor#addItem(Item)
     * @see Actor#addNewStatusLinked(Status.E_STATUS_TYPE, int, int)
     */
    static void applyLinkedEffect(E_EFFECT effect,
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
