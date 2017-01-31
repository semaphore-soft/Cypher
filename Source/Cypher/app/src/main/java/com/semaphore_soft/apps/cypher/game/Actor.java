package com.semaphore_soft.apps.cypher.game;

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

    private enum E_MODIFIER_TYPE
    {
        ATTACK,
        SPECIAL,
        DEFEND
    }

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

                healthCurrent = (damage > healthCurrent) ? 0 : healthCurrent - damage;
                break;
            case DEFEND:
                defenceRating += specialRating;
                break;
            case HEAL:
                healthCurrent = ((healthCurrent + specialRating) > healthMaximum) ? healthMaximum :
                                healthCurrent + specialRating;
                break;
        }
    }
}
