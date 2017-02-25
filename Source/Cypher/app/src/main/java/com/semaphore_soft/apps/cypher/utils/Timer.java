package com.semaphore_soft.apps.cypher.utils;

import java.util.Calendar;

/**
 * Created by Scorple on 2/24/2017.
 */

public class Timer
{
    private long startTime;

    public Timer()
    {

    }

    public void start()
    {
        startTime = Calendar.getInstance().getTimeInMillis();
    }

    public long getTime()
    {
        return Calendar.getInstance().getTimeInMillis() - startTime;
    }
}
