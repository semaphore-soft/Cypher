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
import com.semaphore_soft.apps.cypher.game.Entity;
import com.semaphore_soft.apps.cypher.game.GameController;
import com.semaphore_soft.apps.cypher.game.GameMaster;
import com.semaphore_soft.apps.cypher.game.Model;
import com.semaphore_soft.apps.cypher.game.Room;
import com.semaphore_soft.apps.cypher.game.Special;
import com.semaphore_soft.apps.cypher.networking.NetworkConstants;
import com.semaphore_soft.apps.cypher.networking.ResponseReceiver;
import com.semaphore_soft.apps.cypher.networking.Server;
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
 * @author scorple
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

    /**
     * {@inheritDoc}
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState); //Calls ARActivity's actor, abstract class of ARBaseLib
        setContentView(R.layout.empty);

        // Setup ui
        uiPortalActivity = new UIPortalActivity(this);
        ((FrameLayout) this.findViewById(R.id.empty)).addView(uiPortalActivity);
        uiPortalActivity.setUIListener(this);

        uiPortalOverlay = new UIPortalOverlay(this);
        ((FrameLayout) this.findViewById(R.id.overlay_frame)).addView(uiPortalOverlay);
        uiPortalOverlay.setUIListener(this);

        // Setup AR 3d graphics
        renderer = new PortalRenderer();
        renderer.setContext(this);
        PortalRenderer.setGameController(this);

        // Setup broadcast networking service broadcast receiver
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

        playerCharacterMap =
            (HashMap<Integer, String>) getIntent().getSerializableExtra("character_selection");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart()
    {
        super.onStart();
        // Bind to ServerService
        Intent intent = new Intent(this, ServerService.class);
        bindService(intent, mServerConnection, Context.BIND_AUTO_CREATE);
        handler.postDelayed(heartbeat, NetworkConstants.HEARTBEAT_DELAY);
        // Make sure we are able to reconnect if we were in the background
        Server.setReconnect(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop()
    {
        super.onStop();
        // Don't reconnect while we are in the background
        Server.setReconnect(false);
        // Unbind from the service
        if (mServerBound)
        {
            unbindService(mServerConnection);
            mServerBound = false;
        }
        sendHeartbeat = false;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    //pass our rendering program to the ar framework
    @Override
    protected ARRenderer supplyRenderer()
    {
        return renderer;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
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

                        serverService.writeAll(
                            NetworkConstants.PREFIX_RESERVE_PLAYER + firstUnreservedMarker);
                        serverService.writeAll(NetworkConstants.PREFIX_ATTACH + playerId + ":" +
                                               firstUnreservedMarker);

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
                    int newRoomMark =
                        getNearestNonPlayerMarker(GameMaster.getActorMakerId(playerId));
                    moveActor(playerId, newRoomMark);
                    break;
                case "cmd_btnGenerateRoom":
                    generateRoom(getFirstUnreservedMarker());
                    break;
                case "cmd_btnOpenDoor":
                    if (openDoor())
                    {
                        uiPortalOverlay.overlayAction();
                    }
                    break;
                case "cmd_btnAttack":
                    ArrayList<Pair<String, String>> attackOptions = new ArrayList<>();

                    for (int i : GameMaster.getNonPlayerTargetIds(playerId))
                    {
                        Actor target = GameMaster.getActor(i);
                        if (target != null)
                        {
                            Pair<String, String> targetPair =
                                new Pair<>(target.getDisplayName(),
                                           "cmd_attack:" + i);

                            attackOptions.add(targetPair);
                        }
                    }

                    uiPortalOverlay.overlaySelect(attackOptions);
                    break;
                case "cmd_btnDefend":
                    GameMaster.setActorState(playerId, Actor.E_STATE.DEFEND);
                    Room room = GameMaster.getActorRoom(playerId);

                    Toast.makeText(getApplicationContext(),
                                   "Success",
                                   Toast.LENGTH_SHORT).show();

                    if (room != null)
                    {
                        showAction(room.getMarker(),
                                   playerId,
                                   -1,
                                   1000,
                                   "defend",
                                   null,
                                   true,
                                   false);
                    }

                    uiPortalOverlay.overlayWaitingForTurn();
                    break;
                case "cmd_btnSpecial":
                    ArrayList<Pair<String, String>> specialOptions = new ArrayList<>();

                    for (Special special : GameMaster.getSpecials(playerId).values())
                    {
                        Pair<String, String> specialPair =
                            new Pair<>(special.getDisplayName(), "cmd_special:" + special.getId());

                        specialOptions.add(specialPair);
                    }

                    uiPortalOverlay.overlaySelect(specialOptions);
                    break;
                case "cmd_btnCancel":
                    uiPortalOverlay.overlayAction();
                    break;
                default:
                    break;
            }
        }
        else
        {
            String[] splitCmd    = cmd.split("_");
            String[] splitAction = splitCmd[1].split(":");

            if (splitAction[0].equals("attack"))
            {
                int targetId = Integer.parseInt(splitAction[1]);
                attack(playerId, targetId);
            }
            else if (splitAction[0].equals("special"))
            {
                int specialId = Integer.parseInt(splitAction[1]);

                Special.E_TARGETING_TYPE specialType =
                    GameMaster.getSpecialTargetingType(specialId);

                if (splitAction.length < 3)
                {
                    if (specialType == Special.E_TARGETING_TYPE.AOE_PLAYER || specialType ==
                                                                              Special.E_TARGETING_TYPE.AOE_NON_PLAYER)
                    {
                        performSpecial(playerId, specialId);
                    }
                    else
                    {
                        ArrayList<Pair<String, String>> targetOptions = new ArrayList<>();

                        if (specialType == Special.E_TARGETING_TYPE.SINGLE_NON_PLAYER)
                        {
                            for (Actor target : GameMaster.getNonPlayerTargets(playerId).values())
                            {
                                if (target != null)
                                {
                                    Pair<String, String> targetPair =
                                        new Pair<>(target.getDisplayName(),
                                                   "cmd_special:" + specialId + ":" +
                                                   target.getId());

                                    targetOptions.add(targetPair);
                                }
                            }
                        }
                        else
                        {
                            for (Actor target : GameMaster.getPlayerTargets(playerId).values())
                            {
                                if (target != null)
                                {
                                    Pair<String, String> targetPair =
                                        new Pair<>(target.getDisplayName(),
                                                   "cmd_special:" + specialId + ":" +
                                                   target.getId());

                                    targetOptions.add(targetPair);
                                }
                            }
                        }

                        uiPortalOverlay.overlaySelect(targetOptions);
                    }
                }
                else
                {
                    performSpecial(playerId, Integer.parseInt(splitAction[2]), specialId);
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
        if (msg.startsWith(NetworkConstants.PREFIX_MARK_REQUEST))
        {
            // Expect MarkerID of a marker that the client wants to attach to
            String[] splitMsg = msg.split(":");

            int mark = Integer.parseInt(splitMsg[1]);

            if (selectPlayerMarker(readFrom,
                                   playerCharacterMap.get(readFrom),
                                   mark))
            {
                serverService.writeToClient(NetworkConstants.GAME_WAIT, readFrom);
                serverService.writeToClient(NetworkConstants.PREFIX_ASSIGN_MARK + mark, readFrom);
                serverService.writeAll(NetworkConstants.PREFIX_RESERVE_PLAYER + splitMsg[1]);
                serverService.writeAll(
                    NetworkConstants.PREFIX_ATTACH + readFrom + ":" + splitMsg[1]);

                ++numClientsSelected;

                if (numClientsSelected == numClients && playerMarkerSelected)
                {
                    uiPortalOverlay.overlayStartMarkerSelect();
                }
            }
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_GENERATE_ROOM_REQUEST))
        {
            // Expect MarkerID of a marker that the client wants generate a room on
            String[] splitMsg = msg.split(":");

            int mark = Integer.parseInt(splitMsg[1]);

            generateRoom(mark);
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_MOVE_REQUEST))
        {
            // Expect MarkerID of the room that the client wants to move to
            String[] splitMsg = msg.split(":");

            int mark = Integer.parseInt(splitMsg[1]);
            moveActor(readFrom, mark);
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_OPEN_DOOR_REQUEST))
        {
            // Expect nearestMarkerID,
            // sideOfStartRoom,
            // sideOfEndRoom
            String[] splitMsg = msg.split(":");

            int startRoomId = GameMaster.getActorRoomId(readFrom);
            int endRoomId   = GameMaster.getRoomIdByMarkerId(Integer.parseInt(splitMsg[1]));

            int res =
                GameMaster.openDoor(startRoomId,
                                    endRoomId,
                                    Short.parseShort(splitMsg[2]),
                                    Short.parseShort(splitMsg[3]));

            if (res >= 0)
            {
                postOpenDoorResult(endRoomId, res);
                serverService.writeAll(NetworkConstants.PREFIX_PLACE_ROOM + splitMsg[1]);
            }
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_ACTION_REQUEST))
        {
            String[] splitMsg = msg.split(";");

            String[] splitCmd    = splitMsg[1].split("_");
            String[] splitAction = splitCmd[1].split(":");

            switch (splitAction[0])
            {
                case "attack":
                    int targetId = Integer.parseInt(splitAction[1]);
                    attack(readFrom, targetId);
                    break;
                case "defend":
                    GameMaster.setActorState(readFrom, Actor.E_STATE.DEFEND);
                    Room room = GameMaster.getActorRoom(readFrom);

                    Toast.makeText(getApplicationContext(),
                                   "Success",
                                   Toast.LENGTH_SHORT).show();

                    if (room != null)
                    {
                        showAction(room.getMarker(),
                                   readFrom,
                                   -1,
                                   1000,
                                   "defend",
                                   null,
                                   true,
                                   false);
                    }

                    serverService.writeToClient(NetworkConstants.GAME_TURN_OVER, readFrom);
                    break;
                case "special":
                    int specialId = Integer.parseInt(splitAction[1]);

                    Special.E_TARGETING_TYPE specialType =
                        GameMaster.getSpecialTargetingType(specialId);

                    if (splitAction.length < 3)
                    {
                        if (specialType == Special.E_TARGETING_TYPE.AOE_PLAYER || specialType ==
                                                                                  Special.E_TARGETING_TYPE.AOE_NON_PLAYER)
                        {
                            performSpecial(readFrom, specialId);
                        }
                    }
                    else
                    {
                        performSpecial(readFrom, Integer.parseInt(splitAction[2]), specialId);
                    }
                    break;
                default:
                    break;
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
        if (msg.equals(NetworkConstants.STATUS_SERVER_START))
        {
            serverService.writeAll(NetworkConstants.GAME_RECONNECT);
            if (turnId == playerId)
            {
                uiPortalOverlay.overlayAction();
            }
            else
            {
                uiPortalOverlay.overlayWaitingForTurn();
            }
        }
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
        if (msg.equals(NetworkConstants.ERROR_DISCONNECT_SERVER))
        {
            uiPortalOverlay.overlayDisconnect();
            serverService.writeAll(NetworkConstants.GAME_DISCONNECT);
        }
    }

    @Override
    public void newMarker(final int marker)
    {

    }

    /**
     * Get the reference ID of the first AR marker found by the {@link
     * PortalRenderer} which is not already reserved by a game object, or
     * {@code -1} if no unreserved markers are in view.
     *
     * @return int: The reference ID of the first AR marker found by the {@link
     * PortalRenderer} which is not already reserved by a game object, or
     * {@code -1} if no unreserved markers are in view.
     *
     * @see PortalRenderer
     * @see PortalRenderer#getFirstMarkerExcluding(ArrayList)
     * @see Actor
     * @see Actor#getMarker()
     * @see Room
     * @see Room#getMarker()
     */
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

    /**
     * Get the reference ID of the nearest visible AR marker to a given AR
     * marker via the {@link PortalRenderer} which is not associated with an
     * {@link Actor}, or {@code -1} if either the given marker is not visible
     * or there are no other markers in view.
     *
     * @param mark0 int: The reference ID of the desired AR marker to get the
     *              nearest marker to.
     *
     * @return int: The reference ID of the nearest visible AR marker to a
     * given AR marker via the {@link PortalRenderer} which is not associated
     * with an {@link Actor}, or {@code -1} if either the given marker is not
     * visible or there are no other markers in view.
     *
     * @see PortalRenderer
     * @see PortalRenderer#getNearestMarkerExcluding(int, ArrayList)
     * @see Actor
     * @see Actor#getMarker()
     */
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

    /**
     * Get the reference ID of the nearest visible AR marker to a given AR
     * marker via the {@link PortalRenderer} which is not associated with an
     * {@link Actor} and is not in the list of AR marker reference IDs
     * indicated as excluded, or {@code -1} if either the given marker is not
     * visible or there are no other markers in view.
     *
     * @param mark0  int: The reference ID of the desired AR marker to get the
     *               nearest marker to.
     * @param marksX ArrayList: A list of marker reference IDs to exclude when
     *               searching for the nearest marker to a given marker.
     *
     * @return int: the reference ID of the nearest visible AR marker to a
     * given AR marker via the {@link PortalRenderer} which is not associated
     * with an {@link Actor} and is not in the list of AR marker reference IDs
     * indicated as excluded, or {@code -1} if either the given marker is not
     * visible or there are no other markers in view.
     *
     * @see PortalRenderer
     * @see PortalRenderer#getNearestMarkerExcluding(int, ArrayList)
     * @see Actor
     * @see Actor#getMarker()
     */
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

    /**
     * Create an {@link Actor} with a given logical reference ID, using a given
     * actor reference name, anchored to a given AR marker. If successful, add
     * the {@link Actor} to the {@link Model} and update the {@link
     * PortalRenderer} appropriately.
     *
     * @param playerId      int: The desired logical reference ID to be used
     *                      for the created {@link Actor}.
     * @param characterName String: The reference name to be used in creating
     *                      a new {@link Actor}; will be used to pull in {@link
     *                      Actor} stats, etc.
     * @param mark          int: The reference ID of the AR marker to anchor
     *                      the new {@link Actor} to.
     *
     * @return boolean:
     * <ul>
     * <li>{@code true}: The given marker reference ID is valid and the {@link
     * Actor} is created.</li>
     * <li>{@code false}: The given marker reference ID is not valid and the
     * {@link Actor} is not created.</li>
     * </ul>
     *
     * @see Actor
     * @see Actor#Actor(int, String, int)
     * @see GameStatLoader
     * @see GameStatLoader#loadActorStats(Actor, String, ConcurrentHashMap, Context)
     * @see Model
     * @see Model#addActor(int, Actor)
     * @see PortalRenderer
     * @see PortalRenderer#setPlayerMarker(int, int)
     */
    private boolean selectPlayerMarker(int playerId, String characterName, int mark)
    {
        if (mark > -1 && GameMaster.getMarkerAttachment(mark) == -1)
        {
            Actor actor = new Actor(playerId, characterName, mark);
            GameStatLoader.loadActorStats(actor,
                                          characterName,
                                          model.getSpecials(),
                                          getApplicationContext());
            model.getActors().put(playerId, actor);
            renderer.setPlayerMarker(playerId, mark);

            if (PortalActivity.playerId != playerId)
            {
                String playerSpecials = "";

                for (Special special : actor.getSpecials().values())
                {
                    playerSpecials += "," + special.getId() + "." + special.getDisplayName() + "." +
                                      special.getTargetingType().name();
                }

                if (!playerSpecials.equals(""))
                {
                    playerSpecials = playerSpecials.substring(1);
                    serverService.writeToClient(
                        NetworkConstants.PREFIX_UPDATE_PLAYER_SPECIALS + playerSpecials, playerId);
                }
            }

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

    /**
     * Create a {@link Room} to use as the starting {@link Room} for all player
     * {@link Actor Actors} and place all player{@link Actor Actors} in that
     * {@link Room}. Starting {@link Room} will have unlocked doors on all
     * sides and no non-player {@link Actor Actors} or {@link Entity Entities}.
     * The starting {@link Room} will be added to the {@link Model}, considered
     * 'placed' and the {@link PortalRenderer} will be updated.
     *
     * @return boolean:
     * <ul>
     * <li>{@code true}: The given marker reference ID is valid and the
     * starting{@link Room} is created.</li>
     * <li>{@code false}: The given marker reference ID is not valid and the
     * starting {@link Room} is not created.</li>
     * </ul>
     *
     * @see Room
     * @see Room#Room(int, int, boolean)
     * @see Actor
     * @see Entity
     * @see Model
     * @see PortalRenderer
     * @see PortalRenderer#createRoom(int, String[])
     * @see PortalRenderer#updateRoomResidents(int, ConcurrentHashMap)
     */
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

            createRoom(mark, wallDescriptors);
            serverService.writeAll(NetworkConstants.PREFIX_PLACE_ROOM + mark);
            serverService.writeAll(NetworkConstants.PREFIX_ASSIGN_ROOM_MARK + mark);

            //place every player actor in that room
            for (Actor actor : model.getActors().values())
            {
                if (actor.isPlayer())
                {
                    actor.setRoom(roomId);
                    room.addActor(actor.getId());
                }
            }

            ConcurrentHashMap<Integer, Pair<Boolean, String>> residents = getResidents(roomId);
            updateRoomResidents(room.getMarker(), residents);

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

    /**
     * Attempt to move a given {@link Actor} to a {@link Room} anchored to a
     * given AR marker reference ID.
     *
     * @param actorId          int: Logical reference ID of the desired {@link
     *                         Actor} to move
     * @param proposedMarkerId int: Reference ID of the AR marker to which the
     *                         desired {@link Room} should be anchored.
     *
     * @see PortalActivity#getNearestNonPlayerMarker(int)
     * @see PortalActivity#postMoveResult(int, int, int, int)
     * @see GameMaster#moveActor(int, int)
     */
    private void moveActor(final int actorId, final int proposedMarkerId)
    {
        if (proposedMarkerId > -1)
        {
            if (GameMaster.getMarkerAttachment(proposedMarkerId) == 1)
            {
                int startRoomId = GameMaster.getActorRoomId(actorId);
                int endRoomId   = GameMaster.getIdByMarker(proposedMarkerId);

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
                    updateRoomResidents(room.getMarker(), getResidents(startRoomId));

                    updateClientTargets();
                }
            }
            if (endRoomId > -1)
            {
                if (GameMaster.getPlayersInRoom(endRoomId) == 1)
                {
                    Room room = GameMaster.getRoom(endRoomId);
                    if (room != null)
                    {
                        int markerID = room.getMarker();
                        short roomSide = GameMaster.getSideOfRoomFrom(startRoomId,
                                                                      endRoomId);
                        renderer.updateRoomAlignment(markerID,
                                                     roomSide);
                        serverService.writeAll(
                            NetworkConstants.PREFIX_UPDATE_ROOM_ALIGNMENT + markerID + ":" +
                            roomSide);
                    }
                }
                Room room = GameMaster.getRoom(endRoomId);
                if (room != null)
                {
                    updateRoomResidents(room.getMarker(), getResidents(endRoomId));
                }

                serverService.writeToClient(NetworkConstants.PREFIX_ASSIGN_ROOM_MARK +
                                            GameMaster.getRoomMarkerId(endRoomId), actorId);
            }
        }
    }

    private boolean generateRoom(int mark)
    {
        if (mark > -1 && GameMaster.getMarkerAttachment(mark) == -1)
        {
            Room room =
                GameMaster.generateRoom(this, CollectionManager.getNextID(model.getRooms()), mark);

            String[] wallDescriptors = getWallDescriptors(room.getId());

            createRoom(mark, wallDescriptors);

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
                    serverService.writeAll(NetworkConstants.PREFIX_PLACE_ROOM + nearestMarkerID);

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
                updateRoomWalls(room.getMarker(), getWallDescriptors(endRoomId));
            }

            ArrayList<Integer> adjacentRoomIds = GameMaster.getAdjacentRoomIds(endRoomId);
            for (int id : adjacentRoomIds)
            {
                Room adjacentRoom = GameMaster.getRoom(id);
                if (adjacentRoom != null)
                {
                    updateRoomWalls(adjacentRoom.getMarker(), getWallDescriptors(id));
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
                int markerID = room.getMarker();
                String targetState = (defender != null) ? (defender.getState() ==
                                                           Actor.E_STATE.DEFEND ? "defend" : null) : null;
                showAction(markerID,
                           attackerId,
                           defenderId,
                           1000,
                           "attack",
                           targetState,
                           true,
                           true);
            }

            if (attackerId == playerId)
            {
                uiPortalOverlay.overlayWaitingForTurn();
            }
            else
            {
                serverService.writeToClient(NetworkConstants.GAME_TURN_OVER, attackerId);
            }
        }
    }

    /**
     * Attempt to perform a {@link Special} ability originating from a given
     * {@link Actor} without a specific target using the appropriate {@link
     * GameMaster} method, then post the results using the result code provided
     * by the {@link GameMaster} method.
     *
     * @param sourceId  int: The logical reference ID of the desired {@link
     *                  Actor} from which {@link Special} ability originated.
     * @param specialId int: The logical reference ID of the desired {@link
     *                  Special} ability to attempt to perform.
     *
     * @see Special
     * @see Actor
     * @see GameMaster
     * @see GameMaster#special(int, int)
     */
    private void performSpecial(final int sourceId, final int specialId)
    {
        int res = GameMaster.special(sourceId, specialId);

        postSpecialResult(sourceId, -1, specialId, res);
    }

    /**
     * Attempt to perform a {@link Special} ability originating from a given
     * {@link Actor} and targeted at a given {@link Actor} using the
     * appropriate {@link GameMaster} method, then post the results using the
     * result code provided by the {@link GameMaster} method.
     *
     * @param sourceId  int: The logical reference ID of the desired {@link
     *                  Actor} from which the {@link Special} ability
     *                  originated.
     * @param targetId  int: The logical reference ID of the desired {@link
     *                  Actor} at which the {@link Special} ability is
     *                  targeted.
     * @param specialId int: The logical reference ID of the desired {@link
     *                  Special} ability to attempt to perform.
     *
     * @see Special
     * @see Actor
     * @see GameMaster
     * @see GameMaster#special(int, int, int)
     */
    private void performSpecial(final int sourceId, final int targetId, final int specialId)
    {
        int res = GameMaster.special(sourceId, targetId, specialId);

        postSpecialResult(sourceId, targetId, specialId, res);
    }

    /**
     * Post the results of attempting to perform a {@link Special} ability in a
     * way which provides feedback to the user, e.g. a Toast with a message
     * describing why the {@link Special} could not be performed, or an
     * 'animation' in the {@link PortalRenderer}.
     *
     * @param sourceId  int: The logical reference ID of the desired {@link
     *                  Actor} from which the {@link Special} ability
     *                  originated.
     * @param targetId  int: The logical reference ID of the desired {@link
     *                  Actor} at which the {@link Special} ability is
     *                  targeted, or {@code -1} if the {@link Special ability}
     *                  does not have a specific target.
     * @param specialId int: The logical reference ID of the desired {@link
     *                  Special} ability which was attempted to be performed.
     * @param res       int: A result code provided by the {@link GameMaster}
     *                  when the {@link Special} ability is attempted.
     *
     * @see Special
     * @see Actor
     * @see GameMaster
     * @see GameMaster#special(int, int)
     * @see GameMaster#special(int, int, int)
     * @see PortalRenderer
     * @see PortalRenderer#showAction(int, int, int, long, String, String, boolean, boolean)
     */
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

            String specialType = GameMaster.getSpecialTypeDescriptor(specialId);

            if (room != null)
            {
                showAction(room.getMarker(),
                           sourceId,
                           targetId,
                           1000,
                           "special:" + specialType,
                           ((target != null) ? (target.getState() ==
                                                Actor.E_STATE.DEFEND ? "defend" : null) : null),
                           true,
                           specialType.equals("harm"));
            }

            if (sourceId == playerId)
            {
                uiPortalOverlay.overlayWaitingForTurn();
            }
            else
            {
                serverService.writeToClient(NetworkConstants.GAME_TURN_OVER, sourceId);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param message String: The feedback message to be processed or given to
     */
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

    /**
     * {@inheritDoc}
     *
     * @param sourceId int: The logical reference ID of the {@link Actor}
     *                 performing the action
     * @param targetId int: The logical reference ID of the {@link Actor}
     *                 target of the action if applicable, -1 otherwise.
     * @param action   String: Reference description of the action being
     *                 performed.
     */
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
            showAction(room.getMarker(),
                       sourceId,
                       targetId,
                       1000,
                       action,
                       ((target != null) ? (target.getState() ==
                                            Actor.E_STATE.DEFEND ? "defend" : null) : null),
                       false,
                       forwardAction);
        }

        Logger.logD("exit trace");
    }

    /**
     * {@inheritDoc}
     *
     * @param actorId int: The logical reference ID of the {@link Actor} which
     *                has moved.
     * @param roomId  int: The logical reference ID of the {@link Room} moved
     *                to by the given {@link Actor}.
     */
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
            updateRoomResidents(room.getMarker(), getResidents(room.getId()));
        }
        room = GameMaster.getRoom(roomId);
        if (room != null)
        {
            updateRoomResidents(room.getMarker(), getResidents(room.getId()));
        }

        updateClientTargets();

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

    /**
     * {@inheritDoc}
     *
     * @param turnId int: The logical reference ID of the turn that was passed,
     *               and also the {@link Actor} for which that turn was taken.
     */
    @Override
    public void turnPassed(final int turnId)
    {
        Logger.logD("enter trace");

        Logger.logD("ended non-player turn " + turnId);

        postTurn(turnId);

        Logger.logD("exit trace");
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     *
     * @param actorId int: The logical reference ID of the {@link Actor}
     *                performing the action which has finished.
     */
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
            updateRoomResidents(room.getMarker(), getResidents(room.getId()));
        }

        updateClientTargets();

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

    /**
     * Update room residents for the client and host
     *
     * @param arRoomId  MarkerID of the room to update
     * @param residents Updated list of residents
     *
     * @see PortalRenderer#updateRoomResidents(int, ConcurrentHashMap)
     */
    private void updateRoomResidents(int arRoomId,
                                     ConcurrentHashMap<Integer, Pair<Boolean, String>> residents)
    {
        renderer.updateRoomResidents(arRoomId, residents);
        String clientRoomResidents = "";
        // Use a different delimiter, because of how residents are stored
        for (Integer residentID : residents.keySet())
        {
            Pair<Boolean, String> pair = residents.get(residentID);
            clientRoomResidents = clientRoomResidents.concat(
                residentID + ";" + pair.first + "," + pair.second + "~");
        }
        serverService.writeAll(
            NetworkConstants.PREFIX_UPDATE_ROOM_RESIDENTS + arRoomId + "~" + clientRoomResidents);
    }

    /**
     * Show actions for client and host
     *
     * @param arRoomId
     * @param playerId
     * @param targetId
     * @param length
     * @param actionType
     * @param targetState
     * @param forward
     *
     * @see PortalRenderer#showAction(int, int, int, long, String, String, boolean, boolean)
     */
    private void showAction(int arRoomId,
                            int playerId,
                            int targetId,
                            int length,
                            String actionType,
                            String targetState,
                            boolean playerAction,
                            boolean forward)
    {
        renderer.showAction(arRoomId,
                            playerId,
                            targetId,
                            length,
                            actionType,
                            targetState,
                            playerAction,
                            forward);
        serverService.writeAll(
            NetworkConstants.PREFIX_SHOW_ACTION + arRoomId + ":" + playerId + ":" + targetId + ":" +
            length + ":" + actionType + ":" + targetState + ":" + playerAction + ":" + forward);
    }

    /**
     * Create room for client and host
     *
     * @param roomID          MarkerID of new room
     * @param wallDescriptors Array of wall descriptors
     *
     * @see PortalRenderer#createRoom(int, String[])
     */
    private void createRoom(int roomID, String[] wallDescriptors)
    {
        renderer.createRoom(roomID, wallDescriptors);
        String clientWallDescriptors = "";
        for (String str : wallDescriptors)
        {
            clientWallDescriptors = clientWallDescriptors.concat(str + ":");
        }
        serverService.writeAll(
            NetworkConstants.PREFIX_CREATE_ROOM + roomID + ":" + clientWallDescriptors);
        serverService.writeAll(NetworkConstants.PREFIX_RESERVE_ROOM_MARKER + roomID);
    }

    /**
     * Update room walls for client and host
     *
     * @param arRoomID        MarkerID of room to update
     * @param wallDescriptors Updated array of wall descriptors
     *
     * @see PortalRenderer#updateRoomWalls(int, String[])
     */
    private void updateRoomWalls(int arRoomID, String[] wallDescriptors)
    {
        renderer.updateRoomWalls(arRoomID, wallDescriptors);
        String clientWallDescriptors = "";
        for (String str : wallDescriptors)
        {
            clientWallDescriptors = clientWallDescriptors.concat(str + ":");
        }
        serverService.writeAll(
            NetworkConstants.PREFIX_UPDATE_ROOM_WALLS + arRoomID + ":" + clientWallDescriptors);
    }

    /**
     * Called when a turn has ended.
     *
     * @param actorId int: The logical reference ID of the {@link Actor} which
     *                just took a turn.
     */
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
            uiPortalOverlay.overlayAction();
        }
        else if (!GameMaster.getActorIsPlayer(turnId))
        {
            Logger.logD("turn id is not a player, taking turn for non-player actor " + turnId);

            ActorController.takeTurn(turnId);
        }
        else
        {
            serverService.writeToClient(NetworkConstants.GAME_TURN, turnId);
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

    private void updateClientTargets()
    {
        ArrayList<Integer> playerActorIds = GameMaster.getPlayerActorIds();

        for (int i : playerActorIds)
        {
            if (i != playerId)
            {
                String nonPlayerTargets = "";

                for (Actor actor : GameMaster.getNonPlayerTargets(i).values())
                {
                    nonPlayerTargets +=
                        "," + actor.getId() + "." + actor.getDisplayName();
                }

                if (!nonPlayerTargets.equals(""))
                {
                    nonPlayerTargets = nonPlayerTargets.substring(1);
                }

                serverService.writeToClient(
                    NetworkConstants.PREFIX_UPDATE_NON_PLAYER_TARGETS +
                    nonPlayerTargets, i);

                String playerTargets = "";

                for (Actor actor : GameMaster.getPlayerTargets(i).values())
                {
                    playerTargets +=
                        "," + actor.getId() + "." + actor.getDisplayName();
                }

                if (!playerTargets.equals(""))
                {
                    playerTargets = playerTargets.substring(1);
                }

                serverService.writeToClient(
                    NetworkConstants.PREFIX_UPDATE_PLAYER_TARGETS +
                    playerTargets, i);
            }
        }
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