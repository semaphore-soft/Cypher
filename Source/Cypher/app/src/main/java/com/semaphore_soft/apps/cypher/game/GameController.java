package com.semaphore_soft.apps.cypher.game;

/**
 * Created by Scorple on 2/18/2017.
 */

public interface GameController
{
    void onFinishedLoading();

    void onFinishedAction(int actorId);

    void feedback(String message);

    void onActorAction(int sourceId, int targetId, String action);

    void onActorMove(int actorId, int roomId);

    void turnPassed(int turnId);
}
