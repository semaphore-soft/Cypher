package com.semaphore_soft.apps.cypher.game;

import java.util.concurrent.ConcurrentHashMap;

/**
 * An instance of {@link Model game.Model} holds and maintains information
 * about game state, including the status of {@link Room Rooms}, {@link Actor
 * Actors}, {@link Special Specials}, {@link Entity Entities}, {@link Item
 * Items}, and a {@link Map} describing {@link Room} location and rotation.
 *
 * @author scorple
 * @see Room
 * @see Actor
 * @see Special
 * @see Entity
 * @see Item
 * @see Map
 */
public class Model
{
    private ConcurrentHashMap<Integer, Room>    rooms;
    private ConcurrentHashMap<Integer, Actor>   actors;
    private ConcurrentHashMap<Integer, Special> specials;
    private ConcurrentHashMap<Integer, Entity>  entities;
    private ConcurrentHashMap<Integer, Item>    items;

    private Map map;

    /**
     * Default constructor. Initializes member HashMaps and Map.
     *
     * @see Room
     * @see Actor
     * @see Special
     * @see Entity
     * @see Item
     * @see Map
     */
    public Model()
    {
        rooms = new ConcurrentHashMap<>();
        actors = new ConcurrentHashMap<>();
        specials = new ConcurrentHashMap<>();
        entities = new ConcurrentHashMap<>();
        items = new ConcurrentHashMap<>();

        map = new Map();
    }

    /**
     * Get the HashMap of {@link Room Rooms} being maintained my this {@link
     * Model}.
     *
     * @return ConcurrentHashMap: A HashMap associating integer IDs with {@link
     * Room} objects.
     *
     * @see Room
     */
    public ConcurrentHashMap<Integer, Room> getRooms()
    {
        return rooms;
    }

    /**
     * Get the HashMap of {@link Actor Actors} being maintained my this {@link
     * Model}.
     *
     * @return ConcurrentHashMap: A HashMap associating integer IDs with {@link
     * Actor} objects.
     *
     * @see Actor
     */
    public ConcurrentHashMap<Integer, Actor> getActors()
    {
        return actors;
    }

    /**
     * Get the HashMap of {@link Special Specials} being maintained my this
     * {@link Model}.
     *
     * @return ConcurrentHashMap: A HashMap associating integer IDs with
     * {@link Special} objects.
     *
     * @see Special
     */
    public ConcurrentHashMap<Integer, Special> getSpecials()
    {
        return specials;
    }

    /**
     * Get the HashMap of {@link Entity Entities} being maintained my this
     * {@link Model}.
     *
     * @return ConcurrentHashMap: A HashMap associating integer IDs with {@link
     * Entity} objects.
     *
     * @see Entity
     */
    public ConcurrentHashMap<Integer, Entity> getEntities()
    {
        return entities;
    }

    /**
     * Get the HashMap of {@link Item Items} being maintained my this {@link
     * Model}.
     *
     * @return ConcurrentHashMap: A HashMap associating integer IDs with {@link
     * Item} objects.
     *
     * @see Item
     */
    public ConcurrentHashMap<Integer, Item> getItems()
    {
        return items;
    }

    /**
     * Get the {@link Map} describing the location and rotation of {@link Room
     * Rooms} being maintained by this {@link Model}.
     *
     * @return {@link Map}: A {@link Map} object describing the location and
     * rotation of {@link Room Rooms}.
     *
     * @see Map
     * @see Room
     */
    public Map getMap()
    {
        return map;
    }

    /**
     * Add a single {@link Room} object to this {@link Model}.
     *
     * @param id   int: The logical reference ID of the {@link Room} object
     *             being added.
     * @param room {@link Room}: The {@link Room} object being added.
     *
     * @see Room
     */
    public void addRoom(int id, Room room)
    {
        rooms.put(id, room);
    }

    /**
     * Add a single {@link Actor} object to this {@link Model}.
     *
     * @param id    int: The logical reference ID of the {@link Actor} object
     *              being added.
     * @param actor {@link Actor}: The {@link Actor} object being added.
     *
     * @see Actor
     */
    public void addActor(int id, Actor actor)
    {
        actors.put(id, actor);
    }

    /**
     * Add a single {@link Special} object to this {@link Model}.
     *
     * @param id      int: The logical reference ID of the {@link Special}
     *                being added.
     * @param special {@link Special}: The {@link Special} object being added.
     *
     * @see Special
     */
    public void addSpecial(int id, Special special)
    {
        specials.put(id, special);
    }

    /**
     * Add a single {@link Entity} object to this {@link Model}.
     *
     * @param id     int: The logical reference ID of the {@link Entity} being
     *               added.
     * @param entity {@link Entity}: The {@link Entity} object being added.
     *
     * @see Entity
     */
    public void addEntity(int id, Entity entity)
    {
        entities.put(id, entity);
    }

    /**
     * Add a single {@link Item} object to this {@link Model}.
     *
     * @param id   int: The logical reference ID of the {@link Item} being
     *             added.
     * @param item {@link Item}: The {@link Item} object being added.
     *
     * @see Item
     */
    public void addItem(int id, Item item)
    {
        items.put(id, item);
    }
}
