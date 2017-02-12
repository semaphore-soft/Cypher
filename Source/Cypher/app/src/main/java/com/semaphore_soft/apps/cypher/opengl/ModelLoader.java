package com.semaphore_soft.apps.cypher.opengl;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by ceroj on 2/1/2017.
 */

public class ModelLoader
{
    public static ARModelGLES20 loadModel(Context context, String filename)
    {
        return loadModel(context, filename, 40.0f);
    }

    public static ARModelGLES20 loadModel(Context context, String filename, float size)
    {
        if (filename != null)
        {
            try
            {
                System.out.println("loading model: " + filename);

                BufferedReader br =
                    new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

                System.out.println("opened file");

                ArrayList<Float> vertices = new ArrayList<>();
                //ArrayList<Float> colors    = new ArrayList<>();
                ArrayList<Short> indices = new ArrayList<>();
                //ArrayList<Float> texCoords = new ArrayList<>();

                String line;

                while ((line = br.readLine()) != null)
                {
                    String[] splitLine = line.split(" ");
                    if (splitLine[0].equals("v"))
                    {
                        for (int i = 1; i < 4; ++i)
                        {
                            vertices.add(Float.parseFloat(splitLine[i]));
                        }
                    }
                    else if (splitLine[0].equals("f"))
                    {
                        short faceIndices[] = new short[3];
                        for (int i = 1; i < 4; ++i)
                        {
                            String triad[] = splitLine[i].split("/");
                            faceIndices[3 - i] = (short) (Short.parseShort(triad[0]) - 1);
                        }
                        for (int i = 0; i < 3; ++i)
                        {
                            indices.add(faceIndices[i]);
                        }
                    }
                }

                br.close();

                System.out.println("closed file");

                System.out.println("verts: " + vertices.size() / 3);
                System.out.println("tris: " + indices.size() / 3);

                System.out.println("making opengl object");

                ARModelGLES20 arModel = new ARModelGLES20(size);
                arModel.makeVertexBuffer(vertices);
                arModel.makeIndexBuffer(indices);
                arModel.makeColorBuffer();

                System.out.println("finished opengl object");
                System.out.println("finished loading model");

                return arModel;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }
}
