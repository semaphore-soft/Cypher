package com.semaphore_soft.apps.cypher.game;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Scorple on 2/18/2017.
 */

public class Model
{
    private ConcurrentHashMap<Integer, Room>    rooms;
    private ConcurrentHashMap<Integer, Actor>   actors;
    private ConcurrentHashMap<Integer, Special> specials;
    private ConcurrentHashMap<Integer, Entity>  entities;
    private ConcurrentHashMap<Integer, Item>    items;

    private Map map;

    public Model()
    {
        rooms = new ConcurrentHashMap<>();
        actors = new ConcurrentHashMap<>();
        specials = new ConcurrentHashMap<>();
        entities = new ConcurrentHashMap<>();
        items = new ConcurrentHashMap<>();

        map = new Map();
    }

    public ConcurrentHashMap<Integer, Room> getRooms()
    {
        return rooms;
    }

    public ConcurrentHashMap<Integer, Actor> getActors()
    {
        return actors;
    }

    public ConcurrentHashMap<Integer, Special> getSpecials()
    {
        return specials;
    }

    public ConcurrentHashMap<Integer, Entity> getEntities()
    {
        return entities;
    }

    public ConcurrentHashMap<Integer, Item> getItems()
    {
        return items;
    }

    public Map getMap()
    {
        return map;
    }

    public void addRoom(int id, Room room)
    {
        rooms.put(id, room);
    }

    public void addActor(int id, Actor actor)
    {
        actors.put(id, actor);
    }

    public void addSpecial(int id, Special special)
    {
        specials.put(id, special);
    }

    public void addEntity(int id, Entity entity)
    {
        entities.put(id, entity);
    }

    public void addItem(int id, Item item)
    {
        items.put(id, item);
    }
}
