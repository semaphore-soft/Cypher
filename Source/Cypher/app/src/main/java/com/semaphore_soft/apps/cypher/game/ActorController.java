package com.semaphore_soft.apps.cypher.game;

import com.semaphore_soft.apps.cypher.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ActorController game.ActorController} provides the logic necessary to
 * decide what action a given non-player {@link Actor} should take, and then
 * notify the attached {@link GameController} of that action. Requires a {@link
 * Model} for decision making purposes.
 *
 * @author scorple
 * @see Actor
 * @see GameController
 * @see Model
 */
public class ActorController
{
    private static Model          model;
    private static GameController gameController;

    /**
     * Set the {@link Model} to consult for information about game state to
     * make an informed decision about what action an {@link Actor} will take.
     *
     * @param model {@link Model}: The game state model to consult for
     *              information used in deciding what action an {@link Actor}
     *              will take.
     *
     * @see Model
     * @see Actor
     */
    public static void setModel(Model model)
    {
        ActorController.model = model;
    }

    /**
     * Set the {@link GameController} to report the action taken by a
     * non-player {@link Actor} to.
     *
     * @param gameController {@link GameController}: The {@link GameController}
     *                       to report the action taken by a non-player {@link
     *                       Actor} to.
     *
     * @see GameController
     * @see Actor
     */
    public static void setGameController(GameController gameController)
    {
        ActorController.gameController = gameController;
    }

