package com.semaphore_soft.apps.cypher.opengl;

import android.content.Context;

import com.semaphore_soft.apps.cypher.utils.Logger;
import com.semaphore_soft.apps.cypher.utils.Timer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ceroj on 2/1/2017.
 */

public class ModelLoader
{
    private static final int INT_BYTES   = Integer.SIZE / 8;
    private static final int FLOAT_BYTES = Float.SIZE / 8;
    private static final int SHORT_BYTES = Short.SIZE / 8;

    private static final int VERTEX_SIZE         = 3;
    private static final int TEX_COORDINATE_SIZE = 2;
    private static final int NORMAL_SIZE         = 3;
    private static final int INDEX_SIZE          = 3;

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

    public static ARDrawableGLES20 load(Context context, String name, String subFolder, float size)
    {
        return load(context, name, subFolder, size, null);
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

                            ARModelGLES20 model;

                            if (texture == null)
                            {
                                model = loadModel(context, filename, size, name);
                            }
                            else
                            {
                                model = loadModel(context, filename, size, texture);
                            }

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

                    if (texture == null)
                    {
                        return loadModel(context, filename, size, name);
                    }
                    else
                    {
                        return loadModel(context, filename, size, texture);
                    }
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

            ArrayList<Float> vertices                    = new ArrayList<>();
            ArrayList<Float> colors                      = new ArrayList<>();
            ArrayList<Float> texCoordinatesInVertexOrder = new ArrayList<>();
            ArrayList<Float> normalsInVertexOrder        = new ArrayList<>();
            ArrayList<Short> vertexIndices               = new ArrayList<>();

            String path = "";
            String name;

            String[] splitFilename = filename.split("/");

            for (int i = 0; i < splitFilename.length - 1; ++i)
            {
                path += splitFilename[i] + ((i < splitFilename.length - 2) ? "/" : "");
            }

            name = splitFilename[splitFilename.length - 1].split("\\.")[0];

            Logger.logI("checking for ibo file at " + context.getExternalFilesDir(null).toString()
                        + "/genAssets/" + path + "/" + name + ".ibo", 3);

            File file = new File(context.getExternalFilesDir(null).toString()
                                 + "/genAssets/" + path + "/" + name + ".ibo");

            if (file.exists())
            {
                Logger.logI("found ibo file for model <" + filename + ">", 3);

                readIBO(context,
                        context.getExternalFilesDir(null).toString()
                        + "/genAssets/" + path + "/" + name + ".ibo",
                        vertices,
                        texCoordinatesInVertexOrder,
                        normalsInVertexOrder,
                        vertexIndices);

                Logger.logI(
                    "finished parsing file in " + ((float) timer.getTime()) / 1000f + " seconds",
                    3);
            }
            else
            {
                Logger.logI("no ibo file for model <" + filename + ">, constructing from obj", 3);

                BufferedReader br =
                    new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

                Logger.logI("opened file", 4);

                ArrayList<Float> normals        = new ArrayList<>();
                ArrayList<Float> texCoordinates = new ArrayList<>();

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
                    "finished parsing file in " + ((float) timer.getTime()) / 1000f + " seconds",
                    3);

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
                                                                texCoordinatesByVertexIndex.get(
                                                                    index)[i]);
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

                Logger.logI("indices: " + vertexIndices.size(), 4);
                Logger.logI("vertices: " + vertices.size() / VERTEX_SIZE, 4);
                Logger.logI(
                    "texCoordinates: " + texCoordinatesInVertexOrder.size() / TEX_COORDINATE_SIZE,
                    4);
                Logger.logI("normals: " + normalsInVertexOrder.size() / NORMAL_SIZE, 4);

                Logger.logI("writing model ibo file", 3);

