package com.semaphore_soft.apps.cypher.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.semaphore_soft.apps.cypher.utils.Logger;
import com.semaphore_soft.apps.cypher.utils.Timer;

import java.io.IOException;

/**
 * Created by Scorple on 2/12/2017.
 */

public class TextureLoader
{
    public static int loadTexture(Context context, int resourceId)
    {
        int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;

            Bitmap bitmap =
                BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                                   GLES20.GL_TEXTURE_MIN_FILTER,
                                   GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                                   GLES20.GL_TEXTURE_MAG_FILTER,
                                   GLES20.GL_NEAREST);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            bitmap.recycle();
        }

        return textureHandle[0];
    }

    public static int loadTexture(Context context, String filename)
    {
        Timer timer = new Timer();
        timer.start();

        Logger.logI("loading texture <" + filename + ">", 3);

        int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;

            Bitmap bitmap;

            try
            {
                bitmap = BitmapFactory.decodeStream(context.getAssets().open(filename));
            }
            catch (IOException e)
            {
                try
                {
                    bitmap =
                        BitmapFactory.decodeStream(context.getAssets().open("textures/error.png"));
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();

                    return -1;
                }
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                                   GLES20.GL_TEXTURE_MIN_FILTER,
                                   GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                                   GLES20.GL_TEXTURE_MAG_FILTER,
                                   GLES20.GL_NEAREST);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            bitmap.recycle();

        }

        Logger.logI(
            "finished loading texture <" + filename + "> in " + ((float) timer.getTime()) / 1000f +
            " seconds", 3);
        return textureHandle[0];
    }
}
