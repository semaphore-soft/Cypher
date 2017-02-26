package com.semaphore_soft.apps.cypher.game;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static com.semaphore_soft.apps.cypher.utils.CollectionManager.getNextID;

/**
 * An instance of game.Actor holds and maintains information about the state of
 * one actor in the game, including its stats, Specials, Items, and Statuses.
 *
 * @author scorple
 */
public class Actor
{
    private int     id;
    private String  name;
    private int     markerID;
    private int     roomID;
    private boolean isPlayer;

    private String displayName;

    public enum E_STATE
    {
        NEUTRAL,
        ATTACK,
        SPECIAL,
        DEFEND
    }

    private int     healthMaximum;
    private int     healthCurrent;
    private int     attackRating;
    private int     specialMaximum;
    private int     specialCurrent;
    private int     specialRating;
    private int     defenceRating;
    private E_STATE state;

    private int     attackTickets  = 1;
    private int     defendTickets  = 1;
    private int     specialTickets = 1;
    private int     moveTickets    = 1;
    private boolean seeker         = false;

    private ConcurrentHashMap<Integer, Special> specials;
    private ConcurrentHashMap<Integer, Item>    items;
    private ConcurrentHashMap<Integer, Status>  statuses;

    /**
     * Logical ID and name constructor.
     * <p>
     * Creates an Actor object with a name an no association to an AR marker or
     * Room, in a neutral state. Because it is not created with a Room ID, it
     * will be assumed to be a player Actor. Initializes member HashMaps.
     *
     * @param id   int: The logical reference ID of this Actor.
     * @param name String: The reference name of this Actor. Primarily used for
     *             logging.
     */
    public Actor(int id, String name)
    {
        this(id, name, -1);
    }

    /**
     * Logical ID, name, and marker ID constructor.
     * <p>
     * Creates an Actor object with a name, an association to an AR marker, no
     * associated to a Room and in a neutral state. Because it is not created
     * with a Room ID, it will be assumed to be a player Actor. Initializes
     * member HashMaps.
     *
     * @param id       int: The logical reference ID of this Actor.
     * @param name     String: The reference name of this Actor. Primarily used for
     *                 logging.
     * @param markerID int: The marker/graphical reference ID of this Actor.
     */
    public Actor(int id, String name, int markerID)
    {
        this.id = id;
        this.name = name;
        this.markerID = markerID;
        roomID = -1;
        isPlayer = true;
        state = E_STATE.NEUTRAL;

        specials = new ConcurrentHashMap<>();
        items = new ConcurrentHashMap<>();
        statuses = new ConcurrentHashMap<>();
    }

    /**
     * Logical ID and room ID constructor.
     * <p>
     * Creates an Actor object without a name, associated with a Room, and not
     * associated with an AR marker, in a neutral state. Because it is created
     * with a Room ID, it will be assumed to be a non-player Actor. Initializes
     * member HashMaps.
     *
     * @param id     int: The logical reference ID of this Actor.
     * @param roomID int: The logical reference ID of the Room this Actor is
     *               associated with, or a resident of.
     */
    public Actor(int id, int roomID)
    {
        this(id, roomID, "error");
    }

    /**
     * Logical ID, room ID, and name constructor.
     * <p>
     * Creates an Actor object with a name, associated with a Room, and not
     * associated with an AR marker, in a neutral state. Because it is created
     * with a Room ID, it will be assumed to be a non-player Actor. Initializes
     * member HashMaps.
     *
     * @param id     int: The logical reference ID of this Actor.
     * @param roomID int: The logical reference ID of the Room this Actor is
     *               associated with, or a resident of.
     * @param name   String: The reference name of this Actor. Primarily used
     *               for logging.
     */
    public Actor(int id, int roomID, String name)
    {
        this.id = id;
        this.markerID = -1;
        this.name = name;
        this.roomID = roomID;
        isPlayer = false;
        state = E_STATE.NEUTRAL;

        specials = new ConcurrentHashMap<>();
        items = new ConcurrentHashMap<>();
        statuses = new ConcurrentHashMap<>();
    }

