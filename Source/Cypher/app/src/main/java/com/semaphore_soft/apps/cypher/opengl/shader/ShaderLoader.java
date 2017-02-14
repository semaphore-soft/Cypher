package com.semaphore_soft.apps.cypher.opengl.shader;

import android.content.Context;
import android.opengl.GLES20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Scorple on 2/13/2017.
 */

public class ShaderLoader
{
    public static int createShader(Context context, String filename, int type)
    {
        return compileShader(type, readShader(context, filename));
    }

    public static String readShader(Context context, String filename)
    {
        String res = "";

        try
        {
            BufferedReader br =
                new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

            String readLine;
            while ((readLine = br.readLine()) != null)
            {
                res += readLine + '\n';
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return res;
    }

    public static int compileShader(int shaderType, String shaderSource)
    {
        int shaderHandle = GLES20.glCreateShader(shaderType);

        GLES20.glShaderSource(shaderHandle, shaderSource);
        GLES20.glCompileShader(shaderHandle);

        return shaderHandle;
    }
}
