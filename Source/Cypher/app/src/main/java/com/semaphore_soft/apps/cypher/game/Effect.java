package com.semaphore_soft.apps.cypher.game;

import com.semaphore_soft.apps.cypher.utils.Logger;

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
        ENERGY_RESTORE,
        ATTACK,
        HEALTH_MAXIMUM_UP,
        HEALTH_MAXIMUM_DOWN,
        ATTACK_RATING_UP,
        ATTACK_RATING_DOWN,
        SPECIAL_MAXIMUM_UP,
        SPECIAL_MAXIMUM_DOWN,
        SPECIAL_RATING_UP,
        SPECIAL_RATING_DOWN,
        DEFENSE_RATING_UP,
        DEFENSE_RATING_DOWN
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
     * @param targetActor  {@link Actor}: The target to apply the given {@link Effect}
     *                     to.
     *
     * @see E_EFFECT
     * @see Actor
     * @see StatusTemporary
     * @see Status
     * @see ItemConsumable
     * @see Item
     * @see Special
     * @see Special#applySpecial(int, Actor, Actor)
     * @see Actor#useItem(Model, int)
     * @see Actor#useItem(Item)
     * @see Actor#addNewStatusTemporary(Status.E_STATUS_TYPE, int, int, int)
     */
    static void applyTemporaryEffect(E_EFFECT effect,
                                     int effectRating,
                                     int duration,
                                     Actor sourceActor,
                                     Actor targetActor)
    {
        int adjustedDuration = (sourceActor.getId() == targetActor.getId())
                               ?
                               duration + 2
                               //;
                               :
                               duration + 1;

        Logger.logI(
            "applying effect:<" + effect.toString() + "> with duration:<" + duration + "> to <" +
            targetActor.getDisplayName() + "> from <" + sourceActor.getDisplayName() + ">");

        switch (effect)
        {
            case HEAL:
            {
                Logger.logI("applying heal");
                Logger.logI("pre-health:<" + targetActor.getHealthCurrent() + ">");

                int actorHealthMaximum = targetActor.getHealthMaximum();
                int actorHealthCurrent = targetActor.getHealthCurrent();
                targetActor.setHealthCurrent(((actorHealthCurrent + effectRating) >
                                              actorHealthMaximum) ? actorHealthMaximum :
                                             actorHealthCurrent + effectRating);

                Logger.logI("post-health:<" + targetActor.getHealthCurrent() + ">");
                break;
            }
            case ENERGY_RESTORE:
            {
                Logger.logI("applying energy restore");
                Logger.logI("pre-energy:<" + targetActor.getSpecialCurrent() + ">");

                int actorEnergyMaximum = targetActor.getSpecialMaximum();
                int actorEnergyCurrent = targetActor.getSpecialCurrent();
                targetActor.setSpecialCurrent(((actorEnergyCurrent + effectRating) >
                                               actorEnergyMaximum) ? actorEnergyMaximum :
                                              actorEnergyCurrent + effectRating);

                Logger.logI("post-energy:<" + targetActor.getSpecialCurrent() + ">");
                break;
            }
            case ATTACK:
            {
                int damage = effectRating;

                switch (targetActor.getState())
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

                damage -= targetActor.getRealDefenseRating();
                damage = (damage < 0) ? 0 : damage;

                int actorHealth = targetActor.getHealthCurrent();
                targetActor.setHealthCurrent((damage > actorHealth) ? 0 : actorHealth - damage);
                break;
            }
            case HEALTH_MAXIMUM_UP:
            {
                targetActor.addNewStatusTemporary(Status.E_STATUS_TYPE.HEALTH_MAXIMUM_MODIFIER,
                                                  effectRating,
                                                  adjustedDuration,
                                                  sourceActor.getId());
                sourceActor.addEffectedActor(targetActor);
                break;
            }
            case HEALTH_MAXIMUM_DOWN:
            {
                targetActor.addNewStatusTemporary(Status.E_STATUS_TYPE.HEALTH_MAXIMUM_MODIFIER,
                                                  -effectRating,
                                                  adjustedDuration,
                                                  sourceActor.getId());
                sourceActor.addEffectedActor(targetActor);
                break;
            }
            case ATTACK_RATING_UP:
            {
                targetActor.addNewStatusTemporary(Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                                  effectRating,
                                                  adjustedDuration,
                                                  sourceActor.getId());
                sourceActor.addEffectedActor(targetActor);
                break;
            }
            case ATTACK_RATING_DOWN:
            {
                targetActor.addNewStatusTemporary(Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                                  -effectRating,
                                                  adjustedDuration,
                                                  sourceActor.getId());
                sourceActor.addEffectedActor(targetActor);
                break;
            }
            case SPECIAL_MAXIMUM_UP:
            {
                targetActor.addNewStatusTemporary(Status.E_STATUS_TYPE.SPECIAL_MAXIMUM_MODIFIER,
                                                  effectRating,
                                                  adjustedDuration,
                                                  sourceActor.getId());
                sourceActor.addEffectedActor(targetActor);
                break;
            }
            case SPECIAL_MAXIMUM_DOWN:
            {
                targetActor.addNewStatusTemporary(Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                                  -effectRating,
                                                  adjustedDuration,
                                                  sourceActor.getId());
                sourceActor.addEffectedActor(targetActor);
                break;
            }
            case SPECIAL_RATING_UP:
            {
                targetActor.addNewStatusTemporary(Status.E_STATUS_TYPE.SPECIAL_RATING_MODIFIER,
                                                  effectRating,
                                                  adjustedDuration,
                                                  sourceActor.getId());
                sourceActor.addEffectedActor(targetActor);
                break;
            }
            case SPECIAL_RATING_DOWN:
            {
                targetActor.addNewStatusTemporary(Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                                  -effectRating,
                                                  adjustedDuration,
                                                  sourceActor.getId());
                sourceActor.addEffectedActor(targetActor);
                break;
            }
            case DEFENSE_RATING_UP:
            {
                targetActor.addNewStatusTemporary(Status.E_STATUS_TYPE.DEFENSE_RATING_MODIFIER,
                                                  effectRating,
                                                  adjustedDuration,
                                                  sourceActor.getId());
                sourceActor.addEffectedActor(targetActor);
                break;
            }
            case DEFENSE_RATING_DOWN:
            {
                targetActor.addNewStatusTemporary(Status.E_STATUS_TYPE.DEFENSE_RATING_MODIFIER,
                                                  -effectRating,
                                                  adjustedDuration,
                                                  sourceActor.getId());
                sourceActor.addEffectedActor(targetActor);
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
                actor.addNewStatusLinked(Status.E_STATUS_TYPE.RECURRING_HEAL,
                                         effectRating,
                                         linkId);
                break;
            }
            case ENERGY_RESTORE:
                actor.addNewStatusLinked(Status.E_STATUS_TYPE.RECURRING_ENERGY_RESTORE,
                                         effectRating,
                                         linkId);
                break;
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

                damage -= actor.getRealDefenseRating();
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
            case DEFENSE_RATING_UP:
            {
                actor.addNewStatusLinked(Status.E_STATUS_TYPE.DEFENSE_RATING_MODIFIER,
                                         effectRating,
                                         linkId);
                break;
            }
            case DEFENSE_RATING_DOWN:
            {
                actor.addNewStatusLinked(Status.E_STATUS_TYPE.DEFENSE_RATING_MODIFIER,
                                         -effectRating,
                                         linkId);
                break;
            }
        }
    }
}