    /**
     * Get the logical reference ID of this Actor.
     * <p>
     * To be used for referencing this Actor in a game state context
     * (associating with rooms, checking stats, etc.) ONLY.
     *
     * @return int: The logical reference ID of this Actor.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Get the reference name of this Actor.
     * <p>
     * To be used for internal purposes ONLY. Should not be exposed to the
     * user.
     *
     * @return String: The reference name of this Actor.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the reference name of this Actor.
     * <p>
     * To be used for internal purposes ONLY. Should not be exposed to the
     * user.
     *
     * @param name String: The reference name to be used for this Actor.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Set the marker/graphical reference ID of this Actor.
     * <p>
     * To be used for checking the position of a player marker/waypoint ONLY.
     *
     * @param tagID int: The marker/graphical reference ID to be used for this
     *              Actor.
     */
    public void setMarker(int tagID)
    {
        this.markerID = tagID;
    }

    /**
     * Get the marker/graphical reference ID of this Actor.
     * <p>
     * To be used for checking the position of a player marker/waypoint ONLY.
     *
     * @return int: The marker/graphical reference ID of this Actor.
     */
    public int getMarker()
    {
        return markerID;
    }

    /**
     * Set the logical reference ID of the Room this Actor associates with, or
     * considers itself to be a resident of. This Actor will consider itself
     * to be a resident of, or located within, that Room, until its Room ID is
     * updated.
     *
     * @param roomID int: The logical reference ID of the Room this Actor is
     *               to associate with, or consider itself a resident of.
     */
    public void setRoom(int roomID)
    {
        this.roomID = roomID;
    }

    /**
     * Get the logical reference ID of the Room this Actor associates with, or
     * considers itself to be a resident of.
     *
     * @return int: The logical reference ID of the Room this Actor associates
     * with, or considers itself to be a member of.
     */
    public int getRoom()
    {
        return roomID;
    }

    /**
     * Check whether or not this Actor is a player Actor.
     *
     * @return boolean:
     *         <ul>
     *         <li>True if this Actor is considered to be controlled by a player.</li>
     *         <li>False otherwise.</li>
     *         </ul>
     */
    public boolean isPlayer()
    {
        return isPlayer;
    }

    /**
     * Set the display name of this Actor.
     * <p>
     * To be used for any and all user feedback including the name of this
     * Actor in plain text.
     *
     * @param displayName String: The display name to be used for this Actor.
     */
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    /**
     * Get the display name for this Actor.
     * <p>
     * To be used for any and all user feedback including the name of this
     * Actor in plain text.
     *
     * @return String: The display name to be used for this Actor.
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Set the maximum amount of health this Actor can have.
     * <p>
     * The Actor will start with this amount of health and not be able to be
     * healed to have more than this amount of health.
     *
     * @param healthMaximum int: The maximum amount of health this Actor will
     *                      be able to have.
     */
    public void setHealthMaximum(int healthMaximum)
    {
        this.healthMaximum = healthMaximum;
    }

    /**
     * Get the maximum amount of health this Actor can have.
     * <p>
     * The Actor will start with this amount of health and not be able to be
     * healed to have more than this amount of health.
     *
     * @return int: The maximum amount of health this Actor can have.
     */
    public int getHealthMaximum()
    {
        return healthMaximum;
    }

    /**
     * Set the current amount of health this Actor has.
     *
     * @param healthCurrent int: The new current amount of health this Actor is
     *                      to have.
     */
    public void setHealthCurrent(int healthCurrent)
    {
        this.healthCurrent = healthCurrent;
    }

    /**
     * Get the current amount of health this Actor has.
     *
     * @return int: The current amount of health this Actor has.
     */
    public int getHealthCurrent()
    {
        return healthCurrent;
    }

    /**
     * Set this Actor's base attack rating.
     * <p>
     * Attack rating is used in determining how much damage this Actor does to
     * another Actor when performing an attack.
     *
     * @param attackRating int: The base attack rating this Actor will have.
     */
    public void setAttackRating(int attackRating)
    {
        this.attackRating = attackRating;
    }

