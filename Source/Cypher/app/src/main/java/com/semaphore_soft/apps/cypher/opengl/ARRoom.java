package com.semaphore_soft.apps.cypher.opengl;

import android.opengl.Matrix;

import java.util.Hashtable;

/**
 * Created by rickm on 2/10/2017.
 */

public class ARRoom
{
    ARModelGLES20                  roomModel;
    Hashtable<Long, ARModelGLES20> playerLine;
    Hashtable<Long, ARModelGLES20> enemyLine;
    Hashtable<Long, ARModelGLES20> entityPile;

    ARRoom()
    {
        playerLine = new Hashtable<>();
        enemyLine = new Hashtable<>();
        entityPile = new Hashtable<>();
    }

    public void setRoomModel(ARModelGLES20 roomModel)
    {
        this.roomModel = roomModel;
    }

    public void addPlayer(long id, ARModelGLES20 playerModel)
    {
        playerLine.put(id, playerModel);
    }

    public void addEnemy(long id, ARModelGLES20 enemyModel)
    {
        enemyLine.put(id, enemyModel);
    }

    public void addEntity(long id, ARModelGLES20 entityModel)
    {
        entityPile.put(id, entityModel);
    }

    public void draw(float[] projectionMatrix, float[] modelViewMatrix)
    {
        roomModel.draw(projectionMatrix, modelViewMatrix);

        for (Long id : playerLine.keySet())
        {
            float[] transformationMatrix = new float[16];
            System.arraycopy(modelViewMatrix, 0, transformationMatrix, 0, 16);
            ARModelGLES20 playerModel = playerLine.get(id);
            Matrix.translateM(transformationMatrix, 0, -30.0f, 0.0f, 0.0f);
            Matrix.rotateM(transformationMatrix, 0, 0.0f, 0.0f, 0.0f, (float) Math.PI);
            playerModel.draw(projectionMatrix, transformationMatrix);
        }

        for (Long id : enemyLine.keySet())
        {
            float[] transformationMatrix = new float[16];
            System.arraycopy(modelViewMatrix, 0, transformationMatrix, 0, 16);
            ARModelGLES20 enemyModel = enemyLine.get(id);
            Matrix.translateM(transformationMatrix, 0, 30.0f, 0.0f, 0.0f);
            enemyModel.draw(projectionMatrix, transformationMatrix);
        }

        for (Long id : entityPile.keySet())
        {
            float[] transformationMatrix = new float[16];
            System.arraycopy(modelViewMatrix, 0, transformationMatrix, 0, 16);
            ARModelGLES20 entityModel = entityPile.get(id);
            Matrix.translateM(transformationMatrix, 0, 0.0f, 0.0f, 0.0f);
            entityModel.draw(projectionMatrix, transformationMatrix);
        }
    }
}
