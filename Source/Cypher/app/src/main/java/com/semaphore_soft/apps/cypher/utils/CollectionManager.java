package com.semaphore_soft.apps.cypher.utils;

import java.util.Collections;
import java.util.Hashtable;

/**
 * Created by Scorple on 2/5/2017.
 */

public class CollectionManager
{
    public static long getNextID(Hashtable<Long, ?> hashtable)
    {
        return ((hashtable.size() > 0) ? Collections.max(hashtable.keySet()) + 1 : 0);
    }
}
