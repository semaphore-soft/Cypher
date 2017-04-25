package com.semaphore_soft.apps.cypher.game;

import com.semaphore_soft.apps.cypher.utils.Logger;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static com.semaphore_soft.apps.cypher.utils.CollectionManager.getNextID;

/**
 * An instance of {@link Actor game.Actor} holds and maintains information
 * about the state of one actor in the game, including its stats, {@link
 * Special Specials}, {@link Item Items}, and {@link Status Statuses}.
 * <p>
 * May either be a player controlled 'character' or a non-player 'enemy'
 * controlled by the {@link ActorController}.
 *
 * @author scorple
 * @see Special
 * @see Item
 * @see Status
 * @see ActorController
 */
public class Actor
{
    private int     id;
    private String  name;
    private int     markerId;
    private int     roomId;
    private int     proposedRoomId;
    private boolean isPlayer;

    private String displayName;

    /**
     * Describes the most recently taken action of an {@link Actor}. Used in
     * determining the effectiveness of attacks and {@link Special Specials}
     * used against this {@link Actor}.
     *
     * @see Special
     */
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
    private int     useItemTickets = 1;
    private boolean seeker         = false;

    private boolean boss = false;

    private ConcurrentHashMap<Integer, Special> specials;
    private ConcurrentHashMap<Integer, Item>    items;
    private ConcurrentHashMap<Integer, Status>  statuses;

    /**
     * Logical ID and name constructor.
     * <p>
     * Creates an {@link Actor} object with a name an no association to an AR
     * marker or {@link Room}, in a neutral state. Because it is not created
     * with a {@link Room} ID, it will be assumed to be a player {@link Actor}.
     * Initializes member HashMaps.
     *
     * @param id   int: The logical reference ID of this {@link Actor}.
     * @param name String: The reference name of this {@link Actor}. Primarily
     *             used for logging.
     *
     * @see E_STATE
     */
    public Actor(int id, String name)
    {
        this(id, name, -1);
    }

    /**
     * Logical ID, name, and marker ID constructor.
     * <p>
     * Creates an {@link Actor} object with a name, an association to an AR
     * marker, no associated to a {@link Room} and in a neutral state. Because
     * it is not created with a {@link Room} ID, it will be assumed to be a
     * player {@link Actor}. Initializes member HashMaps.
     *
     * @param id       int: The logical reference ID of this {@link Actor}.
     * @param name     String: The reference name of this {@link Actor}.
     *                 Primarily used for logging.
     * @param markerId int: The marker/graphical reference ID of this {@link
     *                 Actor}.
     *
     * @see E_STATE
     */
    public Actor(int id, String name, int markerId)
    {
        this.id = id;
        this.name = name;
        this.markerId = markerId;
        roomId = -1;
        proposedRoomId = -1;
        isPlayer = true;
        state = E_STATE.NEUTRAL;

        specials = new ConcurrentHashMap<>();
        items = new ConcurrentHashMap<>();
        statuses = new ConcurrentHashMap<>();
    }

    /**
     * Logical ID and {@link Room} ID constructor.
     * <p>
     * Creates an {@link Actor} object without a name, associated with a {@link
     * Room}, and not associated with an AR marker, in a neutral state. Because
     * it is created with a {@link Room} ID, it will be assumed to be a
     * non-player {@link Actor}. Initializes member HashMaps.
     *
     * @param id     int: The logical reference ID of this {@link Actor}.
     * @param roomId int: The logical reference ID of the {@link Room} this
     *               {@link Actor} is associated with, or a resident of.
     *
     * @see E_STATE
     */
    public Actor(int id, int roomId)
    {
        this(id, roomId, "error");
    }

    /**
     * Logical ID, {@link Room} ID, and name constructor.
     * <p>
     * Creates an {@link Actor} object with a name, associated with a {@link
     * Room}, and not associated with an AR marker, in a neutral state. Because
     * it is created with a {@link Room} ID, it will be assumed to be a
     * non-player {@link Actor}. Initializes member HashMaps.
     *
     * @param id     int: The logical reference ID of this {@link Actor}.
     * @param roomId int: The logical reference ID of the {@link Room} this
     *               {@link Actor} is associated with, or a resident of.
     * @param name   String: The reference name of this {@link Actor}.
     *               Primarily used for logging.
     *
     * @see E_STATE
     */
    public Actor(int id, int roomId, String name)
    {
        this(id, roomId, name, false);
    }

    /**
     * Logical ID, {@link Room} ID, and name constructor.
     * <p>
     * Creates an {@link Actor} object with a name, associated with a {@link
     * Room}, and not associated with an AR marker, in a neutral state. Because
     * it is created with a {@link Room} ID, it will be assumed to be a
     * non-player {@link Actor}. Initializes member HashMaps.
     *
     * @param id     int: The logical reference ID of this {@link Actor}.
     * @param roomId int: The logical reference ID of the {@link Room} this
     *               {@link Actor} is associated with, or a resident of.
     * @param name   String: The reference name of this {@link Actor}.
     *               Primarily used for logging.
     *
     * @see E_STATE
     */
    public Actor(int id, int roomId, String name, boolean boss)
    {
        this.id = id;
        this.markerId = -1;
        this.name = name;
        this.roomId = roomId;
        proposedRoomId = -1;
        isPlayer = false;
        state = E_STATE.NEUTRAL;

        this.boss = boss;

        specials = new ConcurrentHashMap<>();
        items = new ConcurrentHashMap<>();
        statuses = new ConcurrentHashMap<>();
    }

