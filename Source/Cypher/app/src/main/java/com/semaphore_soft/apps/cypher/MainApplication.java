package com.semaphore_soft.apps.cypher;

import android.app.Application;
import android.content.Context;

import com.semaphore_soft.apps.cypher.utils.Logger;

import org.artoolkit.ar.base.assets.AssetHelper;

/**
 * Created by rickm on 11/9/2016.
 */

public class MainApplication extends Application
{

    private static Application sInstance;

    // Anywhere in the application where an instance is required, this method
    // can be used to retrieve it.
    public static Application getInstance()
    {
        return sInstance;
    }

    private static Context mContext;

    public Context getContext()
    {
        return mContext;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        sInstance = this;
        mContext = getContext();
        ((MainApplication) sInstance).initializeInstance();

        Logger.setLogLevel(4);
    }

    // Here we do one-off initialisation which should apply to all activities
    // in the application.
    protected void initializeInstance()
    {
        // Unpack assets to cache directory so native library can read them.
        // N.B.: If contents of assets folder changes, be sure to increment the
        // versionCode integer in the modules build.gradle file.
        AssetHelper assetHelper = new AssetHelper(getAssets());
        assetHelper.cacheAssetFolder(getInstance(), "Data");
        assetHelper.cacheAssetFolder(getInstance(), "models");
        assetHelper.cacheAssetFolder(getInstance(), "textures");
    }

}
