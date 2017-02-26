package com.semaphore_soft.apps.cypher.game;

import java.util.concurrent.ConcurrentHashMap;

/**
 * An instance of game.Model holds and maintains information about game state,
 * including the status of rooms, actors, specials, entities, items, and a map
 * describing room location and rotation.
 *
 * @author scorple
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
     * Get the HashMap of Rooms being maintained my this model.
     *
     * @return ConcurrentHashMap: A HashMap associating integer IDs with Room
     * objects.
     */
    public ConcurrentHashMap<Integer, Room> getRooms()
    {
        return rooms;
    }

    /**
     * Get the HashMap of Actors being maintained my this model.
     *
     * @return ConcurrentHashMap: A HashMap associating integer IDs with Actor
     *         objects.
     */
    public ConcurrentHashMap<Integer, Actor> getActors()
    {
        return actors;
    }

    /**
     * Get the HashMap of Specials being maintained my this model.
     *
     * @return ConcurrentHashMap: A HashMap associating integer IDs with
     *         Special objects.
     */
    public ConcurrentHashMap<Integer, Special> getSpecials()
    {
        return specials;
    }

    /**
     * Get the HashMap of Entities being maintained my this model.
     *
     * @return ConcurrentHashMap: A HashMap associating integer IDs with Entity
     *         objects.
     */
    public ConcurrentHashMap<Integer, Entity> getEntities()
    {
        return entities;
    }

    /**
     * Get the HashMap of Items being maintained my this model.
     *
     * @return ConcurrentHashMap: A HashMap associating integer IDs with Item
     *         objects.
     */
    public ConcurrentHashMap<Integer, Item> getItems()
    {
        return items;
    }

    /**
     * Get the Map describing the location and rotation of rooms being
     * maintained by this model.
     *
     * @return Map: A Map object describing the location and rotation of rooms.
     */
    public Map getMap()
    {
        return map;
    }

    /**
     * Add a single Room object to this Model.
     *
     * @param id   int: The logical reference ID of the Room object being added.
     * @param room Room: The Room object being added.
     */
    public void addRoom(int id, Room room)
    {
        rooms.put(id, room);
    }

    /**
     * Add a single Actor object to this Model.
     *
     * @param id    int: The logical reference ID of the Actor object being added.
     * @param actor Actor: The Actor object being added.
     */
    public void addActor(int id, Actor actor)
    {
        actors.put(id, actor);
    }

    /**
     * Add a single Special object to this Model.
     *
     * @param id      int: The logical reference ID of the Special being added.
     * @param special Special: The Special object being added.
     */
    public void addSpecial(int id, Special special)
    {
        specials.put(id, special);
    }

    /**
     * Add a single Entity object to this Model.
     *
     * @param id     int: The logical reference ID of the Entity being added.
     * @param entity Entity: The Entity object being added.
     */
    public void addEntity(int id, Entity entity)
    {
        entities.put(id, entity);
    }

    /**
     * Add a single Item object to this Model.
     *
     * @param id   int: The logical reference ID of the Item being added.
     * @param item Item: The Item object being added.
     */
    public void addItem(int id, Item item)
    {
        items.put(id, item);
    }
}
