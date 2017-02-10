package com.semaphore_soft.apps.cypher.opengl;

import android.content.Context;
import android.opengl.GLES10;

import org.artoolkit.ar.base.rendering.RenderUtils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by ceroj on 2/1/2017.
 */

public class ARModel
{
    public static final int NUM_INDICES = 18;
    private int vectorSize;
    private int character;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;
    private ShortBuffer mIndexBuffer;
    private ModelLoader file = null;
    String  filename;
    Context context;
    private ArrayList<Long> characters = null;

    /*
    public ARModel()
    {
        this(1.0F);
    }

    public ARModel(String filename)
    {
        this(1.0F);
        file.setName(filename);
    }

    public ARModel(float size)
    {
        this(size, 0.0F, 0.0F, 0.0F);
    }

    public ARModel(float size, float x, float y, float z)
    {
        this.setArrays(size, x, y, z);

        characters = new ArrayList<>();
    }
    */
    public ARModel(float size, float x, float y, float z, String filename, Context context)
    {
        this.filename = filename;
        this.context = context;
        file = new ModelLoader(filename, context);
        this.setArrays(size, x, y, z);

        characters = new ArrayList<>();
    }

    public FloatBuffer getmVertexBuffer()
    {
        return this.mVertexBuffer;
    }

    public FloatBuffer getmColorBuffer()
    {
        return this.mColorBuffer;
    }

    public ShortBuffer getmIndexBuffer()
    {
        return this.mIndexBuffer;
    }

    private void setArrays(float size, float x, float y, float z)
    {
        float hs = size / 2.0f;

        vectorSize = file.getVerts().size();
        int           i          = 0;
        Vector<Float> vecVert    = file.getVerts();
        float         vertices[] = new float[vectorSize];
        while (i < vectorSize)
        {
            vertices[i] = hs * vecVert.elementAt(i);
            i++;
        }

        float c        = 1.0f;
        float colors[] = new float[vectorSize * 4];
        i = 0;
        while (i < vectorSize * 4)
        {
            colors[i] = c;
            i++;
        }

        i = 0;
        Vector<Short> vecInd      = file.getIndices();
        int           indicesSize = file.getIndices().size();
        short         indices[]   = new short[indicesSize];
        while (i < indicesSize)
        {
            indices[i] = vecInd.elementAt(i);
            i++;
        }

        mVertexBuffer = RenderUtils.buildFloatBuffer(vertices);
        mColorBuffer = RenderUtils.buildFloatBuffer(colors);
        mIndexBuffer = RenderUtils.buildShortBuffer(indices);
    }

    public void draw(GL10 unused)
    {
        GLES10.glColorPointer(4, GLES10.GL_FLOAT, 0, mColorBuffer);
        GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, mVertexBuffer);

        GLES10.glEnableClientState(GLES10.GL_COLOR_ARRAY);
        GLES10.glEnableClientState(GLES10.GL_VERTEX_ARRAY);

        GLES10.glDrawElements(GLES10.GL_TRIANGLES,
                              NUM_INDICES, GLES10.GL_UNSIGNED_BYTE, mIndexBuffer);

        GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);
        GLES10.glDisableClientState(GLES10.GL_VERTEX_ARRAY);

    }

    public void addCharacter(long character)
    {
        if (!characters.contains(character))
        {
            characters.add(character);
        }
    }

    public void removeCharacter(long character)
    {
        if (characters.contains(character))
        {
            characters.remove(character);
        }
    }

    public void setCharacter(int character)
    {
        this.character = character;

        float c = 1.0f;

        float colors[] = new float[vectorSize * 4];

        switch (character)
        {
            case 0:
                for (int i = 0; i < vectorSize * 4; i += 4)
                {
                    colors[i] = c;
                    colors[i + 1] = c;
                    colors[i + 2] = c;
                    colors[i + 3] = c;
                }
                break;
            case 1:
                for (int i = 0; i < vectorSize * 4; i += 4)
                {
                    colors[i] = c;
                    colors[i + 1] = 0;
                    colors[i + 2] = 0;
                    colors[i + 3] = c;
                }
                break;
            case 2:
                for (int i = 0; i < vectorSize * 4; i += 4)
                {
                    colors[i] = 0;
                    colors[i + 1] = c;
                    colors[i + 2] = 0;
                    colors[i + 3] = c;
                }
                break;
            case 3:
                for (int i = 0; i < vectorSize * 4; i += 4)
                {
                    colors[i] = c;
                    colors[i + 1] = 0;
                    colors[i + 2] = c;
                    colors[i + 3] = c;
                }
                break;
        }

        mColorBuffer = RenderUtils.buildFloatBuffer(colors);
    }

    public void setName(String filename)
    {
        this.filename = filename;
    }

    public int getNumIndices()
    {
        return file.getIndices().size();
    }
}
