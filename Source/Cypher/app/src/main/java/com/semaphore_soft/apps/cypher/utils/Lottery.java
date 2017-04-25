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
        Logger.logD("enter trace");

        int                      totalTickets = 0;
        HashMap<String, Integer> offsetValues = new HashMap<>();

        for (String key : values.keySet())
        {
            int value = values.get(key);

            totalTickets += value;
            offsetValues.put(key, totalTickets);

            Logger.logD(
                "lottery key:<" + key + "> with tickets:<" + (totalTickets - value) + ">-<" +
                (totalTickets - 1) + ">");
        }

        int ticket = (int) (Math.random() * totalTickets);

        for (String key : offsetValues.keySet())
        {
            int offsetValue = offsetValues.get(key);

            if ((offsetValue - values.get(key)) <= ticket &&
                ticket < offsetValue)
            {
                Logger.logD("lottery winner:<" + ticket + ">, key:<" + key + ">");

                Logger.logD("exit trace");

                return key;
            }
        }

        Logger.logD("exit trace");

        return null;
    }

    @Nullable
    public static String performLottery(final String[] keys, final int[] values)
    {
        Logger.logD("enter trace");

        if (keys.length != values.length)
        {
            Logger.logD("exit trace");

            return null;
        }

        int   totalTickets = 0;
        int[] offsetValues = new int[values.length];

        for (int i = 0; i < keys.length; ++i)
        {
            totalTickets += values[i];
            offsetValues[i] = totalTickets;

            Logger.logD(
                "lottery key:<" + keys[i] + "> with tickets:<" + (totalTickets - values[i]) +
                ">-<" +
                (totalTickets - 1) + ">");
        }

        if (totalTickets == 0)
        {
            Logger.logD("exit trace");

            return null;
        }

        int ticket = (int) (Math.random() * totalTickets);

        for (int i = 0; i < keys.length; ++i)
        {
            if (ticket < offsetValues[i])
            {
                Logger.logD("lottery winner:<" + ticket + ">, key:<" + keys[i] + ">");

                Logger.logD("exit trace");

                return keys[i];
            }
        }

        Logger.logD("exit trace");

        return null;
    }
}
