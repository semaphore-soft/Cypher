package com.semaphore_soft.apps.cypher;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.semaphore_soft.apps.cypher.game.Actor;
import com.semaphore_soft.apps.cypher.game.Room;
import com.semaphore_soft.apps.cypher.opengl.ARModel;
import com.semaphore_soft.apps.cypher.opengl.ARModelGLES20;
import com.semaphore_soft.apps.cypher.opengl.ARRoom;
import com.semaphore_soft.apps.cypher.opengl.ARRoomProto;
import com.semaphore_soft.apps.cypher.opengl.ARRoomProtoGLES20;
import com.semaphore_soft.apps.cypher.opengl.ModelLoader;
import com.semaphore_soft.apps.cypher.opengl.shader.SimpleFragmentShader;
import com.semaphore_soft.apps.cypher.opengl.shader.SimpleShaderProgram;
import com.semaphore_soft.apps.cypher.opengl.shader.SimpleVertexShader;
import com.semaphore_soft.apps.cypher.utils.GameStatLoader;

import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.rendering.gles20.ARDrawableOpenGLES20;
import org.artoolkit.ar.base.rendering.gles20.ARRendererGLES20;
import org.artoolkit.ar.base.rendering.gles20.ShaderProgram;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by rickm on 11/9/2016.
 */

class PortalRenderer extends ARRendererGLES20
{
    private Context context;

    private ArrayList<Integer> markers;

    private int[] playerMarkerIDs;

    private ArrayList<ARModelGLES20> characterModels;

    private Hashtable<String, ARModel> models;

    //private Hashtable<Integer, ARRoomProtoGLES20> arRoomModels;
    private Hashtable<Integer, ARRoom> arRooms;

    private SimpleShaderProgram roomShaderProgram;

    public void setContext(Context context)
    {
        this.context = context;
    }

    @Override
    public boolean configureARScene()
    {
        markers = new ArrayList<>();

        markers.add(ARToolKit.getInstance().addMarker("single;Data/00.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/01.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/02.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/03.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/04.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/05.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/06.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/07.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/08.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/09.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/10.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/11.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/12.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/13.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/14.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/15.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/16.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/17.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/18.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/19.patt;80"));

        for (Integer id : markers)
        {
            if (id < 0)
            {
                return false;
            }
        }

        playerMarkerIDs = new int[]{-1, -1, -1, -1};

        return true;
    }

