package com.semaphore_soft.apps.cypher.utils;

import android.util.Log;

/**
 * Created by Scorple on 2/20/2017.
 */

public class Logger
{
    private static int LOG_LEVEL = 0;

    public static void setLogLevel(int LOG_LEVEL)
    {
        Logger.LOG_LEVEL = LOG_LEVEL;
    }

    public static int getLogLevel()
    {
        return LOG_LEVEL;
    }

    private static String getClassName()
    {
        String[] splitClassName =
            Thread.currentThread().getStackTrace()[4].getClassName().split("\\.");
        return splitClassName[splitClassName.length - 1];
    }

    private static String getMethodName()
    {
        return Thread.currentThread().getStackTrace()[4].getMethodName() + "()";
    }

    private static int getLineNumber()
    {
        return Thread.currentThread().getStackTrace()[4].getLineNumber();
    }

    public static void logV(String log)
    {
        Log.v(getClassName(), getLineNumber() + ":" + getMethodName() + ": " + log);
    }

    public static void logV(String log, int level)
    {
        if (LOG_LEVEL <= level)
        {
            Log.v(getClassName(), getLineNumber() + ":" + getMethodName() + ": " + log);
        }
    }

    public static void logD(String log)
    {
        Log.d(getClassName(), getLineNumber() + ":" + getMethodName() + ": " + log);
    }

    public static void logD(String log, int level)
    {
        if (LOG_LEVEL <= level)
        {
            Log.d(getClassName(), getLineNumber() + ":" + getMethodName() + ": " + log);
        }
    }

    public static void logI(String log)
    {
        Log.i(getClassName(), getLineNumber() + ":" + getMethodName() + ": " + log);
    }

    public static void logI(String log, int level)
    {
        if (LOG_LEVEL <= level)
        {
            Log.i(getClassName(), getLineNumber() + ":" + getMethodName() + ": " + log);
        }
    }

    public static void logW(String log)
    {
        Log.w(getClassName(), getLineNumber() + ":" + getMethodName() + ": " + log);
    }

    public static void logW(String log, int level)
    {
        if (LOG_LEVEL <= level)
        {
            Log.w(getClassName(), getLineNumber() + ":" + getMethodName() + ": " + log);
        }
    }

    public static void logE(String log)
    {
        Log.e(getClassName(), getLineNumber() + ":" + getMethodName() + ": " + log);
    }

    public static void logE(String log, int level)
    {
        if (LOG_LEVEL <= level)
        {
            Log.e(getClassName(), getLineNumber() + ":" + getMethodName() + ": " + log);
        }
    }

    public static void logWTF(String log)
    {
        Log.wtf(getClassName(), getLineNumber() + ":" + getMethodName() + ": " + log);
    }
}
