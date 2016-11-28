package com.semaphore_soft.apps.cypher;

import android.opengl.GLES20;

import com.semaphore_soft.apps.cypher.shader.SimpleFragmentShader;
import com.semaphore_soft.apps.cypher.shader.SimpleShaderProgram;
import com.semaphore_soft.apps.cypher.shader.SimpleVertexShader;

import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.rendering.gles20.ARRendererGLES20;
import org.artoolkit.ar.base.rendering.gles20.CubeGLES20;
import org.artoolkit.ar.base.rendering.gles20.ShaderProgram;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by rickm on 11/9/2016.
 */

class PortalRenderer extends ARRendererGLES20
{

    private int mark0 = -1;
    private int mark1 = -1;
    private int mark2 = -1;

    private CubeGLES20       cube;
    private ARTriangleGLES20 arTriangleGLES20;

    @Override
    public boolean configureARScene()
    {
        mark0 = ARToolKit.getInstance().addMarker("single;Data/0.patt;80");
        mark1 = ARToolKit.getInstance().addMarker("single;Data/1.patt;80");
        mark2 = ARToolKit.getInstance().addMarker("single;Data/2.patt;80");
        if (mark0 < 0 || mark1 < 0 || mark2 < 0)
        {
            return false;
        }

        return true;
    }

