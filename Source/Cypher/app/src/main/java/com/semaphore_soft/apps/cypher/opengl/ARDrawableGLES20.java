package com.semaphore_soft.apps.cypher.opengl;

import com.semaphore_soft.apps.cypher.opengl.shader.DynamicShaderProgram;

/**
 * Created by rickm on 2/14/2017.
 */

public interface ARDrawableGLES20
{
    void draw(float[] projectionMatrix, float[] modelViewMatrix);

    void draw(float[] projectionMatrix, float[] modelViewMatrix, float[] lightPos);

    void setShaderProgram(DynamicShaderProgram shaderProgram);

    void setColor(float r, float g, float b, float a);
}