    /**
     * Given a non-player {@link Actor} ID, decide what action that {@link
     * Actor} will take and take that action via necessary calls to {@link
     * GameMaster}. The {@link GameController} will then be notified of this
     * action for presentation to the user.
     * <p>
     * Action decision will be made based on what actions are available to the
     * {@link Actor}, checked against the state of the {@link Model}, and the
     * {@link Actor Actor's} ticket counts for each available action. The total
     * of all ticket counts will be summed and a random ticket selected in that
     * range. The action selected will depend on which action's ticket range
     * the randomly selected ticket number falls within, working as a
     * 'lottery'.
     *
     * @param actorId int: The logical reference ID of the {@link Actor} for
     *                which an action is being selected an taken.
     *
     * @see Actor
     * @see Model
     * @see GameMaster
     * @see GameController
     */
    public static void takeTurn(int actorId)
    {
        Actor actor = GameMaster.getActor(actorId);

        Logger.logI("taking turn for actor " + actor.getName());

        int attackTickets  = 0;
        int defendTickets  = 0;
        int specialTickets = 0;
        ArrayList<Integer> playerTargets =
            GameMaster.getPlayerTargetIds(actorId);
        ArrayList<Integer> nonPlayerTargets =
            GameMaster.getNonPlayerTargetIds(actorId);
        ConcurrentHashMap<Integer, Special> actorSpecials = actor.getSpecials();
        if (playerTargets.size() > 0)
        {
            attackTickets = actor.getAttackTickets();
            defendTickets = actor.getDefendTickets();

            if (actorSpecials != null && actorSpecials.size() > 0)
            {
                for (Special special : actorSpecials.values())
                {
                    if (actor.getSpecialCurrent() >= special.getCost())
                    {
                        specialTickets = actor.getSpecialTickets();
                        break;
                    }
                }
            }
        }
        else
        {
            Logger.logI("no player targets found");
        }

        int                moveTickets    = 0;
        ArrayList<Integer> adjacentRooms  = model.getMap().getAdjacentRooms(actor.getRoom());
        ArrayList<Integer> validMoveRooms = new ArrayList<>();
        if (adjacentRooms != null && adjacentRooms.size() > 0)
        {
            boolean foundValidMove = false;
            for (int roomId : adjacentRooms)
            {
                if (GameMaster.getValidPath(actor.getRoom(), roomId) == 0)
                {
                    validMoveRooms.add(roomId);
                    foundValidMove = true;
                }
            }
            if (foundValidMove)
            {
                moveTickets = actor.getMoveTickets();
            }
        }
        if (playerTargets.size() > 0 && actor.isSeeker())
        {
            moveTickets = 1;
        }

        Logger.logI("attack tickets: " + attackTickets, 1);
        Logger.logI("defend tickets: " + defendTickets, 1);
        Logger.logI("special tickets: " + specialTickets, 1);
        Logger.logI("move tickets: " + moveTickets, 1);

        int totalTickets = attackTickets + defendTickets + specialTickets + moveTickets;

        int ticket = (int) (Math.random() * totalTickets);

        Logger.logI("ticket selected: " + ticket, 1);

        if (attackTickets != 0 && ticket <= attackTickets)
        {
            Collections.shuffle(playerTargets);

            GameMaster.attack(actorId, playerTargets.get(0));

            Logger.logI("actor " + actor.getName() + " attacked " +
                        GameMaster.getActor(playerTargets.get(0)).getName());

            gameController.feedback(GameMaster.getActor(actorId).getDisplayName() + " attacked " +
                                    GameMaster.getActor(playerTargets.get(0)).getDisplayName());

            gameController.onActorAction(actorId, playerTargets.get(0), "attack");
        }
        else if (defendTickets != 0 && ticket <= attackTickets + defendTickets)
        {
            gameController.feedback(GameMaster.getActor(actorId).getDisplayName() + " defended ");

            gameController.onActorAction(actorId, -1, "defend");
        }
        else if (specialTickets != 0 && ticket < attackTickets + defendTickets + specialTickets)
        {
            ArrayList<Integer> specialIds = new ArrayList<>();
            for (Integer specialId : actorSpecials.keySet())
            {
                specialIds.add(specialId);
            }
            Collections.shuffle(specialIds);

            boolean usedSpecial = false;

            for (Integer specialId : specialIds)
            {
                Special special = actorSpecials.get(specialId);

                if (special.getCost() <= actor.getSpecialCurrent())
                {
                    switch (special.getTargetingType())
                    {
                        case SINGLE_PLAYER:
                            if (nonPlayerTargets.size() > 0)
                            {
                                Collections.shuffle(nonPlayerTargets);

                                actor.performSpecial(special,
                                                     GameMaster.getActor(playerTargets.get(0)));

                                Logger.logI(
                                    "actor " + actor.getName() + " used " + special.getName() +
                                    " on " +
                                    GameMaster.getActor(nonPlayerTargets.get(0)).getName());

                                gameController.feedback(actor.getDisplayName() + " used special " +
                                                        special.getDisplayName() +
                                                        " on " +
                                                        GameMaster.getActor(nonPlayerTargets.get(0))
                                                                  .getDisplayName());

                                gameController.onActorAction(actorId,
                                                             nonPlayerTargets.get(0),
                                                             "special:help");

                                usedSpecial = true;
                            }
                            break;
                        case SINGLE_NON_PLAYER:
                            Collections.shuffle(playerTargets);

                            actor.performSpecial(special,
                                                 GameMaster.getActor(playerTargets.get(0)));

                            Logger.logI(
                                "actor " + actor.getName() + " used " + special.getName() +
                                " on " + GameMaster.getActor(playerTargets.get(0)).getName());

                            gameController.feedback(actor.getDisplayName() + " used special " +
                                                    special.getDisplayName() +
                                                    " on " +
                                                    GameMaster.getActor(playerTargets.get(0))
                                                              .getDisplayName());

                            gameController.onActorAction(actorId,
                                                         playerTargets.get(0),
                                                         "special:harm");

                            usedSpecial = true;
                            break;
                        case AOE_PLAYER:
                            if (nonPlayerTargets.size() > 0)
                            {
                                ArrayList<Actor> nonPlayerTargetActors = new ArrayList<>();
                                for (int i : nonPlayerTargets)
                                {
                                    nonPlayerTargetActors.add(GameMaster.getActor(i));
                                }

                                actor.performSpecial(special, nonPlayerTargetActors);

                                Logger.logI(
                                    "actor " + actor.getName() + " used " + special.getName());

                                gameController.feedback(actor.getDisplayName() + " used special " +
                                                        special.getDisplayName());

                                gameController.onActorAction(actorId,
                                                             -1,
                                                             "special:help");

                                usedSpecial = true;
                            }
                            break;
                        case AOE_NON_PLAYER:
                            ArrayList<Actor> playerTargetActors = new ArrayList<>();
                            for (int i : playerTargets)
                            {
                                playerTargetActors.add(GameMaster.getActor(i));
                            }

                            actor.performSpecial(special, playerTargetActors);

                            Logger.logI(
                                "actor " + actor.getName() + " used " + special.getName());

                            gameController.feedback(actor.getDisplayName() + " used special " +
                                                    special.getDisplayName());

                            gameController.onActorAction(actorId,
                                                         -1,
                                                         "special:harm");

                            usedSpecial = true;

                            break;
                    }
                }

                if (usedSpecial)
                {
                    break;
                }
            }

            if (!usedSpecial)
            {
                Logger.logD("actor <" + actorId + "> failed to use special");
            }
        }
        else if (moveTickets != 0 &&
                 ticket < attackTickets + defendTickets + specialTickets + moveTickets)
        {
            if (validMoveRooms.size() > 0)
            {
                Collections.shuffle(validMoveRooms);

                Logger.logI(
                    "actor " + actor.getName() + " moved to " + validMoveRooms.get(0));

                gameController.onActorMove(actorId, validMoveRooms.get(0));
            }
        }
        else
        {
            //actor can't do anything, shouldn't ever be the case

            Logger.logI(
                "actor " + actorId + ":" + actor.getName() + " was unable to act");
        }

        gameController.turnPassed(actorId);
    }
}