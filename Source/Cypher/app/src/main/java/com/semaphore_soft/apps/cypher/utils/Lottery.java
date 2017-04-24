package com.semaphore_soft.apps.cypher.utils;

import android.support.annotation.Nullable;

import java.util.HashMap;

/**
 * Created by rickm on 4/19/2017.
 */

public class Lottery
{
    @Nullable
    public static String performLottery(final HashMap<String, Integer> values)
    {
        int                      totalTickets = 0;
        HashMap<String, Integer> offsetValues = new HashMap<>();

        for (String key : values.keySet())
        {
            totalTickets += values.get(key);
            offsetValues.put(key, totalTickets);
        }

        int ticket = (int) (Math.random() * totalTickets);

        for (String key : offsetValues.keySet())
        {
            if (ticket < offsetValues.get(key))
            {
                return key;
            }
        }

        return null;
    }

    @Nullable
    public static String performLottery(final String[] keys, final int[] values)
    {
        if (keys.length != values.length)
        {
            return null;
        }

        int   totalTickets = 0;
        int[] offsetValues = new int[values.length];

        for (int i = 0; i < keys.length; ++i)
        {
            totalTickets += values[i];
            offsetValues[i] = totalTickets;
        }

        if (totalTickets == 0)
        {
            return null;
        }

        int ticket = (int) (Math.random() * totalTickets);

        for (int i = 0; i < keys.length; ++i)
        {
            if (ticket < offsetValues[i])
            {
                return keys[i];
            }
        }

        return null;
    }
}
