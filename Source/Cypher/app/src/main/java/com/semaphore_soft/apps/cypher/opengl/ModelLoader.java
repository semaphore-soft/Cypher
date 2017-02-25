package com.semaphore_soft.apps.cypher.opengl;

import android.content.Context;

import com.semaphore_soft.apps.cypher.utils.Logger;
import com.semaphore_soft.apps.cypher.utils.Timer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ceroj on 2/1/2017.
 */

public class ModelLoader
{
    private static final int VERTEX_SIZE         = 3;
    private static final int TEX_COORDINATE_SIZE = 2;
    private static final int NORMAL_SIZE         = 3;

    public static ARDrawableGLES20 load(Context context, String name)
    {
        return load(context, name, null);
    }

    public static ARDrawableGLES20 load(Context context, String name, float size)
    {
        return load(context, name, null, size, null);
    }

    public static ARDrawableGLES20 load(Context context, String name, String subFolder)
    {
        return load(context, name, subFolder, 40.0f, null);
    }

    public static ARDrawableGLES20 load(Context context, String name, float size, String texture)
    {
        return load(context, name, null, size, texture);
    }

    public static ARDrawableGLES20 load(Context context,
                                        String name,
                                        String subFolder,
                                        float size,
                                        String texture)
    {
        if (name != null)
        {
            try
            {
                String filename = name;

                if (subFolder != null)
                {
                    filename = subFolder + "/" + filename;
                }

                if (!filename.startsWith("models/"))
                {
                    filename = "models/" + filename;
                }

                String[] poseFiles = context.getAssets().list(filename);
                if (poseFiles != null && poseFiles.length > 0)
                {
                    Logger.logI("found " + poseFiles.length + " poses", 3);

                    String path = filename + "/";

                    ConcurrentHashMap<String, ARModelGLES20> poseLib =
                        new ConcurrentHashMap<>();
                    String defaultPose = "default";

                    for (String poseFile : poseFiles)
                    {
                        if (poseFile.startsWith(name) && poseFile.endsWith(".obj"))
                        {
                            filename = path + poseFile;

                            String[] splitPoseName = poseFile.split("\\.")[0].split("_");

                            String poseName;

                            if (splitPoseName.length > 1)
                            {
                                poseName = splitPoseName[1];
                            }
                            else
                            {
                                poseName = "default";
                            }

                            Logger.logI("found pose <" + poseName + ">", 3);

                            ARModelGLES20 model = loadModel(context, filename, size, texture);

                            if (model != null)
                            {
                                poseLib.put(poseName, model);
                                Logger.logI("added pose <" + filename + ">", 3);
                            }
                            else
                            {
                                Logger.logI("failed to load pose <" + filename + ">", 3);
                            }
                        }
                    }

                    return new ARPoseModel(poseLib, defaultPose);
                }
                else
                {
                    if (!filename.endsWith(".obj"))
                    {
                        filename += ".obj";
                    }

                    return loadModel(context, filename, size, texture);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static ARModelGLES20 loadModel(Context context,
                                           String filename,
                                           float size,
                                           String texture)
    {
        Timer timer = new Timer();
        timer.start();

        try
        {
            Logger.logI("loading model <" + filename + ">", 3);

            BufferedReader br =
                new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

            Logger.logI("opened file", 4);

            ArrayList<Float> vertices       = new ArrayList<>();
            ArrayList<Float> colors         = new ArrayList<>();
            ArrayList<Float> normals        = new ArrayList<>();
            ArrayList<Float> texCoordinates = new ArrayList<>();
            ArrayList<Short> vertexIndices  = new ArrayList<>();

            ArrayList<Float> texCoordinatesInVertexOrder = new ArrayList<>();
            ArrayList<Float> normalsInVertexOrder        = new ArrayList<>();

            HashMap<Short, Float[]> texCoordinatesByVertexIndex =
                new HashMap<>();
            HashMap<Short, Float[]> normalsByVertexIndex =
                new HashMap<>();

            String line;

            while ((line = br.readLine()) != null)
            {
                String[] splitLine = line.split(" ");
                switch (splitLine[0])
                {
                    case "v":
                        for (int i = 1; i < 4; ++i)
                        {
                            vertices.add(Float.parseFloat(splitLine[i]));
                        }
                        break;
                    case "vt":
                        texCoordinates.add(Float.parseFloat(splitLine[1]));
                        texCoordinates.add(1.0f -
                                           Float.parseFloat(splitLine[2]));
                        break;
                    case "vn":
                        for (int i = 1; i < 4; ++i)
                        {
                            normals.add(Float.parseFloat(splitLine[i]));
                        }
                        break;
                    case "f":
                        short vi[] = new short[3];
                        short ti[] = new short[3];
                        short ni[] = new short[3];

                        for (int i = 1; i < 4; ++i)
                        {
                            String triad[] = splitLine[i].split("/");
                            vi[3 - i] = (short) (Short.parseShort(triad[0]) - 1);
                            if (!triad[1].equals(""))
                            {
                                ti[3 - i] = (short) (Short.parseShort(triad[1]) - 1);
                            }
                            ni[3 - i] = (short) (Short.parseShort(triad[2]) - 1);
                        }
                        for (int i = 0; i < 3; ++i)
                        {
                            if (vertexIndices.contains(vi[i]))
                            {
                                int nextIndexTripleStartIndex = vi[i] * VERTEX_SIZE;
                                for (int j = 0; j < 3; ++j)
                                {
                                    vertices.add(vertices.get(nextIndexTripleStartIndex + j));
                                }
                                vi[i] = (short) ((vertices.size() / 3) - 1);
                            }

                            vertexIndices.add(vi[i]);

                            if (texCoordinates.size() > 0)
                            {
                                texCoordinatesByVertexIndex.put(vi[i],
                                                                new Float[]{texCoordinates.get(
                                                                    ti[i] *
                                                                    TEX_COORDINATE_SIZE), texCoordinates.get(
                                                                    ti[i] *
                                                                    TEX_COORDINATE_SIZE +
                                                                    1)});
                            }

                            normalsByVertexIndex.put(vi[i],
                                                     new Float[]{normals.get(
                                                         ni[i] * NORMAL_SIZE), normals.get(
                                                         ni[i] * NORMAL_SIZE + 1), normals.get(
                                                         ni[i] * NORMAL_SIZE + 2)});
                        }
                        break;
                }
            }

            br.close();

            Logger.logI("closed file", 4);

            Logger.logI(
                "finished parsing file in " + ((float) timer.getTime()) / 1000f + " seconds", 3);

            Timer indexTimer = new Timer();
            indexTimer.start();

            if (texCoordinates.size() > 0)
            {
                for (int i = 0; i < vertexIndices.size() * TEX_COORDINATE_SIZE; ++i)
                {
                    texCoordinatesInVertexOrder.add(0.0f);
                }
            }
            for (int i = 0; i < vertexIndices.size() * NORMAL_SIZE; ++i)
            {
                normalsInVertexOrder.add(0.0f);
            }

            try
            {
                if (texCoordinates.size() > 0)
                {
                    for (short index : texCoordinatesByVertexIndex.keySet())
                    {
                        for (int i = 0; i < texCoordinatesByVertexIndex.get(index).length; ++i)
                        {
                            texCoordinatesInVertexOrder.set(index * TEX_COORDINATE_SIZE + i,
                                                            texCoordinatesByVertexIndex.get(index)[i]);
                        }
                    }
                }
                for (short index : normalsByVertexIndex.keySet())
                {
                    for (int i = 0; i < normalsByVertexIndex.get(index).length; ++i)
                    {
                        normalsInVertexOrder.set(index * NORMAL_SIZE + i,
                                                 normalsByVertexIndex.get(index)[i]);
                    }
                }
            }
            catch (IndexOutOfBoundsException e)
            {
                Logger.logD(
                    "encountered bad index for model <" + filename + ">, check model format");
                e.printStackTrace();
                return null;
            }

            Logger.logI(
                "finished reconstructing indices " + ((float) indexTimer.getTime()) / 1000f +
                " seconds", 3);

            Timer openGLTimer = new Timer();
            openGLTimer.start();

            Logger.logI("indices: " + vertexIndices.size(), 4);
            Logger.logI("vertices: " + vertices.size() / VERTEX_SIZE, 4);
            Logger.logI(
                "texCoordinates: " + texCoordinatesInVertexOrder.size() / TEX_COORDINATE_SIZE, 4);
            Logger.logI("normals: " + normalsInVertexOrder.size() / NORMAL_SIZE, 4);

            Logger.logI("making opengl object", 4);

            ARModelGLES20 arModel = new ARModelGLES20(size);
            arModel.makeVertexBuffer(vertices);
            arModel.makeColorBuffer();
            arModel.makeNormalBuffer(normalsInVertexOrder);
            if (texCoordinatesInVertexOrder.size() > 0)
            {
                arModel.makeTexCoordinateBuffer(texCoordinatesInVertexOrder);
            }
            arModel.makeVertexIndexBuffer(vertexIndices);
            arModel.setTextureHandle(TextureLoader.loadTexture(context,
                                                               "textures/" + texture + ".png"));

            Logger.logI(
                "finished making openGL object in " + ((float) openGLTimer.getTime()) / 1000f +
                " seconds", 3);

            Logger.logI("finished opengl object", 4);
            Logger.logI("finished loading model <" + filename + "> in " +
                        ((float) timer.getTime()) / 1000f + " seconds", 3);

            return arModel;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