    /**
     * Get the logical reference ID of this {@link Actor}.
     * <p>
     * To be used for referencing this {@link Actor} in a game state context
     * (associating with {@link Room Rooms}, checking stats, etc.) ONLY.
     *
     * @return int: The logical reference ID of this {@link Actor}.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Get the reference name of this {@link Actor}.
     * <p>
     * To be used for internal purposes ONLY. Should not be exposed to the
     * user.
     *
     * @return String: The reference name of this {@link Actor}.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the reference name of this {@link Actor}.
     * <p>
     * To be used for internal purposes ONLY. Should not be exposed to the
     * user.
     *
     * @param name String: The reference name to be used for this {@link
     *             Actor}.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Set the marker/graphical reference ID of this {@link Actor}.
     * <p>
     * To be used for checking the position of a player marker/waypoint
     * ({@link com.semaphore_soft.apps.cypher.PortalRenderer}) ONLY.
     *
     * @param tagID int: The marker/graphical reference ID to be used for this
     *              {@link Actor}.
     *
     * @see com.semaphore_soft.apps.cypher.PortalRenderer
     */
    public void setMarker(int tagID)
    {
        this.markerId = tagID;
    }

    /**
     * Get the marker/graphical reference ID of this {@link Actor}.
     * <p>
     * To be used for checking the position of a player marker/waypoint
     * ({@link com.semaphore_soft.apps.cypher.PortalRenderer}) ONLY.
     *
     * @return int: The marker/graphical reference ID of this {@link Actor}.
     *
     * @see com.semaphore_soft.apps.cypher.PortalRenderer
     */
    public int getMarker()
    {
        return markerId;
    }

    /**
     * Set the logical reference ID of the {@link Room} this {@link Actor}
     * associates with, or considers itself to be a resident of. This {@link
     * Actor} will consider itself to be a resident of, or located within,
     * that {@link Room}, until its {@link Room} ID is updated.
     *
     * @param roomID int: The logical reference ID of the {@link Room} this
     *               {@link Actor} is to associate with, or consider itself a
     *               resident of.
     *
     * @see Room
     */
    public void setRoom(int roomID)
    {
        this.roomId = roomID;
    }

    /**
     * Get the logical reference ID of the {@link Room} this {@link Actor}
     * associates with, or considers itself to be a resident of.
     *
     * @return int: The logical reference ID of the {@link Room} this {@link
     * Actor} associates with, or considers itself to be a member of.
     *
     * @see Room
     */
    public int getRoom()
    {
        return roomId;
    }

    public void setProposedRoomId(int proposedRoomId)
    {
        this.proposedRoomId = proposedRoomId;
    }

    public int getProposedRoomId()
    {
        return proposedRoomId;
    }

    /**
     * Check whether or not this {@link Actor} is a player {@link Actor}.
     *
     * @return boolean:
     * <ul>
     * <li>True if this {@link Actor} is considered to be controlled by a
     * player.</li>
     * <li>False otherwise.</li>
     * </ul>
     */
    public boolean isPlayer()
    {
        return isPlayer;
    }

    /**
     * Set the display name of this {@link Actor}.
     * <p>
     * To be used for any and all user feedback including the name of this
     * {@link Actor} in plain text.
     *
     * @param displayName String: The display name to be used for this {@link
     *                    Actor}.
     */
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    /**
     * Get the display name for this {@link Actor}.
     * <p>
     * To be used for any and all user feedback including the name of this
     * {@link Actor} in plain text.
     *
     * @return String: The display name to be used for this {@link Actor}.
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Set the maximum amount of health this {@link Actor} can have.
     * <p>
     * The {@link Actor} will start with this amount of health and not be able
     * to be healed to have more than this amount of health.
     *
     * @param healthMaximum int: The maximum amount of health this {@link Actor}
     *                      will be able to have.
     *
     * @see #receiveAttack(int)
     * @see Special#applySpecial(int, Actor)
     * @see GameMaster#attack(Model, int, int)
     * @see GameMaster#special(Model, int, int)
     * @see GameMaster#special(Model, int, int, int)
     */
    public void setHealthMaximum(int healthMaximum)
    {
        this.healthMaximum = healthMaximum;
    }

    /**
     * Get the maximum amount of health this {@link Actor} can have.
     * <p>
     * The {@link Actor} will start with this amount of health and not be able
     * to be healed to have more than this amount of health.
     *
     * @return int: The maximum amount of health this {@link Actor} can have.
     *
     * @see #receiveAttack(int)
     * @see Special#applySpecial(int, Actor)
     * @see GameMaster#attack(Model, int, int)
     * @see GameMaster#special(Model, int, int)
     * @see GameMaster#special(Model, int, int, int)
     */
    public int getHealthMaximum()
    {
        return healthMaximum;
    }

