package com.semaphore_soft.apps.cypher.opengl;

import com.semaphore_soft.apps.cypher.opengl.shader.SimpleShaderProgram;

import org.artoolkit.ar.base.rendering.gles20.ARDrawableOpenGLES20;
import org.artoolkit.ar.base.rendering.gles20.ShaderProgram;

/**
 * Created by ceroj on 2/1/2017.
 */

public class ARModelGLES20 extends ARModel implements ARDrawableOpenGLES20
{
    private SimpleShaderProgram shaderProgram;

    public ARModelGLES20()
    {
        super();
    }

    public ARModelGLES20(float size)
    {
        super(size);
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

        shaderProgram.render(this.getVertexBuffer(),
                             this.getColorBuffer(),
                             this.getIndexBuffer());
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

