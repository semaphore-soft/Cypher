package com.semaphore_soft.apps.cypher.game;

import java.util.ArrayList;
import java.util.Hashtable;

import static com.semaphore_soft.apps.cypher.utils.CollectionManager.getNextID;

/**
 * Created by rickm on 1/19/2017.
 */

public class Actor
{
    private long    id;
    private int     markerID;
    private int     charID;
    private long    roomID;
    private boolean isPlayer;

    public enum E_STATE
    {
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

    Hashtable<Long, Special> specials;
    Hashtable<Long, Item>    items;
    Hashtable<Long, Status>  statuses;

    public Actor(long id)
    {
        this(id, -1);
    }

    public Actor(long id, int charID)
    {
        this(id, charID, -1);
    }

    public Actor(long id, int charID, int markerID)
    {
        this.id = id;
        this.markerID = markerID;
        this.charID = charID;
        roomID = -1;
        isPlayer = true;

        specials = new Hashtable<>();
        items = new Hashtable<>();
        statuses = new Hashtable<>();
    }

    public Actor(long id, long roomID)
    {
        this.id = id;
        this.markerID = -1;
        this.charID = -1;
        this.roomID = roomID;
        isPlayer = false;
    }

    public long getId()
    {
        return id;
    }

    public void setMarker(int tagID)
    {
        this.markerID = tagID;
    }

    public int getMarker()
    {
        return markerID;
    }

    public void setChar(int charID)
    {
        this.charID = charID;
    }

    public int getChar()
    {
        return charID;
    }

    public void setRoom(long roomID)
    {
        this.roomID = roomID;
    }

    public long getRoom()
    {
        return roomID;
    }

    public boolean isPlayer()
    {
        return isPlayer;
    }

    public void setHealthMaximum(int healthMaximum)
    {
        this.healthMaximum = healthMaximum;
    }

    public int getHealthMaximum()
    {
        return healthMaximum;
    }

    public void setHealthCurrent(int healthCurrent)
    {
        this.healthCurrent = healthCurrent;
    }

    public int getHealthCurrent()
    {
        return healthCurrent;
    }

    public void setAttackRating(int attackRating)
    {
        this.attackRating = attackRating;
    }

    public int getAttackRating()
    {
        return attackRating;
    }

    public Hashtable<Long, Special> getSpecials()
    {
        return specials;
    }

    public void addSpecial(Special special)
    {
        if (!specials.containsValue(special))
        {
            specials.put(special.getId(), special);
        }
    }

    public void setSpecialMaximum(int specialMaximum)
    {
        this.specialMaximum = specialMaximum;
    }

    public int getSpecialMaximum()
    {
        return specialMaximum;
    }

    public void setSpecialCurrent(int specialCurrent)
    {
        this.specialCurrent = specialCurrent;
    }

    public int getSpecialCurrent()
    {
        return specialCurrent;
    }

    public void setSpecialRating(int specialRating)
    {
        this.specialRating = specialRating;
    }

    public int getSpecialRating()
    {
        return specialRating;
    }

    public void setDefenceRating(int defenceRating)
    {
        this.defenceRating = defenceRating;
    }

    public int getDefenceRating()
    {
        return defenceRating;
    }

    public void setState(E_STATE state)
    {
        this.state = state;
    }

    public E_STATE getState()
    {
        return state;
    }

    public void attack(Actor actor)
    {
        actor.receiveAttack(getRealAttackRating());

        state = E_STATE.ATTACK;
    }

    private void receiveAttack(int attackRating)
    {
        int damage = attackRating;

        switch (state)
        {
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

    public void removeItem(long itemID)
    {
        if (items.containsKey(itemID))
        {
            items.remove(itemID);
            for (Long statusID : statuses.keySet())
            {
                Status status = statuses.get(statusID);
                if (status instanceof StatusLinked && ((StatusLinked) status).getLinkID() == itemID)
                {
                    removeStatus(status);
                }
            }
        }
    }

    public void removeItem(Item item)
    {
        if (items.containsKey(item.getID()))
        {
            items.remove(item.getID());
            for (Long statusID : statuses.keySet())
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

    public void addStatus(Status status)
    {
        if (!statuses.contains(status.getId()))
        {
            statuses.put(status.getId(), status);
        }
    }

    public void addNewStatusTemporary(Status.E_STATUS_TYPE type, int effectRating, int duration)
    {
        StatusTemporary status =
            new StatusTemporary(getNextID(statuses), type, effectRating, duration);

        statuses.put(status.getId(), status);
    }

    public void addNewStatusLinked(Status.E_STATUS_TYPE type, int effectRating, long linkId)
    {
        StatusLinked status = new StatusLinked(getNextID(statuses), type, effectRating, linkId);

        statuses.put(status.getId(), status);
    }

    public void removeStatus(long statusID)
    {
        if (statuses.containsKey(statusID))
        {
            statuses.remove(statusID);
        }
    }

    public void removeStatus(Status status)
    {
        if (statuses.containsKey(status.getId()))
        {
            statuses.put(status.getId(), status);
        }
    }

    //TODO the following three methods are almost identical, merge them somehow?
    public int getRealAttackRating()
    {
        int realAttackRating = attackRating;

        for (Long statusID : statuses.keySet())
        {
            Status status = statuses.get(statusID);
            if (status.getType() == Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER)
            {
                realAttackRating += status.getEffectRating();
            }
        }

        return realAttackRating;
    }

    public int getRealSpecialRating()
    {
        int realSpecialRating = specialRating;

        for (Long statusID : statuses.keySet())
        {
            Status status = statuses.get(statusID);
            if (status.getType() == Status.E_STATUS_TYPE.SPECIAL_RATING_MODIFIER)
            {
                realSpecialRating += status.getEffectRating();
            }
        }

        return realSpecialRating;
    }

    public int getRealDefenceRating()
    {
        int realDefenceRating = defenceRating;

        for (Long statusID : statuses.keySet())
        {
            Status status = statuses.get(statusID);
            if (status.getType() == Status.E_STATUS_TYPE.DEFENCE_RATING_MODIFIER)
            {
                realDefenceRating += status.getEffectRating();
            }

        }

        return realDefenceRating;
    }

    public void useItem(long itemID)
    {
        if (items.containsKey(itemID))
        {
            useItem(items.get(itemID));
        }
    }

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

    public void tick()
    {
        for (Long statusID : statuses.keySet())
        {
            Status status = statuses.get(statusID);
            if (status instanceof StatusTemporary && ((StatusTemporary) status).tick())
            {
                removeStatus(status);
            }
        }
    }
}
