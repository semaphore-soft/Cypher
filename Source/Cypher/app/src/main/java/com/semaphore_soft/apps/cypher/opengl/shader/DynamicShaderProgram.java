package com.semaphore_soft.apps.cypher.opengl.shader;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Scorple on 2/13/2017.
 */

public class DynamicShaderProgram
{
    private int shaderProgramHandle;

    private final int BYTES_PER_FLOAT = Float.SIZE / 8;

    private final int POSITION_DATA_SIZE    = 3;
    private final int POSITION_STRIDE_BYTES = POSITION_DATA_SIZE * BYTES_PER_FLOAT;

    private final int COLOR_DATA_SIZE    = 4;
    private final int COLOR_STRIDE_BYTES = COLOR_DATA_SIZE * BYTES_PER_FLOAT;

    private final int NORMAL_DATA_SIZE    = 3;
    private final int NORMAL_STRIDE_BYTES = NORMAL_DATA_SIZE * BYTES_PER_FLOAT;

    private final int TEX_COORDINATE_DATA_SIZE    = 2;
    private final int TEX_COORDINATE_STRIDE_BYTES = TEX_COORDINATE_DATA_SIZE * BYTES_PER_FLOAT;

    public DynamicShaderProgram(int vertexShaderHandle,
                                int fragmentShaderHandle,
                                String[] attributes)
    {
        setupShaderProgram(vertexShaderHandle, fragmentShaderHandle, attributes);
    }

    private void setupShaderProgram(int vertexShaderHandle,
                                    int fragmentShaderHandle,
                                    String[] attributes)
    {
        shaderProgramHandle = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgramHandle, vertexShaderHandle);
        GLES20.glAttachShader(shaderProgramHandle, fragmentShaderHandle);

        if (attributes != null)
        {
            for (int i = 0; i < attributes.length; ++i)
            {
                GLES20.glBindAttribLocation(shaderProgramHandle, i, attributes[i]);
            }
        }

        GLES20.glLinkProgram(shaderProgramHandle);
    }

    private int getHandle(String name)
    {
        switch (name.substring(0, 1))
        {
            case "a":
                return GLES20.glGetAttribLocation(shaderProgramHandle, name);
            case "u":
                return GLES20.glGetUniformLocation(shaderProgramHandle, name);
        }

        return 0;
    }

    public void render(float[] modelViewMatrix,
                       float[] projectionMatrix,
                       float[] lightPos,
                       int numIndices,
                       FloatBuffer vertexBuffer,
                       FloatBuffer colorBuffer,
                       FloatBuffer normalBuffer,
                       FloatBuffer texCoordinateBuffer,
                       ShortBuffer indexBuffer,
                       int textureHandle)
    {
        GLES20.glUseProgram(shaderProgramHandle);

        float[] modelViewProjectionMatrix = new float[16];
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
        GLES20.glUniformMatrix4fv(getHandle("u_MVPMatrix"), 1, false, modelViewProjectionMatrix, 0);
        GLES20.glUniformMatrix4fv(getHandle("u_MVMatrix"), 1, false, modelViewMatrix, 0);

        if (textureHandle != -1)
        {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
            GLES20.glUniform1i(getHandle("u_Texture"), 0);
        }

        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(getHandle("a_Position"),
                                     POSITION_DATA_SIZE,
                                     GLES20.GL_FLOAT,
                                     false,
                                     POSITION_STRIDE_BYTES,
                                     vertexBuffer);
        GLES20.glEnableVertexAttribArray(getHandle("a_Position"));

        colorBuffer.position(0);
        GLES20.glVertexAttribPointer(getHandle("a_Color"), COLOR_DATA_SIZE, GLES20.GL_FLOAT, false,
                                     COLOR_STRIDE_BYTES, colorBuffer);
        GLES20.glEnableVertexAttribArray(getHandle("a_Color"));

        normalBuffer.position(0);
        GLES20.glVertexAttribPointer(getHandle("a_Normal"),
                                     NORMAL_DATA_SIZE,
                                     GLES20.GL_FLOAT,
                                     false,
                                     NORMAL_STRIDE_BYTES,
                                     normalBuffer);
        GLES20.glEnableVertexAttribArray(getHandle("a_Normal"));

        if (texCoordinateBuffer != null)
        {
            texCoordinateBuffer.position(0);
            GLES20.glVertexAttribPointer(getHandle("a_TexCoordinate"),
                                         TEX_COORDINATE_DATA_SIZE,
                                         GLES20.GL_FLOAT,
                                         false,
                                         TEX_COORDINATE_STRIDE_BYTES,
                                         texCoordinateBuffer);
            GLES20.glEnableVertexAttribArray(getHandle("a_TexCoordinate"));
        }

        GLES20.glUniform3f(getHandle("u_LightPos"),
                           lightPos[0],
                           lightPos[1],
                           lightPos[2]);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                              numIndices,
                              GLES20.GL_UNSIGNED_SHORT,
                              indexBuffer);
    }
}
