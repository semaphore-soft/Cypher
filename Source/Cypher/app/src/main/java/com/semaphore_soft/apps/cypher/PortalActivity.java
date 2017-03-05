package com.semaphore_soft.apps.cypher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.game.Actor;
import com.semaphore_soft.apps.cypher.game.ActorController;
import com.semaphore_soft.apps.cypher.game.GameController;
import com.semaphore_soft.apps.cypher.game.GameMaster;
import com.semaphore_soft.apps.cypher.game.Model;
import com.semaphore_soft.apps.cypher.game.Room;
import com.semaphore_soft.apps.cypher.game.Special;
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
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

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
    private static boolean mServerBound  = false;
    private static boolean sendHeartbeat = true;
    private static Handler handler       = new Handler();

    private static int    playerId;
    private static String characterName;

    private static final Model model = new Model();

    private static boolean turn;
    private static int     turnId;

    private static ArrayList<Integer> reservedMarkers;

    private static HashMap<Integer, String> playerCharacterMap;

    private static boolean playerMarkerSelected = false;

    private static int numClients;
    private static int numClientsSelected = 0;

    // Runnable to send heartbeat signal to clients
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

        playerId = 0;
        characterName = getIntent().getStringExtra("character");

        GameMaster.setModel(model);

        ActorController.setModel(model);
        ActorController.setGameController(this);

        PortalRenderer.setHandler(handler);

        turn = true;
        turnId = 0;

        reservedMarkers = new ArrayList<>();

        playerCharacterMap = new HashMap<>();
        playerCharacterMap.put(playerId, characterName);

        numClients = getIntent().getExtras().getInt("num_clients", 0);

        int[]    playerIds        = getIntent().getIntArrayExtra("player_ids");
        String[] playerCharacters = getIntent().getStringArrayExtra("player_characters");

        if (playerIds != null && playerCharacters != null)
        {
            if (playerIds.length != playerCharacters.length)
            {
                Toast.makeText(this,
                               "player_ids length != player_characters length, abort",
                               Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            for (int i = 0; i < playerIds.length; ++i)
            {
                playerCharacterMap.put(playerIds[i], playerCharacters[i]);
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // Bind to ServerService
        Intent intent = new Intent(this, ServerService.class);
        bindService(intent, mServerConnection, Context.BIND_AUTO_CREATE);
        handler.postDelayed(heartbeat, NetworkConstants.HEARTBEAT_DELAY);
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

    /**
     * {@inheritDoc}
     *
     * @param cmd Command from UI interaction
     */
    @Override
    public void onCommand(final String cmd)
    {
        Logger.logI("portal activity received command: " + cmd, 3);

        if (cmd.startsWith("cmd_btn"))
        {
            switch (cmd)
            {
                case "cmd_btnPlayerMarkerSelect":
                    int firstUnreservedMarker = getFirstUnreservedMarker();
                    if (firstUnreservedMarker > -1 &&
                        selectPlayerMarker(playerId, characterName, firstUnreservedMarker))
                    {
                        Toast.makeText(getApplicationContext(),
                                       "Player Marker Set",
                                       Toast.LENGTH_SHORT)
                             .show();

                        serverService.writeAll("reserve;" + firstUnreservedMarker);
                        serverService.writeAll("attach;" + firstUnreservedMarker + ";" + playerId);

                        playerMarkerSelected = true;

                        if (numClientsSelected == numClients)
                        {
                            uiPortalOverlay.overlayStartMarkerSelect();
                        }
                        else
                        {
                            uiPortalOverlay.overlayWaitingForClients();
                        }
                    }
                    break;
                case "cmd_btnStartMarkerSelect":
                    if (selectStartMarker())
                    {
                        uiPortalOverlay.overlayAction();

                        serverService.writeAll(NetworkConstants.GAME_START);
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
                    uiPortalOverlay.setEnemyTargets(GameMaster.getNonPlayerTargets(playerId));
                    uiPortalOverlay.setSelectMode(UIPortalOverlay.E_SELECT_MODE.ATTACK_TARGET);
                    uiPortalOverlay.overlayEnemyTargetSelect();
                    break;
                case "cmd_btnDefend":
                    GameMaster.setActorState(playerId, Actor.E_STATE.DEFEND);
                    Room room = GameMaster.getActorRoom(playerId);

                    Toast.makeText(getApplicationContext(),
                                   "Success",
                                   Toast.LENGTH_SHORT).show();

                    if (room != null)
                    {
                        renderer.showAction(room.getMarker(),
                                            playerId,
                                            -1,
                                            1000,
                                            "defend",
                                            null,
                                            false);
                    }
                    break;
                case "cmd_btnSpecial":
                    uiPortalOverlay.setEnemyTargets(GameMaster.getNonPlayerTargets(playerId));
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

    /**
     * {@inheritDoc}
     *
     * @param msg      Message read from network
     * @param readFrom Device that message was received from
     */
    @Override
    public void handleRead(final String msg, final int readFrom)
    {
        Toast.makeText(this, "Read: " + msg + " from <" + readFrom + ">", Toast.LENGTH_SHORT)
             .show();

        if (msg.startsWith(NetworkConstants.PREFIX_MARK_REQUEST))
        {
            // Expect MarkerID of a marker that the client wants to attach to
            String[] splitMsg = msg.split(":");

            if (selectPlayerMarker(readFrom,
                                   playerCharacterMap.get(readFrom),
                                   Integer.parseInt(splitMsg[1])))
            {
                serverService.writeToClient(NetworkConstants.GAME_WAIT, readFrom);
                serverService.writeAll(NetworkConstants.PREFIX_RESERVE + splitMsg[1]);
                serverService.writeAll(
                    NetworkConstants.PREFIX_ATTACH + readFrom + ":" + splitMsg[1]);

                ++numClientsSelected;

                if (numClientsSelected == numClients && playerMarkerSelected)
                {
                    uiPortalOverlay.overlayStartMarkerSelect();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param msg      Status update
     * @param readFrom Device that update was received from
     */
    @Override
    public void handleStatus(final String msg, int readFrom)
    {
        Toast.makeText(this, "Status: " + msg + " from <" + readFrom + ">", Toast.LENGTH_SHORT)
             .show();
    }

    /**
     * {@inheritDoc}
     *
     * @param msg      Error message
     * @param readFrom Device that error was received from
     */
    @Override
    public void handleError(final String msg, int readFrom)
    {
        Toast.makeText(this, "Error: " + msg + " from <" + readFrom + ">", Toast.LENGTH_SHORT)
             .show();
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

    private boolean selectPlayerMarker(int playerId, String characterName, int mark)
    {
        if (mark > -1)
        {
            Actor actor = new Actor(playerId, characterName, mark);
            GameStatLoader.loadActorStats(actor,
                                          characterName,
                                          model.getSpecials(),
                                          getApplicationContext());
            model.getActors().put(playerId, actor);
            renderer.setPlayerMarker(playerId, mark);


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
            int  roomId = getNextID(model.getRooms());
            Room room   = new Room(roomId, mark, true);
            model.getRooms().put(roomId, room);

            String[] wallDescriptors = getWallDescriptors(roomId);

            renderer.createRoom(mark, wallDescriptors);

            //place every player actor in that room
            for (Actor actor : model.getActors().values())
            {
                if (actor.isPlayer())
                {
                    actor.setRoom(roomId);
                    room.addActor(actor.getId());
                }
            }

            renderer.updateRoomResidents(room.getMarker(), getResidents(roomId));

            model.getMap().init(roomId);

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
                Room room = GameMaster.getRoom(startRoomId);
                if (room != null)
                {
                    renderer.updateRoomResidents(room.getMarker(),
                                                 getResidents(startRoomId));
                }
            }
            if (endRoomId > -1)
            {
                if (GameMaster.getPlayersInRoom(endRoomId) == 1)
                {
                    Room room = GameMaster.getRoom(endRoomId);
                    if (room != null)
                    {
                        renderer.updateRoomAlignment(room.getMarker(),
                                                     GameMaster.getSideOfRoomFrom(startRoomId,
                                                                                  endRoomId));
                    }
                }
                Room room = GameMaster.getRoom(endRoomId);
                if (room != null)
                {
                    renderer.updateRoomResidents(room.getMarker(), getResidents(endRoomId));
                }
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

            String[] wallDescriptors = getWallDescriptors(room.getId());

            renderer.createRoom(mark, wallDescriptors);

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

    private static short getWallFromAngle(final float angle)
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
            if (room != null)
            {
                renderer.updateRoomWalls(room.getMarker(), getWallDescriptors(endRoomId));
            }

            ArrayList<Integer> adjacentRoomIds = GameMaster.getAdjacentRoomIds(endRoomId);
            for (int id : adjacentRoomIds)
            {
                Room adjacentRoom = GameMaster.getRoom(id);
                if (adjacentRoom != null)
                {
                    renderer.updateRoomWalls(adjacentRoom.getMarker(), getWallDescriptors(id));
                }
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
            Room  room     = GameMaster.getActorRoom(attackerId);
            Actor defender = GameMaster.getActor(defenderId);

            if (room != null)
            {
                renderer.showAction(room.getMarker(),
                                    attackerId,
                                    defenderId,
                                    1000,
                                    "attack",
                                    ((defender != null) ? (defender.getState() ==
                                                           Actor.E_STATE.DEFEND ? "defend" : null) : null),
                                    true);
            }
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
            Room  room   = GameMaster.getActorRoom(sourceId);
            Actor target = GameMaster.getActor(targetId);

            String specialType = GameMaster.getSpecialType(specialId);

            if (room != null)
            {
                renderer.showAction(room.getMarker(),
                                    sourceId,
                                    targetId,
                                    1000,
                                    "special:" + specialType,
                                    ((target != null) ? (target.getState() ==
                                                         Actor.E_STATE.DEFEND ? "defend" : null) : null),
                                    specialType.equals("harm"));
            }
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

        boolean forwardAction = (action.equals("attack") || action.equals("special:harm"));

        Room  room   = GameMaster.getActorRoom(sourceId);
        Actor target = GameMaster.getActor(targetId);

        Logger.logD("showing action in renderer");

        if (room != null)
        {
            renderer.showAction(room.getMarker(),
                                sourceId,
                                targetId,
                                1000,
                                action,
                                ((target != null) ? (target.getState() ==
                                                     Actor.E_STATE.DEFEND ? "defend" : null) : null),
                                forwardAction);
        }

        Logger.logD("exit trace");
    }

    @Override
    public void onActorMove(final int actorId, final int roomId)
    {
        Logger.logD("enter trace");

        Logger.logD("received move for actor " + actorId + " to room " + roomId);

        int startRoomId = GameMaster.getActorRoomId(actorId);

        GameMaster.moveActor(actorId, roomId);

        Room room = GameMaster.getRoom(startRoomId);
        if (room != null)
        {
            renderer.updateRoomResidents(room.getMarker(), getResidents(room.getId()));
        }
        room = GameMaster.getRoom(roomId);
        if (room != null)
        {
            renderer.updateRoomResidents(room.getMarker(), getResidents(room.getId()));
        }

        if (GameMaster.getPlayersInRoom(roomId) > 0)
        {
            Runnable turnDelayer = new Runnable()
            {
                @Override
                public void run()
                {
                    postTurn(actorId);
                }
            };
            handler.postDelayed(turnDelayer, 1000);
            Logger.logD("postTurn posted to execute in " + 1000);
        }
        else
        {
            postTurn(actorId);
        }

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

        Room room = GameMaster.getActorRoom(actorId);
        if (room != null)
        {
            renderer.updateRoomResidents(room.getMarker(), getResidents(room.getId()));
        }

        if (turn)
        {
            turn = false;
        }

        Runnable turnDelayer = new Runnable()
        {
            @Override
            public void run()
            {
                postTurn(actorId);
            }
        };
        handler.postDelayed(turnDelayer, 1000);
        Logger.logD("postTurn posted to execute in " + 1000);

        Logger.logD("exit trace");
    }

    private void postTurn(final int actorId)
    {
        Logger.logD("enter trace");

        Actor actor = GameMaster.getActor(actorId);
        if (actor != null)
        {
            actor.tick();
        }

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

    /**
     * Construct a list of String wall descriptors describing the walls of a
     * {@link Room} in a way that is meaningful to the {@link PortalRenderer}.
     *
     * @param roomId int: The logical reference ID of the desired {@link Room}.
     *
     * @return String[]: An array of {@link Room} wall descriptors meaningful
     * to the {@link PortalRenderer}.
     *
     * @see Room
     * @see PortalRenderer
     * @see PortalRenderer#createRoom(int, String[])
     * @see PortalRenderer#updateRoomWalls(int, String[])
     */
    @NonNull
    private String[] getWallDescriptors(final int roomId)
    {
        Room room = GameMaster.getRoom(roomId);

        String[] wallDescriptors = new String[4];

        if (room != null)
        {
            for (short i = 0; i < wallDescriptors.length; ++i)
            {
                switch (room.getWallType(i))
                {
                    case NO_DOOR:
                        wallDescriptors[i] = "room_wall";
                        break;
                    case DOOR_UNLOCKED:
                        wallDescriptors[i] = "room_door_unlocked";
                        break;
                    case DOOR_OPEN:
                        wallDescriptors[i] = "room_door_open";
                        break;
                    case DOOR_LOCKED:
                        wallDescriptors[i] = "room_door_locked";
                        break;
                }
            }
        }

        return wallDescriptors;
    }

    /**
     * Construct a map of {@link Actor} logical reference IDs to Pairs of a
     * boolean value representing whether or not that {@link Actor} is player
     * controlled and a String providing the reference name of that {@link
     * Actor} and the state of that {@link Actor} as a pose name meaningful to
     * the {@link PortalRenderer} separated by a colon (<code>:</code>).
     *
     * @param roomId int: The logical reference ID of the {@link Room} to to
     *               get the list of resident {@link Actor Actors} from.
     *
     * @return ConcurrentHashMap: A map indexing Pairs of a boolean value
     * representing whether or not an {@link Actor} is a player and a String
     * providing that {@link Actor Actor's} reference name and state separated
     * by a colon (<code>:</code>) by {@link Actor} logical reference ID.
     *
     * @see Actor
     * @see Room
     * @see PortalRenderer
     * @see PortalRenderer#updateRoomResidents(int, ConcurrentHashMap)
     */
    @NonNull
    private ConcurrentHashMap<Integer, Pair<Boolean, String>> getResidents(final int roomId)
    {
        ConcurrentHashMap<Integer, Pair<Boolean, String>> res = new ConcurrentHashMap<>();

        Room room = GameMaster.getRoom(roomId);

        if (room != null)
        {
            for (int id : room.getResidentActors())
            {
                Actor actor = GameMaster.getActor(id);

                if (actor != null)
                {
                    String name = actor.getName();

                    String pose = "default";

                    if (actor.getHealthCurrent() <= 0)
                    {
                        pose = "wounded";
                    }
                    else
                    {
                        switch (actor.getState())
                        {
                            case NEUTRAL:
                            case ATTACK:
                            case SPECIAL:
                                if (GameMaster.getActorIsPlayer(id))
                                {
                                    if (GameMaster.getEnemiesInRoom(room.getId()) > 0)
                                    {
                                        pose = "ready";
                                    }
                                    else
                                    {
                                        pose = "idle";
                                    }
                                }
                                else
                                {
                                    if (GameMaster.getPlayersInRoom(room.getId()) > 0)
                                    {
                                        pose = "ready";
                                    }
                                    else
                                    {
                                        pose = "idle";
                                    }

                                }
                                break;
                            case DEFEND:
                                pose = "defend";
                                break;
                        }
                    }

                    Pair<Boolean, String> residentDescriptor =
                        new Pair<>(GameMaster.getActorIsPlayer(id),
                                   name + ":" + pose);

                    res.put(id, residentDescriptor);
                }
            }
        }

        return res;
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
}