    /**
     * Get this Actor's base attack rating.
     * <p>
     * Attack rating is used in determining how much damage this Actor does to
     * another Actor when performing an attack.
     *
     * @return int: The base attack rating of this Actor.
     */
    public int getAttackRating()
    {
        return attackRating;
    }

    /**
     * Get the HashMap of Specials this Actor associates with, or has the
     * ability to use.
     *
     * @return ConcurrentHashMap: A HashMap associating integer IDs with
     *         Special objects.
     */
    public ConcurrentHashMap<Integer, Special> getSpecials()
    {
        return specials;
    }

    /**
     * Add a single Special object that this Actor associates with, or has the
     * ability to use.
     *
     * @param special Special: The Special object being associated with.
     */
    public void addSpecial(Special special)
    {
        if (!specials.containsValue(special))
        {
            specials.put(special.getId(), special);
        }
    }

    /**
     * Set the maximum amount of special energy this Actor can have.
     * <p>
     * The Actor will start with this amount of special energy and not be able
     * to recover more than this amount of special energy.
     *
     * @param specialMaximum int: The maximum amount of special energy this
     *                       Actor will be able to have.
     */
    public void setSpecialMaximum(int specialMaximum)
    {
        this.specialMaximum = specialMaximum;
    }

    /**
     * Get the maximum amount of special energy this Actor can have.
     * <p>
     * The Actor will start with this amount of special energy and not be able
     * to recover more than this amount of special energy.
     *
     * @return int: The maximum amount of special energy this Actor can have.
     */
    public int getSpecialMaximum()
    {
        return specialMaximum;
    }

    /**
     * Set the current amount of special energy this Actor has.
     * <p>
     * Used to determine whether or not this Actor has enough energy to perform
     * a Special move.
     *
     * @param specialCurrent int: The new current amount of special energy this
     *                       Actor has.
     */
    public void setSpecialCurrent(int specialCurrent)
    {
        this.specialCurrent = specialCurrent;
    }

    /**
     * Get the current amount of special energy this Actor has.
     * <p>
     * Used to determine whether or not this Actor has enough energy to perform
     * a Special move.
     *
     * @return int: The current amount of special energy this Actor has.
     */
    public int getSpecialCurrent()
    {
        return specialCurrent;
    }

    /**
     * Set this Actor's base Special rating.
     * <p>
     * Used in determining the strength of Special move Effects.
     *
     * @param specialRating int: The base Special rating this Actor will have.
     */
    public void setSpecialRating(int specialRating)
    {
        this.specialRating = specialRating;
    }

    /**
     * Get this Actor's base Special rating.
     * <p>
     * Used in determining the strength of Special move Effects.
     *
     * @return int: The base Special rating of this Actor.
     */
    public int getSpecialRating()
    {
        return specialRating;
    }

    /**
     * Set this Actor's base defense rating.
     * <p>
     * Used in determining the amount of damage this Actor ignores when
     * attacked.
     *
     * @param defenceRating int: The base defense rating this Actor will have.
     */
    public void setDefenceRating(int defenceRating)
    {
        this.defenceRating = defenceRating;
    }

    /**
     * Get this Actor's base defense rating.
     * <p>
     * Used in determining the amount of damage this Actor ignores when
     * attacked.
     *
     * @return int: The base defense rating of this Actor.
     */
    public int getDefenceRating()
    {
        return defenceRating;
    }

    /**
     * Set the state of this Actor.
     * <p>
     * Describes the most recently taken action of this Actor. Used in
     * determining the effectiveness of attacks and Specials used against this
     * Actor.
     *
     * @param state E_STATE: The new state of this Actor.
     */
    public void setState(E_STATE state)
    {
        this.state = state;
    }

    /**
     * Get the state of this Actor.
     * <p>
     * Describes the most recently taken action of this Actor. Used in
     * determining the effectiveness of attacks and Specials used against this
     * Actor.
     *
     * @return E_STATE: The state of this Actor.
     */
    public E_STATE getState()
    {
        return state;
    }

