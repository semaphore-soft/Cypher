package com.semaphore_soft.apps.cypher.opengl;

import android.opengl.Matrix;

import com.semaphore_soft.apps.cypher.opengl.shader.DynamicShaderProgram;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Created by rickm on 2/10/2017.
 */

public class ARRoom implements ARDrawableGLES20
{
    private ARDrawableGLES20 roomModel;
    private ARDrawableGLES20 walls[]       = {null, null, null, null};
    private int              forwardPlayer = -1;
    private int              forwardEnemy  = -1;
    private ConcurrentHashMap<Integer, ARDrawableGLES20> playerLine;
    private ConcurrentHashMap<Integer, ARDrawableGLES20> enemyLine;
    private ConcurrentHashMap<Integer, ARDrawableGLES20> entityPile;
    private ConcurrentHashMap<Integer, ARDrawableGLES20> effects;
    private short alignment = 2;

    private Semaphore assetAccess;

    public ARRoom()
    {
        playerLine = new ConcurrentHashMap<>();
        enemyLine = new ConcurrentHashMap<>();
        entityPile = new ConcurrentHashMap<>();
        effects = new ConcurrentHashMap<>();

        assetAccess = new Semaphore(1);
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
        try
        {
            assetAccess.acquire();

            playerLine.clear();
            enemyLine.clear();

            assetAccess.release();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
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

    /**
     * Applies an effect to the {@link com.semaphore_soft.apps.cypher.game.Actor Actor}
     * specified by {@code id}.
     *
     * @param id    ID of the {@link com.semaphore_soft.apps.cypher.game.Actor Actor} to apply
     *              the effect to
     * @param model The effect texture to overlay on the {@link com.semaphore_soft.apps.cypher.game.Actor Actor}
     */
    public void addEffect(int id, ARDrawableGLES20 model)
    {
        if (!effects.containsKey(id))
        {
            effects.put(id, model);
        }
    }

    /**
     * Remove the effect that has been applied to the {@link com.semaphore_soft.apps.cypher.game.Actor Actor}
     * specified by {@code id}.
     *
     * @param id ID of the {@link com.semaphore_soft.apps.cypher.game.Actor Actor}
     *           to remove the effect from
     */
    public void removeEffect(int id)
    {
        if (effects.containsKey(id))
        {
            effects.remove(id);
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

    public void setAlignment(short side)
    {
        alignment = side;
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
        if (assetAccess.tryAcquire())
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

                switch (alignment)
                {
                    case 0:
                        if (forwardPlayer == id)
                        {
                            Matrix.translateM(transformationMatrix, 0, 0.0f, 40.0f, 0.0f);
                        }
                        else
                        {
                            float actorOffset = lineOffset + (60.0f * i);
                            Matrix.translateM(transformationMatrix, 0, actorOffset, 80.0f, 0.0f);
                        }
                        //Matrix.rotateM(transformationMatrix, 0, 0.0f, 0.0f, 0.0f, 1.0f);
                        break;
                    case 1:
                        if (forwardPlayer == id)
                        {
                            Matrix.translateM(transformationMatrix, 0, 40.0f, 0.0f, 0.0f);
                        }
                        else
                        {
                            float actorOffset = lineOffset + (60.0f * i);
                            Matrix.translateM(transformationMatrix, 0, 80.0f, actorOffset, 0.0f);
                        }
                        Matrix.rotateM(transformationMatrix, 0, 270.0f, 0.0f, 0.0f, 1.0f);
                        break;
                    case 2:
                        if (forwardPlayer == id)
                        {
                            Matrix.translateM(transformationMatrix, 0, 0.0f, -40.0f, 0.0f);
                        }
                        else
                        {
                            float actorOffset = lineOffset + (60.0f * i);
                            Matrix.translateM(transformationMatrix, 0, actorOffset, -80.0f, 0.0f);
                        }
                        Matrix.rotateM(transformationMatrix, 0, 180.0f, 0.0f, 0.0f, 1.0f);
                        break;
                    case 3:
                        if (forwardPlayer == id)
                        {
                            Matrix.translateM(transformationMatrix, 0, -40.0f, 0.0f, 0.0f);
                        }
                        else
                        {
                            float actorOffset = lineOffset + (60.0f * i);
                            Matrix.translateM(transformationMatrix, 0, -80.0f, actorOffset, 0.0f);
                        }
                        Matrix.rotateM(transformationMatrix, 0, 90.0f, 0.0f, 0.0f, 1.0f);
                        break;
                }

                playerLine.get(id).draw(projectionMatrix, transformationMatrix, lightPos);
                if (effects.containsKey(id))
                {
                    // Plane will appear in front of the enemy
                    effects.get(id).draw(projectionMatrix, transformationMatrix, lightPos);
                }
                ++i;
            }

            i = 0;
            spread = (enemyLine.size() - 1) * 60.0f;
            lineOffset = -(spread / 2.0f);
            for (int id : enemyLine.keySet())
            {
                float[] transformationMatrix = new float[16];
                System.arraycopy(modelViewMatrix, 0, transformationMatrix, 0, 16);

                switch (alignment)
                {
                    case 0:
                        if (forwardEnemy == id)
                        {
                            Matrix.translateM(transformationMatrix, 0, 0.0f, -40.0f, 0.0f);
                        }
                        else
                        {
                            float actorOffset = lineOffset + (60.0f * i);
                            Matrix.translateM(transformationMatrix, 0, actorOffset, -80.0f, 0.0f);
                        }
                        Matrix.rotateM(transformationMatrix, 0, 180.0f, 0.0f, 0.0f, 1.0f);
                        break;
                    case 1:
                        if (forwardEnemy == id)
                        {
                            Matrix.translateM(transformationMatrix, 0, -40.0f, 0.0f, 0.0f);
                        }
                        else
                        {
                            float actorOffset = lineOffset + (60.0f * i);
                            Matrix.translateM(transformationMatrix, 0, -80.0f, actorOffset, 0.0f);
                        }
                        Matrix.rotateM(transformationMatrix, 0, 90.0f, 0.0f, 0.0f, 1.0f);
                        break;
                    case 2:
                        if (forwardEnemy == id)
                        {
                            Matrix.translateM(transformationMatrix, 0, 0.0f, 40.0f, 0.0f);
                        }
                        else
                        {
                            float actorOffset = lineOffset + (60.0f * i);
                            Matrix.translateM(transformationMatrix, 0, actorOffset, 80.0f, 0.0f);
                        }
                        //Matrix.rotateM(transformationMatrix, 0, 0.0f, 0.0f, 0.0f, 1.0f);
                        break;
                    case 3:
                        if (forwardEnemy == id)
                        {
                            Matrix.translateM(transformationMatrix, 0, 40.0f, 0.0f, 0.0f);
                        }
                        else
                        {
                            float actorOffset = lineOffset + (60.0f * i);
                            Matrix.translateM(transformationMatrix, 0, 80.0f, actorOffset, 0.0f);
                        }
                        Matrix.rotateM(transformationMatrix, 0, 270.0f, 0.0f, 0.0f, 1.0f);
                        break;
                }

                enemyLine.get(id).draw(projectionMatrix, transformationMatrix, lightPos);
                if (effects.containsKey(id))
                {
                    // Plane will appear in front of the enemy
                    effects.get(id).draw(projectionMatrix, transformationMatrix, lightPos);
                }
                ++i;
            }

            for (int id : entityPile.keySet())
            {
                float[] transformationMatrix = new float[16];
                System.arraycopy(modelViewMatrix, 0, transformationMatrix, 0, 16);
                Matrix.translateM(transformationMatrix, 0, 0.0f, 0.0f, 0.0f);
                entityPile.get(id).draw(projectionMatrix, transformationMatrix, lightPos);
            }

            assetAccess.release();
        }
    }

    public void setShaderProgram(DynamicShaderProgram shaderProgram)
    {

    }

    public void setColor(float r, float g, float b, float a)
    {

    }
}
