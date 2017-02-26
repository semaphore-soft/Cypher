package com.semaphore_soft.apps.cypher.game;

import java.util.ArrayList;

/**
 * An instance of game.Item holds and maintains information about the state of
 * one item in the game, including any Effects it associates with, the rating
 * applied to those Effects, and its display name.
 * <p>
 * Must be extended and given qualifications describing under what conditions
 * the Effects of the Item will apply and cease to apply.
 *
 * @see ItemConsumable
 * @see ItemDurable
 * @see Effect.E_EFFECT
 * @see Effect
 * @see Actor
 * @see Status
 *
 * @author scorple
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
     * Creates an Item object representing an item in the game with a reference
     * name. Initializes Effects list.
     * <p>
     * WARNING: An Item with no Effects and no Effect rating cannot be used
     * properly.
     *
     * @param id   int: The logical reference ID of this Item.
     * @param name String: The reference name of this Item.
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
     * Logical ID, reference name, and Effect rating constructor.
     * <p>
     * Creates an Item object representing an item in the game with a reference
     * name and rating to be used for any Effects applied by this Item.
     * Initializes Effects list.
     * <p>
     * WARNING: An Item with an Effect rating but no Effects cannot be used
     * properly.
     *
     * @param id           int: The logical reference ID of this Item.
     * @param name         String: The reference name of this Item.
     * @param effectRating int: The rating to be used for any Effects applied
     *                     by this Item.
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
     * Get the logical reference ID of this Item.
     *
     * @return int: The logical reference ID of this Item.
     */
    public int getID()
    {
        return id;
    }

    /**
     * Get the reference name of this Item.
     *
     * @return String: The reference name of this Item.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the rating used for any Effects applied by this Item.
     *
     * @return int: The rating used for any Effect applied by this Item.
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
     * Get the display name for this Item.
     * <p>
     * To be used for any and all user feedback including the name of this Item
     * in plain text.
     *
     * @return String: The display name to be used for this Item.
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Set the display name of this Item.
     * <p>
     * To be used for any and all user feedback including the name of this Item
     * in plain text.
     *
     * @param displayName String: The display name of this Item.
     */
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    /**
     * Add a single Effect type for this Item to associate with, or to be
     * applied by this Item.
     *
     * @param effect Effect.E_EFFECT: The type of Effect to associate with this
     *               Item.
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
     * Get a list of Effect types this Item associates with, or applies.
     *
     * @return ArrayList: The list of Effect types this Item associates with,
     * or applies.
     *
     * @see Effect.E_EFFECT
     * @see Effect
     */
    ArrayList<Effect.E_EFFECT> getEffects()
    {
        return effects;
    }
}