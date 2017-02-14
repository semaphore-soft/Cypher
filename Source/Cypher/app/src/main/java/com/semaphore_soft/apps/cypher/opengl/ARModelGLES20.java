package com.semaphore_soft.apps.cypher.opengl;

import android.opengl.Matrix;

import com.semaphore_soft.apps.cypher.opengl.shader.DynamicShaderProgram;

/**
 * Created by ceroj on 2/1/2017.
 */

public class ARModelGLES20 extends ARModel
{
    private DynamicShaderProgram shaderProgram;

    public ARModelGLES20(float size)
    {
        super(size);
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
        shaderProgram.render(modelViewMatrix,
                             projectionMatrix,
                             lightPos,
                             getNumIndices(),
                             getVertexBuffer(),
                             getColorBuffer(),
                             getNormalBuffer(),
                             getTexCoordinateBuffer(),
                             getVertexIndexBuffer(),
                             getTextureHandle());
    }

    public void setShaderProgram(DynamicShaderProgram shaderProgram)
    {
        this.shaderProgram = shaderProgram;
    }
}

