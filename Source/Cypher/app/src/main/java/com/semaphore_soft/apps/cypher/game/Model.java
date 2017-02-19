package com.semaphore_soft.apps.cypher.game;

import java.util.Hashtable;

/**
 * Created by Scorple on 2/18/2017.
 */

public class Model
{
    private Hashtable<Integer, Room>    rooms;
    private Hashtable<Integer, Actor>   actors;
    private Hashtable<Integer, Special> specials;
    private Hashtable<Integer, Entity>  entities;
    private Hashtable<Integer, Item>    items;

    private Map map;

    public Model()
    {
        rooms = new Hashtable<>();
        actors = new Hashtable<>();
        specials = new Hashtable<>();
        entities = new Hashtable<>();
        items = new Hashtable<>();

        map = new Map();
    }

    public Hashtable<Integer, Room> getRooms()
    {
        return rooms;
    }

    public Hashtable<Integer, Actor> getActors()
    {
        return actors;
    }

    public Hashtable<Integer, Special> getSpecials()
    {
        return specials;
    }

    public Hashtable<Integer, Entity> getEntities()
    {
        return entities;
    }

    public Hashtable<Integer, Item> getItems()
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