    /**
     * Get the number of attack tickets given to this Actor.
     * <p>
     * Used in determining the behavior of non-player Actors. If it is possible
     * for this Actor to attack on a turn taken by the computer, it will enter
     * this many attack tickets in the 'action lottery'.
     *
     * @return int: The number of attack tickets given to this Actor.
     */
    public int getAttackTickets()
    {
        return attackTickets;
    }

    /**
     * Set the number of attack tickets given to this Actor.
     * <p>
     * Used in determining the behavior of non-player Actors. If it is possible
     * for this Actor to attack on a turn taken by the computer, it will enter
     * this many attack tickets in the 'action lottery'.
     *
     * @param attackTickets int: The number of attack tickets to be given to
     *                      this Actor.
     */
    public void setAttackTickets(int attackTickets)
    {
        this.attackTickets = attackTickets;
    }

    /**
     * Get the number of defend tickets given to this Actor.
     * <p>
     * Used in determining the behavior of non-player Actors. If there is an
     * Actor for this Actor to defend itself from on a turn taken by the
     * computer, it will enter this many defend tickets in the 'action
     * lottery'.
     *
     * @return int: The number of defend tickets given to this Actor.
     */
    public int getDefendTickets()
    {
        return defendTickets;
    }

    /**
     * Set the number of defend tickets given to this Actor.
     * <p>
     * Used in determining the behavior of non-player Actors. If there is an
     * Actor for this Actor to defend itself from on a turn taken by the
     * computer, it will enter this many defend tickets in the 'action
     * lottery'.
     *
     * @param defendTickets int: The number of defend tickets to be given to
     *                      this Actor.
     */
    public void setDefendTickets(int defendTickets)
    {
        this.defendTickets = defendTickets;
    }

    /**
     * Get the number of Special tickets given to this Actor.
     * <p>
     * Used in determining the behavior of non-player Actors. If there is an
     * Actor for this Actor may use a Special move on, and it has enough
     * Special energy to perform that Special move, during a turn taken by the
     * computer, it will enter this many Special tickets in the 'action
     * lottery'.
     *
     * @return The number of Special tickets given to this Actor.
     */
    public int getSpecialTickets()
    {
        return specialTickets;
    }

    /**
     * Set the number of Special tickets given to this Actor.
     * <p>
     * Used in determining the behavior of non-player Actors. If there is an
     * Actor for this Actor may use a Special move on, and it has enough
     * Special energy to perform that Special move, during a turn taken by the
     * computer, it will enter this many Special tickets in the 'action
     * lottery'.
     *
     * @param specialTickets int: The number of Special tickets to be given to
     *                       this Actor.
     */
    public void setSpecialTickets(int specialTickets)
    {
        this.specialTickets = specialTickets;
    }

    /**
     * Get the number of move tickets given to this Actor.
     * <p>
     * Used in determining the behavior of non-player Actors. If there is a
     * valid Room this Actor may move to on a turn taken by the computer, it
     * will enter this many move tickets in the 'action lottery'.
     *
     * @return int: The number of move tickets given to this Actor.
     */
    public int getMoveTickets()
    {
        return moveTickets;
    }

    /**
     * Set the number of move tickets given to this Actor.
     * <p>
     * Used in determining the behavior of non-player Actors. If there is a
     * valid Room this Actor may move to on a turn taken by the computer, it
     * will enter this many move tickets in the 'action lottery'.
     *
     * @param moveTickets int: The number of move tickets to be given to this
     *                    Actor.
     */
    public void setMoveTickets(int moveTickets)
    {
        this.moveTickets = moveTickets;
    }

    /**
     * Check this Actor's 'seeker' flag.
     * <p>
     * Used in determining the behavior of non-player Actors. An Actor which
     * has the 'seeker' flag set will be given 1 move ticket, such that Actors
     * which would not normally move may move if there is not an Actor present
     * to fight.
     *
     * @return boolean:
     *         <ul>
     *         <li>True of this Actor is considered a 'seeker'.</li>
     *         <li>False otherwise.</li>
     *         </ul>
     */
    public boolean isSeeker()
    {
        return seeker;
    }

