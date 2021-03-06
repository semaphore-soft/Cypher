package com.semaphore_soft.apps.cypher.game;

import android.support.annotation.Nullable;

/**
 * The {@link GameController game.GameController} interface defines the methods
 * which must be defined on a class which coordinates the game state {@link
 * Model} and interaction with it with the {@link
 * com.semaphore_soft.apps.cypher.PortalRenderer graphical presentation} of the
 * game.
 * <p>
 * Used to provide a listener to callback to for the {@link
 * com.semaphore_soft.apps.cypher.PortalRenderer renderer} and the non-player
 * {@link Actor} control class {@link ActorController}.
 *
 * @author scorple
 * @see com.semaphore_soft.apps.cypher.PortalActivity
 * @see com.semaphore_soft.apps.cypher.PortalRenderer
 * @see Model
 * @see Actor
 * @see ActorController
 */
public interface GameController
{
    /**
     * Callback method, informs the {@link GameController} that an asset
     * finished loading resources.
     * <p>
     * i.e. Called when {@link com.semaphore_soft.apps.cypher.PortalRenderer}
     * finished loading OpenGL assets.
     *
     * @see com.semaphore_soft.apps.cypher.PortalRenderer
     */
    void onFinishedLoading();

    /**
     * Callback method, informs {@link GameController} that the presentation
     * of a game action has concluded.
     * <p>
     * i.e. Called when {@link com.semaphore_soft.apps.cypher.PortalRenderer}
     * finishes displaying an action performed by an {@link Actor} in the game.
     *
     * @param actorId int: The logical reference ID of the {@link Actor}
     *                performing the action which has finished.
     *
     * @see Actor
     * @see com.semaphore_soft.apps.cypher.PortalRenderer
     */
    void onFinishedAction(int actorId);

    /**
     * Callback method, provides the {@link GameController} with text feedback
     * which may be processed and/or given directly to the user.
     *
     * @param message String: The feedback message to be processed or given to
     *                the user.
     */
    void feedback(String message);

    /**
     * Callback method, informs the {@link GameController} that an action has
     * been taken by a non-player {@link Actor} with a String reference
     * description of that action and a logical reference ID of the target
     * {@link Actor} of that action if applicable, flag otherwise.
     * <p>
     * Actions reference descriptions include "attack", "defend",
     * "special:help", and "special:harm".
     * <p>
     * Used by {@link ActorController}.
     *
     * @param sourceId int: The logical reference ID of the {@link Actor}
     *                 performing the action
     * @param targetId int: The logical reference ID of the {@link Actor}
     *                 target of the action if applicable, -1 otherwise.
     * @param action   String: Reference description of the action being
     *                 performed.
     *
     * @see Actor
     * @see ActorController
     */
    void onActorAction(int sourceId, int targetId, String action, @Nullable String desc);

    /**
     * Callback method, informed the {@link GameController} that a non-player
     * {@link Actor} has moved to a specified {@link Room}.
     * <p>
     * Used by {@link ActorController}.
     *
     * @param actorId int: The logical reference ID of the {@link Actor} which
     *                has moved.
     * @param roomId  int: The logical reference ID of the {@link Room} moved
     *                to by the given {@link Actor}.
     *
     * @see Actor
     * @see Room
     * @see ActorController
     */
    void onActorMove(int actorId, int roomId);

    /**
     * Callback method, informs the {@link GameController} that the given turn
     * ID, and therefore the turn for the {@link Actor} associated with that
     * ID, has passed.
     * <p>
     * Used by {@link ActorController}.
     *
     * @param turnId int: The logical reference ID of the turn that has passed,
     *               and also the {@link Actor} for which that turn was taken.
     *
     * @see Actor
     * @see ActorController
     */
    void turnPassed(int turnId);
}
