package com.semaphore_soft.apps.cypher.game;

import android.util.Log;

import com.semaphore_soft.apps.cypher.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Scorple on 2/18/2017.
 */

public class ActorController
{
    private static Model          model;
    private static GameController gameController;

    public static void setModel(Model model)
    {
        ActorController.model = model;
    }

    public static void setGameController(GameController gameController)
    {
        ActorController.gameController = gameController;
    }

    public static void takeTurn(int actorId)
    {
        Actor actor = GameMaster.getActor(actorId);

        Log.d("takeTurn", "taking turn for actor " + actor.getName());

        int attackTickets  = 0;
        int defendTickets  = 0;
        int specialTickets = 0;
        ArrayList<Integer> playerTargets =
            GameMaster.getPlayerTargetIds(actorId);
        ArrayList<Integer> nonPlayerTargets =
            GameMaster.getNonPlayerTargetIds(actorId);
        ConcurrentHashMap<Integer, Special> actorSpecials = actor.getSpecials();
        if (playerTargets != null && playerTargets.size() > 0)
        {
            attackTickets = 10;
            defendTickets = 5;

            if (actorSpecials != null && actorSpecials.size() > 0)
            {
                for (Special special : actorSpecials.values())
                {
                    if (actor.getSpecialCurrent() >= special.getCost())
                    {
                        specialTickets = 5;
                        break;
                    }
                }
            }
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
                moveTickets = 5;
            }
        }

        int totalTickets = attackTickets + defendTickets + specialTickets + moveTickets;

        int ticket = (int) (Math.random() * totalTickets);

        if (attackTickets != 0 && ticket <= attackTickets)
        {
            Collections.shuffle(playerTargets);

            GameMaster.attack(actorId, playerTargets.get(0));

            Logger.logI("actor " + actor.getName() + " attacked " +
                        GameMaster.getActor(playerTargets.get(0)).getName());

            gameController.feedback(GameMaster.getActor(actorId).getName() + " attacked " +
                                    GameMaster.getActor(playerTargets.get(0)).getName());

            gameController.onActorAction(actorId, playerTargets.get(0), "attack");
        }
        else if (defendTickets != 0 && ticket <= attackTickets + defendTickets)
        {
            gameController.feedback(GameMaster.getActor(actorId).getName() + " defended ");

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

            for (Integer specialId : specialIds)
            {
                boolean usedSpecial = false;

                Special special = actorSpecials.get(specialId);

                if (special.getCost() < actor.getSpecialCurrent())
                {
                    switch (special.getTargetingType())
                    {
                        case SINGLE_PLAYER:
                            if (nonPlayerTargets != null && nonPlayerTargets.size() > 0)
                            {
                                Collections.shuffle(nonPlayerTargets);

                                Logger.logI(
                                    "actor " + actor.getName() + " used " + special.getName() +
                                    " on " +
                                    GameMaster.getActor(nonPlayerTargets.get(0)).getName());

                                gameController.feedback(
                                    "actor " + actor.getName() + " used special " +
                                    special.getName() +
                                    " on " +
                                    GameMaster.getActor(nonPlayerTargets.get(0)).getName());

                                gameController.onActorAction(actorId,
                                                             nonPlayerTargets.get(0),
                                                             "special:help");

                                usedSpecial = true;
                            }
                            break;
                        case SINGLE_NON_PLAYER:
                            Collections.shuffle(playerTargets);

                            Logger.logI(
                                "actor " + actor.getName() + " used " + special.getName() +
                                " on " + GameMaster.getActor(playerTargets.get(0)).getName());

                            gameController.feedback(
                                "actor " + actor.getName() + " used special " + special.getName() +
                                " on " + GameMaster.getActor(playerTargets.get(0)).getName());

                            gameController.onActorAction(actorId,
                                                         playerTargets.get(0),
                                                         "special:harm");

                            usedSpecial = true;
                            break;
                        case AOE_PLAYER:
                            if (nonPlayerTargets != null && nonPlayerTargets.size() > 0)
                            {
                                Logger.logI(
                                    "actor " + actor.getName() + " used " + special.getName());

                                gameController.feedback(
                                    "actor " + actor.getName() + " used special " +
                                    special.getName());

                                gameController.onActorAction(actorId,
                                                             -1,
                                                             "special:help");

                                usedSpecial = true;
                            }
                            break;
                        case AOE_NON_PLAYER:
                            Logger.logI(
                                "actor " + actor.getName() + " used " + special.getName());

                            gameController.feedback(
                                "actor " + actor.getName() + " used special " + special.getName());

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