package com.semaphore_soft.apps.cypher.game;

import java.util.ArrayList;

/**
 * An instance of {@link Item game.Item} holds and maintains information about
 * the state of one item in the game, including any {@link Effect Effects} it
 * associates with, the rating applied to those {@link Effect Effects}, and its
 * display name.
 * <p>
 * Must be extended and given qualifications describing under what conditions
 * the {@link Effect Effects} of the {@link Item} will apply and cease to
 * apply.
 *
 * @author scorple
 * @see ItemConsumable
 * @see ItemDurable
 * @see Effect.E_EFFECT
 * @see Effect
 * @see Actor
 * @see Status
 */
public abstract class Item
{
    protected int                        id;
    protected String                     name;
    private   int                        effectRating;
    private   ArrayList<Effect.E_EFFECT> effects;

    private String displayName;

    /**
     * Logical ID and reference name constructor.
     * <p>
     * Creates an {@link Item} object representing an item in the game with a
     * reference name. Initializes {@link Effect Effects} list.
     * <p>
     * WARNING: An {@link Item} with no Effects and no Effect rating cannot be
     * used properly.
     *
     * @param id   int: The logical reference ID of this {@link Item}.
     * @param name String: The reference name of this {@link Item}.
     *
     * @see Effect
     * @see Effect.E_EFFECT
     */
    private Item(int id, String name)
    {
        this.id = id;
        this.name = name;
        effects = new ArrayList<>();
    }

    /**
     * Logical ID, reference name, and {@link Effect} rating constructor.
     * <p>
     * Creates an {@link Item} object representing an item in the game with a
     * reference name and rating to be used for any {@link Effect Effects}
     * applied by this {@link Item}. Initializes {@link Effect Effects} list.
     * <p>
     * WARNING: An {@link Item} with an {@link Effect Effects} rating but no
     * {@link Effect Effects} cannot be used properly.
     *
     * @param id           int: The logical reference ID of this {@link Item}.
     * @param name         String: The reference name of this {@link Item}.
     * @param effectRating int: The rating to be used for any {@link Effect
     *                     Effects} applied by this {@link Item}.
     *
     * @see Effect
     * @see Effect.E_EFFECT
     */
    Item(int id, String name, int effectRating)
    {
        this(id, name);
        this.effectRating = effectRating;
    }

    /**
     * Get the logical reference ID of this {@link Item}.
     *
     * @return int: The logical reference ID of this {@link Item}.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Get the reference name of this {@link Item}.
     * <p>
     * To be used for internal purposes ONLY. Should not be exposed to the
     * user.
     *
     * @return String: The reference name of this {@link Item}.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the rating used for any {@link Effect Effects} applied by this
     * {@link Item}.
     *
     * @return int: The rating used for any {@link Effect} applied by this
     * {@link Item}.
     *
     * @see Effect
     * @see Effect.E_EFFECT
     * @see ItemConsumable
     * @see ItemDurable
     * @see Status
     * @see StatusTemporary
     * @see StatusLinked
     */
    public int getEffectRating()
    {
        return effectRating;
    }

    /**
     * Get the display name for this {@link Item}.
     * <p>
     * To be used for any and all user feedback including the name of this
     * {@link Item} in plain text.
     *
     * @return String: The display name to be used for this {@link Item}.
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Set the display name of this {@link Item}.
     * <p>
     * To be used for any and all user feedback including the name of this
     * {@link Item} in plain text.
     *
     * @param displayName String: The display name of this {@link Item}.
     */
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    /**
     * Add a single {@link Effect} {@link Effect.E_EFFECT type} for this {@link
     * Item} to associate with, or to be applied by this {@link Item}.
     *
     * @param effect {@link Effect.E_EFFECT}: The {@link Effect.E_EFFECT type}
     *               of {@link Effect} to associate with this {@link Item}.
     *
     * @see Effect.E_EFFECT
     * @see Effect
     */
    public void addEffect(Effect.E_EFFECT effect)
    {
        if (!effects.contains(effect))
        {
            effects.add(effect);
        }
    }

    /**
     * Get a list of {@link Effect} {@link Effect.E_EFFECT types} this {@link
     * Item} associates with, or applies.
     *
     * @return ArrayList: The list of {@link Effect} {@link Effect.E_EFFECT
     * types} this {@link Item} associates with, or applies.
     *
     * @see Effect.E_EFFECT
     * @see Effect
     */
    public ArrayList<Effect.E_EFFECT> getEffects()
    {
        return effects;
    }
}