    //Shader calls should be within a GL thread that is onSurfaceChanged(), onSurfaceCreated() or onDrawFrame()
    //As the cube instantiates the shader during setShaderProgram call we need to create the cube here.
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config)
    {
        super.onSurfaceCreated(unused, config);

        characterModels = new ArrayList<>();

        for (int i = 0; i < 4; ++i)
        {
            ARModelGLES20 playerMarkerModel =
                ModelLoader.loadModel(context, "models/waypoint.obj", 40.0f);
            //new ARModelGLES20(40.0f, 0.0f, 0.0f, 0.0f, "models/garbage_man.obj", context);
            playerMarkerModel.setCharacter(i);
            ShaderProgram characterShaderProgram =
                new SimpleShaderProgram(playerMarkerModel.getNumIndices(),
                                        new SimpleVertexShader(),
                                        new SimpleFragmentShader());
            playerMarkerModel.setShaderProgram(characterShaderProgram);
            characterModels.add(playerMarkerModel);
        }

        roomShaderProgram =
            new SimpleShaderProgram(ARRoomProto.NUM_INDICES,
                                    new SimpleVertexShader(),
                                    new SimpleFragmentShader());

        arRooms = new Hashtable<>();

        models = new Hashtable<>();

        ArrayList<String> actorNames = GameStatLoader.getList(context, "actors");
        if (actorNames != null)
        {
            for (String name : actorNames)
            {
                ARModelGLES20 actorModel =
                    ModelLoader.loadModel(context, "models/actors/" + name + ".obj");
                if (actorModel != null)
                {
                    ShaderProgram actorShaderProgram =
                        new SimpleShaderProgram(actorModel.getNumIndices(),
                                                new SimpleVertexShader(),
                                                new SimpleFragmentShader());
                    actorModel.setShaderProgram(actorShaderProgram);
                    models.put(name, actorModel);
                }
            }
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

        for (int i = 0; i < playerMarkerIDs.length; ++i)
        {
            if (playerMarkerIDs[i] > -1 &&
                ARToolKit.getInstance().queryMarkerVisible(playerMarkerIDs[i]))
            {
                characterModels.get(i).draw(projectionMatrix,
                                            ARToolKit.getInstance()
                                                     .queryMarkerTransformation(playerMarkerIDs[i]));
            }
        }

        for (Integer i : arRooms.keySet())
        {
            if (ARToolKit.getInstance().queryMarkerVisible(i))
            {
                arRooms.get(i)
                       .draw(projectionMatrix,
                             ARToolKit.getInstance().queryMarkerTransformation(i));
            }
        }
    }

    public int getFirstMarker()
    {
        for (Integer id : markers)
        {
            if (ARToolKit.getInstance().queryMarkerVisible(id))
            {
                return id;
            }
        }

        return -1;
    }

    public int getFirstMarkerExcluding(ArrayList<Integer> marksX)
    {
        for (Integer id : markers)
        {
            if (!marksX.contains(id) && ARToolKit.getInstance().queryMarkerVisible(id))
            {
                return id;
            }
        }

        return -1;
    }

    public int getNearestMarker(int mark0)
    {
        int    nearest          = -1;
        double shortestDistance = -1;

        if (ARToolKit.getInstance().queryMarkerVisible(mark0))
        {
            for (Integer mark1 : markers)
            {
                if (mark0 != mark1 && ARToolKit.getInstance().queryMarkerVisible(mark1))
                {
                    float[] mark0TransInfo =
                        ARToolKit.getInstance().queryMarkerTransformation(mark0);
                    float[] mark0PosInfo =
                        {mark0TransInfo[mark0TransInfo.length - 4], mark0TransInfo[
                            mark0TransInfo.length -
                            3], mark0TransInfo[
                            mark0TransInfo.length - 2]};

                    float[] mark1TransInfo =
                        ARToolKit.getInstance().queryMarkerTransformation(mark1);
                    float[] mark1PosInfo =
                        {mark1TransInfo[mark1TransInfo.length - 4], mark1TransInfo[
                            mark1TransInfo.length -
                            3], mark1TransInfo[
                            mark1TransInfo.length - 2]};

                    double thisDistance =
                        Math.sqrt(Math.pow(mark0PosInfo[0] - mark1PosInfo[0], 2) +
                                  Math.pow(mark0PosInfo[1] - mark1PosInfo[1], 2) +
                                  Math.pow(mark0PosInfo[2] - mark1PosInfo[2], 2));

                    if (thisDistance < shortestDistance || shortestDistance < 0)
                    {
                        nearest = mark1;
                        shortestDistance = thisDistance;
                    }
                }
            }
        }

        return nearest;
    }

    public int getNearestMarkerExcluding(int mark0, ArrayList<Integer> marksX)
    {
        int    nearest          = -1;
        double shortestDistance = -1;

        if (ARToolKit.getInstance().queryMarkerVisible(mark0))
        {
            for (Integer mark1 : markers)
            {
                if (!marksX.contains(mark1) && mark0 != mark1 &&
                    ARToolKit.getInstance().queryMarkerVisible(mark1))
                {
                    float[] mark0TransInfo =
                        ARToolKit.getInstance().queryMarkerTransformation(mark0);
                    float[] mark0PosInfo =
                        {mark0TransInfo[mark0TransInfo.length - 4], mark0TransInfo[
                            mark0TransInfo.length -
                            3], mark0TransInfo[
                            mark0TransInfo.length - 2]};

                    float[] mark1TransInfo =
                        ARToolKit.getInstance().queryMarkerTransformation(mark1);
                    float[] mark1PosInfo =
                        {mark1TransInfo[mark1TransInfo.length - 4], mark1TransInfo[
                            mark1TransInfo.length -
                            3], mark1TransInfo[
                            mark1TransInfo.length - 2]};

                    double thisDistance =
                        Math.sqrt(Math.pow(mark0PosInfo[0] - mark1PosInfo[0], 2) +
                                  Math.pow(mark0PosInfo[1] - mark1PosInfo[1], 2) +
                                  Math.pow(mark0PosInfo[2] - mark1PosInfo[2], 2));

                    if (thisDistance < shortestDistance || shortestDistance < 0)
                    {
                        nearest = mark1;
                        shortestDistance = thisDistance;
                    }
                }
            }
        }

        return nearest;
    }

    public float getMarkerDirection(int mark0)
    {
        float res;

        float[] mark0TransInfo =
            ARToolKit.getInstance().queryMarkerTransformation(mark0);

        for (int i = 0; i < mark0TransInfo.length; i += 4)
        {
            String output = "";
            for (int j = 0; j < 4; ++j)
            {
                output += mark0TransInfo[i + j] + " ";
            }
            System.out.println(output);
        }

        res = ((mark0TransInfo[4] >= 0) ? (float) Math.acos(mark0TransInfo[0]) : (float) (Math.PI +
                                                                                          Math.acos(
                                                                                              -mark0TransInfo[0])));

        res *= (180 / Math.PI);
        System.out.println("Flat angle in degrees: " + res);

        return ((Float.isNaN(res)) ? 0 : res);
    }

    public float getAngleBetweenMarkers(int mark0, int mark1)
    {
        float[] resVector;

        float[] mark0TransInfo =
            ARToolKit.getInstance().queryMarkerTransformation(mark0);
        float[] mark1TransInfo =
            ARToolKit.getInstance().queryMarkerTransformation(mark1);

        mark1TransInfo[12] -= mark0TransInfo[12];
        mark1TransInfo[13] -= mark0TransInfo[13];
        mark1TransInfo[14] -= mark0TransInfo[14];

        mark0TransInfo[12] -= mark0TransInfo[12];
        mark0TransInfo[13] -= mark0TransInfo[13];
        mark0TransInfo[14] -= mark0TransInfo[14];

        float[] mark0TransInv = new float[16];
        Matrix.invertM(mark0TransInv, 0, mark0TransInfo, 0);

        resVector =
            new float[]{mark1TransInfo[12], mark1TransInfo[13], mark1TransInfo[14], mark1TransInfo[15]};

        String output = "ResVector Before Multiply: ";
        for (int i = 0; i < resVector.length; ++i)
        {
            output += resVector[i] + " ";
        }
        System.out.println(output);

        Matrix.multiplyMV(resVector,
                          0,
                          mark0TransInv,
                          0,
                          resVector,
                          0);

        output = "ResVector After Multiply: ";
        for (int i = 0; i < resVector.length; ++i)
        {
            output += resVector[i] + " ";
        }

        float resAngle = (float) Math.atan2(resVector[0], resVector[1]);
        resAngle *= (180 / Math.PI);
        resAngle = ((resAngle < 0) ? (360 + resAngle) : resAngle);

        System.out.println(output);

        return ((Float.isNaN(resAngle)) ? 0 : resAngle);
    }

    public void setPlayerMarker(long playerID, int markerID)
    {
        playerMarkerIDs[(int) playerID] = markerID;
    }

    public void createRoom(Room room)
    {
        ARRoom            arRoom      = new ARRoom();
        ARRoomProtoGLES20 arRoomProto = new ARRoomProtoGLES20(80.0f, 0.0f, 0.0f, 0.0f);
        arRoomProto.setShaderProgram(roomShaderProgram);
        for (short i = 0; i < 4; ++i)
        {
            arRoomProto.setWall(i, room.getWallType(i));
        }
        //arRoomModels.put(room.getMarker(), arRoomProto);
        arRoom.setRoomModel(arRoomProto);
        arRooms.put(room.getMarker(), arRoom);
    }

    public void updateRoomWalls(Room room)
    {
        ARRoom      arRoom      = arRooms.get(room.getMarker());
        ARRoomProto arRoomModel = arRoom.getRoomModelAsRoomProto();
        if (arRoomModel != null)
        {
            for (short i = 0; i < 4; ++i)
            {
                arRoomModel.setWall(i, room.getWallType(i));
            }
        }
    }

    public void updateRoomResidents(Room room, Hashtable<Long, Actor> actors)
    {
        ARRoom arRoom = arRooms.get(room.getMarker());
        arRoom.removeActors();
        for (Long id : room.getResidentActors())
        {
            String name = actors.get(id).getName();
            System.out.println("adding actor:" + id + ":" + name + " to room:" + room.getId());
            Actor actor = actors.get(id);

            if (name != null && models.keySet().contains(name))
            {
                if (actor.isPlayer())
                {
                    arRoom.addPlayer(id, (ARDrawableOpenGLES20) models.get(name));
                }
                else
                {
                    arRoom.addEnemy(id, (ARDrawableOpenGLES20) models.get(name));
                }
            }
        }
    }

    public interface NewMarkerListener
    {
        void newMarker(int marker);
    }
}