    /**
     * Set this Actor's 'seeker' flag.
     * <p>
     * Used in determining the behavior of non-player Actors. An Actor which
     * has the 'seeker' flag set will be given 1 move ticket, such that Actors
     * which would not normally move may move if there is not an Actor present
     * to fight.
     *
     * @param seeker boolean:
     *               <ul>
     *               <li>True of this Actor is to be considered a 'seeker'.</li>
     *               <li>False otherwise.</li>
     *               </ul>
     */
    public void setSeeker(boolean seeker)
    {
        this.seeker = seeker;
    }

    /**
     * Perform an attack on a given Actor. Sets this Actor's state to 'attack'
     * and calls receiveAttack on the target Actor with this Actor's modified
     * attack rating.
     *
     * @param actor Actor: The target of this Actor's attack.
     */
    public void attack(Actor actor)
    {
        actor.receiveAttack(getRealAttackRating());

        state = E_STATE.ATTACK;
    }

    /**
     * Calculate the amount of damage received by this Actor based on the
     * attack rating, this Actor's state, and defence rating. Update this
     * Actor's current health accordingly.
     *
     * @param attackRating int: The attack rating of the incoming attack.
     */
    private void receiveAttack(int attackRating)
    {
        int damage = attackRating;

        switch (state)
        {
            case NEUTRAL:
            case ATTACK:
                damage = attackRating;
                break;
            case SPECIAL:
                damage = attackRating * 2;
                break;
            case DEFEND:
                damage = attackRating / 2;
                break;
            default:
                break;
        }

        damage -= getRealDefenceRating();
        damage = (damage < 0) ? 0 : damage;

        healthCurrent = (damage > healthCurrent) ? 0 : healthCurrent - damage;
    }

    /**
     * Attempt to perform a Special ability targeting one Actor.
     * <p>
     * If this Actor has an amount of Special energy greater than or equal to
     * the Special energy cost of the given Special ability, subtract the cost
     * of the given Special ability from this Actor's current Special energy
     * and have the Special apply itself to the target Actor.
     *
     * @param special Special: The Special ability to attempt to perform.
     * @param actor   Actor: The target Actor of this Actor's Special ability.
     *
     * @return boolean:
     *         <ul>
     *         <li>True if this Actor had enough Special energy to perform the
     *         given Special ability.</li>
     *         <li>False otherwise.</li>
     *         </ul>
     */
    public boolean performSpecial(Special special, Actor actor)
    {
        if (special.getCost() > specialCurrent)
        {
            return false;
        }
        else
        {
            specialCurrent -= special.getCost();
        }

        special.applySpecial(getRealSpecialRating(), actor);

        return true;
    }

    /**
     * Attempt to perform a Special ability targeting one or more Actors.
     * <p>
     * If this Actor has an amount of Special energy greater than or equal to
     * the Special energy cost of the given Special ability, subtract the cost
     * of the given Special ability from this Actor's current Special energy
     * and have the Special apply itself to each of the target Actors.
     *
     * @param special Special: The Special ability to attempt to perform.
     * @param actors  ArrayList: The list of target Actors of this Actor's
     *                Special ability.
     * @return boolean:
     *         <ul>
     *         <li>True if this Actor had enough Special energy to perform the
     *         given Special ability.</li>
     *         <li>False otherwise.</li>
     *         </ul>
     */
    public boolean performSpecial(Special special, ArrayList<Actor> actors)
    {
        if (special.getCost() > specialCurrent)
        {
            return false;
        }
        else
        {
            specialCurrent -= special.getCost();
        }

        for (Actor actor : actors)
        {
            special.applySpecial(getRealSpecialRating(), actor);
        }

        return true;
    }