                writeIBO(context,
                         path,
                         name,
                         vertices,
                         texCoordinatesInVertexOrder,
                         normalsInVertexOrder,
                         vertexIndices);
            }

            Logger.logI("making opengl object", 4);

            Timer openGLTimer = new Timer();
            openGLTimer.start();

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

    public static void readIBO(final Context context,
                               final String filename,
                               final ArrayList<Float> vertices,
                               final ArrayList<Float> texCoords,
                               final ArrayList<Float> normals,
                               final ArrayList<Short> indices)
    {
        try
        {
            File ibo = new File(filename);

            FileInputStream fis = new FileInputStream(ibo);

            byte[] vCountBuffer = new byte[INT_BYTES];
            fis.read(vCountBuffer);
            int        vCount  = ByteBuffer.wrap(vCountBuffer).getInt();

            Logger.logD("expected vertex coords:<" + vCount + ">", 4);

            ByteBuffer vBuffer = ByteBuffer.allocate(vCount * FLOAT_BYTES);
            fis.read(vBuffer.array(), 0, vCount * FLOAT_BYTES);
            for (int i = 0; i < vCount; ++i)
            {
                vertices.add(vBuffer.getFloat(i * FLOAT_BYTES));
                //Logger.logD("vertex coord:<" + vBuffer.getFloat(i * FLOAT_BYTES) + ">");
            }

            byte[] vtCountBuffer = new byte[INT_BYTES];
            fis.read(vtCountBuffer);
            int        vtCount  = ByteBuffer.wrap(vtCountBuffer).getInt();

            Logger.logD("expected texture coords:<" + vtCount + ">", 4);

            ByteBuffer vtBuffer = ByteBuffer.allocate(vtCount * FLOAT_BYTES);
            fis.read(vtBuffer.array(), 0, vtCount * FLOAT_BYTES);
            for (int i = 0; i < vtCount; ++i)
            {
                texCoords.add(vtBuffer.getFloat(i * FLOAT_BYTES));
                //Logger.logD("texture coord:<" + vtBuffer.getFloat(i * FLOAT_BYTES) + ">");
            }

            byte[] vnCountBuffer = new byte[INT_BYTES];
            fis.read(vnCountBuffer);
            int        vnCount  = ByteBuffer.wrap(vnCountBuffer).getInt();

            Logger.logD("expected normals coords:<" + vnCount + ">", 4);

            ByteBuffer vnBuffer = ByteBuffer.allocate(vnCount * FLOAT_BYTES);
            fis.read(vnBuffer.array(), 0, vnCount * FLOAT_BYTES);
            for (int i = 0; i < vnCount; ++i)
            {
                normals.add(vnBuffer.getFloat(i * FLOAT_BYTES));
                //Logger.logD("normal coord:<" + vnBuffer.getFloat(i * FLOAT_BYTES) + ">");
            }

            byte[] viCountBuffer = new byte[4];
            fis.read(viCountBuffer);
            int        viCount  = ByteBuffer.wrap(viCountBuffer).getInt();

            Logger.logD("expected indices:<" + viCount + ">", 4);

            ByteBuffer viBuffer = ByteBuffer.allocate(viCount * FLOAT_BYTES);
            fis.read(viBuffer.array(), 0, viCount * SHORT_BYTES);
            for (int i = 0; i < viCount; ++i)
            {
                indices.add(viBuffer.getShort(i * SHORT_BYTES));
                //Logger.logD("index:<" + viBuffer.getShort(i * SHORT_BYTES) + ">");
            }

            fis.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void writeIBO(final Context context,
                                final String path,
                                final String name,
                                final ArrayList<Float> vertices,
                                final ArrayList<Float> texCoords,
                                final ArrayList<Float> normals,
                                final ArrayList<Short> indices)
    {
        try
        {
            Logger.logI(
                "writing ibo to "
                + context.getExternalFilesDir(null).toString()
                + "/genAssets/" + path + name +
                ".ibo");

            File directory = new File(""
                                      + context.getExternalFilesDir(null).toString()
                                      + "/genAssets/" + path);
            if (!directory.isDirectory())
            {
                if (!directory.mkdirs())
                {
                    Logger.logI("failed to create directory "
                                + context.getExternalFilesDir(null).toString()
                                + "/genAssets/" + path);
                    return;
                }
                else
                {
                    Logger.logI(
                        "created directory "
                        + context.getExternalFilesDir(null).toString()
                        + "/genAssets/" +
                        path);
                }
            }
            else
            {
                Logger.logI(
                    "directory "
                    + context.getFilesDir().toString()
                    + "/genAssets/" + path +
                    " already exists");
            }

            if (!directory.setReadable(true))
            {
                Logger.logI("failed to make directory " + name + ".ibo readable");
            }

            File ibo = new File(directory, name + ".ibo");
            if (!ibo.exists())
            {
                if (!ibo.createNewFile())
                {
                    Logger.logI("failed to create file " + name + ".ibo");
                    return;
                }
                else
                {
                    Logger.logI("created file " + name + ".ibo");
                }
            }

            if (!ibo.setReadable(true))
            {
                Logger.logI("failed to make file " + name + ".ibo readable");
            }

            FileOutputStream fos = new FileOutputStream(ibo);

            Logger.logD("putting vertex buffer size:<" + vertices.size() + ">", 4);

            fos.write(ByteBuffer.allocate(INT_BYTES).putInt(vertices.size()).array());
            ByteBuffer vBuffer = ByteBuffer.allocate(vertices.size() * FLOAT_BYTES);
            for (float v : vertices)
            {
                vBuffer.putFloat(v);
            }
            fos.write(vBuffer.array());

            Logger.logD("putting texCoord buffer size:<" + texCoords.size() + ">", 4);

            fos.write(ByteBuffer.allocate(INT_BYTES).putInt(texCoords.size()).array());
            ByteBuffer vtBuffer = ByteBuffer.allocate(texCoords.size() * FLOAT_BYTES);
            for (float vt : texCoords)
            {
                vtBuffer.putFloat(vt);
            }
            fos.write(vtBuffer.array());

            Logger.logD("putting normal buffer size:<" + normals.size() + ">", 4);

            fos.write(ByteBuffer.allocate(INT_BYTES).putInt(normals.size()).array());
            ByteBuffer vnBuffer = ByteBuffer.allocate(normals.size() * FLOAT_BYTES);
            for (float vn : normals)
            {
                vnBuffer.putFloat(vn);
            }
            fos.write(vnBuffer.array());

            Logger.logD("putting index buffer size:<" + indices.size() + ">", 4);

            fos.write(ByteBuffer.allocate(INT_BYTES).putInt(indices.size()).array());
            ByteBuffer viBuffer = ByteBuffer.allocate(indices.size() * SHORT_BYTES);
            for (short vi : indices)
            {
                viBuffer.putShort(vi);
            }
            fos.write(viBuffer.array());

            fos.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
