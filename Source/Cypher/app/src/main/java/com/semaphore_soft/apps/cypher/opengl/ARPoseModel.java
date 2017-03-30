package com.semaphore_soft.apps.cypher.opengl;

import android.opengl.Matrix;

import com.semaphore_soft.apps.cypher.opengl.shader.DynamicShaderProgram;

import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by rickm on 2/14/2017.
 */

public class ARPoseModel implements ARDrawableGLES20
{
    private ConcurrentHashMap<String, ARModelGLES20> poseLib;
    private String                                   defaultPose;
    private String                                   currentPose;

    public ARPoseModel(ConcurrentHashMap<String, ARModelGLES20> poseLib, String defaultPose)
    {
        this.poseLib = poseLib;
        this.defaultPose = defaultPose;
        currentPose = defaultPose;
    }

    public void setPose(String pose)
    {
        if (poseLib.keySet().contains(pose))
        {
            currentPose = pose;
        }
        else
        {
            currentPose = defaultPose;
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
            lightPos[0] = transformationMatrix[i + 12];
        }
        draw(projectionMatrix, modelViewMatrix, lightPos);
    }

    public void draw(float[] projectionMatrix, float[] modelViewMatrix, float[] lightPos)
    {
        poseLib.get(currentPose).draw(projectionMatrix, modelViewMatrix, lightPos);
    }

    public void setShaderProgram(DynamicShaderProgram shaderProgram)
    {
        for (String pose : poseLib.keySet())
        {
            poseLib.get(pose).setShaderProgram(shaderProgram);
        }
    }

    public void setColor(float r, float g, float b, float a)
    {
        for (String pose : poseLib.keySet())
        {
            poseLib.get(pose).setColor(r, g, b, a);
        }
    }
}
