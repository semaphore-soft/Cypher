package com.semaphore_soft.apps.cypher.opengl;

import android.opengl.Matrix;

import com.semaphore_soft.apps.cypher.opengl.shader.DynamicShaderProgram;

import java.util.ConcurrentModificationException;
import java.util.Hashtable;

/**
 * Created by rickm on 2/10/2017.
 */

public class ARRoom implements ARDrawableGLES20
{
    ARDrawableGLES20 roomModel;
    ARDrawableGLES20 walls[]       = {null, null, null, null};
    int              forwardPlayer = -1;
    int              forwardEnemy  = -1;
    Hashtable<Integer, ARDrawableGLES20> playerLine;
    Hashtable<Integer, ARDrawableGLES20> enemyLine;
    Hashtable<Integer, ARDrawableGLES20> entityPile;

    public ARRoom()
    {
        playerLine = new Hashtable<>();
        enemyLine = new Hashtable<>();
        entityPile = new Hashtable<>();
    }

    public void setRoomModel(ARDrawableGLES20 roomModel)
    {
        this.roomModel = roomModel;
    }

    public void setWall(int index, ARDrawableGLES20 door)
    {
        walls[index] = door;
    }

    public void addPlayer(int id, ARDrawableGLES20 playerModel)
    {
        if (!playerLine.keySet().contains(id))
        {
            playerLine.put(id, playerModel);
        }
    }

    public void removePlayer(int id)
    {
        if (playerLine.keySet().contains(id))
        {
            playerLine.remove(id);
        }
    }

    public void addEnemy(int id, ARDrawableGLES20 enemyModel)
    {
        if (!enemyLine.keySet().contains(id))
        {
            enemyLine.put(id, enemyModel);
        }
    }

    public void removeEnemy(int id)
    {
        if (enemyLine.keySet().contains(id))
        {
            enemyLine.remove(id);
        }
    }

    public void removeActors()
    {
        playerLine.clear();
        enemyLine.clear();
    }

    public void addEntity(int id, ARDrawableGLES20 entityModel)
    {
        if (!entityPile.keySet().contains(id))
        {
            entityPile.put(id, entityModel);
        }
    }

    public void removeEntity(int id)
    {
        if (entityPile.keySet().contains(id))
        {
            entityPile.remove(id);
        }
    }

    public void setForwardPlayer(int id)
    {
        forwardPlayer = id;
    }

    public void clearForwardPlayer()
    {
        setForwardPlayer(-1);
    }

    public void setForwardEnemy(int id)
    {
        forwardEnemy = id;
    }

    public void clearForwardEnemy()
    {
        setForwardEnemy(-1);
    }

    public void setRoomPose(String pose)
    {
        if (roomModel instanceof ARPoseModel)
        {
            ((ARPoseModel) roomModel).setPose(pose);
        }
    }

    public void setResidentPose(int id, String pose)
    {
        for (int playerId : playerLine.keySet())
        {
            if (playerId == id)
            {
                if (playerLine.get(id) instanceof ARPoseModel)
                {
                    ((ARPoseModel) playerLine.get(id)).setPose(pose);
                }
                return;
            }
        }
        for (int enemyId : enemyLine.keySet())
        {
            if (enemyId == id)
            {
                if (enemyLine.get(id) instanceof ARPoseModel)
                {
                    ((ARPoseModel) enemyLine.get(id)).setPose(pose);
                }
                return;
            }
        }
    }

    public void setEntityPose(int id, String pose)
    {
        for (int entityId : entityPile.keySet())
        {
            if (entityId == id)
            {
                if (entityPile.get(id) instanceof ARPoseModel)
                {
                    ((ARPoseModel) entityPile.get(id)).setPose(pose);
                }
                return;
            }
        }
    }

    public void draw(float[] projectionMatrix, float[] modelViewMatrix)
    {
        float[] lightPos             = new float[3];
        float[] transformationMatrix = new float[16];
        System.arraycopy(modelViewMatrix, 0, transformationMatrix, 0, 16);
        Matrix.translateM(transformationMatrix, 0, 0.0f, 0.0f, 80.0f);
        for (int i = 0; i < 3; ++i)
        {
            lightPos[i] = transformationMatrix[i + 12];
        }
        draw(projectionMatrix, modelViewMatrix, lightPos);
    }

    public void draw(float[] projectionMatrix, float[] modelViewMatrix, float[] lightPos)
    {
        try
        {
            if (roomModel != null)
            {
                roomModel.draw(projectionMatrix, modelViewMatrix, lightPos);
            }

            for (int i = 0; i < 4; ++i)
            {
                if (walls[i] != null)
                {
                    float[] transformationMatrix = new float[16];
                    System.arraycopy(modelViewMatrix, 0, transformationMatrix, 0, 16);
                    Matrix.rotateM(transformationMatrix,
                                   0,
                                   i * -90.0f,
                                   0.0f,
                                   0.0f,
                                   1.0f);
                    walls[i].draw(projectionMatrix, transformationMatrix, lightPos);
                }
            }

            int   i          = 0;
            float spread     = (playerLine.size() - 1) * 60.0f;
            float lineOffset = -(spread / 2.0f);
            for (int id : playerLine.keySet())
            {
                float[] transformationMatrix = new float[16];
                System.arraycopy(modelViewMatrix, 0, transformationMatrix, 0, 16);
                if (forwardPlayer == id)
                {
                    Matrix.translateM(transformationMatrix, 0, 0.0f, -40.0f, 0.0f);
                }
                else
                {
                    float actorOffset = lineOffset + (60.0f * i);
                    Matrix.translateM(transformationMatrix, 0, actorOffset, -80.0f, 0.0f);
                    Matrix.rotateM(transformationMatrix, 0, 180.0f, 0.0f, 0.0f, 1.0f);
                }
                playerLine.get(id).draw(projectionMatrix, transformationMatrix, lightPos);
                ++i;
            }

            i = 0;
            spread = (enemyLine.size() - 1) * 60.0f;
            lineOffset = -(spread / 2.0f);
            for (int id : enemyLine.keySet())
            {
                float[] transformationMatrix = new float[16];
                System.arraycopy(modelViewMatrix, 0, transformationMatrix, 0, 16);
                if (forwardEnemy == id)
                {
                    Matrix.translateM(transformationMatrix, 0, 0.0f, 40.0f, 0.0f);
                }
                else
                {
                    float actorOffset = lineOffset + (60.0f * i);
                    Matrix.translateM(transformationMatrix, 0, actorOffset, 80.0f, 0.0f);
                }
                enemyLine.get(id).draw(projectionMatrix, transformationMatrix, lightPos);
                ++i;
            }

            for (int id : entityPile.keySet())
            {
                float[] transformationMatrix = new float[16];
                System.arraycopy(modelViewMatrix, 0, transformationMatrix, 0, 16);
                Matrix.translateM(transformationMatrix, 0, 0.0f, 0.0f, 0.0f);
                entityPile.get(id).draw(projectionMatrix, transformationMatrix, lightPos);
            }
        }
        catch (ConcurrentModificationException x)
        {

        }
    }

    public void setShaderProgram(DynamicShaderProgram shaderProgram)
    {

    }

    public void setColor(float r, float g, float b, float a)
    {

    }
}
