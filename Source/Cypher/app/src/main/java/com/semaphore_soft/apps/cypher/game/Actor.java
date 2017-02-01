package com.semaphore_soft.apps.cypher.game;

import java.util.Collections;
import java.util.Hashtable;

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

    private enum E_SPECIAL_TYPE
    {
        ATTACK,
        DEFEND,
        HEAL
    }

    private enum E_STATE
    {
        ATTACK,
        SPECIAL,
        DEFEND
    }

    private int            healthMaximum;
    private int            healthCurrent;
    private int            attackRating;
    private E_SPECIAL_TYPE specialType;
    private int            specialMaximum;
    private int            specialCurrent;
    private int            specialRating;
    private int            specialCost;
    private int            defenceRating;
    private E_STATE        state;

    Hashtable<Long, Item>   items;
    Hashtable<Long, Status> statuses;

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

    public E_SPECIAL_TYPE getSpecialType()
    {
        return specialType;
    }

    public void setSpecialType(E_SPECIAL_TYPE specialType)
    {
        this.specialType = specialType;
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

    public void setSpecialCost(int specialCost)
    {
        this.specialCost = specialCost;
    }

    public int getSpecialCost()
    {
        return specialCost;
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

    public void attack(Actor victim)
    {
        victim.receiveAttack(attackRating);
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

    public boolean performSpecial(Actor victim)
    {
        if (specialCost > specialCurrent)
        {
            return false;
        }
        else
        {
            specialCurrent -= specialCost;
        }

        victim.receiveSpecial(specialType, specialRating);

        return true;
    }

    private void receiveSpecial(E_SPECIAL_TYPE specialType, int specialRating)
    {
        switch (specialType)
        {
            case ATTACK:
                int damage = specialRating;

                switch (state)
                {
                    case ATTACK:
                        damage = attackRating * 2;
                        break;
                    case SPECIAL:
                        damage = attackRating;
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
                break;
            case DEFEND:
                StatusTemporary defenceBuff = new StatusTemporary(getNextID(statuses),
                                                                  Status.E_STATUS_TYPE.DEFENCE_RATING_MODIFIER,
                                                                  specialRating,
                                                                  2);
                statuses.put(defenceBuff.getID(), defenceBuff);
                break;
            case HEAL:
                healthCurrent = ((healthCurrent + specialRating) > healthMaximum) ? healthMaximum :
                                healthCurrent + specialRating;
                break;
        }
    }

    public void addItem(Item item)
    {
        if (!items.containsKey(item.getID()))
        {
            items.put(item.getID(), item);
            if (item instanceof ItemDurable)
            {
                switch (item.getType())
                {
                    case HEALTH_MAXIMUM_MODIFIER:
                    {
                        Status status = new StatusLinked(getNextID(statuses),
                                                         Status.E_STATUS_TYPE.HEALTH_MAXIMUM_MODIFIER,
                                                         item.getEffectRating(),
                                                         item.getID());
                        addStatus(status);
                        break;
                    }
                    case ATTACK_RATING_MODIFIER:
                    {
                        Status status = new StatusLinked(getNextID(statuses),
                                                         Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                                         item.getEffectRating(),
                                                         item.getID());
                        addStatus(status);
                        break;
                    }
                    case SPECIAL_MAXIMUM_MODIFIER:
                    {
                        Status status = new StatusLinked(getNextID(statuses),
                                                         Status.E_STATUS_TYPE.SPECIAL_MAXIMUM_MODIFIER,
                                                         item.getEffectRating(),
                                                         item.getID());
                        addStatus(status);
                        break;
                    }
                    case SPECIAL_RATING_MODIFIER:
                    {
                        Status status = new StatusLinked(getNextID(statuses),
                                                         Status.E_STATUS_TYPE.SPECIAL_RATING_MODIFIER,
                                                         item.getEffectRating(),
                                                         item.getID());
                        addStatus(status);
                        break;
                    }
                    case SPECIAL_COST_MODIFIER:
                    {
                        Status status = new StatusLinked(getNextID(statuses),
                                                         Status.E_STATUS_TYPE.SPECIAL_COST_MODIFIER,
                                                         item.getEffectRating(),
                                                         item.getID());
                        addStatus(status);
                        break;
                    }
                    case DEFENCE_RATING_MODIFIER:
                    {
                        Status status = new StatusLinked(getNextID(statuses),
                                                         Status.E_STATUS_TYPE.DEFENCE_RATING_MODIFIER,
                                                         item.getEffectRating(),
                                                         item.getID());
                        addStatus(status);
                        break;
                    }
                    default:
                        break;
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
        if (!statuses.contains(status.getID()))
        {
            statuses.put(status.getID(), status);
        }
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
        if (statuses.containsKey(status.getID()))
        {
            statuses.put(status.getID(), status);
        }
    }

    //TODO the following three methods are almost identical, merge them somehow?
    private int getRealAttackRating()
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

    private int getRealSpecialRating()
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

    private int getRealDefenceRating()
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
                switch (item.getType())
                {
                    case KEY:
                        break;
                    case HEALTH_MAXIMUM_MODIFIER:
                    {
                        Status status = new StatusTemporary(getNextID(statuses),
                                                            Status.E_STATUS_TYPE.HEALTH_MAXIMUM_MODIFIER,
                                                            item.getEffectRating(),
                                                            ((ItemConsumable) item).getDuration());
                        addStatus(status);
                        break;
                    }
                    case HEALTH_RESTORE:
                        healthCurrent += item.getEffectRating();
                        break;
                    case ATTACK_RATING_MODIFIER:
                    {
                        Status status = new StatusTemporary(getNextID(statuses),
                                                            Status.E_STATUS_TYPE.ATTACK_RATING_MODIFIER,
                                                            item.getEffectRating(),
                                                            ((ItemConsumable) item).getDuration());
                        addStatus(status);
                        break;
                    }
                    case SPECIAL_MAXIMUM_MODIFIER:
                    {
                        Status status = new StatusTemporary(getNextID(statuses),
                                                            Status.E_STATUS_TYPE.SPECIAL_MAXIMUM_MODIFIER,
                                                            item.getEffectRating(),
                                                            ((ItemConsumable) item).getDuration());
                        addStatus(status);
                        break;
                    }
                    case SPECIAL_RESTORE:
                    {
                        specialCurrent += item.getEffectRating();
                        break;
                    }
                    case SPECIAL_RATING_MODIFIER:
                    {
                        Status status = new StatusTemporary(getNextID(statuses),
                                                            Status.E_STATUS_TYPE.SPECIAL_RATING_MODIFIER,
                                                            item.getEffectRating(),
                                                            ((ItemConsumable) item).getDuration());
                        addStatus(status);
                        break;
                    }
                    case SPECIAL_COST_MODIFIER:
                    {
                        Status status = new StatusTemporary(getNextID(statuses),
                                                            Status.E_STATUS_TYPE.SPECIAL_COST_MODIFIER,
                                                            item.getEffectRating(),
                                                            ((ItemConsumable) item).getDuration());
                        addStatus(status);
                        break;
                    }
                    case DEFENCE_RATING_MODIFIER:
                    {
                        Status status = new StatusTemporary(getNextID(statuses),
                                                            Status.E_STATUS_TYPE.DEFENCE_RATING_MODIFIER,
                                                            item.getEffectRating(),
                                                            ((ItemConsumable) item).getDuration());
                        addStatus(status);
                        break;
                    }
                    default:
                        break;
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

    public long getNextID(Hashtable<Long, ?> hashtable)
    {
        return ((hashtable.size() > 0) ? Collections.max(hashtable.keySet()) + 1 : 0);
    }
}