    /**
     * Add a single Item for this Actor to associate with
     * <p>
     * An Actor may associate, or 'have', Items which are consumable or
     * durable.
     * <p>
     * ItemConsumables may be 'used' to create immediate effects or
     * temporary Statuses.
     * <p>
     * ItemDurables create a linked Status on acquisition, which is removed
     * when the Item is lost.
     *
     * @param item Item: The new Item for this Actor to associate with, or
     *             'have' in its 'inventory'.
     */
    public void addItem(Item item)
    {
        if (!items.containsKey(item.getID()))
        {
            items.put(item.getID(), item);
            if (item instanceof ItemDurable)
            {
                for (Effect.E_EFFECT effect : item.getEffects())
                {
                    Effect.applyLinkedEffect(effect, item.getEffectRating(), this, item.getID());
                }
            }
        }
    }

    /**
     * Dissociate an Item from this Actor, or remove it from this Actor's
     * 'inventory'.
     * <p>
     * ItemConsumables will no longer be able to be used by this Actor once
     * removed from this Actor, unless re-added.
     * <p>
     * Any Statuses added by an ItemDurable will be removed from this Actor
     * along with the ItemDurable, unless re-added.
     *
     * @param itemID int: The logical reference ID of the Item to dissociate,
     *               or remove from, this Actor.
     */
    public void removeItem(int itemID)
    {
        if (items.containsKey(itemID))
        {
            items.remove(itemID);
            for (int statusID : statuses.keySet())
            {
                Status status = statuses.get(statusID);
                if (status instanceof StatusLinked && ((StatusLinked) status).getLinkID() == itemID)
                {
                    removeStatus(status);
                }
            }
        }
    }

    /**
     * Dissociate an Item from this Actor, or remove it from this Actor's
     * 'inventory'.
     * <p>
     * ItemConsumables will no longer be able to be used by this Actor once
     * removed from this Actor, unless re-added.
     * <p>
     * Any Statuses added by an ItemDurable will be removed from this Actor
     * along with the ItemDurable, unless re-added.
     *
     * @param item Item: The Item object to dissociate, or remove from, this
     *             Actor.
     */
    public void removeItem(Item item)
    {
        if (items.containsKey(item.getID()))
        {
            items.remove(item.getID());
            for (int statusID : statuses.keySet())
            {
                Status status = statuses.get(statusID);
                if (status instanceof StatusLinked &&
                    ((StatusLinked) status).getLinkID() == item.getID())
                {
                    removeStatus(status);
                }
            }
        }
    }

    /**
     * Add a single Status for this Actor to associate with, or 'have'.
     *
     * @param status Status: The new Status for this Actor to associate with.
     */
    public void addStatus(Status status)
    {
        if (!statuses.containsKey(status.getId()))
        {
            statuses.put(status.getId(), status);
        }
    }

    /**
     * Create a new temporary Status for this Actor to associate, or 'have',
     * from a given Status type, effect rating, and duration.
     * <p>
     * A temporary Status may be added as the result of this Actor using an
     * ItemConsumable or being the target of a Special ability. It will be
     * removed after this Actor has taken [duration] turns.
     *
     * @param type         E_STATUS_TYPE: The type of the StatusTemporary to be
     *                     created.
     * @param effectRating int: The Effect rating of the StatusTemporary to be
     *                     created.
     * @param duration     int: The duration of the StatusTemporary to be
     *                     created.
     */
    public void addNewStatusTemporary(Status.E_STATUS_TYPE type, int effectRating, int duration)
    {
        StatusTemporary status =
            new StatusTemporary(getNextID(statuses), type, effectRating, duration);

        statuses.put(status.getId(), status);
    }

    /**
     * Create a new linked Status for this Actor to associate with, or 'have',
     * from a given Status type, effect rating, and link ID.
     * <p>
     * A linked Status may be added as the result of this Actor acquiring an
     * ItemDurable. It will be removed if and when the ItemDurable is removed
     * from the Actor.
     *
     * @param type         E_STATUS_TYPE: The type of the StatusLinked to be
     *                     created.
     * @param effectRating int: The Effect rating of the StatusLinked to be
     *                     created.
     * @param linkId       int: The logical reference ID of the Item to which
     *                     the StatusLinked being created is linked.
     */
    public void addNewStatusLinked(Status.E_STATUS_TYPE type, int effectRating, int linkId)
    {
        StatusLinked status = new StatusLinked(getNextID(statuses), type, effectRating, linkId);

        statuses.put(status.getId(), status);
    }

