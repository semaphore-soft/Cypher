package com.semaphore_soft.apps.cypher.utils;

import android.util.Log;

/**
 * Created by Scorple on 2/20/2017.
 */

public class Logger
{
    public static String getClassName()
    {
        String[] splitClassName =
            Thread.currentThread().getStackTrace()[4].getClassName().split("\\.");
        return splitClassName[splitClassName.length - 1];
    }

    public static String getMethodName()
    {
        return Thread.currentThread().getStackTrace()[4].getMethodName() + "()";
    }

    public static int getLingNumber()
    {
        return Thread.currentThread().getStackTrace()[4].getLineNumber();
    }

    public static void logV(String log)
    {
        Log.v(getClassName(), getLingNumber() + ":" + getMethodName() + ": " + log);
    }

    public static void logD(String log)
    {
        Log.d(getClassName(), getLingNumber() + ":" + getMethodName() + ": " + log);
    }

    public static void logI(String log)
    {
        Log.i(getClassName(), getLingNumber() + ":" + getMethodName() + ": " + log);
    }

    public static void logW(String log)
    {
        Log.w(getClassName(), getLingNumber() + ":" + getMethodName() + ": " + log);
    }

    public static void logE(String log)
    {
        Log.e(getClassName(), getLingNumber() + ":" + getMethodName() + ": " + log);
    }

    public static void logWTF(String log)
    {
        Log.wtf(getClassName(), getLingNumber() + ":" + getMethodName() + ": " + log);
    }
}
