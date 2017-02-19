package com.semaphore_soft.apps.cypher.utils;

import java.util.Collections;
import java.util.Hashtable;

/**
 * Created by Scorple on 2/5/2017.
 */

public class CollectionManager
{
    public static int getNextID(Hashtable<Integer, ?> hashtable)
    {
        return ((hashtable.size() > 0) ? Collections.max(hashtable.keySet()) + 1 : 0);
    }

    public static int getNextIdFromId(int currentId, Hashtable<Integer, ?> hashtable)
    {
        if (currentId == Collections.max(hashtable.keySet()))
        {
            return Collections.min(hashtable.keySet());
        }

        int currentClosest = -1;

        for (int id : hashtable.keySet())
        {
            if (id > currentId && (id < currentClosest || currentClosest < 0))
            {
                currentClosest = id;
            }
        }

        return currentClosest;
    }
}
