package com.semaphore_soft.apps.cypher.opengl;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by rickm on 2/10/2017.
 */

public class ARModel
{
    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer indexBuffer;

    public FloatBuffer getVertexBuffer()
    {
        return vertexBuffer;
    }

    public FloatBuffer getColorBuffer()
    {
        return colorBuffer;
    }

    public ShortBuffer getIndexBuffer()
    {
        return indexBuffer;
    }
}