    /**
     * Dissociate, or remove, a Status from this Actor.
     *
     * @param statusID int: The logical reference ID of the Status to remove
     *                 from this Actor.
     */
    public void removeStatus(int statusID)
    {
        if (statuses.containsKey(statusID))
        {
            statuses.remove(statusID);
        }
    }

    /**
     * Dissociate, or remove, a Status from this Actor.
     *
     * @param status Status: The Status object to remove from this Actor.
     */
    public void removeStatus(Status status)
    {
        if (statuses.containsKey(status.getId()))
        {
            statuses.put(status.getId(), status);
        }
    }

    //TODO the following three methods are almost identical, merge them somehow?
    /**
     * Get this Actor's attack rating modified by any Statuses this Actor has
     * which impact attack rating.
     *
     * @return int: This Actor's attack rating modified by Statuses.
     */
    public int getRealAttackRating()
    {
        int realAttackRating = attackRating;

        for (int statusID : statuses.keySet())
        {
            Status status = statuses.get(statusID);
            if (status.getType() == Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER)
            {
                realAttackRating += status.getEffectRating();
            }
        }

        return realAttackRating;
    }

    /**
     * Get this Actor's Special rating modified by any Statuses this Actor has
     * which impact Special rating.
     *
     * @return int: This Actor's Special rating modified by Statuses.
     */
    public int getRealSpecialRating()
    {
        int realSpecialRating = specialRating;

        for (int statusID : statuses.keySet())
        {
            Status status = statuses.get(statusID);
            if (status.getType() == Status.E_STATUS_TYPE.SPECIAL_RATING_MODIFIER)
            {
                realSpecialRating += status.getEffectRating();
            }
        }

        return realSpecialRating;
    }

    /**
     * Get this Actor's defense rating modified by any Statuses this Actor has
     * which impact defense rating.
     *
     * @return int: This Actor's defense rating modified by Statuses.
     */
    public int getRealDefenceRating()
    {
        int realDefenceRating = defenceRating;

        for (int statusID : statuses.keySet())
        {
            Status status = statuses.get(statusID);
            if (status.getType() == Status.E_STATUS_TYPE.DEFENCE_RATING_MODIFIER)
            {
                realDefenceRating += status.getEffectRating();
            }

        }

        return realDefenceRating;
    }

    /**
     * Apply the Effects of a ConsumableItem to this Actor.
     * <p>
     * To be used when an Actor uses a ConsumableItem.
     *
     * @param itemID int: The logical reference ID of the ConsumableItem to
     *               apply the effects of to this Actor.
     */
    public void useItem(int itemID)
    {
        if (items.containsKey(itemID))
        {
            useItem(items.get(itemID));
        }
    }

    /**
     * Apply the Effects of a ConsumableItem to this Actor.
     * <p>
     * To be used when an Actor uses a ConsumableItem.
     *
     * @param item Item: The ConsumableItem object to apply the effects of to
     *             this Actor.
     */
    public void useItem(Item item)
    {
        if (items.containsKey(item.getID()))
        {
            if (item instanceof ItemConsumable)
            {
                for (Effect.E_EFFECT effect : item.getEffects())
                {
                    Effect.applyTemporaryEffect(effect,
                                                item.getEffectRating(),
                                                ((ItemConsumable) item).getDuration(),
                                                this);
                }
            }
        }
    }

    /**
     * Decrement the duration of any temporary Statuses this Actor associates
     * with and remove any expired Statuses.
     * <p>
     * To be called at the end of an Actor's turn.
     */
    public void tick()
    {
        for (int statusID : statuses.keySet())
        {
            Status status = statuses.get(statusID);
            if (status instanceof StatusTemporary && ((StatusTemporary) status).tick())
            {
                removeStatus(status);
            }
        }
    }
}
