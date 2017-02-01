package com.semaphore_soft.apps.cypher.opengl;

import org.artoolkit.ar.base.rendering.gles20.ARDrawableOpenGLES20;
import org.artoolkit.ar.base.rendering.gles20.ShaderProgram;

/**
 * Created by Scorple on 11/28/2016.
 */

public class ARTriangleGLES20 extends ARTriangle implements ARDrawableOpenGLES20
{
    private ShaderProgram shaderProgram;

    public ARTriangleGLES20(ShaderProgram shaderProgram)
    {
        super();
        this.shaderProgram = shaderProgram;
    }

    public ARTriangleGLES20(float size)
    {
        super(size);
    }

    public ARTriangleGLES20(float size, float x, float y, float z)
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
    /**
     * Sets the shader program used by this geometry.
     */
    public void setShaderProgram(ShaderProgram shaderProgram)
    {
        this.shaderProgram = shaderProgram;
    }
}
