package com.semaphore_soft.apps.cypher;

import android.content.Context;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.semaphore_soft.apps.cypher.game.Actor;
import com.semaphore_soft.apps.cypher.game.GameController;
import com.semaphore_soft.apps.cypher.game.GameMaster;
import com.semaphore_soft.apps.cypher.game.Model;
import com.semaphore_soft.apps.cypher.game.Room;
import com.semaphore_soft.apps.cypher.game.Special;
import com.semaphore_soft.apps.cypher.opengl.ARDrawableGLES20;
import com.semaphore_soft.apps.cypher.opengl.ARModelGLES20;
import com.semaphore_soft.apps.cypher.opengl.ARPoseModel;
import com.semaphore_soft.apps.cypher.opengl.ARRoom;
import com.semaphore_soft.apps.cypher.opengl.ModelLoader;
import com.semaphore_soft.apps.cypher.opengl.shader.DynamicShaderProgram;
import com.semaphore_soft.apps.cypher.opengl.shader.ShaderLoader;
import com.semaphore_soft.apps.cypher.utils.GameStatLoader;
import com.semaphore_soft.apps.cypher.utils.Logger;
import com.semaphore_soft.apps.cypher.utils.Timer;

import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.rendering.gles20.ARRendererGLES20;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by rickm on 11/9/2016.
 */

class PortalRenderer extends ARRendererGLES20
{
    private Context context;

    private static GameController gameController;

    private static ArrayList<Integer> markers;
    private static ArrayList<Integer> playerMarkerIDs;

    private static ArrayList<ARModelGLES20>                    characterModels;
    private static ConcurrentHashMap<String, ARDrawableGLES20> models;
    private static ConcurrentHashMap<Integer, ARRoom>          arRooms;

    private static Handler handler = null;

    private static NewMarkerListener newMarkerListener;

    private static ArrayList<Integer> knownMarkers;
    private static boolean lookingForNewMarkers = false;

    private static boolean checkingNearestRoomMarker = false;

    private static int playerMarker      = -1;
    private static int playerRoomMarker  = -1;
    private static int nearestRoomMarker = -1;

    private static MediaPlayer mediaPlayer;

    private static boolean soundEnabled = true;

    @Override
    public boolean configureARScene()
    {
        markers = new ArrayList<>();

        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_00.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_01.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_02.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_03.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_04.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_05.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_06.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_07.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_08.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_09.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_10.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_11.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_12.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_13.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_14.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_15.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_16.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_17.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_18.patt;80"));
        markers.add(ARToolKit.getInstance().addMarker("single;Data/mk2_19.patt;80"));

        for (int id : markers)
        {
            if (id < 0)
            {
                return false;
            }
        }

        playerMarkerIDs = new ArrayList<>();

        for (int i = 0; i < 4; ++i)
        {
            playerMarkerIDs.add(-1);
        }

        knownMarkers = new ArrayList<>();

        return true;
    }

