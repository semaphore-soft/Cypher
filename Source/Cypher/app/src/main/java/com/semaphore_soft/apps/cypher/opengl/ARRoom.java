package com.semaphore_soft.apps.cypher.opengl;

import android.opengl.Matrix;

import org.artoolkit.ar.base.rendering.gles20.ARDrawableOpenGLES20;

import java.util.Hashtable;

/**
 * Created by rickm on 2/10/2017.
 */

public class ARRoom
{
    int markerID = -1;
    ARDrawableOpenGLES20                  roomModel;
    Hashtable<Long, ARDrawableOpenGLES20> playerLine;
    Hashtable<Long, ARDrawableOpenGLES20> enemyLine;
    Hashtable<Long, ARDrawableOpenGLES20> entityPile;

    public ARRoom()
    {
        playerLine = new Hashtable<>();
        enemyLine = new Hashtable<>();
        entityPile = new Hashtable<>();
    }

    public void setRoomModel(ARDrawableOpenGLES20 roomModel)
    {
        this.roomModel = roomModel;
    }

    public ARRoomProto getRoomModelAsRoomProto()
    {
        if (roomModel instanceof ARRoomProto)
        {
            return (ARRoomProto) roomModel;
        }
        return null;
    }

    public void addPlayer(long id, ARDrawableOpenGLES20 playerModel)
    {
        if (!playerLine.keySet().contains(id))
        {
            playerLine.put(id, playerModel);
        }
    }

    public void removePlayer(long id)
    {
        if (playerLine.keySet().contains(id))
        {
            playerLine.remove(id);
        }
    }

    public void addEnemy(long id, ARDrawableOpenGLES20 enemyModel)
    {
        if (!enemyLine.keySet().contains(id))
        {
            enemyLine.put(id, enemyModel);
        }
    }

    public void removeEnemy(long id)
    {
        if (enemyLine.keySet().contains(id))
        {
            enemyLine.remove(id);
        }
    }

    public void removeActors()
    {
        for (Long id : playerLine.keySet())
        {
            playerLine.remove(id);
        }
        for (Long id : enemyLine.keySet())
        {
            enemyLine.remove(id);
        }
    }

    public void addEntity(long id, ARDrawableOpenGLES20 entityModel)
    {
        if (!entityPile.keySet().contains(id))
        {
            entityPile.put(id, entityModel);
        }
    }

    public void removeEntity(long id)
    {
        if (entityPile.keySet().contains(id))
        {
            entityPile.remove(id);
        }
    }

    public void draw(float[] projectionMatrix, float[] modelViewMatrix)
    {
        if (roomModel != null)
        {
            roomModel.draw(projectionMatrix, modelViewMatrix);
        }

        for (Long id : playerLine.keySet())
        {
            float[] transformationMatrix = new float[16];
            System.arraycopy(modelViewMatrix, 0, transformationMatrix, 0, 16);
            ARDrawableOpenGLES20 playerModel = playerLine.get(id);
            Matrix.translateM(transformationMatrix, 0, -30.0f, 0.0f, 0.0f);
            Matrix.rotateM(transformationMatrix, 0, 0.0f, 0.0f, 0.0f, (float) Math.PI);
            playerModel.draw(projectionMatrix, transformationMatrix);
        }

        for (Long id : enemyLine.keySet())
        {
            float[] transformationMatrix = new float[16];
            System.arraycopy(modelViewMatrix, 0, transformationMatrix, 0, 16);
            ARDrawableOpenGLES20 enemyModel = enemyLine.get(id);
            Matrix.translateM(transformationMatrix, 0, 30.0f, 0.0f, 0.0f);
            enemyModel.draw(projectionMatrix, transformationMatrix);
        }

        for (Long id : entityPile.keySet())
        {
            float[] transformationMatrix = new float[16];
            System.arraycopy(modelViewMatrix, 0, transformationMatrix, 0, 16);
            ARDrawableOpenGLES20 entityModel = entityPile.get(id);
            Matrix.translateM(transformationMatrix, 0, 0.0f, 0.0f, 0.0f);
            entityModel.draw(projectionMatrix, transformationMatrix);
        }
    }
}
