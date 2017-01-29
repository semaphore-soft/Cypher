package com.semaphore_soft.apps.cypher;

import com.semaphore_soft.apps.cypher.shader.SimpleShaderProgram;

import org.artoolkit.ar.base.rendering.gles20.ARDrawableOpenGLES20;
import org.artoolkit.ar.base.rendering.gles20.ShaderProgram;

/**
 * Created by Scorple on 1/27/2017.
 */

public class ARRoomGLES20 extends ARRoom implements ARDrawableOpenGLES20
{
    private SimpleShaderProgram shaderProgram;

    public ARRoomGLES20(SimpleShaderProgram shaderProgram)
    {
        super();
        this.shaderProgram = shaderProgram;
    }

    public ARRoomGLES20(float size)
    {
        super(size);
    }

    public ARRoomGLES20(float size, float x, float y, float z)
    {
        super(size, x, y, z);
    }

    @Override
    /**
     * Used to render objects when working with OpenGL ES 2.x
     *
     * @param projectionMatrix The projection matrix obtained from the ARToolkit
     * @param modelViewMatrix  The marker transformation matrix obtained from ARToolkit
     */
    public void draw(float[] projectionMatrix, float[] modelViewMatrix)
    {

        shaderProgram.setProjectionMatrix(projectionMatrix);
        shaderProgram.setModelViewMatrix(modelViewMatrix);

        shaderProgram.render(this.getmVertexBuffer(),
                             this.getmColorBuffer(),
                             this.getmIndexBuffer());
    }

    @Override
    public void setShaderProgram(ShaderProgram shaderProgram)
    {
        setShaderProgram((SimpleShaderProgram) shaderProgram);
    }

    /**
     * Sets the shader program used by this geometry.
     */
    public void setShaderProgram(SimpleShaderProgram shaderProgram)
    {
        this.shaderProgram = shaderProgram;
    }
}