    /**
     * Set the current amount of health this {@link Actor} has.
     *
     * @param healthCurrent int: The new current amount of health this {@link
     *                      Actor} is to have.
     *
     * @see #receiveAttack(int)
     * @see Special#applySpecial(int, Actor)
     * @see GameMaster#attack(Model, int, int)
     * @see GameMaster#special(Model, int, int)
     * @see GameMaster#special(Model, int, int, int)
     */
    public void setHealthCurrent(int healthCurrent)
    {
        this.healthCurrent = healthCurrent;
    }

    /**
     * Get the current amount of health this {@link Actor} has.
     *
     * @return int: The current amount of health this {@link Actor} has.
     *
     * @see #receiveAttack(int)
     * @see Special#applySpecial(int, Actor)
     * @see GameMaster#attack(Model, int, int)
     * @see GameMaster#special(Model, int, int)
     * @see GameMaster#special(Model, int, int, int)
     */
    public int getHealthCurrent()
    {
        return healthCurrent;
    }

    /**
     * Set this {@link Actor Actor's} base attack rating.
     * <p>
     * Attack rating is used in determining how much damage this {@link Actor}
     * does to another {@link Actor} when performing an attack.
     *
     * @param attackRating int: The base attack rating this {@link Actor} will
     *                     have.
     *
     * @see #attack(Actor)
     * @see #getRealAttackRating()
     */
    public void setAttackRating(int attackRating)
    {
        this.attackRating = attackRating;
    }

    /**
     * Get this {@link Actor Actor's} base attack rating.
     * <p>
     * Attack rating is used in determining how much damage this {@link Actor}
     * does to another {@link Actor} when performing an attack.
     *
     * @return int: The base attack rating of this {@link Actor}.
     *
     * @see #attack(Actor)
     * @see #getRealAttackRating()
     */
    public int getAttackRating()
    {
        return attackRating;
    }

    /**
     * Get the HashMap of {@link Special Specials} this {@link Actor}
     * associates with, or has the ability to use.
     *
     * @return ConcurrentHashMap: A HashMap associating integer IDs with
     * {@link Special} objects.
     *
     * @see Special
     * @see #performSpecial(Special, Actor)
     * @see #performSpecial(Special, ArrayList)
     */
    public ConcurrentHashMap<Integer, Special> getSpecials()
    {
        return specials;
    }

    /**
     * Add a single {@link Special} object that this {@link Actor} associates
     * with, or has the ability to use.
     *
     * @param special {@link Special}: The Special object being associated
     *                with.
     *
     * @see Special
     * @see #performSpecial(Special, Actor)
     * @see #performSpecial(Special, ArrayList)
     */
    public void addSpecial(Special special)
    {
        if (!specials.containsValue(special))
        {
            specials.put(special.getId(), special);
        }
    }

    /**
     * Set the maximum amount of {@link Special} energy this {@link Actor} can
     * have.
     * <p>
     * The {@link Actor} will start with this amount of {@link Special} energy
     * and not be able to recover more than this amount of {@link Special}
     * energy.
     *
     * @param specialMaximum int: The maximum amount of {@link Special} energy
     *                       this {@link Actor} will be able to have.
     *
     * @see Special
     * @see #performSpecial(Special, Actor)
     * @see #performSpecial(Special, ArrayList)
     */
    public void setSpecialMaximum(int specialMaximum)
    {
        this.specialMaximum = specialMaximum;
    }

    /**
     * Get the maximum amount of {@link Special} energy this {@link Actor} can
     * have.
     * <p>
     * The {@link Actor} will start with this amount of {@link Special} energy
     * and not be able to recover more than this amount of {@link Special}
     * energy.
     *
     * @return int: The maximum amount of {@link Special} energy this {@link
     * Actor} can have.
     *
     * @see Special
     * @see #performSpecial(Special, Actor)
     * @see #performSpecial(Special, ArrayList)
     */
    public int getSpecialMaximum()
    {
        return specialMaximum;
    }

    /**
     * Set the current amount of {@link Special} energy this {@link Actor} has.
     * <p>
     * Used to determine whether or not this {@link Actor} has enough energy to
     * perform a {@link Special} move.
     *
     * @param specialCurrent int: The new current amount of {@link Special}
     *                       energy this {@link Actor} has.
     *
     * @see Special
     * @see #performSpecial(Special, Actor)
     * @see #performSpecial(Special, ArrayList)
     */
    public void setSpecialCurrent(int specialCurrent)
    {
        this.specialCurrent = specialCurrent;
    }

