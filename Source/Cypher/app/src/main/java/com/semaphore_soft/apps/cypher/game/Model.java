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

    public void addRoom(int id, Room room)
    {

    }

    public void addActor(int id, Actor actor)
    {

    }

    public void addSpecial(int id, Special special)
    {

    }

    public void addEntity(int id, Entity entity)
    {

    }

    public void addItem(int id, Item item)
    {

    }
}
