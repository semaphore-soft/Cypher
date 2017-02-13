package com.semaphore_soft.apps.cypher.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

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
        int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            try
            {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;

                Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets().open(filename));

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
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return textureHandle[0];
    }
}
