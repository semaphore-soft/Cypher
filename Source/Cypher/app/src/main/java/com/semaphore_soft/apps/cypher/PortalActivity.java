package com.semaphore_soft.apps.cypher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.game.Actor;
import com.semaphore_soft.apps.cypher.game.ActorController;
import com.semaphore_soft.apps.cypher.game.GameController;
import com.semaphore_soft.apps.cypher.game.GameMaster;
import com.semaphore_soft.apps.cypher.game.Item;
import com.semaphore_soft.apps.cypher.game.Model;
import com.semaphore_soft.apps.cypher.game.Room;
import com.semaphore_soft.apps.cypher.game.Special;
import com.semaphore_soft.apps.cypher.networking.ClientService;
import com.semaphore_soft.apps.cypher.networking.NetworkConstants;
import com.semaphore_soft.apps.cypher.networking.ResponseReceiver;
import com.semaphore_soft.apps.cypher.networking.ServerService;
import com.semaphore_soft.apps.cypher.ui.UIListener;
import com.semaphore_soft.apps.cypher.ui.UIPortalActivity;
import com.semaphore_soft.apps.cypher.ui.UIPortalOverlay;
import com.semaphore_soft.apps.cypher.utils.CollectionManager;
import com.semaphore_soft.apps.cypher.utils.GameStatLoader;
import com.semaphore_soft.apps.cypher.utils.Logger;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;

import java.util.ArrayList;

import static com.semaphore_soft.apps.cypher.utils.CollectionManager.getNextID;

/**
 * Created by rickm on 11/9/2016.
 */

