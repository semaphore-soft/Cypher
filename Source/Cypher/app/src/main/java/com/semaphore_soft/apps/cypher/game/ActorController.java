package com.semaphore_soft.apps.cypher.game;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Scorple on 2/18/2017.
 */

public class ActorController
{
    private static GameController gameController;

    public static void setGameController(GameController gameController)
    {
        ActorController.gameController = gameController;
    }

    public static void takeTurn(int actorId)
    {
        Actor actor = GameMaster.getActor(actorId);

        Log.d("ActorController", "taking turn for actor " + actor.getName());

        ArrayList<Integer> targets = GameMaster.getPlayerTargetIds(actorId);

        if (targets != null && targets.size() > 0)
        {
            Collections.shuffle(targets);

            GameMaster.attack(actorId, targets.get(0));

            Log.d("ActorController",
                  "actor " + actor.getName() + " attacked " +
                  GameMaster.getActor(targets.get(0)).getName());

            gameController.feedback(GameMaster.getActor(actorId).getName() + " attacked " +
                                    GameMaster.getActor(targets.get(0)).getName());
            gameController.updateRoom(GameMaster.getActorRoomId(actorId));
        }

        gameController.turnPassed(actorId);
    }
}