    /**
     * Get the current amount of {@link Special} energy this {@link Actor} has.
     * <p>
     * Used to determine whether or not this {@link Actor} has enough energy to
     * perform a {@link Special} move.
     *
     * @return int: The current amount of {@link Special} energy this {@link
     * Actor} has.
     *
     * @see Special
     * @see #performSpecial(Special, Actor)
     * @see #performSpecial(Special, ArrayList)
     */
    public int getSpecialCurrent()
    {
        return specialCurrent;
    }

    /**
     * Set this {@link Actor Actor's} base {@link Special} rating.
     * <p>
     * Used in determining the strength of {@link Special} move {@link Effect
     * Effects}.
     *
     * @param specialRating int: The base {@link Special} rating this {@link
     *                      Actor} will have.
     *
     * @see Special
     * @see Effect
     * @see #getRealSpecialRating()
     * @see #performSpecial(Special, Actor)
     * @see #performSpecial(Special, ArrayList)
     * @see Special#applySpecial(int, Actor)
     */
    public void setSpecialRating(int specialRating)
    {
        this.specialRating = specialRating;
    }

    /**
     * Get this {@link Actor Actor's} base {@link Special} rating.
     * <p>
     * Used in determining the strength of {@link Special} move {@link Effect
     * Effects}.
     *
     * @return int: The base {@link Special} rating of this {@link Actor}.
     *
     * @see Special
     * @see Effect
     * @see #getRealSpecialRating()
     * @see #performSpecial(Special, Actor)
     * @see #performSpecial(Special, ArrayList)
     * @see Special#applySpecial(int, Actor)
     */
    public int getSpecialRating()
    {
        return specialRating;
    }

    /**
     * Set this {@link Actor Actor's} base defense rating.
     * <p>
     * Used in determining the amount of damage this {@link Actor} ignores when
     * attacked.
     *
     * @param defenceRating int: The base defense rating this {@link Actor}
     *                      will have.
     *
     * @see #receiveAttack(int)
     * @see #getRealDefenceRating()
     */
    public void setDefenceRating(int defenceRating)
    {
        this.defenceRating = defenceRating;
    }

    /**
     * Get this {@link Actor Actor's} base defense rating.
     * <p>
     * Used in determining the amount of damage this {@link Actor} ignores when
     * attacked.
     *
     * @return int: The base defense rating of this {@link Actor}.
     *
     * @see #receiveAttack(int)
     * @see #getRealDefenceRating()
     */
    public int getDefenceRating()
    {
        return defenceRating;
    }

    /**
     * Set the {@link E_STATE state} of this {@link Actor}.
     * <p>
     * Describes the most recently taken action of this {@link Actor}. Used in
     * determining the effectiveness of attacks and {@link Special Specials}
     * used against this {@link Actor}.
     *
     * @param state {@link E_STATE}: The new {@link E_STATE state} of this
     *              {@link Actor}.
     *
     * @see E_STATE
     * @see Special
     * @see GameMaster#attack(Model, int, int)
     * @see GameMaster#special(Model, int, int)
     * @see GameMaster#special(Model, int, int, int)
     * @see GameMaster#setActorState(Model, int, E_STATE)
     * @see #receiveAttack(int)
     * @see Special#applySpecial(int, Actor)
     */
    void setState(E_STATE state)
    {
        this.state = state;
    }

    /**
     * Get the {@link E_STATE state} of this {@link Actor}.
     * <p>
     * Describes the most recently taken action of this {@link Actor}. Used in
     * determining the effectiveness of attacks and {@link Special Specials}
     * used against this {@link Actor}.
     *
     * @return {@link E_STATE}: The {@link E_STATE state} of this {@link Actor}.
     *
     * @see E_STATE
     * @see #receiveAttack(int)
     * @see Special#applySpecial(int, Actor)
     */
    public E_STATE getState()
    {
        return state;
    }

    /**
     * Get the number of attack tickets given to this {@link Actor}.
     * <p>
     * Used in determining the behavior of non-player {@link Actor Actors}. If
     * it is possible for this {@link Actor} to attack on a turn taken by the
     * {@link ActorController computer}, it will enter this many attack tickets
     * in the 'action lottery'.
     *
     * @return int: The number of attack tickets given to this {@link Actor}.
     *
     * @see ActorController
     * @see ActorController#takeTurn(GameController, Model, int)
     */
    int getAttackTickets()
    {
        return attackTickets;
    }

    /**
     * Set the number of attack tickets given to this {@link Actor}.
     * <p>
     * Used in determining the behavior of non-player {@link Actor Actors}. If
     * it is possible for this {@link Actor} to attack on a turn taken by the
     * {@link ActorController computer}, it will enter this many attack tickets
     * in the 'action lottery'.
     *
     * @param attackTickets int: The number of attack tickets to be given to
     *                      this {@link Actor}.
     *
     * @see ActorController
     * @see ActorController#takeTurn(GameController, Model, int)
     */
    public void setAttackTickets(int attackTickets)
    {
        this.attackTickets = attackTickets;
    }