    //Shader calls should be within a GL thread that is onSurfaceChanged(), onSurfaceCreated() or onDrawFrame()
    //As the cube instantiates the shader during setShaderProgram call we need to create the cube here.
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config)
    {
        super.onSurfaceCreated(unused, config);

        Timer loadTimer = new Timer();
        loadTimer.start();

        characterModels = new ArrayList<>();

        for (int i = 0; i < 4; ++i)
        {
            ARModelGLES20 playerMarkerModel =
                (ARModelGLES20) ModelLoader.load(context, "waypoint", 40.0f);
            playerMarkerModel.setCharacter(i);
            DynamicShaderProgram characterShaderProgram =
                new DynamicShaderProgram(ShaderLoader.createShader(context,
                                                                   "shaders/vertexShaderTextured.glsl",
                                                                   GLES20.GL_VERTEX_SHADER),
                                         ShaderLoader.createShader(context,
                                                                   "shaders/fragmentShaderTextured.glsl",
                                                                   GLES20.GL_FRAGMENT_SHADER),
                                         new String[]{"a_Position", "a_Color", "a_Normal", "a_TexCoordinate"});
            playerMarkerModel.setShaderProgram(characterShaderProgram);
            characterModels.add(playerMarkerModel);
        }

        arRooms = new ConcurrentHashMap<>();

        models = new ConcurrentHashMap<>();

        ARDrawableGLES20 waypoint =
            ModelLoader.load(context, "waypoint", 40.0f);
        DynamicShaderProgram waypointShaderProgram =
            new DynamicShaderProgram(ShaderLoader.createShader(context,
                                                               "shaders/vertexShaderTextured.glsl",
                                                               GLES20.GL_VERTEX_SHADER),
                                     ShaderLoader.createShader(context,
                                                               "shaders/fragmentShaderTextured.glsl",
                                                               GLES20.GL_FRAGMENT_SHADER),
                                     new String[]{"a_Position", "a_Color", "a_Normal", "a_TexCoordinate"});
        waypoint.setShaderProgram(waypointShaderProgram);
        models.put("waypoint", waypoint);

        String[] effects =
            new String[]{"spark", "chill", "flameShot", "heal", "ignusFatuus", "slash"};

        for (String effect : effects)
        {
            ARDrawableGLES20 overlay =
                ModelLoader.load(context, "overlay", 5.0f, effect);
            DynamicShaderProgram overlayShaderProgram =
                new DynamicShaderProgram(ShaderLoader.createShader(context,
                                                                   "shaders/vertexShaderTextured.glsl",
                                                                   GLES20.GL_VERTEX_SHADER),
                                         ShaderLoader.createShader(context,
                                                                   "shaders/fragmentShaderShadelessTexturedTransparent.glsl",
                                                                   GLES20.GL_FRAGMENT_SHADER),
                                         new String[]{"a_Position", "a_Color", "a_Normal", "a_TexCoordinate"});
            overlay.setShaderProgram(overlayShaderProgram);
            models.put(effect, overlay);
        }

        ArrayList<String> actorNames = GameStatLoader.getList(context, "actors");
        if (actorNames != null)
        {
            for (String name : actorNames)
            {
                ARDrawableGLES20 actorModel =
                    ModelLoader.load(context, name, "actors", 10.f);
                if (actorModel != null)
                {
                    DynamicShaderProgram actorShaderProgram =
                        new DynamicShaderProgram(ShaderLoader.createShader(context,
                                                                           "shaders/vertexShaderTextured.glsl",
                                                                           GLES20.GL_VERTEX_SHADER),
                                                 ShaderLoader.createShader(context,
                                                                           "shaders/fragmentShaderTextured.glsl",
                                                                           GLES20.GL_FRAGMENT_SHADER),
                                                 new String[]{"a_Position", "a_Color", "a_Normal"});
                    actorModel.setShaderProgram(actorShaderProgram);
                    models.put(name, actorModel);
                }
            }
        }

        ARDrawableGLES20 roomBase =
            ModelLoader.load(context, "room_base", 120.0f, "room_base");
        DynamicShaderProgram roomBaseShaderProgram =
            new DynamicShaderProgram(ShaderLoader.createShader(context,
                                                               "shaders/vertexShaderTextured.glsl",
                                                               GLES20.GL_VERTEX_SHADER),
                                     ShaderLoader.createShader(context,
                                                               "shaders/fragmentShaderTextured.glsl",
                                                               GLES20.GL_FRAGMENT_SHADER),
                                     new String[]{"a_Position", "a_Color", "a_Normal"});
        roomBase.setShaderProgram(roomBaseShaderProgram);
        //roomBase.setColor(0.5f, 0.5f, 0.5f, 1.0f);
        models.put("room_base", roomBase);

        ARDrawableGLES20 roomWall =
            ModelLoader.load(context, "room_wall_north", 120.0f, "room_base");
        DynamicShaderProgram roomWallShaderProgram =
            new DynamicShaderProgram(ShaderLoader.createShader(context,
                                                               "shaders/vertexShaderTextured.glsl",
                                                               GLES20.GL_VERTEX_SHADER),
                                     ShaderLoader.createShader(context,
                                                               "shaders/fragmentShaderTextured.glsl",
                                                               GLES20.GL_FRAGMENT_SHADER),
                                     new String[]{"a_Position", "a_Color", "a_Normal"});
        roomWall.setShaderProgram(roomWallShaderProgram);
        //roomWall.setColor(0.5f, 0.5f, 0.5f, 1.0f);
        models.put("room_wall", roomWall);

        ARDrawableGLES20 roomDoor =
            ModelLoader.load(context, "room_door_north", 120.0f, "room_base");
        DynamicShaderProgram roomDoorShaderProgram =
            new DynamicShaderProgram(ShaderLoader.createShader(context,
                                                               "shaders/vertexShaderTextured.glsl",
                                                               GLES20.GL_VERTEX_SHADER),
                                     ShaderLoader.createShader(context,
                                                               "shaders/fragmentShaderTextured.glsl",
                                                               GLES20.GL_FRAGMENT_SHADER),
                                     new String[]{"a_Position", "a_Color", "a_Normal"});
        roomDoor.setShaderProgram(roomDoorShaderProgram);
        //roomDoor.setColor(0.5f, 0.25f, 0.125f, 1.0f);
        models.put("room_door_unlocked", roomDoor);

        ARDrawableGLES20 roomDoorOpen =
            ModelLoader.load(context, "room_door_north_open", 120.0f, "room_base");
        DynamicShaderProgram roomDoorOpenShaderProgram =
            new DynamicShaderProgram(ShaderLoader.createShader(context,
                                                               "shaders/vertexShaderTextured.glsl",
                                                               GLES20.GL_VERTEX_SHADER),
                                     ShaderLoader.createShader(context,
                                                               "shaders/fragmentShaderTextured.glsl",
                                                               GLES20.GL_FRAGMENT_SHADER),
                                     new String[]{"a_Position", "a_Color", "a_Normal"});
        roomDoorOpen.setShaderProgram(roomDoorOpenShaderProgram);
        //roomDoorOpen.setColor(0.75f, 0.75f, 0.75f, 1.0f);
        models.put("room_door_open", roomDoorOpen);

        ARDrawableGLES20 error = ModelLoader.load(context, "error", 10.0f);
        DynamicShaderProgram errorShaderProgram =
            new DynamicShaderProgram(ShaderLoader.createShader(context,
                                                               "shaders/vertexShaderUntextured.glsl",
                                                               GLES20.GL_VERTEX_SHADER),
                                     ShaderLoader.createShader(context,
                                                               "shaders/fragmentShaderUntextured.glsl",
                                                               GLES20.GL_FRAGMENT_SHADER),
                                     new String[]{"a_Position", "a_Color", "a_Normal"});
        error.setShaderProgram(errorShaderProgram);
        error.setColor(0.7f, 0.0f, 0.0f, 1.0f);
        models.put("error", error);

        Logger.logI(
            "renderer finished loading in " + ((float) loadTimer.getTime()) / 1000f + " seconds");

        gameController.onFinishedLoading();
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

        for (int id : markers)
        {
            if (ARToolKit.getInstance().queryMarkerVisible(id))
            {
                if (knownMarkers.contains(id))
                {
                    for (int i = 0; i < playerMarkerIDs.size(); ++i)
                    {
                        if (playerMarkerIDs.get(i) == id)
                        {
                            characterModels.get(i)
                                           .draw(projectionMatrix,
                                                 ARToolKit.getInstance()
                                                          .queryMarkerTransformation(playerMarkerIDs
                                                                                         .get(i)));
                        }
                    }
                    for (int i : arRooms.keySet())
                    {
                        if (i == id)
                        {
                            arRooms.get(i)
                                   .draw(projectionMatrix,
                                         ARToolKit.getInstance().queryMarkerTransformation(i));
                        }
                    }
                }
                else if (lookingForNewMarkers)
                {
                    newMarkerListener.newMarker(id);
                }
            }
        }

        if (checkingNearestRoomMarker)
        {
            int newNearestRoomId = getNearestMarkerExcluding(playerMarker, playerMarkerIDs);

            if (newNearestRoomId != nearestRoomMarker)
            {
                nearestRoomMarker = newNearestRoomId;

                newMarkerListener.newNearestRoomMarker(nearestRoomMarker, -1);
            }
        }
    }

    public void setContext(final Context context)
    {
        this.context = context;
    }

    static void setHandler(final Handler handler)
    {
        PortalRenderer.handler = handler;
    }

    static void setGameController(final GameController gameController)
    {
        PortalRenderer.gameController = gameController;
    }

    int getFirstMarker()
    {
        for (int id : markers)
        {
            if (ARToolKit.getInstance().queryMarkerVisible(id))
            {
                return id;
            }
        }

        return -1;
    }

    int getFirstMarkerExcluding(final ArrayList<Integer> marksX)
    {
        for (int id : markers)
        {
            if (!marksX.contains(id) && ARToolKit.getInstance().queryMarkerVisible(id))
            {
                return id;
            }
        }

        return -1;
    }

    int getNearestMarker(final int mark0)
    {
        int    nearest          = -1;
        double shortestDistance = -1;

        if (ARToolKit.getInstance().queryMarkerVisible(mark0))
        {
            for (int mark1 : markers)
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

    int getNearestMarkerExcluding(final int mark0, final ArrayList<Integer> marksX)
    {
        int    nearest          = -1;
        double shortestDistance = -1;

        if (ARToolKit.getInstance().queryMarkerVisible(mark0))
        {
            for (int mark1 : markers)
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

    float getMarkerDirection(final int mark0)
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
            Logger.logI(output, 5);
        }

        res = ((mark0TransInfo[4] >= 0) ? (float) Math.acos(mark0TransInfo[0]) : (float) (Math.PI +
                                                                                          Math.acos(
                                                                                              -mark0TransInfo[0])));

        res *= (180 / Math.PI);
        Logger.logI("Flat angle in degrees: " + res, 5);

        return ((Float.isNaN(res)) ? 0 : res);
    }

    float getAngleBetweenMarkers(final int mark0, final int mark1)
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
        for (float aResVector : resVector)
        {
            output += aResVector + " ";
        }
        Logger.logI(output, 5);

        Matrix.multiplyMV(resVector,
                          0,
                          mark0TransInv,
                          0,
                          resVector,
                          0);

        output = "ResVector After Multiply: ";
        for (float aResVector : resVector)
        {
            output += aResVector + " ";
        }

        float resAngle = (float) Math.atan2(resVector[0], resVector[1]);
        resAngle *= (180 / Math.PI);
        resAngle = ((resAngle < 0) ? (360 + resAngle) : resAngle);

        Logger.logI(output, 5);

        return ((Float.isNaN(resAngle)) ? 0 : resAngle);
    }

    void setPlayerMarker(final int playerID, final int markerID)
    {
        playerMarkerIDs.set(playerID, markerID);

        knownMarkers.add(markerID);
    }

    /**
     * Applies an effect texture on an {@link Actor} for {@code duration}.
     *
     * @param roomId   int: The reference ID of the AR marker to which the
     *                 {@link ARRoom} containing the desired {@link Actor} to
     *                 apply the effect to.
     * @param actorId  int: The logical reference ID of the desired {@link
     *                 Actor} to apply the effect to.
     * @param effect   Name of the effect to apply
     * @param duration How long the effect should last in milliseconds
     */
    private void setActorEffect(final int roomId,
                                final int actorId,
                                final String effect,
                                final long duration)
    {
        final ARRoom room = arRooms.get(roomId);
        room.addEffect(actorId, models.get(effect));
        Runnable removeEffect = new Runnable()
        {
            @Override
            public void run()
            {
                room.removeEffect(actorId);
            }
        };
        // Remove effect after the duration has passed
        handler.postDelayed(removeEffect, duration);
    }

    private void setActorsEffects(final int roomId,
                                  final boolean players,
                                  String effect,
                                  long duration)
    {
        final ARRoom room = arRooms.get(roomId);

        if (room != null)
        {
            Set<Integer> actors = (players) ? room.getPlayers() : room.getEnemies();

            for (final int id : actors)
            {
                room.addEffect(id, models.get(effect));
                Runnable removeEffect = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        room.removeEffect(id);
                    }
                };
                // Remove effect after the duration has passed
                handler.postDelayed(removeEffect, duration);
            }
        }
    }

    /**
     * Create an AR representation of a {@link Room}, an {@link ARRoom},
     * anchored to a given AR marker reference ID and hosting walls matching
     * given descriptors, and add it to the {@link PortalRenderer
     * PortalRenderer's}{@link ARRoom} map.
     *
     * @param arRoomId        int: The reference ID of the AR marker the new
     *                        {@link ARRoom} is anchored to. Should match the
     *                        marker reference ID of exactly one {@link Room}.
     * @param wallDescriptors String[]: An array of descriptors which match
     *                        the names of {@link ARDrawableGLES20} drawables
     *                        to be used for the wall drawable 'slots' in the
     *                        {@link ARRoom}.
     *
     * @see ARRoom
     * @see Room
     * @see ARDrawableGLES20
     * @see PortalActivity#generateRoom(int)
     */
    void createRoom(final int arRoomId,
                    final String[] wallDescriptors)
    {
        ARRoom arRoom = new ARRoom();
        arRoom.setRoomModel(models.get("room_base"));
        for (short i = 0; i < wallDescriptors.length; ++i)
        {
            arRoom.setWall(i, models.get(wallDescriptors[i]));
        }
        arRooms.put(arRoomId, arRoom);

        knownMarkers.add(arRoomId);

        StringBuilder sb = new StringBuilder();

        for (int i : knownMarkers)
        {
            sb.append(i).append(", ");
        }

        Logger.logD(sb.toString());
    }

    /**
     * Updates the {@link ARDrawableGLES20} drawables in the corresponding wall
     * 'slots' of a given {@link ARRoom} based on given wall descriptors.
     *
     * @param arRoomId        int: The reference ID of the AR marker the
     *                        desired {@link ARRoom} is anchored to. Should
     *                        match the marker reference ID of exactly one
     *                        {@link Room}.
     * @param wallDescriptors String[]: An array of descriptors which match
     *                        the names of {@link ARDrawableGLES20} drawables
     *                        to be used for the wall drawable 'slots' in the
     *                        {@link ARRoom}.
     *
     * @see ARRoom
     * @see Room
     * @see ARDrawableGLES20
     * @see PortalActivity#postOpenDoorResult(int, int, int)
     */
    void updateRoomWalls(final int arRoomId,
                         final String[] wallDescriptors)
    {
        ARRoom arRoom = arRooms.get(arRoomId);
        for (short i = 0; i < wallDescriptors.length; ++i)
        {
            arRoom.setWall(i, models.get(wallDescriptors[i]));
        }
    }

    /**
     * Updates the alignment, or orientation of residents, of a given {@link
     * ARRoom}.
     * <p>
     * Alignment is given as an index representing a side of an {@link ARRoom},
     * or {@link Room}, and specifies that player {@link Actor} character
     * {@link ARDrawableGLES20} drawables should be lined up along that wall.
     * Non-player {@link Actor} character {@link ARDrawableGLES20} drawables,
     * then, should be lined up along the opposite wall.
     *
     * @param arRoomId int: The reference ID of the AR marker the desired
     *                 {@link ARRoom} is anchored to. Should match the marker
     *                 reference ID of exactly one {@link Room}.
     * @param side     short: The index of the side of the {@link ARRoom}
     *                 player character {@link ARDrawableGLES20} drawables
     *                 should line up along.
     *
     * @see ARRoom
     * @see Room
     * @see Actor
     * @see ARDrawableGLES20
     */
    void updateRoomAlignment(final int arRoomId, final short side)
    {
        ARRoom arRoom = arRooms.get(arRoomId);
        arRoom.setAlignment(side);
    }

    /**
     * Updates the representation of the resident {@link Actor Actors} of a
     * given {@link ARRoom} given the {@link Actor Actor's} logical reference
     * IDs, player or non- player flag, and their reference name and pose
     * delimited by a {@code :}.
     *
     * @param arRoomId  int: The reference ID of the AR marker the desired
     *                  {@link ARRoom} is anchored to. Should match the marker
     *                  reference ID of exactly one {@link Room}.
     * @param residents ConcurrentHashMap: A map associating the logical
     *                  reference IDs of the resident {@link Actor Actors} of
     *                  the desired {@link Room} with a Pair consisting of
     *                  their player or non-player flag and a string consisting
     *                  of their reference name and pose delimited by a {@code
     *                  :}.
     *
     * @see ARRoom
     * @see Room
     * @see Actor
     */
    void updateRoomResidents(final int arRoomId,
                             final ConcurrentHashMap<Integer, Pair<Boolean, String>> residents)
    {
        Logger.logD("enter trace");

        ARRoom arRoom = arRooms.get(arRoomId);
        arRoom.removeActors();
        for (int id : residents.keySet())
        {
            Pair<Boolean, String> resident = residents.get(id);

            String[] splitResident = resident.second.split(":");

            String name = splitResident[0];

            String pose = ((splitResident.length > 1) ? splitResident[1] : null);

            Logger.logI("adding actor:" + id + ":" + name + ":" + pose + " to arRoom:" + arRoomId,
                        2);

            if (name != null && models.keySet().contains(name))
            {
                if (resident.first)
                {
                    arRoom.addPlayer(id, models.get(name));
                }
                else
                {
                    arRoom.addEnemy(id, models.get(name));
                }

                if (pose != null)
                {
                    arRoom.setResidentPose(id, pose);
                }
            }
            else
            {
                if (resident.first)
                {
                    arRoom.addPlayer(id, models.get("error"));
                }
                else
                {
                    arRoom.addEnemy(id, models.get("error"));
                }
            }
        }

        Logger.logD("enter trace");
    }

    /**
     * Provides a presentation of {@link Actor} action by updating the pose of
     * an action source {@link Actor} and a target {@link Actor}, if
     * applicable, in a given {@link ARRoom}. The action poses will expire
     * after {@code length} and, if present, a Handler will be used to call
     * back to this {@link PortalRenderer PortalRenderer's} associated {@link
     * GameController}.
     *
     * @param arRoomId     int: The reference ID of the AR marker the desired
     *                     {@link ARRoom} in which the action is taking place
     *                     is anchored to. Should match the marker reference ID
     *                     of exactly one {@link Room}.
     * @param sourceId     int: The logical reference ID of the source {@link
     *                     Actor} of the action being shown.
     * @param targetId     int: The logical reference ID of the target {@link
     *                     Actor} at which the action being shown is directed,
     *                     or {@code -1} if the action does not have a specific
     *                     target.
     * @param length       int: The duration in milliseconds to present the
     *                     desired action.
     * @param actionType   String: A description of the action being shown,
     *                     e.g. {@code attack}, {@code defend},
     *                     {@code special:hurt}.
     *                     <p>
     *                     Note: {@link Special} actions must include a
     *                     description of the special type ({@code hurt} or
     *                     {@code help}, see {@link
     *                     GameMaster#getSpecialTypeDescriptor(Model, int)})
     *                     delimited by a {@code :}.
     * @param targetState  String: A description of the state of the target
     *                     {@link Actor} of the desired action, or {@code null}
     *                     if the action being shown does not have a specific
     *                     target {@link Actor}.
     * @param playerAction boolean: A flag indicating whether the source {@link
     *                     Actor} of the action being shown is a player
     *                     controlled {@link Actor}.
     *                     <ul>
     *                     <li>true: The source {@link Actor} is considered to
     *                     be player controlled.</li>
     *                     <li>false: The source {@link Actor} is not
     *                     considered to be player controlled.</li>
     *                     </ul>
     * @param forward      boolean: A flag indicating whether the action
     *                     requires its source and target (if applicable){@link
     *                     Actor Actors} in the 'forward' position - closer to
     *                     room center, directly opposite one another.
     *                     <ul>
     *                     <li>true: The action requires its source and target
     *                     (if applicable) {@link Actor Actors} in the
     *                     'forward' position.</li>
     *                     <li>false: The action DOES NOT require its source
     *                     and target (if applicable) {@link Actor Actors} in
     *                     the 'forward' position.</li>
     *                     </ul>
     *
     * @see Actor
     * @see ARRoom
     * @see ARPoseModel
     * @see GameMaster#getSpecialTypeDescriptor(Model, int)
     * @see PortalActivity#showAction(int, int, int, int, String, String, boolean, boolean, String)
     */
    void showAction(final int arRoomId,
                    final int sourceId,
                    final int targetId,
                    final long length,
                    final String actionType,
                    @Nullable final String targetState,
                    final boolean playerAction,
                    final boolean forward,
                    @Nullable final String desc)
    {
        Logger.logD("action:<" + actionType + ">");
        Logger.logD("action description:<" + desc + ">");

        if (mediaPlayer != null)
        {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        ARRoom arRoom = arRooms.get(arRoomId);
        if (forward)
        {
            if (playerAction)
            {
                arRoom.setForwardPlayer(sourceId);
                if (targetId > -1)
                {
                    arRoom.setForwardEnemy(targetId);
                }
            }
            else
            {
                arRoom.setForwardEnemy(sourceId);
                if (targetId > -1)
                {
                    arRoom.setForwardPlayer(targetId);
                }
            }
        }

        String[] splitAction = actionType.split("\\.");

        switch (splitAction[0])
        {
            case "attack":
                arRoom.setResidentPose(sourceId, "attack");
                if (targetId > -1)
                {
                    if (targetState != null && targetState.equals("defend"))
                    {
                        arRoom.setResidentPose(targetId, "defend");
                    }
                    else
                    {
                        arRoom.setResidentPose(targetId, "hurt");
                    }
                    setActorEffect(arRoomId, targetId, "spark", 1000);
                }
                if (desc != null)
                {
                    switch (desc)
                    {
                        case "knight":
                        case "soldier":
                        case "groblin":
                        case "groblinCommando":
                            if (targetState != null && targetState.equals("defend"))
                            {
                                if (soundEnabled)
                                {
                                    mediaPlayer = MediaPlayer.create(context, R.raw.metalic_prot);
                                    mediaPlayer.start();
                                }
                            }
                            else
                            {
                                if (soundEnabled)
                                {
                                    mediaPlayer = MediaPlayer.create(context, R.raw.metalic_vuln);
                                    mediaPlayer.start();
                                }
                            }
                            break;
                        case "wizard":
                            if (soundEnabled)
                            {
                                mediaPlayer = MediaPlayer.create(context, R.raw.hit);
                                mediaPlayer.start();
                            }
                            break;
                        case "ranger":
                            if (soundEnabled)
                            {
                                mediaPlayer = MediaPlayer.create(context, R.raw.arrow);
                                mediaPlayer.start();
                            }
                            break;
                        default:
                            if (soundEnabled)
                            {
                                mediaPlayer = MediaPlayer.create(context, R.raw.hit);
                                mediaPlayer.start();
                            }
                            break;
                    }
                }
                break;
            case "defend":
                arRoom.setResidentPose(sourceId, "defend");
                break;
            case "special":
                arRoom.setResidentPose(sourceId, "special");
                boolean players = false;
                if (splitAction.length > 1)
                {
                    switch (splitAction[1])
                    {
                        case "harm":
                            if (targetId != -1 && targetId != sourceId)
                            {
                                if (targetState != null && targetState.equals("defend"))
                                {
                                    arRoom.setResidentPose(targetId, "defend");
                                }
                                else
                                {
                                    arRoom.setResidentPose(targetId, "hurt");
                                }
                            }
                            players = !playerAction;
                            break;
                        case "help":
                            if (targetId != -1 && targetId != sourceId)
                            {
                                arRoom.setResidentPose(targetId, "heroic");
                            }
                            players = playerAction;
                            break;
                    }
                }
                if (desc != null)
                {
                    switch (desc)
                    {
                        case "chill":
                            if (soundEnabled)
                            {
                                mediaPlayer = MediaPlayer.create(context, R.raw.chill);
                                mediaPlayer.start();
                            }
                            setActorsEffects(arRoomId, players, "chill", 1000);
                            break;
                        case "flame_shot":
                            if (soundEnabled)
                            {
                                mediaPlayer = MediaPlayer.create(context, R.raw.flame_shot);
                                mediaPlayer.start();
                            }
                            setActorEffect(arRoomId, targetId, "flameShot", 1000);
                            break;
                        case "heal":
                        case "beef_increase":
                            if (soundEnabled)
                            {
                                mediaPlayer = MediaPlayer.create(context, R.raw.magic_hlp);
                                mediaPlayer.start();
                            }
                            setActorEffect(arRoomId, targetId, "heal", 1000);
                            break;
                        case "ignus_fatuus":
                            if (soundEnabled)
                            {
                                mediaPlayer = MediaPlayer.create(context, R.raw.magic_atk);
                                mediaPlayer.start();
                            }
                            setActorEffect(arRoomId, targetId, "ignusFatuus", 1000);
                            break;
                        case "pin":
                            if (soundEnabled)
                            {
                                mediaPlayer = MediaPlayer.create(context, R.raw.arrow);
                                mediaPlayer.start();
                            }
                            setActorEffect(arRoomId, targetId, "spark", 1000);
                            break;
                        case "multishot":
                            if (soundEnabled)
                            {
                                mediaPlayer = MediaPlayer.create(context, R.raw.multishot);
                                mediaPlayer.start();
                            }
                            setActorsEffects(arRoomId, players, "spark", 1000);
                            break;
                        case "flurry_of_fists":
                        case "take_a_shit":
                            if (soundEnabled)
                            {
                                mediaPlayer = MediaPlayer.create(context, R.raw.multi_punch);
                                mediaPlayer.start();
                            }
                            setActorsEffects(arRoomId, players, "spark", 1000);
                            break;
                        case "suckerpunch":
                            if (soundEnabled)
                            {
                                mediaPlayer = MediaPlayer.create(context, R.raw.hit);
                                mediaPlayer.start();
                            }
                            setActorEffect(arRoomId, targetId, "spark", 1000);
                            break;
                        case "spear_toss":
                            if (soundEnabled)
                            {
                                mediaPlayer = MediaPlayer.create(context, R.raw.metalic_vuln);
                                mediaPlayer.start();
                            }
                            setActorEffect(arRoomId, targetId, "spark", 1000);
                    }
                }
                break;
            case "item":
                arRoom.setResidentPose(sourceId, "heroic");
                if (targetId != -1 && targetId != sourceId && splitAction.length > 1)
                {
                    switch (splitAction[1])
                    {
                        case "harm":
                            if (targetState != null && targetState.equals("defend"))
                            {
                                arRoom.setResidentPose(targetId, "defend");
                            }
                            else
                            {
                                arRoom.setResidentPose(targetId, "hurt");
                            }
                            break;
                        case "help":
                            arRoom.setResidentPose(targetId, "heroic");
                            break;
                    }
                }
                break;
            case "door":
                if (soundEnabled)
                {
                    mediaPlayer = MediaPlayer.create(context, R.raw.open_door);
                    mediaPlayer.start();
                }
                break;
            default:
                break;
        }

        if (handler != null)
        {
            Runnable actionFinisher = new Runnable()
            {
                @Override
                public void run()
                {
                    concludeAction(sourceId);
                }
            };
            handler.postDelayed(actionFinisher, length);
        }
    }

    private void concludeAction(final int sourceId)
    {
        gameController.onFinishedAction(sourceId);
    }

    public static void setNewMarkerListener(NewMarkerListener newMarkerListener)
    {
        PortalRenderer.newMarkerListener = newMarkerListener;
    }

    public static void setLookingForNewMarkers(boolean lookingForNewMarkers)
    {
        PortalRenderer.lookingForNewMarkers = lookingForNewMarkers;
    }

    public static void addKnownMarker(int id)
    {
        if (!knownMarkers.contains(id))
        {
            knownMarkers.add(id);
        }
    }

    public void setCheckingNearestRoomMarker(boolean check)
    {
        checkingNearestRoomMarker = check;
    }

    public void setPlayerMarker(int mark)
    {
        playerMarker = mark;
    }

    public void setPlayerRoomMarker(int mark)
    {
        playerRoomMarker = mark;
    }

    interface NewMarkerListener
    {
        void newMarker(int marker);

        void newNearestRoomMarker(int marker, int updateId);
    }

    public void toggleSound()
    {

    }
}
