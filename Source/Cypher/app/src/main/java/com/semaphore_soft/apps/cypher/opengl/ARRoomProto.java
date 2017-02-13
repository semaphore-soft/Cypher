package com.semaphore_soft.apps.cypher.opengl;

import android.opengl.GLES10;

import com.semaphore_soft.apps.cypher.game.Room;

import org.artoolkit.ar.base.rendering.RenderUtils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Scorple on 1/27/2017.
 */

public class ARRoomProto
{
    public static final int NUM_INDICES = 18;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;
    private ShortBuffer mIndexBuffer;

    private ArrayList<Long> characters;

    public ARRoomProto()
    {
        this(1.0F);
    }

    public ARRoomProto(float size)
    {
        this(size, 0.0F, 0.0F, 0.0F);
    }

    public ARRoomProto(float size, float x, float y, float z)
    {
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

        float vertices[] = {
            x - hs, y + hs, z,
            x, y + (hs * 2), z,
            x + hs, y + hs, z,
            x + (hs * 2), y, z,
            x + hs, y - hs, z,
            x, y - (hs * 2), z,
            x - hs, y - hs, z,
            x - (hs * 2), y, z,
        };

        float c = 1.0f;
        float colors[] = {
            c, c, c, c,
            c, c, c, c,
            c, c, c, c,
            c, c, c, c,
            c, c, c, c,
            c, c, c, c,
            c, c, c, c,
            c, c, c, c,
        };

        short indices[] = {
            0, 1, 2,
            2, 3, 4,
            4, 5, 6,
            6, 7, 0,
            0, 2, 4,
            0, 4, 6
        };

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

    public void setWall(short wall, Room.E_WALL_TYPE type)
    {
        float c = 1.0f;

        int[]   toChange;
        float[] color;

        toChange = new int[]{
            4 + (8 * wall),
            5 + (8 * wall),
            6 + (8 * wall),
            7 + (8 * wall),
        };

        color = new float[4];

        switch (type)
        {
            case NO_DOOR:
                color = new float[]{
                    0, 0, 0, c, //black
                };
                break;
            case DOOR_UNLOCKED:
                color = new float[]{
                    0, c, 0, c, //green
                };
                break;
            case DOOR_OPEN:
                color = new float[]{
                    c, c, c, c, //white
                };
                break;
            case DOOR_LOCKED:
                color = new float[]{
                    c, 0, 0, c, //red
                };
                break;
            default:
                break;
        }

        float colors[] = new float[getmColorBuffer().capacity()];
        getmColorBuffer().get(colors);

        for (Integer i : toChange)
        {
            colors[i] = color[i % 4];
        }

        mColorBuffer = RenderUtils.buildFloatBuffer(colors);
    }
}