    /**
     * Get the number of defend tickets given to this {@link Actor}.
     * <p>
     * Used in determining the behavior of non-player {@link Actor Actors}. If
     * there is an {@link Actor} for this {@link Actor} to defend itself from
     * on a turn taken by the {@link ActorController computer}, it will enter
     * this many defend tickets in the 'action lottery'.
     *
     * @return int: The number of defend tickets given to this {@link Actor}.
     *
     * @see ActorController
     * @see ActorController#takeTurn(GameController, Model, int)
     */
    int getDefendTickets()
    {
        return defendTickets;
    }

    /**
     * Set the number of defend tickets given to this {@link Actor}.
     * <p>
     * Used in determining the behavior of non-player {@link Actor Actors}. If
     * there is an {@link Actor} for this {@link Actor} to defend itself from
     * on a turn taken by the {@link ActorController computer}, it will enter
     * this many defend tickets in the 'action lottery'.
     *
     * @param defendTickets int: The number of defend tickets to be given to
     *                      this {@link Actor}.
     *
     * @see ActorController
     * @see ActorController#takeTurn(GameController, Model, int)
     */
    public void setDefendTickets(int defendTickets)
    {
        this.defendTickets = defendTickets;
    }

    /**
     * Get the number of {@link Special} tickets given to this {@link Actor}.
     * <p>
     * Used in determining the behavior of non-player {@link Actor Actors}. If
     * there is an {@link Actor} for this {@link Actor} may use a {@link
     * Special} move on, and it has enough {@link Special} energy to perform
     * that {@link Special} move, during a turn taken by the {@link
     * ActorController computer}, it will enter this many {@link Special}
     * tickets in the 'action lottery'.
     *
     * @return The number of {@link Special} tickets given to this {@link Actor}.
     *
     * @see ActorController
     * @see Special
     * @see ActorController#takeTurn(GameController, Model, int)
     */
    int getSpecialTickets()
    {
        return specialTickets;
    }

    /**
     * Set the number of {@link Special} tickets given to this {@link Actor}.
     * <p>
     * Used in determining the behavior of non-player {@link Actor Actors}. If
     * there is an {@link Actor} for this {@link Actor} may use a {@link
     * Special} move on, and it has enough {@link Special} energy to perform
     * that {@link Special} move, during a turn taken by the {@link
     * ActorController computer}, it will enter this many {@link Special}
     * tickets in the 'action lottery'.
     *
     * @param specialTickets int: The number of {@link Special} tickets to be given to
     *                       this {@link Actor}.
     *
     * @see ActorController
     * @see Special
     * @see ActorController#takeTurn(GameController, Model, int)
     */
    public void setSpecialTickets(int specialTickets)
    {
        this.specialTickets = specialTickets;
    }

    /**
     * Get the number of move tickets given to this {@link Actor}.
     * <p>
     * Used in determining the behavior of non-player {@link Actor Actors}. If
     * there is a valid {@link Room} this {@link Actor} may move to on a turn
     * taken by the {@link ActorController computer}, it will enter this many
     * move tickets in the 'action lottery'.
     *
     * @return int: The number of move tickets given to this {@link Actor}.
     *
     * @see ActorController
     * @see Room
     * @see Map
     * @see ActorController#takeTurn(GameController, Model, int)
     */
    int getMoveTickets()
    {
        return moveTickets;
    }

    /**
     * Set the number of move tickets given to this {@link Actor}.
     * <p>
     * Used in determining the behavior of non-player {@link Actor Actors}. If
     * there is a valid {@link Room} this {@link Actor} may move to on a turn
     * taken by the {@link ActorController computer}, it will enter this many
     * move tickets in the 'action lottery'.
     *
     * @param moveTickets int: The number of move tickets to be given to this
     *                    {@link Actor}.
     *
     * @see ActorController
     * @see Room
     * @see Map
     * @see ActorController#takeTurn(GameController, Model, int)
     */
    public void setMoveTickets(int moveTickets)
    {
        this.moveTickets = moveTickets;
    }

    public int getUseItemTickets()
    {
        return useItemTickets;
    }

    public void setUseItemTickets(int useItemTickets)
    {
        this.useItemTickets = useItemTickets;
    }

    /**
     * Check this {@link Actor Actor's} 'seeker' flag.
     * <p>
     * Used in determining the behavior of non-player {@link Actor Actors}. An
     * {@link Actor} which has the 'seeker' flag set will be given 1 move
     * ticket, such that {@link Actor Actors} which would not normally move may
     * move, if there is not an {@link Actor} present to fight on a turn taken
     * by the {@link ActorController computer}.
     *
     * @return boolean:
     * <ul>
     * <li>True of this {@link Actor} is considered a 'seeker'.</li>
     * <li>False otherwise.</li>
     * </ul>
     *
     * @see ActorController
     * @see ActorController#takeTurn(GameController, Model, int)
     */
    boolean isSeeker()
    {
        return seeker;
    }

