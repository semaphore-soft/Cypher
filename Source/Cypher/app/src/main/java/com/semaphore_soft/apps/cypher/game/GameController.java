package com.semaphore_soft.apps.cypher.game;

/**
 * Created by Scorple on 2/18/2017.
 */

public interface GameController
{
    void feedback(String message);

    void updateRoom(int roomId);

    void turnPassed(int turnId);
}
