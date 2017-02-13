package com.semaphore_soft.apps.cypher.opengl;

import android.opengl.GLES10;

import org.artoolkit.ar.base.rendering.RenderUtils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by ceroj on 2/1/2017.
 */

public class ARModel
{
    public static int NUM_VERTICES;
    public static int NUM_INDICES;

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private FloatBuffer normalBuffer;
    private FloatBuffer texCoordinateBuffer;
    private ShortBuffer vertexIndexBuffer;
    private ShortBuffer texCoordinateIndexBuffer;
    private ShortBuffer normalIndexBuffer;

    protected boolean textured = false;
    private int textureHandle;

    private float size;

    public ARModel()
    {
        this(40.0f);
    }

    public ARModel(float size)
    {
        this.size = size;
    }

    public FloatBuffer getVertexBuffer()
    {
        return this.vertexBuffer;
    }

    public FloatBuffer getColorBuffer()
    {
        return this.colorBuffer;
    }

    public FloatBuffer getNormalBuffer()
    {
        return normalBuffer;
    }

    public FloatBuffer getTexCoordinateBuffer()
    {
        return texCoordinateBuffer;
    }

    public ShortBuffer getVertexIndexBuffer()
    {
        return this.vertexIndexBuffer;
    }

    public void makeVertexBuffer(ArrayList<Float> vertices)
    {
        float[] vertexArray = new float[vertices.size()];

        float sizeModifier = size / 2.0f;

        for (int i = 0; i < vertexArray.length; ++i)
        {
            vertexArray[i] = vertices.get(i) * sizeModifier;
        }

        vertexBuffer = RenderUtils.buildFloatBuffer(vertexArray);

        NUM_VERTICES = vertexArray.length;
    }

    public void makeColorBuffer()
    {
        float[] colorArray = new float[NUM_VERTICES * 4];

        float c = 1.0f;

        for (int i = 0; i < NUM_VERTICES * 4; i += 4)
        {
            colorArray[i] = c;
            colorArray[i + 1] = c;
            colorArray[i + 2] = c;
            colorArray[i + 3] = c;
        }

        colorBuffer = RenderUtils.buildFloatBuffer(colorArray);
    }

    public void makeNormalBuffer(ArrayList<Float> normals)
    {
        float[] normalArray = new float[normals.size()];

        for (int i = 0; i < normalArray.length; ++i)
        {
            normalArray[i] = normals.get(i);
        }

        normalBuffer = RenderUtils.buildFloatBuffer(normalArray);
    }

    public void makeTexCoordinateBuffer(ArrayList<Float> texCoordinates)
    {
        float[] texCoordinateArray = new float[texCoordinates.size()];

        for (int i = 0; i < texCoordinateArray.length; ++i)
        {
            texCoordinateArray[i] = texCoordinates.get(i);
        }

        texCoordinateBuffer = RenderUtils.buildFloatBuffer(texCoordinateArray);

        textured = true;
    }

    public void makeVertexIndexBuffer(ArrayList<Short> indices)
    {
        short[] indexArray = new short[indices.size()];

        for (int i = 0; i < indexArray.length; ++i)
        {
            indexArray[i] = indices.get(i);
        }

        vertexIndexBuffer = RenderUtils.buildShortBuffer(indexArray);

        NUM_INDICES = indexArray.length;
    }

    public void makeTexCoordinateIndexBuffer(ArrayList<Short> indices)
    {
        short[] indexArray = new short[indices.size()];

        for (int i = 0; i < indexArray.length; ++i)
        {
            indexArray[i] = indices.get(i);
        }

        texCoordinateIndexBuffer = RenderUtils.buildShortBuffer(indexArray);
    }

    public void makeNormalIndexBuffer(ArrayList<Short> indices)
    {
        short[] indexArray = new short[indices.size()];

        for (int i = 0; i < indexArray.length; ++i)
        {
            indexArray[i] = indices.get(i);
        }

        normalIndexBuffer = RenderUtils.buildShortBuffer(indexArray);
    }

    public void setTextureHandle(int textureHandle)
    {
        this.textureHandle = textureHandle;
    }

    public int getTextureHandle()
    {
        return textureHandle;
    }

    public void draw(GL10 unused)
    {
        GLES10.glColorPointer(4, GLES10.GL_FLOAT, 0, colorBuffer);
        GLES10.glVertexPointer(3, GLES10.GL_FLOAT, 0, vertexBuffer);

        GLES10.glEnableClientState(GLES10.GL_COLOR_ARRAY);
        GLES10.glEnableClientState(GLES10.GL_VERTEX_ARRAY);

        GLES10.glDrawElements(GLES10.GL_TRIANGLES,
                              NUM_INDICES, GLES10.GL_UNSIGNED_BYTE, vertexIndexBuffer);

        GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);
        GLES10.glDisableClientState(GLES10.GL_VERTEX_ARRAY);
    }

    public void setColor(float r, float g, float b, float o)
    {
        float colors[] = new float[NUM_VERTICES * 4];

        for (int i = 0; i < NUM_VERTICES * 4; i += 4)
        {
            colors[i] = r;
            colors[i + 1] = g;
            colors[i + 2] = b;
            colors[i + 3] = o;
        }

        colorBuffer = RenderUtils.buildFloatBuffer(colors);
    }

    public void setCharacter(int character)
    {
        float c = 1.0f;

        switch (character)
        {
            case 0:
                setColor(c, c, c, c);
                break;
            case 1:
                setColor(c, 0, 0, c);
                break;
            case 2:
                setColor(0, c, 0, c);
                break;
            case 3:
                setColor(c, 0, c, c);
                break;
        }
    }

    public int getNumIndices()
    {
        return NUM_INDICES;
    }
}