    /**
     * Set this {@link Actor Actor's} 'seeker' flag.
     * <p>
     * Used in determining the behavior of non-player {@link Actor Actors}. An
     * {@link Actor} which has the 'seeker' flag set will be given 1 move
     * ticket, such that {@link Actor Actors} which would not normally move may
     * move, if there is not an {@link Actor} present to fight on a turn taken
     * by the {@link ActorController computer}.
     *
     * @param seeker boolean:
     *               <ul>
     *               <li>True of this {@link Actor} is to be considered a 'seeker'.</li>
     *               <li>False otherwise.</li>
     *               </ul>
     *
     * @see ActorController
     * @see ActorController#takeTurn(GameController, Model, int)
     */
    public void setSeeker(boolean seeker)
    {
        this.seeker = seeker;
    }

    public boolean isBoss()
    {
        return boss;
    }

    public void setBoss(boolean boss)
    {
        this.boss = boss;
    }

    /**
     * Perform an attack on a given {@link Actor}. Sets this {@link Actor
     * Actor's} {@link E_STATE state} to 'attack' and calls receiveAttack on
     * the target {@link Actor} with this {@link Actor Actor's} modified attack
     * rating.
     *
     * @param actor {@link Actor}: The target of this {@link Actor Actor's}
     *              attack.
     *
     * @see E_STATE
     * @see #receiveAttack(int)
     */
    void attack(Actor actor)
    {
        actor.receiveAttack(getRealAttackRating());

        state = E_STATE.ATTACK;
    }

    /**
     * Calculate the amount of damage received by this {@link Actor} based on
     * the attack rating, this {@link Actor Actor's} {@link E_STATE state}, and
     * defence rating. Update this {@link Actor Actor's} current health
     * accordingly.
     *
     * @param attackRating int: The attack rating of the incoming attack.
     *
     * @see E_STATE
     * @see #attack(Actor)
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
     * Attempt to perform a {@link Special} ability targeting one {@link
     * Actor}.
     * <p>
     * If this {@link Actor} has an amount of {@link Special} energy greater
     * than or equal to the {@link Special} energy cost of the given {@link
     * Special} ability, subtract the cost of the given {@link Special} ability
     * from this {@link Actor Actor's} current {@link Special} energy and have
     * the {@link Special} apply itself to the target {@link Actor}.
     *
     * @param special {@link Special}: The {@link Special} ability to attempt
     *                to perform.
     * @param actor   {@link Actor}: The target {@link Actor} of this {@link
     *                Actor Actor's} {@link Special} ability.
     *
     * @return boolean:
     * <ul>
     * <li>True if this {@link Actor} had enough {@link Special} energy to
     * perform the given {@link Special} ability.</li>
     * <li>False otherwise.</li>
     * </ul>
     *
     * @see Special
     * @see Special#applySpecial(int, Actor)
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

        state = E_STATE.SPECIAL;

        // ensure a self-targeted special's effects will be present for their
        // entire duration after the turn the special was used
        if (id == actor.getId())
        {
            special.incrementDuration();
        }

        special.applySpecial(getRealSpecialRating(), actor);

        return true;
    }

    /**
     * Attempt to perform a {@link Special} ability targeting one or more
     * {@link Actor Actors}.
     * <p>
     * If this {@link Actor} has an amount of {@link Special} energy greater
     * than or equal to the {@link Special} energy cost of the given {@link
     * Special} ability, subtract the cost of the given {@link Special} ability
     * from this {@link Actor Actor's} current {@link Special} energy and have
     * the {@link Special} apply itself to each of the target {@link Actor
     * Actors}.
     *
     * @param special {@link Special}: The {@link Special} ability to attempt
     *                to perform.
     * @param actors  ArrayList: The list of target {@link Actor Actors} of
     *                this {@link Actor Actor's} {@link Special} ability.
     *
     * @return boolean:
     * <ul>
     * <li>True if this {@link Actor} had enough {@link Special} energy to
     * perform the given {@link Special} ability.</li>
     * <li>False otherwise.</li>
     * </ul>
     *
     * @see Special
     * @see Special#applySpecial(int, Actor)
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

        state = E_STATE.SPECIAL;

        for (Actor actor : actors)
        {
            special.applySpecial(getRealSpecialRating(), actor);
        }

        return true;
    }

    /**
     * Add a single {@link Item} for this {@link Actor} to associate with, or
     * 'have' in its 'inventory'.
     * <p>
     * An {@link Actor} may associate with, or 'have', {@link Item Items} which are
     * {@link ItemConsumable consumable} or {@link ItemDurable durable}.
     * <p>
     * {@link ItemConsumable ItemConsumables} may be 'used' to create immediate
     * {@link Effect effects} or {@link StatusTemporary temporary Statuses}.
     * <p>
     * {@link ItemDurable ItemDurables} create a {@link StatusLinked linked
     * Status} on acquisition, which is removed when the {@link ItemDurable}
     * is lost.
     *
     * @param item {@link Item}: The new {@link Item} for this {@link Actor} to
     *             associate with, or 'have' in its 'inventory'.
     *
     * @see Item
     * @see ItemConsumable
     * @see ItemDurable
     * @see Effect
     * @see Effect#applyLinkedEffect(Effect.E_EFFECT, int, Actor, int)
     * @see Status
     * @see StatusLinked
     * @see StatusTemporary
     */
    public void addItem(Item item)
    {
        if (!items.containsKey(item.getId()))
        {
            items.put(item.getId(), item);
            if (item instanceof ItemDurable)
            {
                for (Effect.E_EFFECT effect : item.getEffects())
                {
                    Effect.applyLinkedEffect(effect, item.getEffectRating(), this, item.getId());
                }
            }
        }
    }