public class PortalActivity extends ARActivity implements PortalRenderer.NewMarkerListener,
                                                          ResponseReceiver.Receiver,
                                                          UIListener,
                                                          GameController
{
    private UIPortalActivity uiPortalActivity;
    private UIPortalOverlay  uiPortalOverlay;

    private PortalRenderer renderer;

    private static ResponseReceiver responseReceiver;
    private static ServerService    serverService;
    private static ClientService    clientService;
    private static boolean mServerBound  = false;
    private static boolean mClientBound  = false;
    private static boolean sendHeartbeat = true;
    private static Handler handler       = new Handler();

    private static boolean host;

    private static int    playerId;
    private static String characterName;

    private static final Model model = new Model();

    private static boolean turn;
    private static int     turnId;

    private Runnable heartbeat = new Runnable()
    {
        @Override
        public void run()
        {
            if (sendHeartbeat)
            {
                serverService.writeAll(NetworkConstants.GAME_HEARTBEAT);
                handler.postDelayed(heartbeat, NetworkConstants.HEARTBEAT_DELAY);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState); //Calls ARActivity's actor, abstract class of ARBaseLib
        setContentView(R.layout.empty);

        //setup ui
        uiPortalActivity = new UIPortalActivity(this);
        ((FrameLayout) this.findViewById(R.id.empty)).addView(uiPortalActivity);
        uiPortalActivity.setUIListener(this);

        uiPortalOverlay = new UIPortalOverlay(this);
        ((FrameLayout) this.findViewById(R.id.overlay_frame)).addView(uiPortalOverlay);
        uiPortalOverlay.setUIListener(this);

        //setup ar 3d graphics
        renderer = new PortalRenderer();
        renderer.setContext(this);
        PortalRenderer.setGameController(this);

        //setup broadcast networking service broadcast receiver
        responseReceiver = new ResponseReceiver();
        responseReceiver.setListener(this);
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(responseReceiver, NetworkConstants.getFilter());

        //get this user's game roles passed down from the previous activity
        host = getIntent().getBooleanExtra("host", false);
        playerId = getIntent().getIntExtra("player", 0);
        characterName = getIntent().getExtras().getString("character", "knight");

        GameMaster.setModel(model);

        ActorController.setGameController(this);

        PortalRenderer.setHandler(handler);

        turn = host;
        turnId = 0;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (host)
        {
            // Bind to ServerService
            Intent intent = new Intent(this, ServerService.class);
            bindService(intent, mServerConnection, Context.BIND_AUTO_CREATE);
            handler.postDelayed(heartbeat, NetworkConstants.HEARTBEAT_DELAY);
        }
        else
        {
            // Bind to ClientService
            Intent intent = new Intent(this, ClientService.class);
            bindService(intent, mClientConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        // Unbind from the services
        if (mServerBound)
        {
            unbindService(mServerConnection);
            mServerBound = false;
        }
        else if (mClientBound)
        {
            unbindService(mClientConnection);
            mClientBound = false;
        }
        sendHeartbeat = false;
    }

    //pass our rendering program to the ar framework
    @Override
    protected ARRenderer supplyRenderer()
    {
        return renderer;
    }

    //pass the the frame to draw the camera feed and
    //the ar graphics within to the ar framework
    @Override
    protected FrameLayout supplyFrameLayout()
    {
        return (FrameLayout) this.findViewById(R.id.portal_frame);
    }

    @Override
    public void onCommand(final String cmd)
    {
        System.out.println("portal activity received commend: " + cmd);

        if (cmd.startsWith("cmd_btn"))
        {
            switch (cmd)
            {
                case "cmd_btnPlayerMarkerSelect":
                    if (selectPlayerMarker())
                    {
                        if (host)
                        {
                            uiPortalOverlay.overlayStartMarkerSelect();
                        }
                        else
                        {
                            //wait for host
                        }
                    }
                    break;
                case "cmd_btnStartMarkerSelect":
                    if (selectStartMarker())
                    {
                        uiPortalOverlay.overlayAction();
                    }
                    break;
                case "cmd_btnEndTurn":
                    moveActor(playerId);
                    break;
                case "cmd_btnGenerateRoom":
                    generateRoom();
                    break;
                case "cmd_btnOpenDoor":
                    if (openDoor())
                    {
                        uiPortalOverlay.overlayAction();
                    }
                    break;
                case "cmd_btnAttack":
                    uiPortalOverlay.setEnemyTargets(GameMaster.getEnemyTargets(playerId));
                    uiPortalOverlay.setSelectMode(UIPortalOverlay.E_SELECT_MODE.ATTACK_TARGET);
                    uiPortalOverlay.overlayEnemyTargetSelect();
                    break;
                case "cmd_btnDefend":
                    GameMaster.setActorState(playerId, Actor.E_STATE.DEFEND);
                    renderer.updateRoomResidents(GameMaster.getActorRoom(playerId),
                                                 model.getActors());
                    Toast.makeText(getApplicationContext(),
                                   "Success",
                                   Toast.LENGTH_SHORT).show();

                    renderer.showAction(GameMaster.getActorRoom(playerId),
                                        model.getActors(),
                                        playerId,
                                        -1,
                                        1000,
                                        "defend",
                                        false);
                    break;
                case "cmd_btnSpecial":
                    uiPortalOverlay.setEnemyTargets(GameMaster.getEnemyTargets(playerId));
                    uiPortalOverlay.setPlayerTargets(GameMaster.getPlayerTargets(playerId));
                    uiPortalOverlay.setSpecials(GameMaster.getSpecials(playerId));
                    uiPortalOverlay.setSelectMode(UIPortalOverlay.E_SELECT_MODE.SPECIAL);
                    uiPortalOverlay.overlaySpecialSelect();
                default:
                    break;
            }
        }
        else
        {
            String[] splitCmd    = cmd.split("_");
            String[] splitAction = splitCmd[1].split(";");

            String[] action1 = splitAction[0].split(":");

            if (action1[0].equals("attack"))
            {
                int targetId = Integer.parseInt(action1[1]);
                attack(playerId, targetId);
            }
            else if (action1[0].equals("special"))
            {
                int specialId = Integer.parseInt(action1[1]);

                if (splitAction.length == 1)
                {
                    performSpecial(playerId, specialId);
                }
                else
                {
                    String[] action2 = splitAction[1].split(":");

                    if (action2[0].equals("target"))
                    {
                        int targetId = Integer.parseInt(action2[1]);
                        performSpecial(playerId, targetId, specialId);
                    }
                }
            }
        }
    }

    @Override
    public void handleRead(final String msg, final int readFrom)
    {
        Toast.makeText(this, "Read: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handleStatus(final String msg, int readFrom)
    {
        Toast.makeText(this, "Status: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handleError(final String msg, int readFrom)
    {
        Toast.makeText(this, "Error: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void newMarker(final int marker)
    {

    }

    private int getFirstUnreservedMarker()
    {
        ArrayList<Integer> marksX = new ArrayList<>();
        for (Actor actor : model.getActors().values())
        {
            marksX.add(actor.getMarker());
        }
        for (Room room : model.getRooms().values())
        {
            marksX.add(room.getMarker());
        }

        int foundMarker = renderer.getFirstMarkerExcluding(marksX);

        if (foundMarker > -1)
        {
            return foundMarker;
        }

        return -1;
    }

    private int getNearestNonPlayerMarker(final int mark0)
    {
        ArrayList<Integer> actorMarkers = new ArrayList<>();
        for (int id : model.getActors().keySet())
        {
            actorMarkers.add(model.getActors().get(id).getMarker());
        }

        int foundMarker = renderer.getNearestMarkerExcluding(mark0, actorMarkers);

        if (foundMarker > -1)
        {
            return foundMarker;
        }

        return -1;
    }

    private int getNearestNonPlayerMarkerExcluding(final int mark0, final ArrayList<Integer> marksX)
    {
        for (Actor actor : model.getActors().values())
        {
            marksX.add(actor.getMarker());
        }

        int foundMarker = renderer.getNearestMarkerExcluding(mark0, marksX);

        if (foundMarker > -1)
        {
            return foundMarker;
        }

        return -1;
    }

    private boolean selectPlayerMarker()
    {
        int mark = getFirstUnreservedMarker();
        if (mark > -1)
        {
            Actor actor = new Actor(playerId, characterName, mark);
            GameStatLoader.loadActorStats(actor,
                                          characterName,
                                          model.getSpecials(),
                                          getApplicationContext());
            model.getActors().put(playerId, actor);
            renderer.setPlayerMarker(playerId, mark);

            Item item = GameStatLoader.loadItemStats("delightful_bread",
                                                     model.getItems(),
                                                     model.getSpecials(),
                                                     getApplicationContext());

            Toast.makeText(getApplicationContext(),
                           "Player Marker Set",
                           Toast.LENGTH_SHORT)
                 .show();

            return true;
        }
        else
        {
            Toast.makeText(getApplicationContext(),
                           "Failed to Find Marker",
                           Toast.LENGTH_SHORT)
                 .show();

            return false;
        }
    }

    private boolean selectStartMarker()
    {
        int mark = getFirstUnreservedMarker();
        if (mark > -1)
        {
            //generate a new, placed room
            int  roomID = getNextID(model.getRooms());
            Room room   = new Room(roomID, mark, true);
            model.getRooms().put(roomID, room);
            renderer.createRoom(room);

            //place every player actor in that room
            for (Actor actor : model.getActors().values())
            {
                if (actor.isPlayer())
                {
                    actor.setRoom(roomID);
                    room.addActor(actor.getId());
                }
            }

            renderer.updateRoomResidents(room, model.getActors());

            model.getMap().init(roomID);

            Toast.makeText(getApplicationContext(),
                           "Starting Room Established",
                           Toast.LENGTH_SHORT)
                 .show();

            return true;
        }
        else
        {
            Toast.makeText(getApplicationContext(),
                           "Failed to Find Valid Marker",
                           Toast.LENGTH_SHORT)
                 .show();

            return false;
        }
    }

    private void moveActor(final int actorId)
    {
        int nearestMarkerId =
            getNearestNonPlayerMarker(GameMaster.getActorMakerId(actorId));
        if (nearestMarkerId > -1)
        {
            if (GameMaster.getMarkerAttachment(nearestMarkerId) == 1)
            {
                int startRoomId = GameMaster.getActorRoomId(actorId);
                int endRoomId   = GameMaster.getIdByMarker(nearestMarkerId);

                int res = GameMaster.moveActor(actorId, endRoomId);

                postMoveResult(actorId, startRoomId, endRoomId, res);
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(),
                           "Couldn't Find Valid Room",
                           Toast.LENGTH_SHORT)
                 .show();
        }
    }

    private void postMoveResult(final int actorId,
                                final int startRoomId,
                                final int endRoomId,
                                final int res)
    {
        if (actorId == playerId)
        {
            switch (res)
            {
                case 2:
                    Toast.makeText(this, "Moved to starting room", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(this, "Remaining in room", Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    Toast.makeText(this, "Moved to new room", Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    Toast.makeText(this, "Can't move, no door to room", Toast.LENGTH_SHORT).show();
                    break;
                case -2:
                    Toast.makeText(this, "Can't move, room not adjacent", Toast.LENGTH_SHORT)
                         .show();
                    break;
                case -3:
                    Toast.makeText(this, "Can't move, room not placed", Toast.LENGTH_SHORT).show();
                    break;
                case -4:
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        if (res >= 0)
        {
            if (startRoomId > -1)
            {
                renderer.updateRoomResidents(GameMaster.getRoom(startRoomId), model.getActors());
            }
            if (endRoomId > -1)
            {
                if (GameMaster.getPlayersInRoom(endRoomId) == 1)
                {
                    renderer.updateRoomAlignment(GameMaster.getRoom(endRoomId),
                                                 GameMaster.getSideOfRoomFrom(startRoomId,
                                                                              endRoomId));
                }
                renderer.updateRoomResidents(GameMaster.getRoom(endRoomId), model.getActors());
            }
        }
    }

    private boolean generateRoom()
    {
        int mark = getFirstUnreservedMarker();
        if (mark > -1)
        {
            Room room =
                GameMaster.generateRoom(this, CollectionManager.getNextID(model.getRooms()), mark);

            renderer.createRoom(room);

            Toast.makeText(getApplicationContext(),
                           "New Room Generated",
                           Toast.LENGTH_SHORT)
                 .show();

            return true;
        }
        else
        {
            Toast.makeText(getApplicationContext(),
                           "Couldn't Find Valid Marker",
                           Toast.LENGTH_SHORT)
                 .show();

            return false;
        }
    }

    private short getWallFromAngle(final float angle)
    {
        if (angle > 315 || angle <= 45)
        {
            return Room.WALL_TOP;
        }
        else if (angle > 45 && angle <= 135)
        {
            return Room.WALL_RIGHT;
        }
        else if (angle > 135 && angle <= 225)
        {
            return Room.WALL_BOTTOM;
        }
        else
        {
            return Room.WALL_LEFT;
        }
    }

    private boolean openDoor()
    {
        int startRoomId = GameMaster.getActorRoomId(playerId);

        ArrayList<Integer> placedRoomMarkers = GameMaster.getPlacedRoomMarkerIds();

        int nearestMarkerID =
            getNearestNonPlayerMarkerExcluding(GameMaster.getRoomMarkerId(startRoomId),
                                               placedRoomMarkers);
        if (nearestMarkerID > -1)
        {
            if (GameMaster.getMarkerAttachment(nearestMarkerID) == 1)
            {
                int endRoomId = GameMaster.getIdByMarker(nearestMarkerID);

                float angle0 =
                    renderer.getAngleBetweenMarkers(GameMaster.getRoomMarkerId(startRoomId),
                                                    nearestMarkerID);
                float angle1 = renderer.getAngleBetweenMarkers(nearestMarkerID,
                                                               GameMaster.getRoomMarkerId(
                                                                   startRoomId));

                short sideOfStartRoom = getWallFromAngle(angle0);
                short sideOfEndRoom   = getWallFromAngle(angle1);

                int res =
                    GameMaster.openDoor(startRoomId, endRoomId, sideOfStartRoom, sideOfEndRoom);

                if (res >= 0)
                {
                    postOpenDoorResult(endRoomId, res);

                    return true;
                }

                return false;
            }
            else
            {
                Toast.makeText(getApplicationContext(),
                               "Error: Tried to Open Door to a Room Which does not Exist\nPlease Generate the Room First",
                               Toast.LENGTH_SHORT)
                     .show();

                return false;
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(),
                           "Couldn't Find Valid Marker",
                           Toast.LENGTH_SHORT)
                 .show();

            return false;
        }
    }

    private void postOpenDoorResult(final int endRoomId, final int res)
    {
        switch (res)
        {
            case 0:
                break;
            case -1:
                break;
        }

        if (res >= 0)
        {
            Room room = GameMaster.getRoom(endRoomId);
            renderer.updateRoomWalls(room);

            ArrayList<Integer> adjacentRoomIds = GameMaster.getAdjacentRoomIds(endRoomId);
            for (int id : adjacentRoomIds)
            {
                Room adjacentRoom = GameMaster.getRoom(id);
                renderer.updateRoomWalls(adjacentRoom);
            }
        }
    }

    private void attack(final int attackerId, final int defenderId)
    {
        int res = GameMaster.attack(attackerId, defenderId);

        postAttackResults(attackerId, defenderId, res);
    }

    private void postAttackResults(final int attackerId, final int defenderId, final int res)
    {
        if (attackerId == playerId)
        {
            switch (res)
            {
                case 1:
                    Toast.makeText(this, "Killed target", Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }

        if (res >= 0)
        {
            Room room = GameMaster.getActorRoom(attackerId);

            renderer.showAction(room,
                                model.getActors(),
                                attackerId,
                                defenderId,
                                1000,
                                "attack",
                                true);
        }
    }

    private void performSpecial(final int sourceId, final int specialId)
    {
        int res = GameMaster.special(sourceId, specialId);

        postSpecialResult(sourceId, -1, specialId, res);
    }

    private void performSpecial(final int sourceId, final int targetId, final int specialId)
    {
        int res = GameMaster.special(sourceId, targetId, specialId);

        postSpecialResult(sourceId, targetId, specialId, res);
    }

    private void postSpecialResult(final int sourceId,
                                   final int targetId,
                                   final int specialId,
                                   final int res)
    {
        if (sourceId == playerId)
        {
            switch (res)
            {
                case 1:
                    Toast.makeText(this, "Killed target", Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    Toast.makeText(this, "You don't have enough energy", Toast.LENGTH_SHORT).show();
                    break;
                case -2:
                    Toast.makeText(this, "No valid targets", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }

        if (res >= 0)
        {
            Room room = GameMaster.getActorRoom(sourceId);
            //renderer.updateRoomResidents(room, model.getActors());

            String specialType = GameMaster.getSpecialType(specialId);

            renderer.showAction(room,
                                model.getActors(),
                                sourceId,
                                targetId,
                                1000,
                                "special:" + specialType,
                                specialType.equals("harm"));
        }
    }

    private ArrayList<Actor> getActorsInRoom(final Room room, final boolean getPlayers)
    {
        ArrayList<Actor> res = new ArrayList<>();

        for (int actorId : room.getResidentActors())
        {
            Actor actor = model.getActors().get(actorId);
            if ((getPlayers && actor.isPlayer()) || (!getPlayers && !actor.isPlayer()))
            {
                res.add(actor);
            }
        }

        return res;
    }

    private void performPlayerSpecial(final Actor actor, final Special special)
    {
        Room             room    = model.getRooms().get(actor.getRoom());
        ArrayList<Actor> targets = new ArrayList<>();

        switch (special.getTargetingType())
        {
            case SINGLE_PLAYER:
            {
                targets = getActorsInRoom(room, true);
                if (targets.size() < 1)
                {
                    Toast.makeText(getApplicationContext(),
                                   "Nothing to use special on here",
                                   Toast.LENGTH_SHORT).show();
                }
                else if (targets.size() == 1)
                {
                    actor.performSpecial(special, targets.get(0));
                }
                else
                {
                    //TODO display target selection
                    Toast.makeText(getApplicationContext(),
                                   "Multiple possible targets, not yet implemented",
                                   Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case SINGLE_NON_PLAYER:
            {
                targets = getActorsInRoom(room, false);
                if (targets.size() < 1)
                {
                    Toast.makeText(getApplicationContext(),
                                   "Nothing to use special on here",
                                   Toast.LENGTH_SHORT).show();
                }
                else if (targets.size() == 1)
                {
                    if (!actor.performSpecial(special, targets.get(0)))
                    {
                        Toast.makeText(getApplicationContext(),
                                       "Not enough SP",
                                       Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    //TODO display target selection
                    Toast.makeText(getApplicationContext(),
                                   "Multiple possible targets, not yet implemented",
                                   Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case AOE_PLAYER:
            {
                targets = getActorsInRoom(room, true);
                if (targets.size() < 1)
                {
                    Toast.makeText(getApplicationContext(),
                                   "Nothing to use special on here",
                                   Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (!actor.performSpecial(special, targets))
                    {
                        Toast.makeText(getApplicationContext(),
                                       "Not enough SP",
                                       Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case AOE_NON_PLAYER:
            {
                targets = getActorsInRoom(room, false);
                if (targets.size() < 1)
                {
                    Toast.makeText(getApplicationContext(),
                                   "Nothing to use special on here",
                                   Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (!actor.performSpecial(special, targets))
                    {
                        Toast.makeText(getApplicationContext(),
                                       "Not enough SP",
                                       Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void feedback(final String message)
    {
        Runnable uiUpdate = new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        };

        runOnUiThread(uiUpdate);
    }

    @Override
    public void onActorAction(final int sourceId, final int targetId, final String action)
    {
        Logger.logD("enter trace");

        Logger.logD("received action for actor " + sourceId);

        Room room = GameMaster.getActorRoom(sourceId);

        Logger.logD("showing action in renderer");

        renderer.showAction(room,
                            model.getActors(),
                            sourceId,
                            targetId,
                            1000,
                            action,
                            true);

        Logger.logD("exit trace");
    }

    @Override
    public void turnPassed(final int turnId)
    {
        Logger.logD("enter trace");

        Logger.logD("ended non-player turn " + turnId);

        Logger.logD("exit trace");
    }

    @Override
    public void onFinishedLoading()
    {
        Runnable uiUpdate = new Runnable()
        {
            @Override
            public void run()
            {
                uiPortalOverlay.overlayPlayerMarkerSelect();
            }
        };

        runOnUiThread(uiUpdate);
    }

    @Override
    public void onFinishedAction(final int actorId)
    {
        Logger.logD("enter trace");

        Logger.logD("finished action for " + actorId);

        Logger.logD("removing dead actors");
        GameMaster.removeDeadActors();
        Logger.logD("updating renderer room");
        renderer.updateRoomResidents(GameMaster.getActorRoom(actorId), model.getActors());

        if (turn)
        {
            turn = false;
        }

        Runnable turnDelayer = new Runnable()
        {
            @Override
            public void run()
            {
                postTurn();
            }
        };
        handler.postDelayed(turnDelayer, 1000);
        Logger.logD("postTurn posted to execute in " + 1000);

        Logger.logD("exit trace");
    }

    private void postTurn()
    {
        Logger.logD("enter trace");

        turnId = CollectionManager.getNextIdFromId(turnId, model.getActors());

        Logger.logD("next turn is " + turnId);

        if (turnId == playerId)
        {
            turn = true;
        }
        else if (!GameMaster.getActorIsPlayer(turnId))
        {
            Logger.logD("turn id is not a player, taking turn for non-player actor " + turnId);

            ActorController.takeTurn(turnId);
        }

        Logger.logD("exit trace");
    }

    // Defines callbacks for service binding, passed to bindService()
    private ServiceConnection mServerConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            // We've bound to ServerService, cast the IBinder and get ServerService instance
            ServerService.LocalBinder binder = (ServerService.LocalBinder) iBinder;
            serverService = binder.getService();
            mServerBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mServerBound = false;
        }
    };

    private ServiceConnection mClientConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            // We've bound to ServerService, cast the IBinder and get ServerService instance
            ClientService.LocalBinder binder = (ClientService.LocalBinder) iBinder;
            clientService = binder.getService();
            mClientBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mClientBound = false;
        }
    };
}