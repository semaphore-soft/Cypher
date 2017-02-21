package com.semaphore_soft.apps.cypher.utils;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Scorple on 2/5/2017.
 */

public class CollectionManager
{
    public static int getNextID(ConcurrentHashMap<Integer, ?> concurrentHashMap)
    {
        return ((concurrentHashMap.size() > 0) ?
                Collections.max(concurrentHashMap.keySet()) + 1 : 0);
    }

    public static int getNextIdFromId(int currentId,
                                      ConcurrentHashMap<Integer, ?> concurrentHashMap)
    {
        if (currentId == Collections.max(concurrentHashMap.keySet()))
        {
            return Collections.min(concurrentHashMap.keySet());
        }

        int currentClosest = -1;

        for (int id : concurrentHashMap.keySet())
        {
            if (id > currentId && (id < currentClosest || currentClosest < 0))
            {
                currentClosest = id;
            }
        }

        return currentClosest;
    }
}