    /**
     * Dissociate an {@link Item} from this {@link Actor}, or remove it from
     * this {@link Actor Actor's} 'inventory'.
     * <p>
     * {@link ItemConsumable ItemConsumables} will no longer be able to be used
     * by this {@link Actor} once removed from this {@link Actor}, unless
     * re-added.
     * <p>
     * Any {@link StatusLinked linked Statuses} added by an {@link ItemDurable}
     * will be removed from this {@link Actor} along with the {@link
     * ItemDurable}, unless re-added.
     *
     * @param itemID int: The logical reference ID of the {@link Item} to
     *               dissociate, or remove from, this {@link Actor}.
     *
     * @see Item
     * @see ItemConsumable
     * @see ItemDurable
     * @see StatusLinked
     * @see Status
     * @see #removeStatus(Status)
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
     * Dissociate an {@link Item} from this {@link Actor}, or remove it from
     * this {@link Actor Actor's} 'inventory'.
     * <p>
     * {@link ItemConsumable ItemConsumables} will no longer be able to be used
     * by this {@link Actor} once removed from this {@link Actor}, unless
     * re-added.
     * <p>
     * Any {@link StatusLinked linked Statuses} added by an {@link ItemDurable}
     * will be removed from this {@link Actor} along with the {@link
     * ItemDurable}, unless re-added.
     *
     * @param item Item: The {@link Item} object to dissociate, or remove from,
     *             this {@link Actor}.
     *
     * @see Item
     * @see ItemConsumable
     * @see ItemDurable
     * @see StatusLinked
     * @see Status
     * @see #removeStatus(Status)
     */
    public void removeItem(Item item)
    {
        if (items.containsKey(item.getId()))
        {
            items.remove(item.getId());
            for (int statusID : statuses.keySet())
            {
                Status status = statuses.get(statusID);
                if (status instanceof StatusLinked &&
                    ((StatusLinked) status).getLinkID() == item.getId())
                {
                    removeStatus(status);
                }
            }
        }
    }

    /**
     * Add a single {@link Status} for this {@link Actor} to associate with, or
     * 'have'.
     *
     * @param status {@link Status}: The new {@link Status} for this {@link
     *               Actor} to associate with.
     *
     * @see Status
     */
    public void addStatus(Status status)
    {
        if (!statuses.containsKey(status.getId()))
        {
            statuses.put(status.getId(), status);
        }
    }

    /**
     * Create a new {@link StatusTemporary temporary Status} for this {@link
     * Actor} to associate with, or 'have', from a given {@link Status} {@link
     * Status.E_STATUS_TYPE type}, {@link Effect} rating, and duration.
     * <p>
     * A {@link StatusTemporary} may be added as the result of this {@link
     * Actor} using an {@link ItemConsumable} or being the target of a {@link
     * Special} ability. It will be removed after this {@link Actor} has taken
     * [duration] turns.
     *
     * @param type         {@link Status.E_STATUS_TYPE}: The {@link
     *                     Status.E_STATUS_TYPE type} of the {@link
     *                     StatusTemporary} to be created.
     * @param effectRating int: The {@link Effect} rating of the {@link
     *                     StatusTemporary} to be created.
     * @param duration     int: The duration of the {@link StatusTemporary} to
     *                     be created.
     *
     * @see Status.E_STATUS_TYPE
     * @see StatusTemporary
     * @see Status
     * @see ItemConsumable
     * @see Item
     * @see Special
     * @see Effect
     */
    void addNewStatusTemporary(Status.E_STATUS_TYPE type, int effectRating, int duration)
    {
        StatusTemporary status =
            new StatusTemporary(getNextID(statuses), type, effectRating, duration);

        statuses.put(status.getId(), status);
    }

    /**
     * Create a new {@link StatusLinked linked Status} for this {@link Actor}
     * to associate with, or 'have', from a given {@link Status} {@link
     * Status.E_STATUS_TYPE type}, {@link Effect} rating, and link ID.
     * <p>
     * A {@link StatusLinked} may be added as the result of this {@link Actor}
     * acquiring an {@link ItemDurable}. It will be removed if and when the
     * {@link ItemDurable} is removed from the {@link Actor}.
     *
     * @param type         {@link Status.E_STATUS_TYPE}: The {@link
     *                     Status.E_STATUS_TYPE type} of the {@link
     *                     StatusLinked} to be created.
     * @param effectRating int: The {@link Effect} rating of the {@link
     *                     StatusLinked} to be created.
     * @param linkId       int: The logical reference ID of the {@link
     *                     ItemDurable} to which the {@link StatusLinked} being
     *                     created is linked.
     *
     * @see Status.E_STATUS_TYPE
     * @see StatusLinked
     * @see Status
     * @see ItemDurable
     * @see Item
     * @see Effect
     */
    void addNewStatusLinked(Status.E_STATUS_TYPE type, int effectRating, int linkId)
    {
        StatusLinked status = new StatusLinked(getNextID(statuses), type, effectRating, linkId);

        statuses.put(status.getId(), status);
    }

