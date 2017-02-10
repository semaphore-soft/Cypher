package com.semaphore_soft.apps.cypher.opengl;

import com.semaphore_soft.apps.cypher.opengl.shader.SimpleShaderProgram;

import org.artoolkit.ar.base.rendering.gles20.ARDrawableOpenGLES20;
import org.artoolkit.ar.base.rendering.gles20.ShaderProgram;

/**
 * Created by rickm on 2/10/2017.
 */

public class ARModelGLES20 extends ARModel implements ARDrawableOpenGLES20
{
    private SimpleShaderProgram shaderProgram;

    public void draw(float[] projectionMatrix, float[] modelViewMatrix)
    {
        shaderProgram.setProjectionMatrix(projectionMatrix);
        shaderProgram.setModelViewMatrix(modelViewMatrix);

        shaderProgram.render(getVertexBuffer(),
                             getColorBuffer(),
                             getIndexBuffer());
    }

    @Override
    public void setShaderProgram(ShaderProgram shaderProgram)
    {
        setShaderProgram((SimpleShaderProgram) shaderProgram);
    }

    public void setShaderProgram(SimpleShaderProgram shaderProgram)
    {
        this.shaderProgram = shaderProgram;
    }
}