    //Shader calls should be within a GL thread that is onSurfaceChanged(), onSurfaceCreated() or onDrawFrame()
    //As the cube instantiates the shader during setShaderProgram call we need to create the cube here.
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config)
    {
        super.onSurfaceCreated(unused, config);

        ShaderProgram cubeShaderProgram =
            new SimpleShaderProgram(36, new SimpleVertexShader(), new SimpleFragmentShader());
        cube = new CubeGLES20(40.0f, 0.0f, 0.0f, 20.0f);
        cube.setShaderProgram(cubeShaderProgram);

        ShaderProgram triangleShaderProgram =
            new SimpleShaderProgram(3, new SimpleVertexShader(), new SimpleFragmentShader());
        arTriangleGLES20 = new ARTriangleGLES20(40.0f, 0.0f, 0.0f, 20.0f);
        arTriangleGLES20.setShaderProgram(triangleShaderProgram);
    }

    /**
     * Override the draw function from ARRenderer.
     */
    @Override
    public void draw(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadMatrixf(ARToolKit.getInstance().getProjectionMatrix(), 0);

        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glFrontFace(GL10.GL_CW);

        gl.glMatrixMode(GL10.GL_MODELVIEW);

        if (ARToolKit.getInstance().queryMarkerVisible(mark0) &&
                ARToolKit.getInstance().queryMarkerVisible(mark1) &&
                ARToolKit.getInstance().queryMarkerVisible(mark2))
        {
            float[] mark0TransInfo = ARToolKit.getInstance().queryMarkerTransformation(mark0);
            float[] mark0PosInfo =
                    {mark0TransInfo[mark0TransInfo.length - 4], mark0TransInfo[mark0TransInfo.length -
                            3], mark0TransInfo[
                            mark0TransInfo.length - 2]};

            System.out.println("begin mark0 pos matrix");
            for (float x : mark0PosInfo)
            {
                System.out.println(x);
            }
            System.out.println("end mark0 pos matrix");

            float[] mark1TransInfo = ARToolKit.getInstance().queryMarkerTransformation(mark1);
            float[] mark1PosInfo =
                    {mark1TransInfo[mark1TransInfo.length - 4], mark1TransInfo[mark1TransInfo.length -
                            3], mark1TransInfo[
                            mark1TransInfo.length - 2]};

            System.out.println("begin mark1 pos matrix");
            for (float x : mark1PosInfo)
            {
                System.out.println(x);
            }
            System.out.println("end mark1 pos matrix");

            float[] mark2TransInfo = ARToolKit.getInstance().queryMarkerTransformation(mark2);
            float[] mark2PosInfo =
                    {mark2TransInfo[mark2TransInfo.length - 4], mark2TransInfo[mark2TransInfo.length -
                            3], mark2TransInfo[
                            mark2TransInfo.length - 2]};

            System.out.println("begin mark2 pos matrix");
            for (float x : mark2PosInfo)
            {
                System.out.println(x);
            }
            System.out.println("end mark2 pos matrix");

            double distanceMark0Mark1 = Math.sqrt(Math.pow(mark0PosInfo[0] - mark1PosInfo[0], 2) +
                    Math.pow(mark0PosInfo[1] - mark1PosInfo[1], 2) +
                    Math.pow(mark0PosInfo[2] - mark1PosInfo[2], 2));

            double distanceMark0Mark2 = Math.sqrt(Math.pow(mark0PosInfo[0] - mark2PosInfo[0], 2) +
                    Math.pow(mark0PosInfo[1] - mark2PosInfo[1], 2) +
                    Math.pow(mark0PosInfo[2] - mark2PosInfo[2], 2));

            System.out.println("distance between mark0 and mark1 is " + distanceMark0Mark1);
            System.out.println("distance between mark0 and mark2 is " + distanceMark0Mark2);

            if (distanceMark0Mark1 < distanceMark0Mark2)
            {
                System.out.println("mark0 is closer to mark1");
                gl.glLoadMatrixf(ARToolKit.getInstance().queryMarkerTransformation(mark1), 0);
            }
            else
            {
                System.out.println("mark0 is closer to mark2");
                gl.glLoadMatrixf(ARToolKit.getInstance().queryMarkerTransformation(mark2), 0);
            }

            gl.glPushMatrix();
            cube.draw(gl);
            gl.glPopMatrix();
        }
    }

    /**
     * Override the render function from {@link ARRendererGLES20}.
     */
    @Override
    public void draw()
    {
        super.draw();

        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glFrontFace(GLES20.GL_CW);

        float[] projectionMatrix = ARToolKit.getInstance().getProjectionMatrix();

        // If the marker is visible, apply its transformation, and render a triangle
        if (ARToolKit.getInstance().queryMarkerVisible(mark0))
        {
            arTriangleGLES20.draw(projectionMatrix,
                                  ARToolKit.getInstance().queryMarkerTransformation(mark0));
        }

        if (ARToolKit.getInstance().queryMarkerVisible(mark0) &&
            ARToolKit.getInstance().queryMarkerVisible(mark1) &&
            ARToolKit.getInstance().queryMarkerVisible(mark2))
        {
            float[] mark0TransInfo = ARToolKit.getInstance().queryMarkerTransformation(mark0);
            float[] mark0PosInfo =
                {mark0TransInfo[mark0TransInfo.length - 4], mark0TransInfo[mark0TransInfo.length -
                                                                           3], mark0TransInfo[
                    mark0TransInfo.length - 2]};

            System.out.println("begin mark0 pos matrix");
            for (float x : mark0PosInfo)
            {
                System.out.println(x);
            }
            System.out.println("end mark0 pos matrix");

            float[] mark1TransInfo = ARToolKit.getInstance().queryMarkerTransformation(mark1);
            float[] mark1PosInfo =
                {mark1TransInfo[mark1TransInfo.length - 4], mark1TransInfo[mark1TransInfo.length -
                                                                           3], mark1TransInfo[
                    mark1TransInfo.length - 2]};

            System.out.println("begin mark1 pos matrix");
            for (float x : mark1PosInfo)
            {
                System.out.println(x);
            }
            System.out.println("end mark1 pos matrix");

            float[] mark2TransInfo = ARToolKit.getInstance().queryMarkerTransformation(mark2);
            float[] mark2PosInfo =
                {mark2TransInfo[mark2TransInfo.length - 4], mark2TransInfo[mark2TransInfo.length -
                                                                           3], mark2TransInfo[
                    mark2TransInfo.length - 2]};

            System.out.println("begin mark2 pos matrix");
            for (float x : mark2PosInfo)
            {
                System.out.println(x);
            }
            System.out.println("end mark2 pos matrix");

            double distanceMark0Mark1 = Math.sqrt(Math.pow(mark0PosInfo[0] - mark1PosInfo[0], 2) +
                                                  Math.pow(mark0PosInfo[1] - mark1PosInfo[1], 2) +
                                                  Math.pow(mark0PosInfo[2] - mark1PosInfo[2], 2));

            double distanceMark0Mark2 = Math.sqrt(Math.pow(mark0PosInfo[0] - mark2PosInfo[0], 2) +
                                                  Math.pow(mark0PosInfo[1] - mark2PosInfo[1], 2) +
                                                  Math.pow(mark0PosInfo[2] - mark2PosInfo[2], 2));

            System.out.println("distance between mark0 and mark1 is " + distanceMark0Mark1);
            System.out.println("distance between mark0 and mark2 is " + distanceMark0Mark2);

            if (distanceMark0Mark1 < distanceMark0Mark2)
            {
                System.out.println("mark0 is closer to mark1");
                cube.draw(projectionMatrix,
                          ARToolKit.getInstance().queryMarkerTransformation(mark1));
            }
            else
            {
                System.out.println("mark0 is closer to mark2");
                cube.draw(projectionMatrix,
                          ARToolKit.getInstance().queryMarkerTransformation(mark2));
            }
        }
    }
}