    /**
     * Dissociate, or remove, a {@link Status} from this {@link Actor}.
     *
     * @param statusID int: The logical reference ID of the {@link Status} to
     *                 remove from this {@link Actor}.
     *
     * @see Status
     */
    public void removeStatus(int statusID)
    {
        if (statuses.containsKey(statusID))
        {
            statuses.remove(statusID);
        }
    }

    /**
     * Dissociate, or remove, a {@link Status} from this {@link Actor}.
     *
     * @param status Status: The {@link Status} object to remove from this
     *               {@link Actor}.
     *
     * @see Status
     */
    private void removeStatus(Status status)
    {
        if (statuses.containsKey(status.getId()))
        {
            statuses.remove(status.getId());
        }
    }

    //TODO the following three methods are almost identical, merge them somehow?

    /**
     * Get this {@link Actor Actor's} attack rating modified by any {@link
     * Status Statuses} this {@link Actor} has which impact attack rating.
     *
     * @return int: This {@link Actor Actor's} attack rating modified by {@link
     * Status Statuses}.
     *
     * @see Status
     * @see Status.E_STATUS_TYPE
     * @see Status#getType()
     * @see Status#getEffectRating()
     */
    private int getRealAttackRating()
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
     * Get this {@link Actor Actor's} {@link Special} rating modified by any
     * {@link Status Statuses} this {@link Actor} has which impact {@link
     * Special} rating.
     *
     * @return int: This {@link Actor Actor's} {@link Special} rating modified
     * by {@link Status Statuses}.
     *
     * @see Special
     * @see Status
     * @see Status.E_STATUS_TYPE
     * @see Status#getType()
     * @see Status#getEffectRating()
     */
    private int getRealSpecialRating()
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
     * Get this {@link Actor Actor's} defense rating modified by any {@link
     * Status Statuses} this {@link Actor} has which impact defense rating.
     *
     * @return int: This {@link Actor Actor's} defense rating modified by
     * {@link Status Statuses}.
     *
     * @see Status
     * @see Status.E_STATUS_TYPE
     * @see Status#getType()
     * @see Status#getEffectRating()
     */
    int getRealDefenceRating()
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
     * Apply the {@link Effect Effects} of an {@link ItemConsumable} to this
     * {@link Actor}.
     * <p>
     * To be used when an {@link Actor} uses a {@link ItemConsumable}.
     *
     * @param itemID int: The logical reference ID of the {@link
     *               ItemConsumable} to apply the {@link Effect Effects} of to
     *               this {@link Actor}.
     *
     * @see ItemConsumable
     * @see Item
     * @see Effect
     * @see Effect.E_EFFECT
     * @see Effect#applyTemporaryEffect(Effect.E_EFFECT, int, int, Actor)
     */
    public boolean useItem(int itemID)
    {
        if (items.containsKey(itemID))
        {
            return useItem(items.get(itemID));
        }

        return false;
    }

    /**
     * Apply the {@link Effect Effects} of an {@link ItemConsumable} to this
     * {@link Actor}.
     * <p>
     * To be used when an {@link Actor} uses an {@link ItemConsumable}.
     *
     * @param item Item: The {@link ItemConsumable} object to apply the {@link
     *             Effect Effects} of to this {@link Actor}.
     *
     * @see ItemConsumable
     * @see Item
     * @see Effect
     * @see Effect.E_EFFECT
     * @see Effect#applyTemporaryEffect(Effect.E_EFFECT, int, int, Actor)
     */
    public boolean useItem(Item item)
    {
        if (items.containsKey(item.getId()))
        {
            if (item instanceof ItemConsumable)
            {
                for (Effect.E_EFFECT effect : item.getEffects())
                {
                    Logger.logI("applying effect:<" + effect.toString() + "> with rating:<" +
                                item.getEffectRating() + ">");

                    Effect.applyTemporaryEffect(effect,
                                                item.getEffectRating(),
                                                ((ItemConsumable) item).getDuration(),
                                                this);
                }

                removeItem(item);

                return true;
            }
        }

        return false;
    }

    /**
     * Decrement the duration of any {@link StatusTemporary temporary Statuses}
     * this {@link Actor} associates with and remove any expired {@link
     * StatusTemporary Statuses}.
     * <p>
     * To be called at the end of an {@link Actor Actor's} turn.
     *
     * @see StatusTemporary
     * @see Status
     * @see StatusTemporary#tick()
     * @see #removeStatus(Status)
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

    public ConcurrentHashMap<Integer, Item> getItems()
    {
        return items;
    }
}
